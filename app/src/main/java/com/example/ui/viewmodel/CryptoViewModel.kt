package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.TransactionEntity
import com.example.data.database.UserEntity
import com.example.data.repository.CryptoRepository
import com.example.data.repository.FirestoreUser
import com.example.data.repository.FirestoreFile
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CryptoRepository(application)

    // Current session
    val currentUserId: StateFlow<Int?> = repository.currentUserId
    val isMining: StateFlow<Boolean> = repository.isMining
    val isAdmin: StateFlow<Boolean> = repository.isAdmin
    val blockchainState = repository.blockchainState

    // Admin state flows for Firestore users list
    private val _adminUsersList = MutableStateFlow<List<FirestoreUser>>(emptyList())
    val adminUsersList: StateFlow<List<FirestoreUser>> = _adminUsersList.asStateFlow()

    private val _isAdminLoadingUsers = MutableStateFlow(false)
    val isAdminLoadingUsers: StateFlow<Boolean> = _isAdminLoadingUsers.asStateFlow()

    fun loadAdminUsersList() {
        _isAdminLoadingUsers.value = true
        viewModelScope.launch {
            try {
                _adminUsersList.value = repository.getAllUsersFromFirestore()
            } catch (e: Exception) {
                Log.e("CryptoViewModel", "Failed to retrieve Firestore users", e)
            } finally {
                _isAdminLoadingUsers.value = false
            }
        }
    }

    // Live Flow updates of current logged-in user profile from local database
    val currentUser: StateFlow<UserEntity?> = currentUserId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getUserFlow(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Live Flow updates of current user's blockchain withdrawal transactions
    val userTransactions: StateFlow<List<TransactionEntity>> = currentUserId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getTransactionsFlow(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI Auth Feedback states
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Document Upload UI Feedback states
    private val _uploadProgress = MutableStateFlow<Double?>(null)
    val uploadProgress: StateFlow<Double?> = _uploadProgress.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    // Withdrawal submission feedback states
    private val _withdrawalState = MutableStateFlow<WithdrawalUiState>(WithdrawalUiState.Idle)
    val withdrawalState: StateFlow<WithdrawalUiState> = _withdrawalState.asStateFlow()

    // Upgrade Feedback states
    private val _upgradeMessage = MutableStateFlow<String?>(null)
    val upgradeMessage: StateFlow<String?> = _upgradeMessage.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthUiState.Error("Username and password cannot be empty")
            return
        }
        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val success = repository.loginUser(username, password)
                if (success) {
                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.Error("Invalid username or password")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.localizedMessage ?: "Invalid credentials or network failure")
            }
        }
    }

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthUiState.Error("Username and password cannot be empty")
            return
        }
        if (password.length < 6) { // Firebase Auth requires at least 6 characters
            _authState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val success = repository.registerUser(username, password)
                if (success) {
                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.Error("Username already exists in local node")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.localizedMessage ?: "Registration rejected by network")
            }
        }
    }

    fun uploadKycDocument(fileUri: android.net.Uri) {
        val userId = currentUserId.value
        if (userId == null) {
            _uploadState.value = UploadUiState.Error("Operator session expired. Please log in.")
            return
        }

        _uploadState.value = UploadUiState.Loading
        _uploadProgress.value = 0.0

        viewModelScope.launch {
            try {
                val downloadUrl = repository.uploadKycDocument(userId, fileUri) { progress ->
                    _uploadProgress.value = progress
                }
                _uploadState.value = UploadUiState.Success(downloadUrl)
            } catch (e: Exception) {
                _uploadState.value = UploadUiState.Error(e.localizedMessage ?: "File upload failed")
            } finally {
                _uploadProgress.value = null
            }
        }
    }

    fun clearUploadState() {
        _uploadState.value = UploadUiState.Idle
        _uploadProgress.value = null
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthUiState.Idle
        _withdrawalState.value = WithdrawalUiState.Idle
        _uploadState.value = UploadUiState.Idle
        _upgradeMessage.value = null
    }

    fun toggleMining() {
        repository.toggleMining()
    }

    fun clearAuthError() {
        _authState.value = AuthUiState.Idle
    }

    fun clearWithdrawalState() {
        _withdrawalState.value = WithdrawalUiState.Idle
    }

    fun clearUpgradeMessage() {
        _upgradeMessage.value = null
    }

    fun purchaseRigUpgrade(rigName: String, hashrateBonus: Double, costUsdt: Double) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            val success = repository.purchaseUpgrade(userId, rigName, hashrateBonus, costUsdt)
            if (success) {
                _upgradeMessage.value = "Successfully activated $rigName! Mining speed increased +${hashrateBonus} MH/s."
            } else {
                _upgradeMessage.value = "Failed to purchase: Insufficient USDT balance in wallet."
            }
        }
    }

    fun submitWithdrawal(cryptoCurrency: String, address: String, amountUsdt: Double) {
        val userId = currentUserId.value
        if (userId == null) {
            _withdrawalState.value = WithdrawalUiState.Error("User session expired. Please log in.")
            return
        }

        _withdrawalState.value = WithdrawalUiState.Loading
        viewModelScope.launch {
            val result = repository.requestWithdrawal(userId, cryptoCurrency, address, amountUsdt)
            result.fold(
                onSuccess = { txId ->
                    _withdrawalState.value = WithdrawalUiState.Success(txId)
                },
                onFailure = { error ->
                    _withdrawalState.value = WithdrawalUiState.Error(error.message ?: "Withdrawal processing failed.")
                }
            )
        }
    }

    // Admin state flows for local users and transactions
    val allUsersLocal: StateFlow<List<UserEntity>> = repository.getAllUsersLocalFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactionsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val miningPlans: StateFlow<List<com.example.data.repository.MiningPlan>> = repository.miningPlans
    val announcements: StateFlow<List<String>> = repository.announcements
    val registrationGiftUsdt: StateFlow<Double> = repository.registrationGiftUsdt
    val miningMultiplier: StateFlow<Double> = repository.miningMultiplier

    fun adminUpdateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.adminUpdateUser(user)
        }
    }

    fun adminDeleteUser(userId: Int) {
        viewModelScope.launch {
            repository.adminDeleteUser(userId)
        }
    }

    fun adminApproveKyc(userId: Int) {
        viewModelScope.launch {
            repository.adminApproveKyc(userId)
        }
    }

    fun adminRejectKyc(userId: Int) {
        viewModelScope.launch {
            repository.adminRejectKyc(userId)
        }
    }

    fun adminAddMiningPlan(name: String, description: String, bonusHashrate: Double, costUsdt: Double) {
        repository.adminAddMiningPlan(name, description, bonusHashrate, costUsdt)
    }

    fun adminDeleteMiningPlan(planId: String) {
        repository.adminDeleteMiningPlan(planId)
    }

    fun adminUpdateRewardConfig(gift: Double, multiplier: Double) {
        repository.adminUpdateRewardConfig(gift, multiplier)
    }

    fun adminApproveTransaction(txId: Int) {
        viewModelScope.launch {
            repository.adminApproveTransaction(txId)
        }
    }

    fun adminCancelTransaction(txId: Int) {
        viewModelScope.launch {
            repository.adminCancelTransaction(txId)
        }
    }

    fun adminAddAnnouncement(text: String) {
        repository.adminAddAnnouncement(text)
    }

    fun adminDeleteAnnouncement(index: Int) {
        repository.adminDeleteAnnouncement(index)
    }
}

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface WithdrawalUiState {
    object Idle : WithdrawalUiState
    object Loading : WithdrawalUiState
    data class Success(val transactionId: Int) : WithdrawalUiState
    data class Error(val message: String) : WithdrawalUiState
}

sealed interface UploadUiState {
    object Idle : UploadUiState
    object Loading : UploadUiState
    data class Success(val downloadUrl: String) : UploadUiState
    data class Error(val message: String) : UploadUiState
}
