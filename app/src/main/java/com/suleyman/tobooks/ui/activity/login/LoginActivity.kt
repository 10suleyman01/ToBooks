package com.suleyman.tobooks.ui.activity.login

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import butterknife.ButterKnife
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.Common.showAlertDialog
import com.suleyman.tobooks.app.ToBooksApp
import com.suleyman.tobooks.databinding.ActivityLoginBinding
import com.suleyman.tobooks.databinding.EnterEmailViewBinding
import kotlinx.coroutines.flow.collect

@SuppressLint("NonConstantResourceId")
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)

        binding.btnSignIn.setOnClickListener(this)
        binding.btnSignUp.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)

        lifecycleScope.launchWhenStarted {
            loginViewModel.loginUiState.collect {
                when (it) {
                    is LoginViewModel.LoginUiState.Success -> {
                        ToBooksApp.toast(it.message)
                    }
                    is LoginViewModel.LoginUiState.Error -> {
                        ToBooksApp.toast(it.message)
                    }
                    is LoginViewModel.LoginUiState.Loading -> {
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSignIn -> {
                loginViewModel.signIn(
                    binding.etLogin.text.toString(),
                    binding.etPassword.text.toString()
                )
            }
            R.id.btnSignUp -> {
                loginViewModel.signUp(
                    binding.etLogin.text.toString(),
                    binding.etPassword.text.toString()
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
                    val email = enterEmailView.tInLayoutEmail.editText?.text.toString()
                    if (email.isNotEmpty()) {
                        loginViewModel.resetPassword(email)
                    }
                }.show()
            }
        }
    }
}