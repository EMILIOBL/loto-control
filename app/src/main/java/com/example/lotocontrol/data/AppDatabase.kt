package com.example.lotocontrol.data

import androidx.room.*
import com.example.lotocontrol.data.models.Client
import com.example.lotocontrol.data.models.LotterySettings
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [Client::class, LotterySettings::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun settingsDao(): LotterySettingsDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name")
    fun getAllClientsFlow(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClient(clientId: Long): Client?

    @Insert
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    @Query("SELECT * FROM clients WHERE name LIKE :searchQuery || '%'")
    fun searchClients(searchQuery: String): Flow<List<Client>>
}

@Dao
interface LotterySettingsDao {
    @Query("SELECT * FROM lottery_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<LotterySettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: LotterySettings)

    @Update
    suspend fun updateSettings(settings: LotterySettings)
}
