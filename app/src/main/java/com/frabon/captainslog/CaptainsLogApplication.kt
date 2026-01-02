package com.frabon.captainslog

import android.app.Application
import com.frabon.captainslog.data.AppDatabase
import com.frabon.captainslog.data.EventRepository

class CaptainsLogApplication : Application() {

    // Database and Repository initialization (Lazy loading)
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { EventRepository(database.eventDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}