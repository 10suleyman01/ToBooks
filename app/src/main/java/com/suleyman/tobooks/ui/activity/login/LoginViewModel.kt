package com.suleyman.tobooks.ui.activity.login

import androidx.lifecycle.ViewModel
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.ToBooksApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Empty)
    var loginUiState: StateFlow<LoginUiState> = _loginUiState

    fun signIn(login: String, password: String) {
        if (login.isNotEmpty() && password.isNotEmpty()) {
            _loginUiState.value = LoginUiState.Loading
            ToBooksApp.authInstance()
                .signInWithEmailAndPassword(login, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginUiState.value =
                            LoginUiState.Success(ToBooksApp.getString(R.string.sign_in_successful))
                    } else {
                        _loginUiState.value =
                            LoginUiState.Error(ToBooksApp.getString(R.string.sign_in_error))
                    }
                }
        }
    }

    fun signUp(login: String, password: String) {
        if (login.isNotEmpty() && password.isNotEmpty()) {
            _loginUiState.value = LoginUiState.Loading
            ToBooksApp.authInstance()
                .createUserWithEmailAndPassword(login, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginUiState.value =
                            LoginUiState.Success(ToBooksApp.getString(R.string.sign_up_successful))
                    } else {
                        _loginUiState.value =
                            LoginUiState.Error(ToBooksApp.getString(R.string.sign_up_error))
                    }
                }
        }
    }

    fun resetPassword(login: String) {
        if (login.isNotEmpty()) {
            _loginUiState.value = LoginUiState.Loading
            ToBooksApp.authInstance()
                .sendPasswordResetEmail(login)
                .addOnSuccessListener {
                    _loginUiState.value = LoginUiState.Success(ToBooksApp.getString(R.string.send_link_to_your_mail))
                }.addOnFailureListener {
                    _loginUiState.value = LoginUiState.Error(ToBooksApp.getString(R.string.enter_correct_email))
                }
        }
    }

    sealed class LoginUiState {
        data class Success(val message: String) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
        object Loading : LoginUiState()
        object Empty : LoginUiState()
    }

}