package com.example.drivedemo.presentation.home

import com.google.api.services.drive.model.File

sealed class HomeScreenEvent {
    data class SelectFile(val file: File, val onClick: () -> Unit) :
        HomeScreenEvent()
}