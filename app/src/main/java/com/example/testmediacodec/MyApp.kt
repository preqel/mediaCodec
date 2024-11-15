package com.example.testmediacodec

import android.app.Application
import android.content.Context

/**
 * @author: w.k
 * @date: 2024/11/15
 * @description:
 */
class MyApp : Application() {

    companion object {
         lateinit var context: Context
    }



    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }



}