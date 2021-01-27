package com.greenhub.counter.login_screen

import android.content.Context
import android.text.TextUtils
import com.greenhub.counter.R
import com.greenhub.counter.backend.AmazonRegistration

class LoginPresenter(context: Context, viewProvider: LoginProvider.ViewProvider){

    private val mContext = context
    val mViewProvider = viewProvider

    fun startLogin(login: String, password: String){
        AmazonRegistration.getInstance().login(login, password)
    }

    fun onResume(){
        AmazonRegistration.getInstance().init(mContext)
        AmazonRegistration.getInstance().setLoginActivityListener(object: AmazonRegistration.LoginActivityListener{
            override fun onRegistrationResponse(state: Boolean?, message: String?) {
               if(state!!){ mViewProvider.showMessage("Registration success") }
               else{ mViewProvider.showError(message!!) } }

            override fun onLoginResponse(state: Boolean?, message: String?) {
                if(state!!){
                }else{ mViewProvider.showError(message!!) } }

            override fun onNewPasswordResponse(state: Boolean?, message: String?) { mViewProvider.showMessage(AmazonRegistration.getInstance().getLocaleMessageFromAmazon(message)!!) }

            override fun onForgotPasswordResponse(destination: String?) { TODO("Not yet implemented") }

            override fun onUnconfirmResponse() { TODO("Not yet implemented") }

            override fun onError(error: String?) { mViewProvider.showError(error!!) }
        })
    }
}