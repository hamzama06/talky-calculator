package com.maouni92.calculator

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi

class Preferences(context: Context) {

    var themeSharedPref: SharedPreferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    var soundSharedPref: SharedPreferences = context.getSharedPreferences("sound", Context.MODE_PRIVATE)


    fun changeThemePref(isDark:Boolean){
        val editor:SharedPreferences.Editor = themeSharedPref.edit()
        editor.putBoolean("is_dark", isDark)
        editor.apply()
    }
    fun isDarkTheme() : Boolean{
       return themeSharedPref.getBoolean("is_dark",false)
   }


   fun enableSound(isSoundEnabled:Boolean){
       val editor:SharedPreferences.Editor = soundSharedPref.edit()
       editor.putBoolean("sound_enabled", isSoundEnabled)
       editor.apply()
   }

  fun isSoundEnabled():Boolean{
      return soundSharedPref.getBoolean("sound_enabled", true)
  }
}