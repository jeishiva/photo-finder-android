package com.experiment.facedetector.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.experiment.facedetector.data.local.AppDatabase
import com.experiment.facedetector.data.local.repo.RoomDbInvalidationRepository
import com.experiment.facedetector.domain.repo.DbInvalidationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app-db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    } bind RoomDatabase::class

    single { get<AppDatabase>().mediaDao() }

    single { get<AppDatabase>().faceDao() }

    single { get<AppDatabase>().mediaWithFacesDao() }

    single<DbInvalidationRepository> {
        RoomDbInvalidationRepository(db = get<RoomDatabase>())
    }

}
