package com.example.drivedemo.presentation.home

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drivedemo.R
import com.example.drivedemo.presentation.components.FileItemCard
import com.example.drivedemo.presentation.util.DriveServiceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive

@Composable
fun HomeScreen(
    scrollState: LazyListState,
    loggedInStatus: Boolean? = false,
    viewModel: HomeViewModel = viewModel(LocalContext.current as ComponentActivity)
) {
    val context = LocalContext.current

    val homeScreenState by viewModel.homeScreenState

    if (loggedInStatus == true) {
        CheckForGooglePermissions(
            context
        ) {
            DriveSetUp(context) {
                LaunchedEffect(key1 = "key") {
                    viewModel.getFileList(it)
                }
                ShowList(homeScreenState, scrollState, viewModel)
            }
        }
    } else {
        LoginUI(viewModel)
    }
}

@Composable
fun LoginUI(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_to_continue),
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.width(200.dp),
            onClick = {
                viewModel.login()
            }
        ) {
            Text(
                text = stringResource(R.string.login),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun NoFileUI(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_files_uploaded),
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.width(200.dp),
            onClick = {
                viewModel.uploadFile()
            }
        ) {
            Text(
                text = stringResource(R.string.upload),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ShowList(
    homeScreenState: HomeScreenState,
    scrollState: LazyListState,
    viewModel: HomeViewModel
) {
    if (homeScreenState.filesList?.isNotEmpty() == true) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier =
                Modifier
                    .fillMaxWidth(),
                state = scrollState,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                items(homeScreenState.filesList.size) {
                    FileItemCard(
                        file = homeScreenState.filesList[it],
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .clickable {
                                viewModel.downloadFile(homeScreenState.filesList[it])
                            }
                    )
                }
            }
            FloatingActionButton(
                backgroundColor = Color(0xFFF7FAFF),
                onClick = {
                    viewModel.uploadFile()
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(start = 15.dp, top = 15.dp, end = 8.dp, bottom = 15.dp)
                    .background(Color.Transparent)
            )
            {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "fab")
            }
        }
    } else if (homeScreenState.filesList?.isEmpty() == true) {
        NoFileUI(viewModel)
    }
}

@Composable
private fun CheckForGooglePermissions(context: Context, onSuccess: @Composable () -> Unit) {
    val accessDriveScope = Scope(Scopes.DRIVE_FILE)
    val scopeEmail = Scope(Scopes.EMAIL)

    if (!GoogleSignIn.hasPermissions(
            GoogleSignIn.getLastSignedInAccount(context),
            accessDriveScope,
            scopeEmail
        )
    ) {
        GoogleSignIn.requestPermissions(
            context as Activity,
            1,
            GoogleSignIn.getLastSignedInAccount(context),
            accessDriveScope,
            scopeEmail
        )
    } else {
        onSuccess()
    }
}

@Composable
private fun DriveSetUp(context: Context, onComplete: @Composable (DriveServiceHelper) -> Unit) {
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
    onComplete(DriveServiceHelper(googleDriveService))
}
