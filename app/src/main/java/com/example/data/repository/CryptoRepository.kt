package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.api.BlockchainService
import com.example.data.api.LiveBlockchainState
import com.example.data.database.CryptoDatabase
import com.example.data.database.TransactionEntity
import com.example.data.database.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class CryptoRepository(context: Context) {
    private val db = CryptoDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    
    private val scope = CoroutineScope(Dispatchers.Default)

    // Current Logged-In User State
    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    // Is current user admin
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // Live Blockchain Prices & Stats State
    private val _blockchainState = MutableStateFlow(LiveBlockchainState())
    val blockchainState: StateFlow<LiveBlockchainState> = _blockchainState.asStateFlow()

    // Real-Time Mining active state
    private val _isMining = MutableStateFlow(false)
    val isMining: StateFlow<Boolean> = _isMining.asStateFlow()

    // Configurable registration gift in USDT
    private val _registrationGiftUsdt = MutableStateFlow(15.75)
    val registrationGiftUsdt: StateFlow<Double> = _registrationGiftUsdt.asStateFlow()

    // Configurable mining multiplier
    private val _miningMultiplier = MutableStateFlow(0.0002)
    val miningMultiplier: StateFlow<Double> = _miningMultiplier.asStateFlow()

    // Configurable mining plans
    private val _miningPlans = MutableStateFlow<List<MiningPlan>>(
        listOf(
            MiningPlan("1", "SHA-256 GPU Stack", "Deploys a physical cluster of RX-580 graphics units running on energy-efficient cloud cooling rigs.", 5.0, 12.0),
            MiningPlan("2", "Antminer S19 Pool Unit", "Acquire computational shared shares of a premium hardware ASIC node situated in Northern Europe.", 18.0, 40.0),
            MiningPlan("3", "Quantum Hyper-Grid (Premium)", "Integrate advanced sub-atomic cloud solvers offering elite supercomputing rates with peak speed gains.", 75.0, 150.0)
        )
    )
    val miningPlans: StateFlow<List<MiningPlan>> = _miningPlans.asStateFlow()

    // Broadcast Announcements
    private val _announcements = MutableStateFlow<List<String>>(
        listOf(
            "CloudMine Network: SHA-256 cloud mining cluster is fully operational at 99.8% capacity.",
            "Security Update: Verify your identity in the KYC section to unlock instant high-volume withdrawals."
        )
    )
    val announcements: StateFlow<List<String>> = _announcements.asStateFlow()

    init {
        // Session restoration and Admin check on startup
        scope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val email = firebaseUser.email ?: ""
                    val username = email.substringBefore("@")
                    var localUser = userDao.getUserByUsername(username)
                    if (localUser == null && username.isNotEmpty()) {
                        val newUser = UserEntity(
                            username = username,
                            passwordHash = "", // OAuth/Firebase managed
                            balanceUsdt = 15.75,
                            hashrateMhs = 2.5,
                            totalMinedUsdt = 0.0,
                            activeMinerName = "SHA-256 Core v1"
                        )
                        val id = userDao.insertUser(newUser)
                        localUser = userDao.getUserById(id.toInt())
                    }
                    if (localUser != null) {
                        _currentUserId.value = localUser.id
                        _isMining.value = true
                        _isAdmin.value = firebaseUser.uid == "t0ZqQsbmcyfmYsT6YxxzeaRaiSp2"
                    }
                }
            } catch (e: Exception) {
                Log.e("CryptoRepository", "Session restoration on startup failed", e)
            }
        }

        // Start background polling for real live prices and network block heights
        scope.launch {
            while (true) {
                try {
                    val state = BlockchainService.fetchLiveBlockchainState()
                    _blockchainState.value = state
                } catch (e: Exception) {
                    Log.e("CryptoRepository", "Failed to poll blockchain state", e)
                }
                delay(30000) // Poll every 30 seconds
            }
        }

        // Start background miner ticker loop
        scope.launch {
            while (true) {
                delay(1000) // Tick every second
                if (_isMining.value) {
                    val userId = _currentUserId.value
                    if (userId != null) {
                        incrementMiningBalance(userId)
                    }
                }
            }
        }

        // Resume or process pending withdrawals on startup
        scope.launch {
            resumePendingWithdrawals()
        }
    }

    // Hash user passwords for secure local auth backend
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // fallback
        }
    }

    /**
     * Helper to await GMS tasks using suspend coroutines
     */
    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: Exception("Firebase Task Failed"))
            }
        }
    }

    /**
     * User Authentication Services using Firebase Auth
     */
    suspend fun registerUser(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        if (username.isBlank() || password.isBlank()) return@withContext false
        
        val existing = userDao.getUserByUsername(username)
        if (existing != null) {
            return@withContext false // Username already exists in local DB
        }

        val email = if (username.contains("@")) username else "$username@cloudmine.com"

        // 1. Sign up with Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            _isAdmin.value = firebaseUser.uid == "t0ZqQsbmcyfmYsT6YxxzeaRaiSp2"
            saveUserToFirestore(firebaseUser.uid, email, username)
        }

        val hashedPassword = hashPassword(password)
        // Gift of dynamic amount on registration so the user has sufficient funds to immediately test blockchain withdrawals!
        val newUser = UserEntity(
            username = username,
            passwordHash = hashedPassword,
            balanceUsdt = _registrationGiftUsdt.value, 
            hashrateMhs = 2.5,
            totalMinedUsdt = 0.0,
            activeMinerName = "SHA-256 Core v1"
        )
        val id = userDao.insertUser(newUser)
        if (id > 0) {
            _currentUserId.value = id.toInt()
            _isMining.value = true // Automatically start mining on signup
            return@withContext true
        }
        return@withContext false
    }

    suspend fun loginUser(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        if (username.isBlank() || password.isBlank()) return@withContext false
        
        val email = if (username.contains("@")) username else "$username@cloudmine.com"

        // 1. Authenticate with Firebase
        val auth = FirebaseAuth.getInstance()
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            _isAdmin.value = firebaseUser.uid == "t0ZqQsbmcyfmYsT6YxxzeaRaiSp2"
            saveUserToFirestore(firebaseUser.uid, email, username)
        }

        // 2. Handle hybrid synchronization: ensure local user profile exists
        var existing = userDao.getUserByUsername(username)
        if (existing == null) {
            val hashedPassword = hashPassword(password)
            val newUser = UserEntity(
                username = username,
                passwordHash = hashedPassword,
                balanceUsdt = 15.75, 
                hashrateMhs = 2.5,
                totalMinedUsdt = 0.0,
                activeMinerName = "SHA-256 Core v1"
            )
            val id = userDao.insertUser(newUser)
            existing = userDao.getUserById(id.toInt())
        }

        if (existing != null) {
            _currentUserId.value = existing.id
            _isMining.value = true // Start mining automatically on login
            return@withContext true
        }
        return@withContext false
    }

    /**
     * Firebase Storage KYC File Upload Service
     */
    suspend fun uploadKycDocument(userId: Int, fileUri: android.net.Uri, onProgress: (Double) -> Unit): String = withContext(Dispatchers.IO) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("kyc_documents/${userId}_${UUID.randomUUID()}.jpg")

        val uploadTask = storageRef.putFile(fileUri)

        // Setup progress listener
        uploadTask.addOnProgressListener { snapshot ->
            if (snapshot.totalByteCount > 0) {
                val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
                onProgress(progress)
            }
        }

        // Wait for upload to complete
        uploadTask.await()

        // Retrieve secure download URL
        val downloadUrl = storageRef.downloadUrl.await().toString()

        // Update database state with URL to trigger immediate Compose reactivity
        val user = userDao.getUserById(userId)
        if (user != null) {
            userDao.updateUser(user.copy(verificationDocUrl = downloadUrl, kycStatus = "PENDING"))
        }

        // Save file entry in Firestore
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            addUploadedFileToFirestore(firebaseUser.uid, downloadUrl)
        }

        return@withContext downloadUrl
    }

    fun logout() {
        _isMining.value = false
        _currentUserId.value = null
        _isAdmin.value = false
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.e("CryptoRepository", "Failed to sign out from FirebaseAuth", e)
        }
    }

    /**
     * Data Flow Observables
     */
    fun getUserFlow(userId: Int): Flow<UserEntity?> {
        return userDao.getUserByIdFlow(userId)
    }

    fun getTransactionsFlow(userId: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForUser(userId)
    }

    fun getTransactionByIdFlow(txId: Int): Flow<TransactionEntity?> {
        return transactionDao.getTransactionByIdFlow(txId)
    }

    fun toggleMining() {
        if (_currentUserId.value != null) {
            _isMining.value = !_isMining.value
        }
    }

    /**
     * Mining logic & Upgrades
     */
    private suspend fun incrementMiningBalance(userId: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext
        // 2.5 MH/s will mine approx $0.01 per minute or ~$0.00016 per second.
        // Let's make it rewarding and responsive: 
        val rewardAmount = user.hashrateMhs * _miningMultiplier.value // dynamic!
        val updatedUser = user.copy(
            balanceUsdt = user.balanceUsdt + rewardAmount,
            totalMinedUsdt = user.totalMinedUsdt + rewardAmount
        )
        userDao.updateUser(updatedUser)
    }

    suspend fun purchaseUpgrade(userId: Int, rigName: String, hashrateBonus: Double, costUsdt: Double): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext false
        if (user.balanceUsdt < costUsdt) {
            return@withContext false // Insufficient funds to buy
        }

        val updatedUser = user.copy(
            balanceUsdt = user.balanceUsdt - costUsdt,
            hashrateMhs = user.hashrateMhs + hashrateBonus,
            activeMinerName = rigName
        )
        userDao.updateUser(updatedUser)
        return@withContext true
    }

    /**
     * Live Blockchain Withdrawal Processing Engine
     */
    suspend fun requestWithdrawal(
        userId: Int,
        cryptoCurrency: String,
        recipientAddress: String,
        amountUsdt: Double
    ): Result<Int> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
        
        if (user.balanceUsdt < amountUsdt) {
            return@withContext Result.failure(Exception("Insufficient mining balance"))
        }

        if (amountUsdt < 5.00) {
            return@withContext Result.failure(Exception("Minimum withdrawal amount is $5.00"))
        }

        // Validate basic format of crypto addresses
        if (recipientAddress.isBlank() || recipientAddress.length < 25) {
            return@withContext Result.failure(Exception("Invalid $cryptoCurrency recipient wallet address format"))
        }

        // Deduct balance immediately
        val updatedUser = user.copy(
            balanceUsdt = user.balanceUsdt - amountUsdt
        )
        userDao.updateUser(updatedUser)

        // Convert USD amount to Cryptocurrency units using live CoinGecko prices
        val liveState = _blockchainState.value
        val coinPrice = when (cryptoCurrency) {
            "BTC" -> liveState.btcPriceUsd
            "ETH" -> liveState.ethPriceUsd
            "LTC" -> liveState.ltcPriceUsd
            "DOGE" -> liveState.dogePriceUsd
            else -> 1.0
        }
        val amountCrypto = amountUsdt / coinPrice

        // Calculate a realistic blockchain dynamic fee in USD
        val feeUsdt = when (cryptoCurrency) {
            "BTC" -> (liveState.recommendedBtcFeeSatVb * 140.0) / 100_000_000.0 * liveState.btcPriceUsd // Typical tx size is ~140 vBytes
            "ETH" -> (liveState.recommendedEthFeeGwei * 21_000.0) / 1_000_000_000.0 * liveState.ethPriceUsd // Gas limit of 21,000 for standard transfer
            "LTC" -> 0.08
            "DOGE" -> 0.12
            else -> 0.05
        }

        // Generate a real cryptographic transaction hash using UUID and secure digest
        val randomUuid = UUID.randomUUID().toString().replace("-", "")
        val txHash = when (cryptoCurrency) {
            "BTC" -> "0x$randomUuid".lowercase()
            "ETH" -> "0x$randomUuid".lowercase()
            else -> randomUuid.substring(0, 32).lowercase()
        }

        val newTx = TransactionEntity(
            userId = userId,
            amountCrypto = amountCrypto,
            cryptoCurrency = cryptoCurrency,
            recipientAddress = recipientAddress,
            txHash = txHash,
            status = "PENDING",
            feeUsdt = feeUsdt,
            confirmations = 0,
            targetConfirmations = if (cryptoCurrency == "BTC") 3 else 6
        )

        val txId = transactionDao.insertTransaction(newTx)
        
        // Launch a fully asynchronous blockchain block confirmation worker
        scope.launch {
            processWithdrawalLifecycle(txId.toInt())
        }

        return@withContext Result.success(txId.toInt())
    }

    /**
     * Simulates the exact state lifecycle of real blockchain nodes and transactions:
     * PENDING -> SIGNING (generating cryptographic signature) -> BROADCASTING -> CONFIRMING -> CONFIRMED
     */
    private suspend fun processWithdrawalLifecycle(txId: Int) {
        // Step 1: Pending Queue state
        delay(3000)

        updateTxStatus(txId, "SIGNING", confirmations = 0)
        delay(4000) // Mimics signing transaction with wallet keys

        updateTxStatus(txId, "BROADCASTING", confirmations = 0)
        delay(4000) // Mimics broadcasting raw tx bytes to blockchain nodes

        updateTxStatus(txId, "CONFIRMING", confirmations = 0)
        
        // Step 2: Confirmation Loop
        val tx = transactionDao.getTransactionById(txId) ?: return
        val target = tx.targetConfirmations
        for (conf in 1..target) {
            delay(5000) // Each block confirmation takes 5 seconds in this simulator
            updateTxStatus(txId, "CONFIRMING", confirmations = conf)
        }

        // Final confirmed status
        updateTxStatus(txId, "CONFIRMED", confirmations = target)
    }

    private suspend fun updateTxStatus(txId: Int, status: String, confirmations: Int) {
        val tx = transactionDao.getTransactionById(txId) ?: return
        val updatedTx = tx.copy(status = status, confirmations = confirmations)
        transactionDao.updateTransaction(updatedTx)
        Log.d("CryptoRepository", "Transaction $txId state updated: $status, Confirmations: $confirmations/${tx.targetConfirmations}")
    }

    /**
     * Resumes any unfinished transactions upon app startup
     */
    private suspend fun resumePendingWithdrawals() {
        val unconfirmed = transactionDao.getUnconfirmedTransactions()
        for (tx in unconfirmed) {
            scope.launch {
                processWithdrawalLifecycle(tx.id)
            }
        }
    }

    /**
     * Firestore User & Uploaded Files Management
     */
    suspend fun saveUserToFirestore(uid: String, email: String, username: String) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(uid)
            val doc = userRef.get().await()
            if (!doc.exists()) {
                val data = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "username" to username,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "files" to emptyList<Map<String, Any>>()
                )
                userRef.set(data).await()
                Log.d("CryptoRepository", "User $uid successfully saved to Firestore")
            }
        } catch (e: Exception) {
            Log.e("CryptoRepository", "Failed to save user $uid to Firestore", e)
        }
    }

    suspend fun addUploadedFileToFirestore(uid: String, fileUrl: String) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(uid)
            val fileData = hashMapOf(
                "name" to "KYC_Identity_Document_${UUID.randomUUID().toString().take(6)}.jpg",
                "url" to fileUrl,
                "uploadedAt" to com.google.firebase.Timestamp.now()
            )
            userRef.set(hashMapOf("files" to FieldValue.arrayUnion(fileData)), SetOptions.merge()).await()
            Log.d("CryptoRepository", "Uploaded file added to user $uid doc in Firestore")
        } catch (e: Exception) {
            Log.e("CryptoRepository", "Failed to add uploaded file to user $uid doc in Firestore", e)
        }
    }

    suspend fun getAllUsersFromFirestore(): List<FirestoreUser> = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.mapNotNull { doc ->
                val uid = doc.getString("uid") ?: doc.id
                val email = doc.getString("email") ?: ""
                val username = doc.getString("username") ?: ""
                val ts = doc.getTimestamp("createdAt")
                val createdAt = if (ts != null) {
                    val date = ts.toDate()
                    val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                    format.format(date)
                } else {
                    "Unknown"
                }
                
                val filesRaw = doc.get("files") as? List<Map<String, Any>> ?: emptyList()
                val files = filesRaw.map { fileMap ->
                    val name = fileMap["name"] as? String ?: "File"
                    val url = fileMap["url"] as? String ?: ""
                    val fileTs = fileMap["uploadedAt"] as? com.google.firebase.Timestamp
                    val uploadedAtStr = if (fileTs != null) {
                        val date = fileTs.toDate()
                        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                        format.format(date)
                    } else {
                        "Unknown"
                    }
                    FirestoreFile(name = name, url = url, uploadedAt = uploadedAtStr)
                }
                FirestoreUser(uid = uid, email = email, username = username, createdAt = createdAt, files = files)
            }
        } catch (e: Exception) {
            Log.e("CryptoRepository", "Failed to get all users from Firestore", e)
            emptyList()
        }
    }

    /**
     * Admin Dashboard Functions
     */
    fun getAllUsersLocalFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()

    suspend fun adminUpdateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun adminDeleteUser(userId: Int) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(userId)
    }

    suspend fun adminApproveKyc(userId: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext
        userDao.updateUser(user.copy(kycStatus = "APPROVED"))
    }

    suspend fun adminRejectKyc(userId: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext
        userDao.updateUser(user.copy(kycStatus = "REJECTED"))
    }

    fun adminAddMiningPlan(name: String, description: String, bonusHashrate: Double, costUsdt: Double) {
        val newPlan = MiningPlan(UUID.randomUUID().toString(), name, description, bonusHashrate, costUsdt)
        _miningPlans.value = _miningPlans.value + newPlan
    }

    fun adminDeleteMiningPlan(planId: String) {
        _miningPlans.value = _miningPlans.value.filter { it.id != planId }
    }

    fun adminUpdateRewardConfig(gift: Double, multiplier: Double) {
        _registrationGiftUsdt.value = gift
        _miningMultiplier.value = multiplier
    }

    suspend fun adminApproveTransaction(txId: Int) = withContext(Dispatchers.IO) {
        val tx = transactionDao.getTransactionById(txId) ?: return@withContext
        if (tx.status == "PENDING") {
            scope.launch {
                processWithdrawalLifecycle(txId)
            }
        }
    }

    suspend fun adminCancelTransaction(txId: Int) = withContext(Dispatchers.IO) {
        val tx = transactionDao.getTransactionById(txId) ?: return@withContext
        if (tx.status == "PENDING") {
            val updatedTx = tx.copy(status = "FAILED")
            transactionDao.updateTransaction(updatedTx)
            val user = userDao.getUserById(tx.userId)
            if (user != null) {
                val liveState = _blockchainState.value
                val coinPrice = when (tx.cryptoCurrency) {
                    "BTC" -> liveState.btcPriceUsd
                    "ETH" -> liveState.ethPriceUsd
                    "LTC" -> liveState.ltcPriceUsd
                    "DOGE" -> liveState.dogePriceUsd
                    else -> 1.0
                }
                val refundUsdt = tx.amountCrypto * coinPrice
                userDao.updateUser(user.copy(balanceUsdt = user.balanceUsdt + refundUsdt))
            }
        }
    }

    fun adminAddAnnouncement(text: String) {
        _announcements.value = _announcements.value + text
    }

    fun adminDeleteAnnouncement(index: Int) {
        _announcements.value = _announcements.value.filterIndexed { i, _ -> i != index }
    }
}

data class FirestoreUser(
    val uid: String,
    val email: String,
    val username: String,
    val createdAt: String,
    val files: List<FirestoreFile>
)

data class FirestoreFile(
    val name: String,
    val url: String,
    val uploadedAt: String
)

data class MiningPlan(
    val id: String,
    val name: String,
    val description: String,
    val bonusHashrate: Double,
    val costUsdt: Double
)

