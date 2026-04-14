package com.vikash.pokertrainer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vikash.pokertrainer.data.model.Converters
import com.vikash.pokertrainer.data.model.UserProgress
import com.vikash.pokertrainer.data.model.PlayerTypeProgress

@Database(
    entities = [UserProgress::class, PlayerTypeProgress::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poker_trainer_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
