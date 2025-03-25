package com.example.dbit_almaconnect3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dbit_almaconnect3.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Using Pair<Boolean, String> where the Boolean indicates success and the String is either the user's role (on success) or the error message (on failure).
class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginSuccess = MutableStateFlow<Pair<Boolean, String>?>(null)
    val loginSuccess: StateFlow<Pair<Boolean, String>?> = _loginSuccess

    private val _signUpSuccess = MutableStateFlow<Pair<Boolean, String>?>(null)
    val signUpSuccess: StateFlow<Pair<Boolean, String>?> = _signUpSuccess

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.loginUser(email, password,
                onSuccess = { returnedEmail, role ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _loginSuccess.value = Pair(true, role)
                    }
                },
                onError = { errorMessage ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _loginSuccess.value = Pair(false, errorMessage)
                    }
                }
            )
        }
    }

    fun signUp(email: String, password: String, role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.signupUser(email, password, role,
                onSuccess = { returnedEmail, returnedRole ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _signUpSuccess.value = Pair(true, returnedRole)
                    }
                },
                onError = { errorMessage ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _signUpSuccess.value = Pair(false, errorMessage)
                    }
                }
            )
        }
    }
}
