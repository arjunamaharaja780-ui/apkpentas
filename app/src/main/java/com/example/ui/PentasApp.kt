package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppRoute
import com.example.model.UserRole
import com.example.ui.components.IslamicGeometricBackdrop
import com.example.ui.components.LoadingOverlay
import com.example.ui.components.LogoOrEmblem
import com.example.ui.components.MarqueeBanner
import com.example.ui.screens.CetakExcelScreen
import com.example.ui.screens.CetakJuaraScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NoUrutScreen
import com.example.ui.screens.PengaturanPenilaianScreen
import com.example.ui.screens.PenilaianScreen
import com.example.ui.screens.PesertaScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.RekapNilaiScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PaiEmerald200
import com.example.ui.theme.PaiEmerald400
import com.example.ui.theme.PaiEmerald600
import com.example.ui.theme.PaiEmerald900
import com.example.ui.theme.Rose700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.PentasUiState
import com.example.viewmodel.PentasViewModel
import kotlinx.coroutines.launch

private data class NavMenuItem(
    val route: AppRoute,
    val icon: ImageVector
)

private fun allowedMenuItems(role: UserRole): List<NavMenuItem> {
    return when (role) {
        UserRole.ADMIN -> listOf(
            NavMenuItem(AppRoute.HOME, Icons.Default.Home),
            NavMenuItem(AppRoute.REGISTER, Icons.Default.PersonAdd),
            NavMenuItem(AppRoute.PESERTA, Icons.Default.Groups),
            NavMenuItem(AppRoute.NO_URUT, Icons.Default.FormatListNumbered),
            NavMenuItem(AppRoute.CETAK_EXCEL, Icons.Default.FileDownload),
            NavMenuItem(AppRoute.CETAK_JUARA, Icons.Default.WorkspacePremium),
            NavMenuItem(AppRoute.LEADERBOARD, Icons.Default.EmojiEvents),
            NavMenuItem(AppRoute.PENILAIAN, Icons.Default.Star),
            NavMenuItem(AppRoute.REKAP_JURI, Icons.AutoMirrored.Filled.Assignment),
            NavMenuItem(AppRoute.REKAP_ADMIN, Icons.Default.Storage),
            NavMenuItem(AppRoute.PENGATURAN_PENILAIAN, Icons.Default.Tune),
            NavMenuItem(AppRoute.AKSES, Icons.Default.Key)
        )
        UserRole.SEKOLAH -> listOf(
            NavMenuItem(AppRoute.HOME, Icons.Default.Home),
            NavMenuItem(AppRoute.REGISTER, Icons.Default.PersonAdd),
            NavMenuItem(AppRoute.PESERTA, Icons.Default.Groups),
            NavMenuItem(AppRoute.LEADERBOARD, Icons.Default.EmojiEvents)
        )
        UserRole.JURI -> listOf(
            NavMenuItem(AppRoute.HOME, Icons.Default.Home),
            NavMenuItem(AppRoute.LEADERBOARD, Icons.Default.EmojiEvents),
            NavMenuItem(AppRoute.PENILAIAN, Icons.Default.Star),
            NavMenuItem(AppRoute.REKAP_JURI, Icons.AutoMirrored.Filled.Assignment)
        )
        UserRole.UMUM -> listOf(
            NavMenuItem(AppRoute.HOME, Icons.Default.Home),
            NavMenuItem(AppRoute.LEADERBOARD, Icons.Default.EmojiEvents)
        )
    }
}

