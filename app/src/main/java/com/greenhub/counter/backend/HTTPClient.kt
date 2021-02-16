package com.greenhub.counter.backend

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class HTTPClient(context: Context) {

    companion object{
        private var instance: HTTPClient? = null
        fun getInstance(): HTTPClient {
           return instance!! }
        fun initHttpClient(context: Context): HTTPClient {
            if(instance == null){
                instance = HTTPClient(context) }
            return instance!!
        }
    }

    var context: Context? = context
    var mClient: OkHttpClient? = null
    private var mCookieJar: ClearableCookieJar? = null
    private var mCallbackRCSync: Callback? = null
    private var mCallbackRCCheck: Callback? = null
    private var mCallbackRCDel: Callback? = null
    private var mCallbackAmazon: Callback? = null
    private var mLogoutCallbackAmazon: Callback? = null
    private var mHostUrl: String? = null
    private var mHandler: Handler? = null
    private var mLoginActivityListener: LoginActivityListener? = null

    init {
        mHandler = Handler(Looper.getMainLooper())
        mCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
        mHostUrl = "http://34.252.42.20:1029/"
        val trustAllCerts =
            arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    val cArrr = arrayOfNulls<X509Certificate>(0)
                    return cArrr
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }
            })
        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContext.getInstance("SSL")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        try {
            sslContext!!.init(null, trustAllCerts, SecureRandom())
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
        val client: OkHttpClient.Builder = OkHttpClient.Builder()
           // .callTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
        client.sslSocketFactory(sslContext!!.socketFactory)
        val hostnameVerifier =
            HostnameVerifier { hostname, session ->
                Log.d("Trust", "Trust Host:$hostname")
                true
            }
        client.hostnameVerifier(hostnameVerifier)
        mClient = client.cookieJar(mCookieJar).build()
        mCallbackRCSync = object : Callback {
            override fun onFailure(call: Call?, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                val body: String = response.body()!!.string()
                val obj: JSONObject
                when (response.code()) {
                    200 -> try {
                        obj = JSONObject(body)
                        val sec_key = obj.getInt("sec_key")
                    } catch (e: JSONException) {

                        e.printStackTrace()
                    }
                    400 -> try {
                        obj = JSONObject(body)
                        val code = obj.getInt("errorCode")
                        val message = obj.getString("errorText")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        mCallbackRCCheck = object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                mHandler!!.post {
                    if (mLoginActivityListener != null) mLoginActivityListener!!.onError(
                        400,
                        "SERVER ERROR"
                    )
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                val body: String = response.body()!!.string()
                val obj: JSONObject
                when (response.code()) {
                    200 -> try {
                        obj = JSONObject(body)
                        if (obj.getBoolean("synced")) {
                            val id = obj.getString("id")
                            val sec_key = obj.getInt("sec_key")
                            mHandler!!.post {
                                if (mLoginActivityListener != null) mLoginActivityListener!!.onRcChecked(
                                    200,
                                    true,
                                    id,
                                    sec_key
                                )
                            }
                        } else {
                            mHandler!!.post {
                                if (mLoginActivityListener != null) mLoginActivityListener!!.onRcChecked(
                                    200,
                                    false,
                                    null,
                                    null
                                )
                            }
                        }
                    } catch (e: JSONException) {
                        mHandler!!.post {
                            if (mLoginActivityListener != null) mLoginActivityListener!!.onError(
                                400,
                                "JSON ERROR"
                            )
                        }
                        e.printStackTrace()
                    }
                    401 -> try {
                        obj = JSONObject(body)
                        val code = obj.getInt("errorCode")
                        val message = obj.getString("errorText")
                        mHandler!!.post {
                            if (mLoginActivityListener != null) mLoginActivityListener!!.onError(
                                code,
                                message
                            )
                        }
                    } catch (e: JSONException) {
                        mHandler!!.post {
                            if (mLoginActivityListener != null) mLoginActivityListener!!.onError(
                                400,
                                "JSON ERROR"
                            )
                        }
                        e.printStackTrace()
                    }
                }
            }
        }

        mCallbackRCDel = object : Callback{
            override fun onFailure(call: Call?, e: IOException?) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                val body: String = response.body()!!.string()
                val obj: JSONObject
                when (response.code()) {
                    200 -> mHandler!!.post {

                    }
                    400 -> try {
                        obj = JSONObject(body)
                        val code = obj.getInt("errorCode")
                        val message = obj.getString("errorText")

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        mCallbackAmazon = object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {}
            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                val body: String = response.body()!!.string()
                val obj: JSONObject
                try {
                    obj = JSONObject(body)
                    val arr = obj.getJSONArray("UserAttributes")
                    var id: String? = null
                    var name: String? = null
                    var email: String? = null
                    for (i in 0 until arr.length()) {
                        if (arr.getJSONObject(i).getString("Name") == "name") {
                            name = arr.getJSONObject(i).getString("Value")
                        } else if (arr.getJSONObject(i).getString("Name") == "email") {
                            email = arr.getJSONObject(i).getString("Value")
                        }
                        if (arr.getJSONObject(i).getString("Name") == "identities") {
                            val v =  arr.getJSONObject(i).getString("Value").replace("\\", "")
                            val arr1 = JSONArray(v)
                            for (j in 0 until arr1.length()) {
                                val userId = arr1.getJSONObject(j)
                                id = userId[userId.names().getString(0)].toString()
                                Log.v(
                                    "JSONObject",
                                    "key = " + userId.names()
                                        .getString(0) + " value = " + userId[userId.names()
                                        .getString(
                                            0
                                        )]
                                )
                            }
                        }
                    }
                    if (id != null) mLoginActivityListener!!.onAmazonUserLogin(
                        id,
                        name,
                        email
                    ) else mLoginActivityListener!!.onError(400, "Error")
                } catch (e: JSONException) {
                    mLoginActivityListener!!.onError(400, "Error")
                    e.printStackTrace()
                }
            }
        }

        mLogoutCallbackAmazon = object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {}
            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response?) {
            }
        }
    }

    //_____________________
    @Throws(IOException::class)
    fun rcCheck() {
        val MEDIA_TYPE = MediaType.parse("application/json")
        val client = OkHttpClient()
        val postdata = JSONObject()
        val url = "http://34.252.42.20:1029/counterTypes"

        try {
            val token = AmazonRegistration.getInstance().getToken()!!
            postdata.put("acctoken", token)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val body = RequestBody.create(MEDIA_TYPE, postdata.toString())
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val mMessage = e.message.toString()
                Log.w("OTBETA_OT_CEPBEPA_HET", mMessage)
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val mMessage = response.body()!!.string()
                Log.e("OTBET_OT_CEPBEPA", mMessage)
            }
        })
        mClient!!.newCall(request).enqueue(mCallbackRCCheck)
    }
    //_____________________


