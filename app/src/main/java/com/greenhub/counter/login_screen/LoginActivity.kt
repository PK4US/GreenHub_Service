package com.greenhub.counter.login_screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.greenhub.counter.R
import com.greenhub.counter.backend.AmazonRegistration
import com.greenhub.counter.backend.HTTPClient
import com.greenhub.counter.backend.NetworkChangeReceiver


class LoginActivity : AppCompatActivity(), View.OnTouchListener, LoginProvider.ViewProvider {

    private lateinit var mNetworkChangeReceiver: NetworkChangeReceiver


    private lateinit var btnLogin: ImageButton
    private var etLogin: EditText? = null
    private var etPassword: EditText? = null
    private lateinit var btnShowPassword: ImageButton
    private lateinit var mPresenter: LoginPresenter
    private lateinit var mSharedPreference: SharedPreferences

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)

        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        btnShowPassword =  findViewById(R.id.btnLoginShowPassword)
        btnShowPassword.setOnTouchListener(this)

        mSharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)

        if(mSharedPreference.contains("login")){
            etLogin!!.setText(mSharedPreference.getString("login", ""))
        }
        etPassword!!.setText("");

        mPresenter = LoginPresenter(this, this)


        btnLogin = findViewById(R.id.btnLogin)
//        btnLogin.setOnClickListener { mPresenter.startLogin(etLogin!!.text.toString(), etPassword!!.text.toString())}


        val FIRST_SETTING_ACTIVITY = "FIRST_SETTING_ACTIVITY"
        btnLogin.setOnClickListener {
            (HapticFeedbackConstants.VIRTUAL_KEY)


            val intent = Intent(FIRST_SETTING_ACTIVITY)
            startActivity(intent)
            finish()}



        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        mNetworkChangeReceiver = NetworkChangeReceiver()
        registerReceiver(mNetworkChangeReceiver, filter)

        HTTPClient.getInstance().setLoginActivityListener(object :
            HTTPClient.LoginActivityListener {

            override fun onRcChecked(code: Int?, synced: Boolean?, id: String?, sec_key: Int?) {

                val edit = mSharedPreference.edit()
                edit.putString("login", etLogin!!.text.toString())
                edit.putString("token", AmazonRegistration.getInstance().getToken())
                edit.putLong("lastEnterTime", System.currentTimeMillis())
                edit.putString(
                    "refreshToken",
                    AmazonRegistration.getInstance().mCognitoRefreshToken
                )
                Log.i(
                    "Amazon",
                    "Refresh token saved: ${AmazonRegistration.getInstance().mCognitoRefreshToken}"
                )
                edit.apply()

            }

            override fun onAmazonUserLogin(id: String?, name: String?, email: String?) {
                AmazonRegistration.getInstance().userName = name!!
                AmazonRegistration.getInstance().userEmail = email!!
                HTTPClient.getInstance().rcCheck()
                Log.v("HTTPClient resp", "$name $email")
            }

            override fun onError(error_code: Int?, error_message: String?) {
                //showError(error_message!!)
                Log.i("TAG", "HTTP error $error_message")
                if (mSharedPreference.contains("lastEnterTime")) {
                    var lastLogintTime = mSharedPreference.getLong("lastEnterTime", 0)
                    if (lastLogintTime == 0L || System.currentTimeMillis() > (lastLogintTime + (1000 * 60 * 5))) return
                    else {
                        loginSuccess()
                    }
                }
            }
        })

        AmazonRegistration.getInstance().setLoginActivityListener(object :
            AmazonRegistration.LoginActivityListener {
            override fun onRegistrationResponse(state: Boolean?, message: String?) {
                showMessage(message!!)
            }

            override fun onLoginResponse(state: Boolean?, message: String?) {
                if (state!!)
                    HTTPClient.getInstance().rcCheck()
                else
                    showError(message!!)
            }

            override fun onNewPasswordResponse(state: Boolean?, message: String?) {
                TODO("Not yet implemented")
            }

            override fun onForgotPasswordResponse(destination: String?) {
                TODO("Not yet implemented")
            }

            override fun onUnconfirmResponse() {
                TODO("Not yet implemented")
            }

            override fun onError(error: String?) {
                Log.i("TAG", "Error $error")
                if (error != null) {
                    showError(error)
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if(v!!.id == btnShowPassword.id){
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> etPassword!!.inputType = InputType.TYPE_CLASS_TEXT
                MotionEvent.ACTION_UP -> etPassword!!.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword!!.setSelection(etPassword!!.text.length)
            return true
        }
        return false
    }

    override fun showError(errorMessage: String) {
        runOnUiThread { Toast.makeText(
            this, AmazonRegistration.getInstance().getLocaleMessageFromAmazon(
                errorMessage
            ), Toast.LENGTH_LONG
        ).show() }
    }

    override fun showMessage(message: String) {
        runOnUiThread { Toast.makeText(
            this, "${
                AmazonRegistration.getInstance().getLocaleMessageFromAmazon(
                    message
                )
            }", Toast.LENGTH_LONG
        ).show() }
    }

    override fun loginSuccess() {

        var e = mSharedPreference.edit()
        e.putLong("lastEnterTime", System.currentTimeMillis())
        e.putString("token", AmazonRegistration.getInstance().getToken())
        e.putString("refreshToken", AmazonRegistration.getInstance().mCognitoRefreshToken)
        e.apply()
        Log.i(
            "Amazon",
            "Refresh token saved: ${AmazonRegistration.getInstance().mCognitoRefreshToken}"
        )
        //finish()
        //supportFragmentManager.beginTransaction().add(R.id.fragment_container, !!, "Login").commit().beginTransaction().add(R.id.fragment_container, PageManager(), "Pages").commit()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data = intent.data
        if (data != null) {
            val mainPart = data.toString().split("#").toTypedArray()[1]
            val arguments = mainPart.split("&").toTypedArray()
            val argument = arguments[0]
            val token = argument.split("=").toTypedArray()[1]
            AmazonRegistration.getInstance().setToken(token)
            HTTPClient.getInstance().getAmazonUser(token)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(mNetworkChangeReceiver)
        }catch (e: Exception){
        }catch (e: Exception){
        }
    }
}