package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.database.TransactionEntity
import com.example.data.database.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CryptoViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AdminSubsystem(val title: String, val desc: String) {
    USER_MANAGEMENT("User Directory", "Adjust balance credit/debit, manage speeds, or ban nodes."),
    KYC_APPROVAL("KYC Approval Queue", "Review submitted physical documents and grant operator passes."),
    MINING_PLANS("Mining Plan Manager", "Depl_oy or retire hardware mining rig investment configurations."),
    REWARD_CONFIG("Reward Configurator", "Calibrate registration signup gift limits and real-time multiplier."),
    WALLET_MONITOR("Wallet & Withdrawals", "Sign pending blockchain payouts, reject transactions, and monitor flows."),
    REVENUE_REPORTS("Revenue & Analytics", "Visual aggregates of network hashrates, balance stores, and dynamic payouts."),
    FRAUD_DETECTION("Anti-Fraud Engine", "Automated scan identifying duplicate identities, speed caps, or balance anomalies."),
    ANNOUNCEMENTS("Broadcast Alerts", "Push priority promotional campaigns and maintenance alerts to operator nodes.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubsystem by remember { mutableStateOf<AdminSubsystem?>(null) }
    val localUsers by viewModel.allUsersLocal.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (activeSubsystem != null) {
                        IconButton(
                            onClick = { activeSubsystem = null },
                            modifier = Modifier.testTag("admin_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to Control Center",
                                tint = GeoPrimary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8DEF8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = Color(0xFF6750A4),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = activeSubsystem?.title ?: "CloudMine Command",
                            color = GeoTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFB3261E))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ADMIN CONSOLE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = if (activeSubsystem != null) "Active Module" else "Central Hub",
                                color = GeoPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Admin Sign Out",
                        tint = GeoTextSecondary
                    )
                }
            }

            HorizontalDivider(color = GeoBorder, thickness = 1.dp)

            if (activeSubsystem == null) {
                // Main Control Directory view
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "COMMAND PROTOCOLS",
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(AdminSubsystem.values()) { subsystem ->
                        val badgeCount = when (subsystem) {
                            AdminSubsystem.KYC_APPROVAL -> localUsers.count { it.kycStatus == "PENDING" }
                            AdminSubsystem.WALLET_MONITOR -> allTransactions.count { it.status == "PENDING" }
                            else -> 0
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GeoCardBorder, RoundedCornerShape(16.dp))
                                .clickable { activeSubsystem = subsystem }
                                .testTag("admin_card_${subsystem.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GeoPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (subsystem) {
                                                AdminSubsystem.USER_MANAGEMENT -> Icons.Default.People
                                                AdminSubsystem.KYC_APPROVAL -> Icons.Default.Verified
                                                AdminSubsystem.MINING_PLANS -> Icons.Default.Layers
                                                AdminSubsystem.REWARD_CONFIG -> Icons.Default.Settings
                                                AdminSubsystem.WALLET_MONITOR -> Icons.Default.AccountBalanceWallet
                                                AdminSubsystem.REVENUE_REPORTS -> Icons.Default.Analytics
                                                AdminSubsystem.FRAUD_DETECTION -> Icons.Default.Security
                                                AdminSubsystem.ANNOUNCEMENTS -> Icons.Default.Notifications
                                            },
                                            contentDescription = null,
                                            tint = GeoOnPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = subsystem.title,
                                            color = GeoTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = subsystem.desc,
                                            color = GeoTextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                if (badgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFB3261E))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$badgeCount ACTION",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = GeoTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Individual active panel
                Box(modifier = Modifier.weight(1f)) {
                    when (activeSubsystem) {
                        AdminSubsystem.USER_MANAGEMENT -> UserManagementPanel(viewModel, localUsers)
                        AdminSubsystem.KYC_APPROVAL -> KycApprovalPanel(viewModel, localUsers)
                        AdminSubsystem.MINING_PLANS -> MiningPlansPanel(viewModel)
                        AdminSubsystem.REWARD_CONFIG -> RewardConfigPanel(viewModel)
                        AdminSubsystem.WALLET_MONITOR -> WalletMonitorPanel(viewModel, allTransactions, localUsers)
                        AdminSubsystem.REVENUE_REPORTS -> RevenueReportsPanel(localUsers, allTransactions)
                        AdminSubsystem.FRAUD_DETECTION -> FraudDetectionPanel(viewModel, localUsers)
                        AdminSubsystem.ANNOUNCEMENTS -> AnnouncementsPanel(viewModel)
                        null -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementPanel(viewModel: CryptoViewModel, users: List<UserEntity>) {
    var searchQuery by remember { mutableStateOf("") }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var adjustBalanceAmount by remember { mutableStateOf("") }
    var adjustSpeedAmount by remember { mutableStateOf("") }

    val filteredUsers = users.filter {
        it.username.contains(searchQuery, ignoreCase = true) ||
                (it.activeMinerName ?: "").contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Node Operators") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredUsers) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = user.username.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GeoTextPrimary
                                )
                                Text(
                                    text = "Node ID: #${user.id}",
                                    fontSize = 10.sp,
                                    color = GeoTextSecondary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        userToEdit = user
                                        adjustBalanceAmount = String.format(Locale.US, "%.2f", user.balanceUsdt)
                                        adjustSpeedAmount = String.format(Locale.US, "%.1f", user.hashrateMhs)
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("ADJUST", fontSize = 10.sp)
                                }

                                IconButton(
                                    onClick = { viewModel.adminDeleteUser(user.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete node",
                                        tint = Color(0xFFB3261E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = GeoCardBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("BALANCE", fontSize = 9.sp, color = GeoTextSecondary, fontWeight = FontWeight.Bold)
                                Text("$${String.format(Locale.US, "%.4f", user.balanceUsdt)} USDT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPrimary)
                            }
                            Column {
                                Text("HASHRATE", fontSize = 9.sp, color = GeoTextSecondary, fontWeight = FontWeight.Bold)
                                Text("${user.hashrateMhs} MH/s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("MINER STATS", fontSize = 9.sp, color = GeoTextSecondary, fontWeight = FontWeight.Bold)
                                Text(user.activeMinerName ?: "None", fontSize = 11.sp, color = GeoTextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Adjust user parameters Dialog
    userToEdit?.let { user ->
        AlertDialog(
            onDismissRequest = { userToEdit = null },
            confirmButton = {
                Button(
                    onClick = {
                        val finalBal = adjustBalanceAmount.toDoubleOrNull() ?: user.balanceUsdt
                        val finalSpeed = adjustSpeedAmount.toDoubleOrNull() ?: user.hashrateMhs
                        viewModel.adminUpdateUser(user.copy(balanceUsdt = finalBal, hashrateMhs = finalSpeed))
                        userToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary)
                ) {
                    Text("SAVE CHANGES")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) {
                    Text("CANCEL")
                }
            },
            title = { Text("Update Operator Protocol", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = adjustBalanceAmount,
                        onValueChange = { adjustBalanceAmount = it },
                        label = { Text("Deduct / Credit USDT Balance") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adjustSpeedAmount,
                        onValueChange = { adjustSpeedAmount = it },
                        label = { Text("Set Mining Speed (MH/s)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = GeoBackground
        )
    }
}

@Composable
fun KycApprovalPanel(viewModel: CryptoViewModel, users: List<UserEntity>) {
    val kycUsers = users.filter { it.verificationDocUrl != null }

    if (kycUsers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Verified, null, tint = GeoTextSecondary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No identity documents submitted by operator nodes.", color = GeoTextSecondary, fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(kycUsers) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GeoBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = user.username.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text("Joined: Operator #${user.id}", fontSize = 11.sp, color = GeoTextSecondary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (user.kycStatus) {
                                            "APPROVED" -> Color(0xFFDCFCE7)
                                            "REJECTED" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFFEF9C3)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = user.kycStatus,
                                    color = when (user.kycStatus) {
                                        "APPROVED" -> Color(0xFF166534)
                                        "REJECTED" -> Color(0xFF991B1B)
                                        else -> Color(0xFF854D0E)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = user.verificationDocUrl,
                                contentDescription = "KYC Attachment",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.adminApproveKyc(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16803D)),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                    Text("APPROVE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { viewModel.adminRejectKyc(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                    Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiningPlansPanel(viewModel: CryptoViewModel) {
    val plans by viewModel.miningPlans.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bonusHashrate by remember { mutableStateOf("") }
    var costUsdt by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DEPLOY NEW CONTRACT TEMPLATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoPrimary)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Hardware Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description Specs") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bonusHashrate,
                        onValueChange = { bonusHashrate = it },
                        label = { Text("Speed (MH/s)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = costUsdt,
                        onValueChange = { costUsdt = it },
                        label = { Text("Cost (USDT)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                Button(
                    onClick = {
                        val speedVal = bonusHashrate.toDoubleOrNull() ?: 0.0
                        val costVal = costUsdt.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && speedVal > 0.0 && costVal > 0.0) {
                            viewModel.adminAddMiningPlan(name, description, speedVal, costVal)
                            name = ""
                            description = ""
                            bonusHashrate = ""
                            costUsdt = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("LAUNCH INVESTMENT PROTOCOL")
                }
            }
        }

        Text("ACTIVE HARDWARE MODELS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary, modifier = Modifier.padding(bottom = 6.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(plans) { plan ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(plan.description, fontSize = 10.sp, color = GeoTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                                Text("+${plan.bonusHashrate} MH/s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16803D))
                                Text("Price: $${plan.costUsdt} USDT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoPrimary)
                            }
                        }
                        IconButton(onClick = { viewModel.adminDeleteMiningPlan(plan.id) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFB3261E))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RewardConfigPanel(viewModel: CryptoViewModel) {
    val gift by viewModel.registrationGiftUsdt.collectAsState()
    val multiplier by viewModel.miningMultiplier.collectAsState()

    var giftText by remember(gift) { mutableStateOf(String.format(Locale.US, "%.2f", gift)) }
    var multiplierText by remember(multiplier) { mutableStateOf(String.format(Locale.US, "%.6f", multiplier)) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("REWARD SYSTEM PARAMETERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoPrimary)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Registration Signup Credit ($ USDT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary)
                    OutlinedTextField(
                        value = giftText,
                        onValueChange = { giftText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Mining Efficiency Multiplier (USDT/MH/sec)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary)
                    OutlinedTextField(
                        value = multiplierText,
                        onValueChange = { multiplierText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val g = giftText.toDoubleOrNull() ?: gift
                        val m = multiplierText.toDoubleOrNull() ?: multiplier
                        viewModel.adminUpdateRewardConfig(g, m)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("COMMIT PARAMETER SHIFT", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("INTEGRITY NOTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                Text(
                    text = "Altering reward scales adjusts downstream node payouts dynamically. The registration gift dictates the free balance allocation for newly joined mine operators.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun WalletMonitorPanel(
    viewModel: CryptoViewModel,
    transactions: List<TransactionEntity>,
    users: List<UserEntity>
) {
    val sdf = SimpleDateFormat("HH:mm:ss (dd MMM)", Locale.US)

    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No withdrawals triggered yet.", color = GeoTextSecondary)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(transactions) { tx ->
                val user = users.find { it.id == tx.userId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GeoBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Operator: ${user?.username?.uppercase() ?: "Unknown"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = sdf.format(Date(tx.timestamp)),
                                    fontSize = 9.sp,
                                    color = GeoTextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (tx.status) {
                                            "CONFIRMED" -> Color(0xFFDCFCE7)
                                            "PENDING" -> Color(0xFFFEF9C3)
                                            "FAILED" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFE0E7FF)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tx.status,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (tx.status) {
                                        "CONFIRMED" -> Color(0xFF166534)
                                        "PENDING" -> Color(0xFF854D0E)
                                        "FAILED" -> Color(0xFF991B1B)
                                        else -> Color(0xFF3730A3)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = GeoCardBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Recipient Address: ${tx.recipientAddress}",
                            fontSize = 10.sp,
                            color = GeoTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Payout: ${String.format(Locale.US, "%.5f", tx.amountCrypto)} ${tx.cryptoCurrency}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GeoPrimary
                            )
                            Text(
                                text = "Blockchain Fee: ${String.format(Locale.US, "%.4f", tx.feeUsdt)} USD",
                                fontSize = 10.sp,
                                color = GeoTextSecondary
                            )
                        }

                        if (tx.status == "PENDING") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { viewModel.adminApproveTransaction(tx.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16803D)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("SIGN & DISPATCH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.adminCancelTransaction(tx.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("REFUND PROTOCOL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueReportsPanel(users: List<UserEntity>, transactions: List<TransactionEntity>) {
    val totalOperators = users.size
    val totalHashrate = users.sumOf { it.hashrateMhs }
    val totalMinedUsdt = users.sumOf { it.totalMinedUsdt }
    val totalPayouts = transactions.filter { it.status == "CONFIRMED" || it.status == "CONFIRMING" }
        .sumOf { it.amountCrypto * 5.0 } // approximate reference payout calculation

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SYSTEM WIDE AGGREGATES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f).border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("OPERATORS", fontSize = 9.sp, color = GeoTextSecondary, fontWeight = FontWeight.Bold)
                    Text("$totalOperators", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GeoPrimary)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f).border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("NET POWER", fontSize = 9.sp, color = GeoTextSecondary, fontWeight = FontWeight.Bold)
                    Text("${String.format(Locale.US, "%.1f", totalHashrate)} MH/s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f).border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TOTAL MINED", fontSize = 9.sp, color = GeoTextSecondary, fontWeight = FontWeight.Bold)
                    Text("$${String.format(Locale.US, "%.2f", totalMinedUsdt)} USDT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f).border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DISPATCHED PAY", fontSize = 9.sp, color = GeoTextSecondary, fontWeight = FontWeight.Bold)
                    Text("$${String.format(Locale.US, "%.2f", totalPayouts)} USDT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                }
            }
        }

        Text("FINANCIAL BALANCE CHART", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary, modifier = Modifier.padding(top = 10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(180.dp).border(1.dp, GeoBorder, RoundedCornerShape(16.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = maxOf(totalMinedUsdt, totalPayouts, 10.0)
                    val widthScale = size.width - 120f
                    val yGap = size.height / 3

                    // Grid lines
                    for (i in 0..2) {
                        val y = i * yGap
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(100f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f
                        )
                    }

                    // Bar 1 - Mined
                    val barMinedWidth = (totalMinedUsdt / maxVal) * widthScale
                    drawRect(
                        color = Color(0xFFF59E0B),
                        size = Size(barMinedWidth.toFloat(), 30f),
                        topLeft = Offset(100f, yGap * 0.5f - 15f)
                    )

                    // Bar 2 - Dispatched
                    val barDisWidth = (totalPayouts / maxVal) * widthScale
                    drawRect(
                        color = Color(0xFF0EA5E9),
                        size = Size(barDisWidth.toFloat(), 30f),
                        topLeft = Offset(100f, yGap * 1.5f - 15f)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text("MINED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary)
                    Text("PAYOUTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary)
                }
            }
        }
    }
}

@Composable
fun FraudDetectionPanel(viewModel: CryptoViewModel, users: List<UserEntity>) {
    val anomalies = remember(users) {
        val list = mutableListOf<String>()
        // 1. Duplicate verification document filenames
        val docUrls = users.mapNotNull { it.verificationDocUrl }
        val duplicates = docUrls.groupBy { it }.filter { it.value.size > 1 }.keys
        users.forEach { user ->
            if (user.verificationDocUrl != null && duplicates.contains(user.verificationDocUrl)) {
                list.add("Suspicious identity overlap: Operator #${user.id} (${user.username}) shares Verification Attachment URL with another operator node!")
            }
            if (user.hashrateMhs > 400.0) {
                list.add("Anomalous high-speed miner: Operator #${user.id} (${user.username}) running custom hashrates at ${user.hashrateMhs} MH/s! High potential bot/cheat injection.")
            }
            if (user.balanceUsdt > 5000.0) {
                list.add("Excessive USDT balances: Operator #${user.id} (${user.username}) currently holds $${String.format(Locale.US, "%.2f", user.balanceUsdt)} USDT! Recommended monitoring.")
            }
        }
        list
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text("AUTOMATED INTEGRITY SCANS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (anomalies.isEmpty()) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (anomalies.isEmpty()) "SECURE" else "ALERT (${anomalies.size})",
                    color = if (anomalies.isEmpty()) Color(0xFF166534) else Color(0xFF991B1B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }
        }

        if (anomalies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Security, null, tint = Color(0xFF166534), modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Zero anomalies found. All hardware operators compliant.", color = GeoTextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(anomalies) { warning ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFB3261E), modifier = Modifier.size(20.dp))
                            Text(
                                text = warning,
                                color = Color(0xFF7F1D1D),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementsPanel(viewModel: CryptoViewModel) {
    val announcements by viewModel.announcements.collectAsState()
    var alertText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("POST SYSTEM BROADCAST ALERT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoPrimary)
                OutlinedTextField(
                    value = alertText,
                    onValueChange = { alertText = it },
                    placeholder = { Text("Enter alert or promotional campaign text...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Button(
                    onClick = {
                        if (alertText.isNotBlank()) {
                            viewModel.adminAddAnnouncement(alertText)
                            alertText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("POST TO BROADCAST LAYER")
                }
            }
        }

        Text("ACTIVE SYSTEM ALERTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextSecondary, modifier = Modifier.padding(bottom = 6.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            itemsIndexed(announcements) { index, alert ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GeoBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = alert,
                            fontSize = 11.sp,
                            color = GeoTextPrimary,
                            modifier = Modifier.weight(1f),
                            lineHeight = 15.sp
                        )
                        IconButton(onClick = { viewModel.adminDeleteAnnouncement(index) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFB3261E))
                        }
                    }
                }
            }
        }
    }
}
