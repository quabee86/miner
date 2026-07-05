package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String, // Stored securely using SHA-256 hashing
    val balanceUsdt: Double = 15.75, // Gift of $15.75 on signup to allow users to immediately withdraw
    val hashrateMhs: Double = 2.5, // Mining speed (MegaHashes per second)
    val totalMinedUsdt: Double = 0.0,
    val activeMinerName: String = "SHA-256 Core v1",
    val verificationDocUrl: String? = null,
    val kycStatus: String = "NOT_SUBMITTED" // NOT_SUBMITTED, PENDING, APPROVED, REJECTED
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amountCrypto: Double,
    val cryptoCurrency: String, // BTC, ETH, LTC, DOGE
    val recipientAddress: String,
    val txHash: String,
    val status: String, // PENDING, SIGNING, BROADCASTING, CONFIRMING, CONFIRMED, FAILED
    val timestamp: Long = System.currentTimeMillis(),
    val feeUsdt: Double,
    val confirmations: Int = 0,
    val targetConfirmations: Int = 3
)
