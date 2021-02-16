package com.greenhub.counter.login_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.greenhub.counter.R

class LoginScreen : Fragment(), LoginProvider.ViewProvider, View.OnTouchListener {

    private lateinit var btnLogin: ImageButton
    private var etLogin: EditText? = null
    private var etPassword: EditText? = null
    private lateinit var btnShowPassword: ImageButton

    var mPresenter: LoginPresenter? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        etLogin = view.findViewById(R.id.etLogin)
        etPassword = view.findViewById(R.id.etPassword)
        btnShowPassword = view.findViewById(R.id.btnLoginShowPassword)
        btnShowPassword.setOnTouchListener(this)

        mPresenter = LoginPresenter(context!!, this)

        btnLogin = view.findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener { mPresenter!!.startLogin(etLogin!!.text.toString(), etPassword!!.text.toString())}

        return view
    }

    override fun onResume() {
        super.onResume()
        mPresenter!!.onResume()
    }

    override fun showError(errorMessage: String) {
        Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
    }

    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun loginSuccess() {
        TODO("Not yet implemented")
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
       if(v!!.id == btnShowPassword!!.id){
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
}