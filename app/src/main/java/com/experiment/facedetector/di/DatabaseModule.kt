package com.experiment.facedetector.di

import androidx.room.Room
import com.experiment.facedetector.data.local.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(), AppDatabase::class.java, "finder_db"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<AppDatabase>().mediaDao() }

    single { get<AppDatabase>().faceDao() }

    single { get<AppDatabase>().searchFaceDao() }

}
