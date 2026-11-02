package com.joseluiscruz.capturainventario.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.BuildConfig
import com.joseluiscruz.capturainventario.R
import com.joseluiscruz.capturainventario.ui.login.state.LoginEvent
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.BrandRadius
import com.joseluiscruz.capturainventario.ui.theme.BrandSizes
import com.joseluiscruz.capturainventario.ui.theme.BrandSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: com.joseluiscruz.capturainventario.ui.login.state.LoginUiState,
    onEvent: (LoginEvent) -> Unit
) {
    var username by remember { mutableStateOf(if (BuildConfig.DEBUG) "demo" else "") }
    var password by remember { mutableStateOf(if (BuildConfig.DEBUG) "demo" else "") }
    var passwordVisible by remember { mutableStateOf(false) }

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.85f) }
    val cardOffset = remember { Animatable(32f) }
    val cardAlpha = remember { Animatable(0f) }
    val footerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(120)
        launch { logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { logoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        delay(180)
        launch { cardAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch { cardOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
        delay(200)
        launch { footerAlpha.animateTo(1f, tween(400)) }
    }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val screenVerticalPadding = if (isKeyboardVisible) BrandSpacing.md else BrandSpacing.xxl
    val logoSize = if (isKeyboardVisible) 72.dp else 100.dp
    val titleToCardSpacer = if (isKeyboardVisible) BrandSpacing.md else BrandSpacing.xl
    val cardPadding = if (isKeyboardVisible) BrandSpacing.md else BrandSpacing.lg
    val formSpacer = if (isKeyboardVisible) BrandSpacing.md else BrandSpacing.lg

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = if (isKeyboardVisible) Alignment.TopCenter else Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = BrandSpacing.lg, vertical = screenVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isKeyboardVisible) Arrangement.Top else Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer(
                    alpha = logoAlpha.value,
                    scaleX = logoScale.value,
                    scaleY = logoScale.value
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isKeyboardVisible) 100.dp else 140.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    BrandColors.BrandRed.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_inventory),
                    contentDescription = "Logo Inventario Pro",
                    modifier = Modifier.size(logoSize)
                )
            }

            Spacer(modifier = Modifier.height(if (isKeyboardVisible) BrandSpacing.xs else BrandSpacing.md))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(alpha = logoAlpha.value)
            ) {
                Text(
                    text = "INVENTORY SCANNER",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrandColors.BrandWhite,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(BrandSpacing.xxs))
                Text(
                    text = if (BuildConfig.DEBUG) "DEMO EDITION · ADVANCED" else "CORPORATE EDITION · ADVANCED",
                    color = BrandColors.BrandRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(titleToCardSpacer))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        alpha = cardAlpha.value,
                        translationY = cardOffset.value
                    )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrandRadius.xl),
                    color = BrandColors.Surface1,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        BrandColors.Surface3.copy(alpha = 0.8f)
                    ),
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(cardPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BIENVENIDO",
                            color = BrandColors.TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(BrandSpacing.xxs))
                        Text(
                            "Accede al sistema de inventarios",
                            color = BrandColors.TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        if (BuildConfig.DEBUG) {
                            Spacer(modifier = Modifier.height(BrandSpacing.xs))
                            Text(
                                text = "Acceso demo: demo / demo",
                                color = BrandColors.TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(formSpacer))

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                onEvent(LoginEvent.OnErrorDismissed)
                            },
                            label = { Text("Usuario Corporativo") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = BrandColors.BrandRed
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) }
                            ),
                            shape = RoundedCornerShape(BrandRadius.lg),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BrandColors.Surface2,
                                unfocusedContainerColor = BrandColors.Surface2.copy(alpha = 0.7f),
                                focusedIndicatorColor = BrandColors.BrandRed,
                                unfocusedIndicatorColor = BrandColors.Surface3,
                                focusedTextColor = BrandColors.TextPrimary,
                                unfocusedTextColor = BrandColors.TextPrimary,
                                cursorColor = BrandColors.BrandRed,
                                focusedLabelColor = BrandColors.BrandRed,
                                unfocusedLabelColor = BrandColors.TextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(BrandSpacing.sm))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                onEvent(LoginEvent.OnErrorDismissed)
                            },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = BrandColors.BrandRed
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = BrandColors.TextSecondary
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    onEvent(LoginEvent.OnLoginClick(username, password))
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(BrandRadius.lg),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BrandColors.Surface2,
                                unfocusedContainerColor = BrandColors.Surface2.copy(alpha = 0.7f),
                                focusedIndicatorColor = BrandColors.BrandRed,
                                unfocusedIndicatorColor = BrandColors.Surface3,
                                focusedTextColor = BrandColors.TextPrimary,
                                unfocusedTextColor = BrandColors.TextPrimary,
                                cursorColor = BrandColors.BrandRed,
                                focusedLabelColor = BrandColors.BrandRed,
                                unfocusedLabelColor = BrandColors.TextSecondary
                            )
                        )

                        AnimatedVisibility(
                            visible = uiState.error != null,
                            enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(BrandSpacing.sm))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(BrandRadius.md),
                                    color = BrandColors.ErrorRed.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        BrandColors.ErrorRed.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(BrandSpacing.sm)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = BrandColors.ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = uiState.error ?: "",
                                            color = BrandColors.ErrorRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(formSpacer))

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onEvent(LoginEvent.OnLoginClick(username, password))
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(BrandSizes.ctaMinHeight),
                            shape = RoundedCornerShape(BrandRadius.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.BrandRed,
                                disabledContainerColor = BrandColors.BrandRed.copy(alpha = 0.4f),
                                contentColor = BrandColors.BrandWhite,
                                disabledContentColor = BrandColors.BrandWhite.copy(alpha = 0.7f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                "ACCEDER AL SISTEMA",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.width(BrandSpacing.sm))
                            Icon(
                                Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (!isKeyboardVisible) {
                Spacer(modifier = Modifier.height(BrandSpacing.xl))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer(alpha = footerAlpha.value)
                ) {
                    Text(
                        text = "PLANTA DE MANUFACTURA",
                        color = BrandColors.BrandWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(BrandSpacing.xxs))
                    Text(
                        text = "v${com.joseluiscruz.capturainventario.BuildConfig.VERSION_NAME} · PRO",
                        color = BrandColors.TextSecondary.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
