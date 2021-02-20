package com.suleyman.tobooks.ui.activity.login

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.suleyman.tobooks.R
import com.suleyman.tobooks.utils.NetworkHelper
import com.suleyman.tobooks.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val utils: Utils,
    val networkHelper: NetworkHelper,
    val auth: FirebaseAuth,
) : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Empty)
    var loginUiState: StateFlow<LoginUiState> = _loginUiState

    init {
        auth.useAppLanguage()
    }

    fun signIn(login: String, password: String, activity: LoginActivity) {
        if (login.isNotEmpty()) {
            if (login.contains("@") && password.isNotEmpty()) {
                _loginUiState.value = LoginUiState.Loading(true)
                if (networkHelper.isNetworkConnected()) {
                    auth.signInWithEmailAndPassword(login, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                _loginUiState.value =
                                    LoginUiState.Success(utils.getString(R.string.sign_in_successful))
                            } else {
                                _loginUiState.value =
                                    LoginUiState.Error(utils.getString(R.string.sign_in_error))
                            }
                        }
                } else {
                    _loginUiState.value =
                        LoginUiState.Error(utils.getString(R.string.not_connected))
                }
            } else if (login.substring(1).isDigitsOnly()) {
                if (login.startsWith("+")) {
                    verifyNumber(login, activity)
                } else {
                    utils.toastLong(utils.getString(R.string.invalid_number_format))
                }
            }
        }


    }

    private fun callbacks() = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        }

        override fun onVerificationFailed(error: FirebaseException) {
            _loginUiState.value = LoginUiState.Loading(false)
            _loginUiState.value = LoginUiState.Error(utils.getString(R.string.more_requests_try_again_later))
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            _loginUiState.value = LoginUiState.Loading(false)
            _loginUiState.value =
                LoginUiState.VerifyNumber(
                    utils.getString(R.string.sms_code_message),
                    verificationId
                )
        }
    }

    private fun verifyNumber(login: String, activity: LoginActivity) {
        _loginUiState.value = LoginUiState.Loading(true)
        if (networkHelper.isNetworkConnected()) {
            PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(login)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks())
                    .build()
            )
        } else {
            _loginUiState.value =
                LoginUiState.Error(utils.getString(R.string.not_connected))
        }
    }

    fun signWithPhoneCredentials(credential: PhoneAuthCredential) {
        _loginUiState.value = LoginUiState.Loading(true)
        if (networkHelper.isNetworkConnected()) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        _loginUiState.value =
                            LoginUiState.Success(utils.getString(R.string.sign_in_successful))
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            _loginUiState.value =
                                LoginUiState.Error(utils.getString(R.string.sign_in_error))
                        }
                    }
                }
        } else {
            _loginUiState.value =
                LoginUiState.Error(utils.getString(R.string.not_connected))
        }
    }

    fun signUp(login: String, password: String) {
        if (login.isNotEmpty()) {
            if (login.contains("@") && password.isNotEmpty()) {
                registerWithEmail(login, password)
            }
        }
    }

    private fun registerWithEmail(login: String, password: String) {
        _loginUiState.value = LoginUiState.Loading(true)
        if (networkHelper.isNetworkConnected()) {
            auth.createUserWithEmailAndPassword(login, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginUiState.value = LoginUiState.Loading(false)
                        _loginUiState.value =
                            LoginUiState.Success(utils.getString(R.string.sign_up_successful))
                        _loginUiState.value = LoginUiState.Loading(false)
                    } else {
                        _loginUiState.value = LoginUiState.Loading(false)
                        _loginUiState.value =
                            LoginUiState.Error(utils.getString(R.string.sign_up_error))
                    }
                }
        } else {
            _loginUiState.value =
                LoginUiState.Error(utils.getString(R.string.not_connected))
        }
    }

    fun resetPassword(login: String) {
        if (login.isNotEmpty()) {
            _loginUiState.value = LoginUiState.Loading(true)
            if (networkHelper.isNetworkConnected()) {
                auth.sendPasswordResetEmail(login)
                    .addOnSuccessListener {
                        _loginUiState.value =
                            LoginUiState.Success(utils.getString(R.string.send_link_to_your_mail))
                        _loginUiState.value = LoginUiState.Loading(false)
                    }.addOnFailureListener {
                        _loginUiState.value =
                            LoginUiState.Error(utils.getString(R.string.enter_correct_email))
                        _loginUiState.value = LoginUiState.Loading(false)
                    }
            } else {
                _loginUiState.value =
                    LoginUiState.Error(utils.getString(R.string.not_connected))
            }
        }
    }

    sealed class LoginUiState {
        data class Success(val message: String) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
        data class VerifyNumber(val message: String, val verificationId: String) : LoginUiState()
        data class Loading(val isLoading: Boolean) : LoginUiState()
        object Empty : LoginUiState()
    }

}