//    fun rcCheck() {
//        val request: Request = Request.Builder()
//            .url(mHostUrl + "boiler/check")
//            .addHeader("acctoken", AmazonRegistration.getInstance().getToken()!!)
//            .build()
//        Log.i("TAG", AmazonRegistration.getInstance().getToken()!!)
//        mClient!!.newCall(request).enqueue(mCallbackRCCheck)
//    }

    fun getAmazonUser(accessToken: String) {
        Log.v("TSTTTT", accessToken.length.toString())
        val obj = JSONObject()
        try {
            obj.put("AccessToken", accessToken)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val JSON: MediaType = MediaType.parse("application/x-amz-json-1.1; charset=utf-8")!!
        val body: RequestBody = RequestBody.create(JSON, obj.toString())
        val request: Request = Request.Builder()
            .url("https://cognito-idp.eu-west-1.amazonaws.com/")
            .addHeader("x-amz-target", "AWSCognitoIdentityProviderService.GetUser")
            .post(body)
            .build()
        mClient!!.newCall(request).enqueue(mCallbackAmazon)
    }

    public fun refreshToken(
        clientID: String,
        refreshToken: String,
        secret: String,
        callback: AmazonOnRefreshTokenCallback
    ){
        val jArrayFacebookData = JSONObject()
        var jObjectType = JSONObject()
        try {
            jArrayFacebookData.put("AuthFlow", "REFRESH_TOKEN_AUTH")
            jObjectType.put("REFRESH_TOKEN", refreshToken)
            jObjectType.put("SECRET_HASH", secret)
            jArrayFacebookData.put("AuthParameters", jObjectType)
            jArrayFacebookData.put("ClientId", clientID)
            Log.i("Amazon", "Refresh token request = ${jArrayFacebookData.toString()}")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val JSON: MediaType = MediaType.parse("application/x-amz-json-1.1; charset=utf-8")!!
        val body: RequestBody = RequestBody.create(JSON, jArrayFacebookData.toString())
        val request: Request = Request.Builder()
            .url("https://cognito-idp.eu-west-1.amazonaws.com/")
            .addHeader("x-amz-target", "AWSCognitoIdentityProviderService.InitiateAuth")
            .post(body)
            .build()
        mClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("Amazon", "On Refresh token fail : ${e.localizedMessage}")
                callback.onError(e.localizedMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseMsg = response.body()!!.string()
                    Log.i("Amazon", "On Refresh token response : message: ${responseMsg}")
                    var obj = JSONObject(responseMsg)
                    var obj2 = obj.getJSONObject("AuthenticationResult")
                    var accesToken = obj2.getString("AccessToken")
                    Log.i("Amazon", "New access token = ${accesToken}")
                    callback.onSuccessRefresh(accesToken)
                } catch (exception: Exception) {
                    callback.onError(exception.localizedMessage)
                    exception.printStackTrace()
                }
            }
        })
    }

    fun isAmazonTokenValid(accessToken: String, amazonTokenValidCallback: AmazonTokenValidCallback): Boolean{
        val obj = JSONObject()
        try {
            obj.put("AccessToken", accessToken)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val JSON: MediaType = MediaType.parse("application/x-amz-json-1.1; charset=utf-8")!!
        val body: RequestBody = RequestBody.create(JSON, obj.toString())
        val request: Request = Request.Builder()
            .url("https://cognito-idp.eu-west-1.amazonaws.com/")
            .addHeader("x-amz-target", "AWSCognitoIdentityProviderService.GetUser")
            .post(body)
            .build()
        mClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.i("Amazon", "Error check token: ${e!!.localizedMessage}")
            }

            override fun onResponse(call: Call?, response: Response?) {
                //if(response!!.body() != null && request.body()
                if (response!!.code() == 400) {
                    amazonTokenValidCallback.responseValidationToken(false)
                } else {
                    amazonTokenValidCallback.responseValidationToken(true)
                }
            }
        })
        return false
    }

    fun logoutAmazonUser(userId: String) {
        val request: Request = Request.Builder()
            .url("https://cognito-idp.eu-west-1.amazonaws.com/logout?client_id=$userId")
            .build()
        mClient!!.newCall(request).enqueue(mLogoutCallbackAmazon)
    }

    fun setLoginActivityListener(loginActivityListener: LoginActivityListener?) {
        mLoginActivityListener = loginActivityListener
    }

    interface LoginActivityListener {
        fun onRcChecked(
            code: Int?,
            synced: Boolean?,
            id: String?,
            sec_key: Int?
        )

        fun onAmazonUserLogin(
            id: String?,
            name: String?,
            email: String?
        )

        fun onError(error_code: Int?, error_message: String?)
    }

    interface AmazonTokenValidCallback{
        fun responseValidationToken(isValid: Boolean)
    }

    interface AmazonOnRefreshTokenCallback{
        fun onSuccessRefresh(newToken: String)
        fun onError(errorMsg: String)
    }
}