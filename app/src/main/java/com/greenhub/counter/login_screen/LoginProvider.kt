package com.greenhub.counter.login_screen

interface LoginProvider {
    interface ViewProvider{
        fun showError(errorMessage: String)
        fun showMessage(message: String)
        fun loginSuccess()
    }
}