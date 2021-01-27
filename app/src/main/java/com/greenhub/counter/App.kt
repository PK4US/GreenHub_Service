package com.greenhub.counter

import android.app.Application
import android.content.Context
import com.testfairy.TestFairy

class App : Application() {
    companion object{
        lateinit var contextOfApplication: Context
    }

    override fun onCreate() {
        super.onCreate()
        contextOfApplication = applicationContext
        TestFairy.begin(applicationContext, "SDK-LuIQTGjc")
        TestFairy.setUserId("likent253@gmail.com")
        TestFairy.disableVideo()
    }
}