package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainApp(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentUser,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { user ->
            if (user == null) {
                LoginScreen(viewModel = viewModel, onLoginSuccess = {
                    // Handled automatically by ViewModel currentUser flow update
                })
            } else {
                when (user.role) {
                    "SUPER_ADMIN" -> {
                        AdminDashboard(
                            admin = user,
                            viewModel = viewModel,
                            onLogout = { viewModel.logout() }
                        )
                    }
                    "TEACHER" -> {
                        TeacherDashboard(
                            teacher = user,
                            viewModel = viewModel,
                            onLogout = { viewModel.logout() }
                        )
                    }
                    "STUDENT" -> {
                        StudentDashboard(
                            student = user,
                            viewModel = viewModel,
                            onLogout = { viewModel.logout() }
                        )
                    }
                    else -> {
                        // Safe fallback logout if custom roles exist
                        viewModel.logout()
                    }
                }
            }
        }
    }
}
