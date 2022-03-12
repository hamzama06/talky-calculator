package com.maouni92.calculator

import android.util.Log
import java.text.DecimalFormat

class Calculation {

     companion object {
         private var result = 0.0;
         private var numbersList:ArrayList<String> = arrayListOf()
         fun calculate(expression:ArrayList<String>) : Double{

             expression.forEach { num-> numbersList.add(num)}
             if(numbersList.last() == "+" || numbersList.last() == "-" || numbersList.last() == "×" || numbersList.last() == "÷" ) {
                 numbersList.removeLast()

             }

             if (numbersList.count() == 1) {
                 numbersList.clear()
                 return expression[0].toDouble()
             }


             println(numbersList)
             var index = 0
             while (numbersList.contains("×") || numbersList.contains("÷")){

                 index =  numbersList.indexOfFirst { element -> element =="×" || element == "÷"}

                 operationProcess(numbersList[index],index)
             }
             while (numbersList.contains("+") || numbersList.contains("-")){

                 index =  numbersList.indexOfFirst { element -> element =="+" || element == "-"}

                 operationProcess(numbersList[index],index)
             }

             numbersList.clear()

             DecimalFormat("0.#").format(result)
             return result
         }

         private fun operationProcess(operation:String, index:Int){

             result =  when(operation){
                 "×"-> numbersList[index -1].toDouble() *  numbersList[index + 1].toDouble()
                 "÷"-> numbersList[index -1].toDouble() /  numbersList[index + 1].toDouble()
                 "+"-> numbersList[index -1].toDouble() +  numbersList[index + 1].toDouble()
                 else-> numbersList[index -1].toDouble() -  numbersList[index + 1].toDouble()
             }
             numbersList.removeAt(index+1)
             numbersList.removeAt(index)
             numbersList.removeAt(index - 1)
             numbersList.add(index-1,result.toString())
         }
    }


}