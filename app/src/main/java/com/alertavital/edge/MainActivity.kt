package com.alertavital.edge

import android.os.Build
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private val REQUIRED_PERMISSIONS: Array<String> = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val REQUEST_CODE_PERMISSIONS = 1001


    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.all { it.value }
            if (allGranted) {
                startEdgeService()
            } else {
                // Mostrar mensaje de permisos denegados
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        enableEdgeToEdge()

        setContent {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EdgeHome(
                        modifier = Modifier.padding(innerPadding),
                        onStartService = {
                            startEdgeService()
                        }
                    )
                }
        }
    }


    /*
    private fun startEdgeService() {
        if (!hasPermissions()) {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            val intent = Intent(this, EdgeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }*/


    private fun startEdgeService() {
        Log.d("MainActivity", "Intentando iniciar EdgeService") // <-- log aquí
        if (!hasPermissions()) {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            val intent = Intent(this, EdgeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }


    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }



}

@Composable
fun EdgeHome(modifier: Modifier = Modifier, onStartService: () -> Unit) {
    var status by remember { mutableStateOf("Servicio detenido") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = status, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            Log.d("MainActivity", "Botón presionado")  // <-- log aquí
            onStartService()
            status = "Servicio Edge iniciado"
        }) {
            Text("Iniciar Edge Gateway")
        }
    }
}
