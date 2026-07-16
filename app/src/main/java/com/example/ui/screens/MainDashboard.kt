package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.LiveBlockchainState
import com.example.data.database.TransactionEntity
import com.example.data.database.UserEntity
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoCardBorder
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.viewmodel.CryptoViewModel
import com.example.ui.viewmodel.WithdrawalUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainDashboardScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isMining by viewModel.isMining.collectAsState()
    val blockchainState by viewModel.blockchainState.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val isCloudActive by viewModel.isCloudBackendActive.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Miner, 1 = Server Upgrades, 2 = Live Withdrawals

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar designed with Geometric Balance styling (height 16, items-center, padding, transparent white context)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Left Brand Icon/Avatar as per spec header layout
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GeoPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Justmine Logo",
                            tint = GeoOnPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Justmine",
                            color = GeoTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "BLOCKCHAIN NODE 04",
                                color = GeoPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isCloudActive) Color(0xFF81C784) else Color(0xFFFFB74D))
                            )
                        }
                    }
                }

                // Operator session logout action button
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = GeoTextSecondary
                    )
                }
            }

            // Tabs Content View (grows above navigation)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> MinerTab(viewModel, currentUser, isMining, blockchainState)
                    1 -> UpgradesTab(viewModel, currentUser)
                    2 -> WithdrawTab(viewModel, currentUser, blockchainState, transactions)
                }
            }

            // Navigation Bar styled exactly to spec: height 80, bg-[#F3EDF7], border-t border-[#CAC4D0]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(GeoSurfaceVariant)
                    .border(width = 0.5.dp, color = GeoBorder, shape = RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    title = "Home",
                    icon = Icons.Default.Dns,
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavBarItem(
                    title = "Mining",
                    icon = Icons.Default.Memory,
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavBarItem(
                    title = "Ledger",
                    icon = Icons.Default.CurrencyBitcoin,
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeBg = GeoPrimaryContainer
    val activeContentColor = GeoOnPrimaryContainer
    val inactiveContentColor = GeoTextSecondary

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(64.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Active visual highlight pill as requested by "Geometric Balance" navigation spec: bg-[#EADDFF] px-5 py-1 rounded-full
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (selected) activeBg else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) activeContentColor else inactiveContentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            color = if (selected) activeContentColor else inactiveContentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun MinerTab(
    viewModel: CryptoViewModel,
    currentUser: UserEntity?,
    isMining: Boolean,
    blockchainState: LiveBlockchainState
) {
    val isCloudActive by viewModel.isCloudBackendActive.collectAsState()
    // Infinitely spinning mining gears animation if mining
    val infiniteTransition = rememberInfiniteTransition(label = "gears")
    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isMining) 4000 else 0, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val announcements by viewModel.announcements.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (announcements.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFDA4AF), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE4E6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Broadcast Announcement",
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            announcements.forEach { announcement ->
                                Text(
                                    text = "• $announcement",
                                    color = Color(0xFF9F1239),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Balance Card: styled exactly as the spec's bg-[#EADDFF] rounded-[28px] p-6 shadow-sm
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoPrimaryContainer),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header row inside card (Available Balance Title + Rose live badge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Balance",
                            color = GeoOnPrimaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Live sync badge: bg-[#FFD8E4] text-[#31111D] text-[10px] with a #B3261E dot
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFFFFD8E4))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val pulseTransition = rememberInfiniteTransition(label = "pulse_red")
                            val pulseAlpha by pulseTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "red_alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB3261E).copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = "LIVE SYNC",
                                color = Color(0xFF31111D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Formatted ticking USD Balance (e.g. 0.08421 BTC / USD format)
                    val balanceFormatted = String.format(Locale.US, "$ %.6f", currentUser?.balanceUsdt ?: 0.0)
                    Text(
                        text = balanceFormatted,
                        color = GeoOnPrimaryContainer,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // BTC Equivalent estimate
                    val btcEquivalent = (currentUser?.balanceUsdt ?: 0.0) / blockchainState.btcPriceUsd
                    Text(
                        text = String.format(Locale.US, "≈ %.8f BTC", btcEquivalent),
                        color = GeoTextSecondary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Linear slider visual representation as requested by spec
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(GeoOnPrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .fillMaxHeight()
                                .background(GeoOnPrimaryContainer, RoundedCornerShape(100.dp))
                        )
                    }
                }
            }
        }

        // Miner Control Action Button Card (Surface variant)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isMining) Color(0xFFEADDFF) else Color(0xFFFFD8E4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMining) Icons.Default.Bolt else Icons.Default.OfflineBolt,
                                contentDescription = "Engine Icon",
                                tint = if (isMining) GeoOnPrimaryContainer else Color(0xFFB3261E),
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(gearRotation)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isMining) "Cloud Miner Active" else "Miner Suspended",
                                color = GeoTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isMining) "Generating blockchain rewards..." else "Activate hash node to mine",
                                color = GeoTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleMining() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMining) Color(0xFFB3261E) else GeoPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.testTag("toggle_miner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power Miner",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMining) "PAUSE" else "START",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Market & Blockchain Network Header
        item {
            Text(
                text = "LIVE BLOCKCHAIN METRICS",
                color = GeoPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Statistics Grid using clean border styled cards matching the theme
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Blockchain network stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = "Connection",
                                tint = GeoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bitcoin Block Height",
                                color = GeoTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "# ${blockchainState.bitcoinBlockHeight}",
                            color = GeoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GeoCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Prices List
                    LivePriceRow("Bitcoin (BTC)", blockchainState.btcPriceUsd, blockchainState.btc24hChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    LivePriceRow("Ethereum (ETH)", blockchainState.ethPriceUsd, blockchainState.eth24hChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    LivePriceRow("Litecoin (LTC)", blockchainState.ltcPriceUsd, blockchainState.ltc24hChange)
                    Spacer(modifier = Modifier.height(12.dp))
                    LivePriceRow("Dogecoin (DOGE)", blockchainState.dogePriceUsd, blockchainState.doge24hChange)
                }
            }
        }

        // Extra info statistics bar (Active Rig / Current Power) in clean geometric style
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GeoSurfaceVariant)
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Current Power", color = GeoTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(Locale.US, "%.1f MH/s", currentUser?.hashrateMhs ?: 0.0),
                        color = GeoTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Active Rig", color = GeoTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = currentUser?.activeMinerName ?: "Standard Node",
                        color = GeoTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Wallet Broadcasting Engine Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header row with satellite/broadcast icon and status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (currentUser?.isBroadcastingActive == true) GeoPrimaryContainer else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = "Broadcasting Icon",
                                    tint = if (currentUser?.isBroadcastingActive == true) GeoOnPrimaryContainer else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LIVE WALLET BROADCASTING",
                                    color = GeoTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Auto-forward miner rewards",
                                    color = GeoTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Switch toggle
                        Switch(
                            checked = currentUser?.isBroadcastingActive ?: false,
                            onCheckedChange = { active ->
                                viewModel.updateBroadcastingConfig(
                                    isActive = active,
                                    walletType = currentUser?.broadcastingWalletType ?: "BTC",
                                    address = currentUser?.broadcastingWalletAddress ?: ""
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeoPrimary,
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("broadcasting_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = GeoCardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Configuration Section
                    var expandedCoinDropdown by remember { mutableStateOf(false) }
                    val coinOptions = listOf("BTC", "ETH", "LTC", "DOGE")
                    val selectedCoin = currentUser?.broadcastingWalletType ?: "BTC"
                    var destAddressText by remember(currentUser?.broadcastingWalletAddress) { mutableStateOf(currentUser?.broadcastingWalletAddress ?: "") }

                    // Coin Select Dropdown
                    Text(
                        text = "Target Cryptocurrency",
                        color = GeoTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "$selectedCoin - Blockchain Payout",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedCoinDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand Coin Dropdown",
                                        tint = GeoPrimary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeoPrimary,
                                unfocusedBorderColor = GeoBorder,
                                focusedTextColor = GeoTextPrimary,
                                unfocusedTextColor = GeoTextPrimary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCoinDropdown = true }
                                .testTag("broadcasting_coin_selector"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedCoinDropdown,
                            onDismissRequest = { expandedCoinDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            coinOptions.forEach { coin ->
                                DropdownMenuItem(
                                    text = { Text(coin, color = GeoTextPrimary, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        expandedCoinDropdown = false
                                        viewModel.updateBroadcastingConfig(
                                            isActive = currentUser?.isBroadcastingActive ?: false,
                                            walletType = coin,
                                            address = destAddressText
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Address input
                    Text(
                        text = "Destination Wallet Address",
                        color = GeoTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = destAddressText,
                        onValueChange = {
                            destAddressText = it
                        },
                        placeholder = { Text("Enter external $selectedCoin address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("broadcasting_address_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save Button
                    Button(
                        onClick = {
                            viewModel.updateBroadcastingConfig(
                                isActive = currentUser?.isBroadcastingActive ?: false,
                                walletType = selectedCoin,
                                address = destAddressText
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_broadcasting_config_button")
                    ) {
                        Text("Save Wallet Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Live Status tunnel/details when active
                    if (currentUser?.isBroadcastingActive == true && !currentUser.broadcastingWalletAddress.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // Soft green container
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Pulsing green live dot
                                    val pulseTransition = rememberInfiniteTransition(label = "pulse_green")
                                    val pulseAlpha by pulseTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "green_alpha"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A).copy(alpha = pulseAlpha))
                                    )
                                    Text(
                                        text = "TUNNEL ACTIVE - BROADCASTING TO $selectedCoin NODE",
                                        color = Color(0xFF15803D),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Threshold Progress Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Next Auto-Broadcast Pool",
                                        color = Color(0xFF166534),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = String.format(Locale.US, "$ %.4f / $0.50", currentUser.broadcastPoolUsdt),
                                        color = Color(0xFF166534),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Progress Bar
                                val progressPercent = (currentUser.broadcastPoolUsdt / 0.50).coerceIn(0.0, 1.0).toFloat()
                                LinearProgressIndicator(
                                    progress = { progressPercent },
                                    color = Color(0xFF16A34A),
                                    trackColor = Color(0xFFDCFCE7),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Total Broadcasted Payouts Stat
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Broadcasted Mined",
                                        color = Color(0xFF166534),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = String.format(Locale.US, "$ %.4f USDT", currentUser.totalBroadcastedUsdt),
                                        color = Color(0xFF15803D),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // KYC Operator Node Verification Card
        item {
            val uploadState by viewModel.uploadState.collectAsState()
            val uploadProgress by viewModel.uploadProgress.collectAsState()
            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: android.net.Uri? ->
                uri?.let { viewModel.uploadKycDocument(it) }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with secure shield icon and dynamic badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentUser?.verificationDocUrl != null) GeoPrimaryContainer
                                        else Color(0xFFFFD8E4)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentUser?.verificationDocUrl != null) Icons.Default.Verified else Icons.Default.Shield,
                                    contentDescription = "KYC Status Icon",
                                    tint = if (currentUser?.verificationDocUrl != null) GeoOnPrimaryContainer else Color(0xFFB3261E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SECURE OPERATOR KYC",
                                    color = GeoTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Node validation protocol",
                                    color = GeoTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(
                                    if (currentUser?.verificationDocUrl != null) GeoPrimaryContainer
                                    else Color(0xFFFFE082) // Amber/Yellow
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (currentUser?.verificationDocUrl != null) "VERIFIED" else "PENDING KYC",
                                color = if (currentUser?.verificationDocUrl != null) GeoOnPrimaryContainer else Color(0xFF5D4037),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GeoCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentUser?.verificationDocUrl == null) {
                        // Unverified state: explain and offer file picker
                        Text(
                            text = "To authorize unlimited high-volume blockchain withdrawal transfers, the node operator must upload a valid proof of identity (ID Card, Passport, or Driver's License) securely to Firebase Storage.",
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom dashed scanner-like container for file upload
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(
                                    width = 1.dp,
                                    color = GeoBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (uploadState !is com.example.ui.viewmodel.UploadUiState.Loading) {
                                        filePickerLauncher.launch("image/*")
                                    }
                                }
                                .testTag("kyc_file_picker_area"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload Document Placeholder",
                                    tint = GeoPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "TAP TO CHOOSE PHOTO",
                                    color = GeoPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Supports JPEG or PNG formats",
                                    color = GeoTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Upload progress indicator
                        if (uploadState is com.example.ui.viewmodel.UploadUiState.Loading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = { (uploadProgress ?: 0.0).toFloat() / 100f },
                                    color = GeoPrimary,
                                    trackColor = GeoPrimaryContainer,
                                    modifier = Modifier.fillMaxWidth().height(4.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Uploading identity document to Firebase Storage: ${String.format(Locale.US, "%.1f", uploadProgress ?: 0.0)}%",
                                    color = GeoPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Upload error message
                        if (uploadState is com.example.ui.viewmodel.UploadUiState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (uploadState as com.example.ui.viewmodel.UploadUiState.Error).message,
                                color = Color(0xFFB3261E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { filePickerLauncher.launch("image/*") },
                            enabled = uploadState !is com.example.ui.viewmodel.UploadUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GeoPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = GeoPrimary.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("upload_kyc_button")
                        ) {
                            Text(
                                text = "UPLOAD OPERATOR ID",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else {
                        // Verified state: display verification summary and load the live document from Firebase Storage
                        Text(
                            text = "This node operator credentials has been securely registered on Firebase Storage database feeds. Unlimited withdrawal authorization status is ACTIVE.",
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render the image dynamically using Coil
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, GeoCardBorder, RoundedCornerShape(16.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = currentUser?.verificationDocUrl,
                                contentDescription = "Verified Identity Document Preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { filePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = GeoPrimary
                            ),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .border(1.dp, GeoPrimary, RoundedCornerShape(100.dp))
                                .testTag("reupload_kyc_button")
                        ) {
                            Text(
                                text = "RE-UPLOAD NEW IDENTITY DOCUMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Secure Session / Operator Log Out option
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GeoPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Operator Icon",
                                tint = GeoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OPERATOR: ${currentUser?.username?.uppercase(Locale.US) ?: "OFFLINE"}",
                                color = GeoTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isCloudActive) "Firebase Cloud Backend sync is ACTIVE" else "Simulated Local Node Backup is ACTIVE",
                                color = if (isCloudActive) Color(0xFF81C784) else Color(0xFFFFB74D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GeoCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB3261E),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dashboard_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out Icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOG OUT SECURELY",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LivePriceRow(name: String, priceUsd: Double, changePercent: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = GeoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format(Locale.US, "$%,.2f", priceUsd),
                color = GeoTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            val isGreen = changePercent >= 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isGreen) Color(0xFFEADDFF) else Color(0xFFFFD8E4))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (isGreen) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isGreen) GeoOnPrimaryContainer else Color(0xFFB3261E),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = String.format(Locale.US, "%.2f%%", changePercent),
                    color = if (isGreen) GeoOnPrimaryContainer else Color(0xFFB3261E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun UpgradesTab(
    viewModel: CryptoViewModel,
    currentUser: UserEntity?
) {
    val upgradeMsg by viewModel.upgradeMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "RENT CLOUD MINING HARDWARE",
            color = GeoPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Upgrade result notification
        AnimatedVisibility(
            visible = upgradeMsg != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            upgradeMsg?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.contains("Success")) GeoPrimaryContainer else Color(0xFFFFD8E4)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, if (it.contains("Success")) GeoPrimary.copy(alpha = 0.5f) else Color(0xFFB3261E).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (it.contains("Success")) Icons.Default.Bolt else Icons.Default.Warning,
                            contentDescription = "Upgrade Info",
                            tint = if (it.contains("Success")) GeoOnPrimaryContainer else Color(0xFFB3261E),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = it,
                            color = if (it.contains("Success")) GeoOnPrimaryContainer else Color(0xFF31111D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        val plans by viewModel.miningPlans.collectAsState()

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(plans) { plan ->
                UpgradeItemCard(
                    title = plan.name,
                    description = plan.description,
                    bonusHashrate = plan.bonusHashrate,
                    costUsdt = plan.costUsdt,
                    isActive = currentUser?.activeMinerName == plan.name,
                    onBuy = { viewModel.purchaseRigUpgrade(plan.name, plan.bonusHashrate, plan.costUsdt) }
                )
            }
        }
    }
}

@Composable
fun UpgradeItemCard(
    title: String,
    description: String,
    bonusHashrate: Double,
    costUsdt: Double,
    isActive: Boolean,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) GeoPrimary.copy(alpha = 0.5f) else GeoCardBorder,
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = GeoTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(GeoPrimaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = GeoOnPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = String.format(Locale.US, "$%,.2f", costUsdt),
                        color = GeoPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = GeoTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Speed Gain",
                        tint = GeoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+$bonusHashrate MH/s Speed",
                        color = GeoPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onBuy,
                    enabled = !isActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoPrimary,
                        disabledContainerColor = GeoCardBorder,
                        contentColor = Color.White,
                        disabledContentColor = GeoTextSecondary
                    ),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.testTag("buy_${title.replace(" ", "_").lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Rent Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isActive) "Rent Active" else "Rent Rig", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WithdrawTab(
    viewModel: CryptoViewModel,
    currentUser: UserEntity?,
    blockchainState: LiveBlockchainState,
    transactions: List<TransactionEntity>
) {
    val withdrawalState by viewModel.withdrawalState.collectAsState()
    val context = LocalContext.current

    var selectedCoin by remember { mutableStateOf("BTC") }
    var walletAddress by remember { mutableStateOf("") }
    var amountUsdtText by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val coinPrice = when (selectedCoin) {
        "BTC" -> blockchainState.btcPriceUsd
        "ETH" -> blockchainState.ethPriceUsd
        "LTC" -> blockchainState.ltcPriceUsd
        "DOGE" -> blockchainState.dogePriceUsd
        else -> 1.0
    }

    val amountUsdt = amountUsdtText.toDoubleOrNull() ?: 0.0
    val amountCrypto = amountUsdt / coinPrice

    val estimatedFee = when (selectedCoin) {
        "BTC" -> (blockchainState.recommendedBtcFeeSatVb * 140.0) / 100_000_000.0 * blockchainState.btcPriceUsd
        "ETH" -> (blockchainState.recommendedEthFeeGwei * 21_000.0) / 1_000_000_000.0 * blockchainState.ethPriceUsd
        "LTC" -> 0.08
        "DOGE" -> 0.12
        else -> 0.05
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Withdrawal Form Card (Surface Variant with balanced design)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "EXECUTE BLOCKCHAIN WITHDRAWAL",
                        color = GeoTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Coin Select Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "$selectedCoin - Live Blockchain Node",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selected Blockchain Network") },
                            trailingIcon = {
                                IconButton(onClick = { isDropdownExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand coin selector",
                                        tint = GeoPrimary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeoPrimary,
                                unfocusedBorderColor = GeoBorder,
                                focusedTextColor = GeoTextPrimary,
                                unfocusedTextColor = GeoTextPrimary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDropdownExpanded = true }
                                .testTag("coin_selector_trigger")
                        )

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(GeoSurfaceVariant)
                                .border(1.dp, GeoCardBorder)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Bitcoin Network (BTC)", color = GeoTextPrimary) },
                                onClick = {
                                    selectedCoin = "BTC"
                                    isDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ethereum Network (ETH)", color = GeoTextPrimary) },
                                onClick = {
                                    selectedCoin = "ETH"
                                    isDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Litecoin Blockchain (LTC)", color = GeoTextPrimary) },
                                onClick = {
                                    selectedCoin = "LTC"
                                    isDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Dogecoin Network (DOGE)", color = GeoTextPrimary) },
                                onClick = {
                                    selectedCoin = "DOGE"
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Address Input Field
                    OutlinedTextField(
                        value = walletAddress,
                        onValueChange = { walletAddress = it },
                        label = { Text("Recipient Destination Wallet Address") },
                        placeholder = { Text("Paste live wallet node address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedLabelColor = GeoPrimary,
                            unfocusedLabelColor = GeoTextSecondary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wallet_address_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Input Field with MAX shortcut button
                    OutlinedTextField(
                        value = amountUsdtText,
                        onValueChange = { amountUsdtText = it },
                        label = { Text("Amount to withdraw (USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    amountUsdtText = String.format(Locale.US, "%.2f", currentUser?.balanceUsdt ?: 0.0)
                                },
                                modifier = Modifier.testTag("max_amount_button")
                            ) {
                                Text("MAX", color = GeoPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoBorder,
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedLabelColor = GeoPrimary,
                            unfocusedLabelColor = GeoTextSecondary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdrawal_amount_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Converted Cryptocurrency Equivalents Box
                    if (amountUsdt > 0.0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, GeoCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Dynamic Fee estimation:", color = GeoTextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = String.format(Locale.US, "$ %.2f USDT", estimatedFee),
                                        color = GeoTextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Net payout total:", color = GeoTextSecondary, fontSize = 12.sp)
                                    Text(
                                        text = String.format(Locale.US, "≈ %.6f %s", amountCrypto, selectedCoin),
                                        color = GeoPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Error/Success state notification
                    AnimatedVisibility(
                        visible = withdrawalState is WithdrawalUiState.Error,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val errMsg = (withdrawalState as? WithdrawalUiState.Error)?.message ?: ""
                        Text(
                            text = errMsg,
                            color = Color(0xFFB3261E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = withdrawalState is WithdrawalUiState.Success,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Blockchain request successfully broadcasted! Check live logs below.",
                            color = GeoPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    // Withdraw trigger button (Geometric rounded 100.dp)
                    Button(
                        onClick = {
                            viewModel.submitWithdrawal(selectedCoin, walletAddress, amountUsdt)
                        },
                        enabled = withdrawalState !is WithdrawalUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            disabledContainerColor = GeoPrimary.copy(alpha = 0.4f),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("execute_withdrawal_button")
                    ) {
                        if (withdrawalState is WithdrawalUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "BROADCAST WITHDRAWAL",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Blockchain Confirmation Tracker (Active or Last Transaction)
        val activeTx = transactions.firstOrNull { it.status != "CONFIRMED" && it.status != "FAILED" }
        if (activeTx != null) {
            item {
                ActiveWithdrawalTrackerCard(activeTx, viewModel)
            }
        }

        // Header for History Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Logs",
                    tint = GeoPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BLOCKCHAIN TRANSACTION HISTORY",
                    color = GeoPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Empty logs state representation
        if (transactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = "Empty",
                            tint = GeoTextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No withdrawals registered yet.",
                            color = GeoTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionHistoryCard(tx)
            }
        }
    }
}

@Composable
fun ActiveWithdrawalTrackerCard(
    tx: TransactionEntity,
    viewModel: CryptoViewModel
) {
    val context = LocalContext.current
    val steps = listOf("PENDING", "SIGNING", "BROADCASTING", "CONFIRMING")
    val currentStepIndex = when (tx.status) {
        "PENDING" -> 0
        "SIGNING" -> 1
        "BROADCASTING" -> 2
        "CONFIRMING", "CONFIRMED" -> 3
        else -> 0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, GeoPrimary.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE LIVE BLOCKCHAIN PIPELINE",
                    color = GeoPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(GeoPrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tx.status,
                        color = GeoOnPrimaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step status visual elements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { idx, stepName ->
                    val isCompleted = idx <= currentStepIndex
                    val stepColor = if (isCompleted) GeoPrimary else GeoTextSecondary.copy(alpha = 0.3f)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(stepColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stepName,
                            color = stepColor,
                            fontSize = 9.sp,
                            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Block confirmation indicator
            if (tx.status == "CONFIRMING" || tx.status == "CONFIRMED") {
                val progressPercent = tx.confirmations.toFloat() / tx.targetConfirmations.toFloat()
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ledger Confirmations",
                            color = GeoTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${tx.confirmations} of ${tx.targetConfirmations} Blocks Verified",
                            color = GeoTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        color = GeoPrimary,
                        trackColor = GeoBorder.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = GeoPrimary,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (tx.status) {
                            "PENDING" -> "Queueing transaction node payload..."
                            "SIGNING" -> "Signing private keys secure signature..."
                            "BROADCASTING" -> "Broadcasting RAW hexadecimal hash stream..."
                            else -> "Syncing live nodes..."
                        },
                        color = GeoTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GeoCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Tx Hash visual copy banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Tx Hash", tx.txHash)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Tx Hash Copied!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TRANSACTION SOURCE HASH", color = GeoTextSecondary, fontSize = 9.sp)
                    Text(
                        text = tx.txHash,
                        color = GeoTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Hash",
                    tint = GeoPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionHistoryCard(tx: TransactionEntity) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(tx.timestamp))

    val statusColor = when (tx.status) {
        "CONFIRMED" -> GeoPrimary
        "FAILED" -> Color(0xFFB3261E)
        else -> GeoPrimary
    }

    val containerBg = when (tx.status) {
        "CONFIRMED" -> GeoPrimaryContainer
        "FAILED" -> Color(0xFFFFD8E4)
        else -> GeoSurfaceVariant
    }

    val onContainerColor = when (tx.status) {
        "CONFIRMED" -> GeoOnPrimaryContainer
        "FAILED" -> Color(0xFF31111D)
        else -> GeoTextPrimary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(containerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyBitcoin,
                            contentDescription = tx.cryptoCurrency,
                            tint = onContainerColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = String.format(Locale.US, "%.6f %s", tx.amountCrypto, tx.cryptoCurrency),
                            color = GeoTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = formattedDate,
                            color = GeoTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(containerBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (tx.status == "CONFIRMING") "${tx.confirmations}/${tx.targetConfirmations}" else tx.status,
                        color = onContainerColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GeoCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Address display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Target Wallet Address", color = GeoTextSecondary, fontSize = 12.sp)
                Text(
                    text = if (tx.recipientAddress.length > 16) {
                        tx.recipientAddress.take(8) + "..." + tx.recipientAddress.takeLast(8)
                    } else {
                        tx.recipientAddress
                    },
                    color = GeoTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Address", tx.recipientAddress)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Wallet Address Copied!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Hash display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Transaction Hash", color = GeoTextSecondary, fontSize = 12.sp)
                Text(
                    text = tx.txHash.take(10) + "..." + tx.txHash.takeLast(10),
                    color = GeoTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Hash", tx.txHash)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Tx Hash Copied!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
