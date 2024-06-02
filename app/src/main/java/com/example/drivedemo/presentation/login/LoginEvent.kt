package com.example.drivedemo.presentation.login

sealed class LoginEvent {
    data class PerformLogin(val value: Boolean) :
        LoginEvent()
}