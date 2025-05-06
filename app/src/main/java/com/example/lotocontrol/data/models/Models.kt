package com.example.lotocontrol.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ticketsDelivered: Int = 0,
    val ticketsReturned: Int = 0,
    val amountPaid: Double = 0.0,
    val previousDebt: Double = 0.0
) {
    fun calculateBalance(ticketPrice: Double): Double {
        val totalDue = (ticketsDelivered - ticketsReturned) * ticketPrice
        return totalDue + previousDebt - amountPaid
    }

    fun hasDebt(ticketPrice: Double): Boolean {
        return calculateBalance(ticketPrice) > 0
    }
}

@Entity(tableName = "lottery_settings")
data class LotterySettings(
    @PrimaryKey
    val id: Int = 1,
    val drawDate: LocalDate = LocalDate.now(),
    val ticketPrice: Double = 0.0
)

data class ClientWithBalance(
    val client: Client,
    val currentBalance: Double,
    val hasDebt: Boolean
)
