package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoPrimary
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoCardBorder
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.CryptoViewModel

@Composable
fun LoginScreen(
    viewModel: CryptoViewModel,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val isCloudActive by viewModel.isCloudBackendActive.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Logo / Icon styled with Primary Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GeoPrimaryContainer)
                    .border(1.5.dp, GeoPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Justmine Logo",
                    tint = GeoOnPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Justmine",
                color = GeoTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Text(
                text = "BLOCKCHAIN NODE 04",
                color = GeoPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            BackendStatusBadge(isCloudActive = isCloudActive)

            Spacer(modifier = Modifier.height(24.dp))

            // Login Panel Card with Surface Variant & card border
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Access Mining Panel",
                        color = GeoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username Icon",
                                tint = GeoPrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoCardBorder,
                            focusedLabelColor = GeoPrimary,
                            unfocusedLabelColor = GeoTextSecondary,
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = GeoPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = GeoTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoCardBorder,
                            focusedLabelColor = GeoPrimary,
                            unfocusedLabelColor = GeoTextSecondary,
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Feedback Banner
                    AnimatedVisibility(
                        visible = authState is AuthUiState.Error,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val errorMsg = (authState as? AuthUiState.Error)?.message ?: ""
                        Text(
                            text = errorMsg,
                            color = Color(0xFFB3261E), // Red (standard error in Geometric Balance)
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Login Action Button (Dynamic Primary Color)
                    Button(
                        onClick = { viewModel.login(username, password) },
                        enabled = authState !is AuthUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            disabledContainerColor = GeoPrimary.copy(alpha = 0.4f),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(100.dp), // Fully rounded as per Geometric Balance specs
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("login_button")
                    ) {
                        if (authState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Start Mining Session",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation to Sign Up
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "New operator?",
                    color = GeoTextSecondary,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = {
                        viewModel.clearAuthError()
                        onNavigateToSignUp()
                    },
                    modifier = Modifier.testTag("go_to_signup_button")
                ) {
                    Text(
                        text = "Create Node Account",
                        color = GeoPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    viewModel: CryptoViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val isCloudActive by viewModel.isCloudBackendActive.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GeoPrimaryContainer)
                    .border(1.5.dp, GeoPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Justmine Logo",
                    tint = GeoOnPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Establish Mining Node",
                color = GeoTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Deploy remote hash powers instantly.",
                color = GeoTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            BackendStatusBadge(isCloudActive = isCloudActive)

            Spacer(modifier = Modifier.height(16.dp))

            // Bonus Promotion Card using light primary container and red live sync theme
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoPrimaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoPrimary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFD8E4), RoundedCornerShape(100.dp))
                            .border(1.dp, Color(0xFF31111D).copy(alpha = 0.12f), RoundedCornerShape(100.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Bonus",
                            tint = Color(0xFFB3261E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ONBOARDING GIFT: $15.75 USDT",
                            color = GeoOnPrimaryContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "New operators receive starting mining assets to test withdrawals on live blockchain feeds instantly.",
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Panel Card
            Card(
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoCardBorder, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Credentials setup",
                        color = GeoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Create Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username Icon",
                                tint = GeoPrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoCardBorder,
                            focusedLabelColor = GeoPrimary,
                            unfocusedLabelColor = GeoTextSecondary,
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_username_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Secure Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = GeoPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = GeoTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimary,
                            unfocusedBorderColor = GeoCardBorder,
                            focusedLabelColor = GeoPrimary,
                            unfocusedLabelColor = GeoTextSecondary,
                            focusedTextColor = GeoTextPrimary,
                            unfocusedTextColor = GeoTextPrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_password_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Feedback Banner
                    AnimatedVisibility(
                        visible = authState is AuthUiState.Error,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val errorMsg = (authState as? AuthUiState.Error)?.message ?: ""
                        Text(
                            text = errorMsg,
                            color = Color(0xFFB3261E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Register Action Button (Dynamic Primary Color)
                    Button(
                        onClick = { viewModel.register(username, password) },
                        enabled = authState !is AuthUiState.Loading,
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
                            .testTag("signup_button")
                    ) {
                        if (authState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Deploy Mining Hardware",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have a node?",
                    color = GeoTextSecondary,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = {
                        viewModel.clearAuthError()
                        onNavigateToLogin()
                    },
                    modifier = Modifier.testTag("go_to_login_button")
                ) {
                    Text(
                        text = "Sign In Here",
                        color = GeoPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BackendStatusBadge(
    isCloudActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isCloudActive) Color(0xFF1B3B22) else Color(0xFF3B2F1B)
    val textColor = if (isCloudActive) Color(0xFF81C784) else Color(0xFFFFB74D)
    val borderColor = if (isCloudActive) Color(0xFF2E7D32) else Color(0xFFE65100)
    val text = if (isCloudActive) "🟢 FIREBASE BACKEND ACTIVE" else "🟡 LOCAL OFFLINE NODE BACKUP"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
