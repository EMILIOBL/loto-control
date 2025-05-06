package com.example.lotocontrol

import android.app.Application
import androidx.room.Room
import com.example.lotocontrol.data.AppDatabase

class LotoControlApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "lotocontrol_database"
        ).build()
    }

    companion object {
        lateinit var instance: LotoControlApp
            private set
    }

    init {
        instance = this
    }
}
