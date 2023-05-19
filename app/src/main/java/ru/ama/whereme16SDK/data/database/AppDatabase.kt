package ru.ama.whereme16SDK.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [LocationDbModel::class,SmsCallDbModel::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 3, to = 4),AutoMigration(from = 4, to = 5)]
)
//@TypeConverters(OffsetDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {

        private var db: AppDatabase? = null
        private const val DB_NAME = "main.db"
        private val LOCK = Any()

        fun getInstance(context: Context): AppDatabase {
            synchronized(LOCK) {
                db?.let { return it }
                val instance =
                    Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        DB_NAME
                    )
                        //.addMigrations(AppDatabase.MIGRATION_3_4)
                        //.fallbackToDestructiveMigration()
                        // .allowMainThreadQueries()
                        .build()
                db = instance
                return instance
            }
        }

        /*val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tab_locations ADD COLUMN isOnOff INTEGER")
            }
        }*/
    }

    abstract fun locationDao(): LocationDao


}
