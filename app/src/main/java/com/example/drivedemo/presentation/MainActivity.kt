package com.example.drivedemo.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.drivedemo.presentation.home.HomeViewModel
import com.example.drivedemo.presentation.login.LoginEvent
import com.example.drivedemo.presentation.util.DriveServiceHelper
import com.example.drivedemo.presentation.util.Screen
import com.example.drivedemo.presentation.util.SetupNavigation
import com.example.drivedemo.ui.DriveTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient

    private var file: com.google.api.services.drive.model.File ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition {
            homeViewModel.isLoading.value
        }

        initObservers()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
            .requestScopes(
                Scope(DriveScopes.DRIVE_FILE)
            ).build()
        gsc = GoogleSignIn.getClient(this, gso)

        signIn()

        setContent {
            DriveTheme {
                val loginStatus by homeViewModel.loginState
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SetupNavigation(
                        startDestination = Screen.HomeScreen.route,
                        loggedInStatus = loginStatus
                    )
                }
            }
        }
    }

    private fun initObservers() {
        homeViewModel.uploadFileLiveData.observe(this) {
            uploadFile()
        }

        homeViewModel.login.observe(this){
            signIn()
        }

        homeViewModel.downloadFileLiveData.observe(this){
            file = it
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                downloadFile(this, it)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            file?.let { downloadFile(this, it) }
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()

        }
    }

    private fun uploadFile() {
            val i = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            uploadLauncher.launch(i)
    }

    private fun signIn() {
        val signInIntent = gsc.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var uploadLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val selectedImage: Uri? = result.data?.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                    val cursor = selectedImage?.let {
                        contentResolver.query(
                            it,
                            filePathColumn, null, null, null
                        )
                    }
                    cursor?.moveToFirst()

                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    val picturePath = columnIndex?.let { cursor.getString(it) }
                    cursor?.close()
                    uploadImageIntoDrive(BitmapFactory.decodeFile(picturePath))
                }

                else -> {
                    Toast.makeText(this, "Did not select any image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun uploadImageIntoDrive(decodeFile: Bitmap) {
        try {
            val file = File(applicationContext.filesDir, "demo${(0..999999).random()}")
            val bos = ByteArrayOutputStream()
            decodeFile.compress(Bitmap.CompressFormat.PNG, 0,  /*ignored for PNG*/bos)
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()

            val mAccount = GoogleSignIn.getLastSignedInAccount(this)

            val credential =
                GoogleAccountCredential.usingOAuth2(
                    this, setOf(Scopes.DRIVE_FILE)
                )
            credential.setSelectedAccount(mAccount?.account)
            val googleDriveService =
                Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                )
                    .setApplicationName("Drive Demo")
                    .build()

            DriveServiceHelper(googleDriveService).uploadFile(file, "image/jpeg", null)
                .addOnSuccessListener { googleDriveFileHolder ->
                    Log.i(
                        "TAG help",
                        "Successfully Uploaded. File Id :" + googleDriveFileHolder?.id
                    )
                }
                .addOnFailureListener { e ->
                    Log.i(
                        "TAG help",
                        "Failed to Upload. File Id :" + e.message
                    )
                }
        } catch (e: Exception) {
            Log.i("TAG help", "Exception : " + e.message)
        }
    }


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        task.getResult(ApiException::class.java)
                        homeViewModel.onEvent(LoginEvent.PerformLogin(true))
                    } catch (e: ApiException) {
                        Toast.makeText(this, "Something went wrong ($e)", Toast.LENGTH_SHORT).show()
                    }
                }

                Activity.RESULT_CANCELED -> {
                    homeViewModel.onEvent(LoginEvent.PerformLogin(false))
                }
            }
        }
}

fun downloadFile(context: Context, file: com.google.api.services.drive.model.File) {
    val mAccount = GoogleSignIn.getLastSignedInAccount(context)

    val credential =
        GoogleAccountCredential.usingOAuth2(
            context, setOf(Scopes.DRIVE_FILE)
        )
    credential.setSelectedAccount(mAccount?.account)
    val googleDriveService =
        Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Drive Demo")
            .build()
    val targetDirectory = Environment.getExternalStorageDirectory()
    val targetFile = File(targetDirectory, file.name)
    DriveServiceHelper(googleDriveService).downloadFile(targetFile, file.id)
        .addOnSuccessListener {
            Log.i("TAG test", "Downloaded the file")
            val fileSize = targetFile.length() / 1024
            Log.i("TAG test", "file Size :$fileSize")
            Log.i("TAG test", "file Path :" + targetFile.absolutePath)
        }
        .addOnFailureListener { e ->
            Log.i(
                "TAG test",
                "Failed to Download the file, Exception :" + e.message
            )
        }
}