@Composable
fun PentasApp(viewModel: PentasViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    fun showLocalToast(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val session = uiState.session
        if (session == null) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = if (uiState.isStatusError) Rose700 else Slate900,
                            contentColor = Color.White
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    LoginScreen(
                        uiState = uiState,
                        onRefresh = { viewModel.refreshFromServer(true) },
                        onLogin = { role, pwd, school, juriId ->
                            viewModel.login(role, pwd, school, juriId)
                        }
                    )
                }
            }
        } else {
            AuthenticatedShell(
                uiState = uiState,
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onShowMessage = ::showLocalToast
            )
        }

        LoadingOverlay(
            visible = uiState.isBusy,
            message = uiState.busyMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedShell(
    uiState: PentasUiState,
    viewModel: PentasViewModel,
    snackbarHostState: SnackbarHostState,
    onShowMessage: (String) -> Unit
) {
    val session = uiState.session ?: return
    val isAdmin = session.role == UserRole.ADMIN
    val menuItems = remember(session.role) { allowedMenuItems(session.role) }
    val bottomItems = remember(menuItems) { menuItems.take(4) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showEditRunningText by remember { mutableStateOf(false) }
    var showEditKecamatan by remember { mutableStateOf(false) }

    if (uiState.currentRoute != AppRoute.HOME) {
        BackHandler {
            viewModel.navigate(AppRoute.HOME)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 840.dp

        val drawerContent: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(290.dp)
                    .background(Slate900)
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LogoOrEmblem(logoDataUrl = uiState.appLogoDataUrl, size = 44.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PENTAS PAI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(
                            text = "${uiState.namaKecamatan} ${uiState.tahunPelaksanaan}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PaiEmerald400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Slate800,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Masuk Sebagai:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Text(
                                text = session.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = PaiEmerald200,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { showLogoutConfirm = true }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar", tint = Color(0xFFFCA5A5))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate800)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    menuItems.forEach { item ->
                        val selected = uiState.currentRoute == item.route
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = item.route.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            selected = selected,
                            onClick = {
                                viewModel.navigate(item.route)
                                if (!isExpanded) {
                                    scope.launch { drawerState.close() }
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = PaiEmerald600.copy(alpha = 0.28f),
                                selectedTextColor = Color.White,
                                selectedIconColor = PaiEmerald400,
                                unselectedContainerColor = Color.Transparent,
                                unselectedTextColor = Color(0xFFCBD5E1),
                                unselectedIconColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                HorizontalDivider(color = Slate800)
                Text(
                    text = "PENTAS PAI ${uiState.namaKecamatan} © ${uiState.tahunPelaksanaan}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }

        val mainScaffold: @Composable () -> Unit = {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = if (uiState.isStatusError) Rose700 else Slate900,
                            contentColor = Color.White
                        )
                    }
                },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = uiState.currentRoute.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            if (!isExpanded) {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("drawer_menu_button")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Buka Menu")
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.refreshFromServer(true) },
                                modifier = Modifier.testTag("topbar_refresh_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Segarkan Data")
                            }
                            IconButton(
                                onClick = { showLogoutConfirm = true },
                                modifier = Modifier.testTag("topbar_logout_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = PaiEmerald900,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    if (!isExpanded && bottomItems.size >= 2) {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            bottomItems.forEach { item ->
                                NavigationBarItem(
                                    selected = uiState.currentRoute == item.route,
                                    onClick = { viewModel.navigate(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.route.title) },
                                    label = {
                                        Text(
                                            text = when (item.route) {
                                                AppRoute.HOME -> "Beranda"
                                                AppRoute.REGISTER -> "Daftar"
                                                AppRoute.PESERTA -> "Peserta"
                                                AppRoute.NO_URUT -> "No Tampil"
                                                AppRoute.LEADERBOARD -> "Klasemen"
                                                AppRoute.PENILAIAN -> "Nilai"
                                                AppRoute.REKAP_JURI -> "Rekap"
                                                else -> item.route.title
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    IslamicGeometricBackdrop()

                    Column(modifier = Modifier.fillMaxSize()) {
                        MarqueeBanner(
                            runningText = uiState.runningText,
                            kecamatan = uiState.namaKecamatan,
                            isAdmin = isAdmin,
                            onEditRunningText = { showEditRunningText = true },
                            onEditKecamatan = { showEditKecamatan = true }
                        )

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (uiState.currentRoute) {
                                AppRoute.HOME -> HomeScreen(
                                    uiState = uiState,
                                    onNavigate = { viewModel.navigate(it) }
                                )
                                AppRoute.REGISTER -> RegisterScreen(
                                    uiState = uiState,
                                    onSubmit = { names, sek, lomba, onSuccess ->
                                        viewModel.submitPendaftaran(names, sek, lomba, onSuccess)
                                    },
                                    onShowMessage = onShowMessage
                                )
                                AppRoute.PESERTA -> PesertaScreen(
                                    uiState = uiState,
                                    onUpdatePeserta = { id, no, nama, sek ->
                                        viewModel.updatePesertaInfo(id, no, nama, sek)
                                    },
                                    onDeletePeserta = { id -> viewModel.deletePeserta(id) }
                                )
                                AppRoute.NO_URUT -> NoUrutScreen(
                                    uiState = uiState,
                                    onSimpanSemua = { map -> viewModel.simpanNoUrutSekolah(map) }
                                )
                                AppRoute.CETAK_EXCEL -> CetakExcelScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onShowMessage = onShowMessage
                                )
                                AppRoute.CETAK_JUARA -> CetakJuaraScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onShowMessage = onShowMessage
                                )
                                AppRoute.LEADERBOARD -> LeaderboardScreen(
                                    uiState = uiState,
                                    onRefresh = { viewModel.refreshFromServer(true) }
                                )
                                AppRoute.PENILAIAN -> PenilaianScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onShowError = onShowMessage
                                )
                                AppRoute.REKAP_JURI -> RekapNilaiScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    isAdminMode = false
                                )
                                AppRoute.REKAP_ADMIN -> RekapNilaiScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    isAdminMode = true
                                )
                                AppRoute.PENGATURAN_PENILAIAN -> PengaturanPenilaianScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onShowError = onShowMessage
                                )
                                AppRoute.AKSES -> SettingsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onShowMessage = onShowMessage
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isExpanded) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(drawerContainerColor = Slate900) {
                        drawerContent()
                    }
                },
                content = mainScaffold
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = Slate900) {
                        drawerContent()
                    }
                },
                content = mainScaffold
            )
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Akhiri Sesi?") },
            text = { Text("Anda akan keluar dari akun ${session.displayName}.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaiEmerald600)
                ) { Text("Ya, Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Batal") }
            }
        )
    }

    if (showEditRunningText) {
        var textInput by remember { mutableStateOf(uiState.runningText) }
        AlertDialog(
            onDismissRequest = { showEditRunningText = false },
            title = { Text("Edit Running Text (Info Pentas)") },
            text = {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Pengumuman Berjalan") },
                    minLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveRunningText(textInput)
                    showEditRunningText = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditRunningText = false }) { Text("Batal") }
            }
        )
    }

    if (showEditKecamatan) {
        var kecInput by remember { mutableStateOf(uiState.namaKecamatan) }
        AlertDialog(
            onDismissRequest = { showEditKecamatan = false },
            title = { Text("Ubah Nama Kecamatan") },
            text = {
                OutlinedTextField(
                    value = kecInput,
                    onValueChange = { kecInput = it },
                    label = { Text("Nama Kecamatan") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateNamaKecamatan(kecInput)
                    showEditKecamatan = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditKecamatan = false }) { Text("Batal") }
            }
        )
    }
}
