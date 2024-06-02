package com.example.drivedemo.data.repository

import com.example.drivedemo.domain.repository.HomeRepository
import com.example.drivedemo.presentation.util.DriveServiceHelper
import com.google.api.services.drive.model.File

class HomeRepositoryImpl() : HomeRepository {
    override suspend fun getFilesList(driveServiceHelper: DriveServiceHelper): Results<List<File>> {
        return Results.Success(driveServiceHelper.listDriveImageFiles())
    }
}