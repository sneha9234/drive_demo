package com.example.drivedemo.presentation.home

import com.google.api.services.drive.model.File

data class HomeScreenState(
    val filesList: List<File>? = null
)