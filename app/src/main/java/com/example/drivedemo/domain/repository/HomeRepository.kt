package com.example.drivedemo.domain.repository

import com.example.drivedemo.data.repository.Results
import com.example.drivedemo.presentation.util.DriveServiceHelper
import com.google.api.services.drive.model.File

interface HomeRepository {
    suspend fun getFilesList(driveServiceHelper: DriveServiceHelper): Results<List<File>>
}
