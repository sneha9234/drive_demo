package com.example.drivedemo.presentation.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivedemo.data.repository.Results
import com.example.drivedemo.domain.repository.HomeRepository
import com.example.drivedemo.presentation.login.LoginEvent
import com.example.drivedemo.presentation.util.DriveServiceHelper
import com.example.drivedemo.presentation.util.SingleLiveData
import com.google.api.services.drive.model.File
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
    ) : ViewModel() {

    private val _homeScreenState = mutableStateOf(
        HomeScreenState()
    )
    val homeScreenState: State<HomeScreenState> = _homeScreenState

    private var _uploadFileLiveData = SingleLiveData<Boolean>()
    val uploadFileLiveData : LiveData<Boolean> get() = _uploadFileLiveData

    private var _downloadFileLiveData = SingleLiveData<File>()
    val downloadFileLiveData : LiveData<File> get() = _downloadFileLiveData

    private var _login = SingleLiveData<Boolean>()
    val login : LiveData<Boolean> get() = _login

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _loginState: MutableState<Boolean> = mutableStateOf(false)
    val loginState: State<Boolean> = _loginState

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.PerformLogin -> {
                viewModelScope.launch  {
                    _loginState.value = event.value
                }
                _isLoading.value = false
            }
        }
    }

    suspend fun getFileList(driveServiceHelper: DriveServiceHelper) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.getFilesList(driveServiceHelper)) {
                is Results.Success -> _homeScreenState.value = homeScreenState.value.copy(
                    filesList = result.data
                )
                is Results.Error -> {
                }
            }
        }
    }

    fun uploadFile() {
        _uploadFileLiveData.postValue(true)
    }

    fun downloadFile(file: File) {
        _downloadFileLiveData.postValue(file)
    }

    fun login() {
        _login.postValue(true)
    }

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.SelectFile -> {
                viewModelScope.launch {
                   // userDataRepository.setRestaurant(event.restaurant)
                    event.onClick()
                }
            }
        }
    }
}