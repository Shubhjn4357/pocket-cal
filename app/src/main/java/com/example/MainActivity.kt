package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.EventRepository
import com.example.ui.CalendarScreen
import com.example.ui.theme.PocketCalTheme
import com.example.util.HapticHelper
import com.example.viewmodel.CalendarViewModel
import com.example.viewmodel.CalendarViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val context = LocalContext.current
            
            // Instantiating local data persistent structures reactively
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { EventRepository(database.eventDao()) }
            val hapticHelper = remember { HapticHelper(context) }
            
            val viewModel: CalendarViewModel = viewModel(
                factory = CalendarViewModelFactory(context, repository, hapticHelper)
            )
            
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            
            PocketCalTheme(darkTheme = isDarkTheme) {
                CalendarScreen(viewModel = viewModel)
            }
        }
    }
}
