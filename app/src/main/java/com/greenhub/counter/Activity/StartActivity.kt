package com.greenhub.counter.Activity

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.greenhub.counter.R
import com.greenhub.counter.login_screen.LoginActivity
import com.greenhub.counter.backend.*
import com.greenhub.counter.backend.AmazonRegistration
import com.greenhub.counter.backend.HTTPClient
import com.greenhub.counter.backend.NetworkChangeReceiver
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class StartActivity : AppCompatActivity(), HTTPClient.LoginActivityListener {
    private var mToken: String? = null
    private lateinit var mSharedPreference: SharedPreferences
    private var fragment_container: FrameLayout? = null
    private var timerToAskExit: Timer? = null
    private lateinit var mNetworkChangeReceiver: NetworkChangeReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment_container = findViewById(R.id.fragment_container)


        val res = applicationContext.resources
        val displayMetrics = res.displayMetrics
        val conf: android.content.res.Configuration = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(Locale(Locale.UK.displayLanguage))
            res.updateConfiguration(conf, displayMetrics)
        }

        HTTPClient.initHttpClient(applicationContext)
        AmazonRegistration.getInstance().init(this)
        HTTPClient.getInstance().setLoginActivityListener(this)

        Handler().postDelayed({
            connectBroadcast()
            val filter = IntentFilter()
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            mNetworkChangeReceiver = NetworkChangeReceiver()
            registerReceiver(mNetworkChangeReceiver, filter)
        }, 1000)
    }

    fun startChecks(){
        mSharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        if (mSharedPreference.contains("token")) {
            AmazonRegistration.getInstance()
                .setToken(mSharedPreference.getString("token", "")!!)
            if (AmazonRegistration.getInstance().getToken() != null && AmazonRegistration.getInstance().getToken()!!.isNotEmpty()) {
                val token: String = mSharedPreference.getString("token", "")!!
                HTTPClient.getInstance()
                    .isAmazonTokenValid(token, object : HTTPClient.AmazonTokenValidCallback {
                        override fun responseValidationToken(isValid: Boolean) {
                            Log.i("Amazon", "Token $token is valid? ${isValid}")
                            if (isValid) {
                                mToken = token
                                showMain()
                                //HTTPClient.getInstance().getAmazonUser(token)
                            } else refreshToken()
                        }
                    })

            }
        } else {
            val intent = Intent(this@StartActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(mNetworkChangeReceiver)
        }catch (e: Exception){
        }
    }

    fun connectBroadcast(){
        val intentFilter = IntentFilter(NetworkChangeReceiver.NETWORK_AVAILABLE_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val isNetworkAvailable = intent!!.getBooleanExtra(NetworkChangeReceiver.IS_NETWORK_AVAILABLE, false)
                Log.i("Network","Network state is $isNetworkAvailable")
                if(isNetworkAvailable){
                    startChecks()
                    noInternerPopup.visibility = View.INVISIBLE
                }else{
                    noInternerPopup.visibility = View.VISIBLE
                }
            }
        }, intentFilter)
    }

    fun refreshToken() {
        if (mSharedPreference.contains("refreshToken") && mSharedPreference.getString(
                "refreshToken",
                ""
            )!!.isNotEmpty()
        ) {
            HTTPClient.getInstance().refreshToken(
                AmazonRegistration.getInstance().clientId,
                mSharedPreference.getString("refreshToken", "")!!,
                AmazonRegistration.getInstance().clientSecret,
                object : HTTPClient.AmazonOnRefreshTokenCallback{
                    override fun onSuccessRefresh(newToken: String) {
                        Log.i("Amazon", "Token refreshed success")
                        AmazonRegistration.getInstance().setToken(newToken)
                        HTTPClient.getInstance()
                            .isAmazonTokenValid(
                                AmazonRegistration.getInstance().getToken()!!,
                                object : HTTPClient.AmazonTokenValidCallback {
                                    override fun responseValidationToken(isValid: Boolean) {
                                        if (isValid) {
                                            mToken = newToken
                                            val edit = mSharedPreference.edit()
                                            edit.putString("token", newToken)
                                            edit.apply()
                                            showMain()
                                            Log.i("Amazon", "Refreshed access token is valid")
                                            //HTTPClient.getInstance().getAmazonUser(token)
                                        } else {
                                            Log.i("Amazon", "Refreshed access token is NOT valid")
                                            val intent = Intent(this@StartActivity, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }

                                })
                    }

                    override fun onError(errorMsg: String) {
                        Log.i("Amazon", "Refresh token error")
                        val intent = Intent(this@StartActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            )

        }else{
            Log.i("Amazon", "Access token is not valid and dont have refresh token")
            val intent = Intent(this@StartActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onRcChecked(code: Int?, synced: Boolean?, id: String?, sec_key: Int?) {
        mToken = AmazonRegistration.getInstance().userToken!!
        showMain()
    }

    override fun onAmazonUserLogin(id: String?, name: String?, email: String?) {
        mToken = AmazonRegistration.getInstance().userToken!!
        showMain()
    }

    override fun onError(error_code: Int?, error_message: String?) {
        Log.i("Amazon", "OnError: $error_message with code: $error_code" )
        val intent = Intent(this@StartActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun showMain(){
        val intent = Intent(this@StartActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if(timerToAskExit != null){
            timerToAskExit!!.cancel()
            timerToAskExit = null
            finish()
        }
        if(timerToAskExit == null){
            Toast.makeText(this, "Натисніть ще раз щоб вийти з програми", Toast.LENGTH_SHORT).show()
            timerToAskExit = Timer()
            timerToAskExit!!.schedule(object: TimerTask(){
                override fun run() {
                    timerToAskExit = null
                }
            }, 3000)
        }

    }
}