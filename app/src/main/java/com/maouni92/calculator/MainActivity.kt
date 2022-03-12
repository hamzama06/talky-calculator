package com.maouni92.calculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.maouni92.calculator.databinding.ActivityMainBinding
import java.lang.Math.pow
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private var number = ""
    private var reslt: Double = 0.0
    private var lastOperation: Operations? = Operations.Null
    private var numbersList = arrayListOf<String>()
    private var isResult = false
    private lateinit var pref: Preferences
    private var textToSpeech: TextToSpeech? = null
    private var isSoundEnabled = true


    override fun onCreate(savedInstanceState: Bundle?) {
       pref = Preferences(this)
       initPrefTheme()
       initSoundPref()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)

        if(pref.isDarkTheme())  binding.themeBtn.isChecked = true
        if(pref.isSoundEnabled())  binding.soundBtn.isChecked = true

        binding.themeBtn.setOnCheckedChangeListener { _, isChecked -> changeTheme(isChecked) }
       binding.soundBtn.setOnCheckedChangeListener { _, isChecked -> enableSound(isChecked) }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("result", reslt)
        outState.putString("number", number)
        outState.putString("last_operation", lastOperation!!.name)
        outState.putStringArrayList("numbers_list", numbersList)
        outState.putBoolean("is_result", isResult)
        outState.putString("expression", binding.expressionTv.text.toString())
        outState.putString("result_expression", binding.resultTv.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        reslt = savedInstanceState.getDouble("result")
        number = savedInstanceState.getString("number", "")
        lastOperation = Operations.valueOf(savedInstanceState.getString("last_operation", "Null"))
        numbersList = savedInstanceState.getStringArrayList("numbers_list") as ArrayList<String>
        isResult = savedInstanceState.getBoolean("is_result")
        binding.expressionTv.text = savedInstanceState.getString("expression")
        binding.resultTv.text = savedInstanceState.getString("result_expression")

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onClickButton(view: View) {
        when (view.id) {
            R.id.zero_btn -> addNumber("0")
            R.id.one_btn -> addNumber("1")
            R.id.two_btn -> addNumber("2")
            R.id.three_btn -> addNumber("3")
            R.id.four_btn -> addNumber("4")
            R.id.five_btn -> addNumber("5")
            R.id.six_btn -> addNumber("6")
            R.id.seven_btn -> addNumber("7")
            R.id.eight_btn -> addNumber("8")
            R.id.nine_btn -> addNumber("9")
            R.id.comma_btn -> addComma()
            R.id.neg_btn -> makeNumberNegative()
            R.id.perc_btn -> setPercentage()
            R.id.clear_btn -> clear()
            R.id.back_btn -> removeLastChar()
            R.id.plus_btn -> makeOperation(Operations.Addition, "+")
            R.id.subs_btn -> makeOperation(Operations.Subtraction, "-")
            R.id.multiply_btn -> makeOperation(Operations.Multiplication, "×")
            R.id.divide_btn -> makeOperation(Operations.Division, "÷")
            R.id.equal_btn -> showResult()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showResult() {
        if (lastOperation == Operations.Null) return

        if (!isResult) changeTextColor()

        if (number.isNotEmpty()) {
            if (number.last() == '.') number = number.dropLast(1)
            numbersList.add(number)
        }

        speakText("equal")

        reslt = Calculation.calculate(numbersList)

        binding.expressionTv.text = DecimalFormat("0.#########").format(reslt)
        binding.resultTv.text = ""
        number = ""
        lastOperation = Operations.Null
        numbersList.clear()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun clear() {

        speakText("Clear")
        number = ""
        reslt = 0.0
        binding.expressionTv.text = ""
        binding.resultTv.text = ""
        if (isResult) changeTextColor()
        changeTextColor()
        lastOperation = Operations.Null
        numbersList.clear()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun addNumber(num: String) {

        if (num == "0" && binding.expressionTv.text.toString() == "0") return

        if (isResult) changeTextColor()

        speakText(num)
        if ((number.isEmpty() && lastOperation == Operations.Null) || (number == "0" && lastOperation == Operations.Null)) {
            number = num
            binding.expressionTv.text = number

        } else if (number == "0" && lastOperation != Operations.Null) {
            number = num
            binding.expressionTv.text = binding.expressionTv.text.toString().dropLast(1)
            binding.expressionTv.append(num)
        } else {
            number += num
            binding.expressionTv.append(num)
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun addComma() {
        if (number.contains('.')) return

        if (isResult) changeTextColor()

        speakText("comma")
        if (number.isEmpty()) {
            number = "0."
            if (lastOperation == Operations.Null) {
                binding.expressionTv.text = number
            } else {
                binding.expressionTv.append(number)
            }
        } else {
            number += "."
            binding.expressionTv.append(".")
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun makeOperation(operation: Operations, symbol: String) {
        if ((lastOperation == operation && number.isEmpty())
            || binding.expressionTv.text.toString().isEmpty()
        ) return

        // if the user entered a negative sign without any number
        if(number.endsWith("-")|| number.endsWith("(")) return

        // if (number.last() == '.') number = number.dropLast(1)
        if (isResult) changeTextColor()

        speakOperation(operation)
        if (number.contains("-")) binding.expressionTv.append(")")
        if (number.isEmpty() && lastOperation != Operations.Null) {
            binding.expressionTv.text = binding.expressionTv.text.toString().dropLast(1)
            binding.expressionTv.append(symbol)
            numbersList.removeLast()
            numbersList.add(symbol)
            lastOperation = operation
            return
        }

        lastOperation = operation

        if (number.isEmpty()) {
            number = binding.expressionTv.text.toString()
        }

        binding.expressionTv.append(symbol)

        if (numbersList.size == 0) {
            numbersList.add(number)
        }
        if (numbersList[numbersList.size - 1] == "+"
            || numbersList[numbersList.size - 1] == "-"
            || numbersList[numbersList.size - 1] == "×"
            || numbersList[numbersList.size - 1] == "÷"
        ) {
            numbersList.add(number)
        }

        numbersList.add(symbol)
        reslt = Calculation.calculate(numbersList)

        binding.resultTv.text = DecimalFormat("0.#########").format(reslt)
        number = ""
    }

    private fun makeNumberNegative() {

        if (isResult) changeTextColor()

        if (number.isEmpty() && lastOperation == Operations.Null && binding.expressionTv.text.isNotEmpty())
            number = binding.expressionTv.text.toString()

        if (number.startsWith('-')) {
            number = number.replace("-", "")
            binding.expressionTv.text = binding.expressionTv.text.dropLast(number.length + 2)
            binding.expressionTv.append(number)
        } else {
            binding.expressionTv.text = binding.expressionTv.text.dropLast(number.length)
            number = "-$number"
            binding.expressionTv.append("($number")

        }
    }
    // change last operation after removing numbers
    private fun changeLastOperation(oper: String): Operations {

        return when (oper) {
            "+" -> Operations.Addition
            "-" -> Operations.Subtraction
            "×" -> Operations.Multiplication
            else -> Operations.Division
        }
    }

    private fun removeLastChar() {

        if (binding.expressionTv.text.isEmpty()) return

        if (number.isEmpty() && lastOperation == Operations.Null && binding.expressionTv.text.isNotEmpty())
            number =binding.expressionTv.text.toString()

        if (isResult) changeTextColor()

        if (number.isNotEmpty()) {
            number = number.dropLast(1)
            binding.expressionTv.text = binding.expressionTv.text.dropLast(1)
            return
        }
        if (number.isEmpty()) {
            numbersList.removeLast()
            number = numbersList[numbersList.size - 1]
            binding.expressionTv.text = binding.expressionTv.text.dropLast(1)

            lastOperation =
                if (numbersList.contains("+") || numbersList.contains("-") || numbersList.contains("×") || numbersList.contains(
                        "÷"
                    )
                ) {
                    changeLastOperation(numbersList[numbersList.size - 2])
                } else {
                    null
                }
        }

    }


    private fun setPercentage() {
        if (number.isEmpty() && binding.expressionTv.text.isNotEmpty() && lastOperation == Operations.Null) {
            number = binding.expressionTv.text.toString()
        }

        if (number.isEmpty()) return

        if (isResult) changeTextColor()

        binding.expressionTv.text = binding.expressionTv.text.dropLast(number.length)


        number = (number.toDouble() / 100).toString()

        binding.expressionTv.append(number)
    }


    private fun changeTextColor() {
        isResult = !isResult
        if (isResult) {

            binding.expressionTv.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.green_200
                )
            )
        } else {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                binding.expressionTv.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.white
                    )
                )
            } else {
                binding.expressionTv.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black
                    )
                )
            }

        }

    }

    override fun onInit(status: Int) {
        if (!isSoundEnabled) return

        if (status == TextToSpeech.SUCCESS) {
            textToSpeech!!.language = Locale.US
        }
    }

    // Convert text to speech
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun speakText(text: String) {
        if (!isSoundEnabled) return
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_ADD, null, "")
    }

    // Get Operation name
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun speakOperation(operation: Operations) {
        when (operation) {
            Operations.Addition -> speakText("plus")
            Operations.Subtraction -> speakText("Minus")
            Operations.Division -> speakText("Divide by")
            else
            -> speakText("Multiply")
        }
    }

    // initialize theme preference
    private fun initPrefTheme() {

        if (pref.isDarkTheme()) {

            setTheme(R.style.ThemeDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        } else {
            setTheme(R.style.ThemeApp)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        }
    }

    /// change theme to dark or light
    private fun changeTheme(checked: Boolean) {
        if (checked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            pref.changeThemePref(true)
        } else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            pref.changeThemePref(false)
        }
    }

    // initialize sound preference
    private fun initSoundPref() {
        isSoundEnabled = pref.isSoundEnabled()


    }

    // enable and disable sound feature
    private fun enableSound(checked: Boolean) {
        isSoundEnabled = checked
        pref.enableSound(checked)

    }


}

enum class Operations { Addition, Subtraction, Multiplication, Division, Null }