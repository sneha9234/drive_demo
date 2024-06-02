package com.example.drivedemo.presentation.util

import androidx.annotation.Nullable
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class DriveServiceHelper(private val mDriveService: Drive) {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()

    fun downloadFile(targetFile: java.io.File?, fileId: String?): Task<Void?> {
        return Tasks.call(mExecutor) {
            val outputStream: OutputStream = FileOutputStream(targetFile)
            mDriveService.files()[fileId].executeMediaAndDownloadTo(outputStream)
            null
        }
    }

    // TO LIST FILES
    @Throws(IOException::class)
    fun listDriveImageFiles(): List<File> {
        var result: FileList
        var pageToken: String? = null
        do {
            result = mDriveService.files()
                .list()
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .setPageToken(pageToken)
                .execute()

            pageToken = result.nextPageToken
        } while (pageToken != null)

        return result.files
    }

    // TO UPLOAD A FILE ONTO DRIVE
    fun uploadFile(
        localFile: java.io.File,
        mimeType: String?, @Nullable folderId: String?
    ): Task<GoogleDriveFileHolder?> {
        return Tasks.call(mExecutor) { // Retrieve the metadata as a File object.
            val root: List<String> = if (folderId == null) {
                listOf("root")
            } else {
                listOf(folderId)
            }

            val metadata = File()
                .setParents(root)
                .setMimeType(mimeType)
                .setName(localFile.name)

            val fileContent = FileContent(mimeType, localFile)

            val fileMeta = mDriveService.files().create(
                metadata,
                fileContent
            ).execute()
            val googleDriveFileHolder = GoogleDriveFileHolder()
            googleDriveFileHolder.id = fileMeta.id
            googleDriveFileHolder.name = fileMeta.name
            googleDriveFileHolder
        }
    }
}