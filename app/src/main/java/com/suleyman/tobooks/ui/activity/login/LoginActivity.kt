package com.suleyman.tobooks.ui.activity.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.PhoneAuthProvider
import com.suleyman.tobooks.R
import com.suleyman.tobooks.databinding.ActivityLoginBinding
import com.suleyman.tobooks.utils.Common.showAlertDialog
import com.suleyman.tobooks.utils.Utils
import com.suleyman.tobooks.databinding.ActivitySmsCodeBinding
import com.suleyman.tobooks.databinding.EnterEmailViewBinding
import com.suleyman.tobooks.utils.textString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var utils: Utils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.includeLoginToolbar.toolbar)

        with (binding) {
            btnSignIn.setOnClickListener(this@LoginActivity)
            btnSignUp.setOnClickListener(this@LoginActivity)
            tvForgotPassword.setOnClickListener(this@LoginActivity)
            etLogin.addTextChangedListener(this@LoginActivity)
        }

        lifecycleScope.launchWhenStarted {
            loginViewModel.loginUiState.collect {
                when (it) {
                    is LoginViewModel.LoginUiState.Success -> {
                        utils.toast(it.message)
                    }
                    is LoginViewModel.LoginUiState.Error -> {
                        utils.toast(it.message)
                    }
                    is LoginViewModel.LoginUiState.Loading -> {
                        binding.pbLoadingLogin.isVisible = it.isLoading
                    }
                    is LoginViewModel.LoginUiState.VerifyNumber -> {
                        val viewSmsCode = ActivitySmsCodeBinding.inflate(layoutInflater)
                        val verificationId = it.verificationId
                        showAlertDialog(
                            this@LoginActivity,
                            title = getString(R.string.confirm_number),
                            message = getString(R.string.enter_the_code_from_sms),
                            view = viewSmsCode.root
                        ).setPositiveButton(
                            getString(R.string.confirm)
                        ) { _, _ ->

                            val credential = PhoneAuthProvider.getCredential(
                                verificationId,
                                viewSmsCode.etSmsCode.textString()
                            )

                            loginViewModel.signWithPhoneCredentials(
                                credential
                            )

                        }.show()
                    }
                    else -> LoginViewModel.LoginUiState.Empty
                }
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        s?.let { number ->
            val isPhoneSign =
                !(number.isNotEmpty() && (number.isDigitsOnly() || number.startsWith("+")))
            binding.tInLayoutPassword.isVisible = isPhoneSign
            binding.btnSignUp.isVisible = isPhoneSign
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSignIn -> {
                loginViewModel.signIn(
                    binding.etLogin.textString(),
                    binding.etPassword.textString(),
                    this@LoginActivity
                )
            }
            R.id.btnSignUp -> {
                loginViewModel.signUp(
                    binding.etLogin.textString(),
                    binding.etPassword.textString()
                )
            }

            R.id.tvForgotPassword -> {
                val enterEmailView = EnterEmailViewBinding.inflate(layoutInflater)
                showAlertDialog(
                    context = this@LoginActivity,
                    title = getString(R.string.reset_password),
                    message = getString(R.string.enter_the_email),
                    view = enterEmailView.root
                ).setPositiveButton(getString(R.string.send)) { _, _ ->
                    val email = enterEmailView.tInLayoutEmail.editText!!.textString()
                    if (email.isNotEmpty()) {
                        loginViewModel.resetPassword(email)
                    }
                }.show()
            }
        }
    }
}

