package com.greenhub.counter.backend

import android.content.Context
import android.util.Log
import android.util.Patterns
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult

class AmazonRegistration {

    private val userPoolId = "eu-west-1_SsxZZgrwO" // "eu-west-1_A86u5aXda" //"eu-west-1_oWPA6GUaJ"; // eu-west-1_oWPA6GUaJ     //eu-west-1_SsxZZgrwO
    public val clientId = "58af695u7h38l9m4k4q0pcauk9" //"2n9rj8t2vc4up2oc0lchpndmac"; // 2n9rj8t2vc4up2oc0lchpndmac     //58af695u7h38l9m4k4q0pcauk9
    public val clientSecret = ""  //id9qh1kfvlh7023f99ck0b3sr5gh7an48rn6rm7e1ohi1qjrnri

    private val cognitoRegion: Regions = Regions.EU_WEST_1
    private var userPool: CognitoUserPool? = null
    private var mCognitoUser: CognitoUser? = null
    private var mPassword = ""
    public var mContext: Context? = null
    var userType = LoginTypes.Email
    var userName = ""
    var userEmail = ""
    var userPhone = ""
    private var mSession: CognitoUserSession? = null
    private var mToken: String? = null
    private var mLogin: String? = null
    private var mAuthenticationDetails: AuthenticationDetails? = null
    private var mRegistrationCallback: SignUpHandler? = null
    private var mConfirmingCallback: GenericHandler? = null
    private var mAuthenticationHandler: AuthenticationHandler? = null
    var userToken: String? = null
    public var mCognitoRefreshToken: String? = null
    private var mLoginActivityListener: LoginActivityListener? = null
    private var mMainActivityListener: MainActivityListener? = null
    // This will cause confirmation to fail if the user attribute has been verified for another user in the same pool
    var forcedAliasCreation = false

    companion object{
        private var instance: AmazonRegistration? = null

        fun getInstance(): AmazonRegistration {
            if(instance == null){
                instance = AmazonRegistration()
            }
            return instance!!
        }
    }

    fun init(context: Context?) {
        mContext = context
        userPool = CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion)
        userPool!!.setPersistenceEnabled(false)
        mRegistrationCallback = object : SignUpHandler {
            override fun onSuccess(
                user: CognitoUser?,
                signUpResult: SignUpResult?
            ) { // Sign-up was successful
                mCognitoUser = user
                mLoginActivityListener!!.onUnconfirmResponse()
            }

            override fun onFailure(exception: Exception) { //  if (exception.hashCode() != null) // ex is the Exception
                mLoginActivityListener!!.onRegistrationResponse(
                    false,
                    exception.message
                )
            }
        }
        // Call back handler for confirmSignUp API
        mConfirmingCallback = object : GenericHandler {
            override fun onSuccess() { // User was successfully confirmed
                mLoginActivityListener!!.onRegistrationResponse(
                    true,
                    getLocaleMessageFromAmazon("User was successfully confirmed")
                )
            }

            override fun onFailure(exception: Exception?) { // User confirmation failed. Check exception for the cause.
            }
        }
        // Callback handler for the sign-in process
        mAuthenticationHandler = object : AuthenticationHandler {
            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                mSession = userSession
                mToken = mSession!!.idToken.jwtToken
                userToken = mSession!!.accessToken.jwtToken
                mCognitoRefreshToken = mSession!!.refreshToken.token;
                mLoginActivityListener!!.onLoginResponse(true, "Success!")
                // refreshToken();
                userType = LoginTypes.Email
                userDetails
            }

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, userId: String?) { // The API needs user sign-in credentials to continue
                mAuthenticationDetails =
                    AuthenticationDetails(userId, mPassword, null)
                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(mAuthenticationDetails)
                // Allow the sign-in to continue
                authenticationContinuation.continueTask()
            }

            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {}
            override fun authenticationChallenge(continuation: ChallengeContinuation?) {}
            override fun onFailure(exception: Exception) {
                if (exception.message!!.indexOf(".") != -1) mLoginActivityListener!!.onLoginResponse(
                    false,
                    exception.message!!.substring(0, exception.message!!.indexOf("."))
                ) else mLoginActivityListener!!.onLoginResponse(
                    false,
                    exception.message!!.substring(0, exception.message!!.indexOf("("))
                )
                if (exception.javaClass.name == "com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException") {
                    mLoginActivityListener!!.onUnconfirmResponse()
                    mCognitoUser =
                        userPool!!.getUser(mLogin)
                    mCognitoUser!!.resendConfirmationCodeInBackground(object :
                        VerificationHandler {
                        override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails?) {}
                        override fun onFailure(exception: Exception?) {}
                    })
                }
            }
        }
    }

    fun confirmSignUp(confirmationCode: String?) { // Call API to confirm this user
        mCognitoUser!!.confirmSignUpInBackground(
            confirmationCode,
            forcedAliasCreation,
            mConfirmingCallback
        )
    }

    private fun refreshToken() {
        mCognitoUser = userPool!!.getUser(mLogin)
        mCognitoUser!!.getSessionInBackground(mAuthenticationHandler)
    }

    fun login(userID: String?, password: String) {
        mCognitoUser = userPool!!.getUser(userID)
        mLogin = userID
        mPassword = password
        mCognitoUser!!.getSessionInBackground(mAuthenticationHandler)
    }

    fun resendSecureCode() {
        mCognitoUser!!.resendConfirmationCodeInBackground(object :
            VerificationHandler {
            override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails?) {
                Log.i("Amazon", "On resended security password success")
            }
            override fun onFailure(exception: Exception?) {
                Log.i("Amazon", "On resended security password fail")
            }
        })
    }

    private val userDetails: Unit
        private get() {
            mCognitoUser!!.getDetailsInBackground(object : GetDetailsHandler {
                override fun onSuccess(cognitoUserDetails: CognitoUserDetails) {
                    val user: Map<String, String> =
                        cognitoUserDetails.getAttributes().getAttributes()
                    for ((key, value) in user) {
                        Log.d("key", "key: $key")
                        Log.d("key", "value: $value")
                        if (key == "name") {
                            userName = value
                        } else if (key == "email") {
                            userEmail = value
                        } else if (key == "phone") {
                            userPhone = value
                        }
                    }
                }

                override fun onFailure(exception: Exception?) {}
            })
        }

    fun logOut() {
       /* if(mCognitoUser != null){
            mCognitoUser!!.signOut()
            userToken = ""
            mCognitoUser!!.globalSignOutInBackground(object : GenericHandler{
                override fun onSuccess() {}
                override fun onFailure(exception: Exception?) {}
            })
        }*/

    }

    fun changeUsername(username: String?) {
        val userAttributes = CognitoUserAttributes()
        userAttributes.addAttribute("name", username)
        mCognitoUser!!.updateAttributesInBackground(
            userAttributes,
            object : UpdateAttributesHandler {
                override fun onSuccess(attributesVerificationList: List<CognitoUserCodeDeliveryDetails?>?) {
                    mCognitoUser!!.getDetailsInBackground(object :
                        GetDetailsHandler{
                        override fun onSuccess(cognitoUserDetails: CognitoUserDetails) {
                            val user: Map<String, String> =
                                cognitoUserDetails.getAttributes().getAttributes()
                            for ((key, value) in user) {
                                Log.d("key", "key: $key")
                                Log.d("key", "value: $value")
                                if (key == "name") {
                                    userName = value
                                } else if (key == "email") {
                                    userEmail = value
                                } else if (key == "phone") {
                                    userPhone = value
                                }
                            }
                            mMainActivityListener!!.onUserNameChanged(
                                true,
                                "User name changed successfully"
                            )
                        }

                        override fun onFailure(exception: Exception?) {}
                    })
                }

                override fun onFailure(exception: Exception?) {
                    mMainActivityListener!!.onUserNameChanged(
                        true,
                        "User name changed error"
                    )

                }
            })
    }

    fun changePassword(oldPassword: String?, newPassword: String?) {
        mCognitoUser!!.changePasswordInBackground(
            oldPassword,
            newPassword,
            object : GenericHandler {
                override fun onSuccess() {
                    mMainActivityListener!!.onChangePasswordResponse(
                        true,
                        "Password successfully changed"
                    )
                }

                override fun onFailure(exception: Exception) {
                    mMainActivityListener!!.onChangePasswordResponse(
                        false,
                        exception.message!!.substring(0, exception.message!!.indexOf("."))
                    )
                }
            })
    }

    fun setNewPassword(verificationCode: String?, newPassword: String?) {
        mCognitoUser!!.confirmPasswordInBackground(
            verificationCode,
            newPassword,
            object : ForgotPasswordHandler{
                override fun onSuccess() {
                    mLoginActivityListener!!.onNewPasswordResponse(true, "Success")
                }

                override fun getResetCode(continuation: ForgotPasswordContinuation?) {
                    mLoginActivityListener!!.onNewPasswordResponse(true, "Success")
                }

                override fun onFailure(exception: Exception?) {
                    mLoginActivityListener!!.onNewPasswordResponse(
                        false,
                        "Failure"
                    )
                }
            })
    }


    fun setLoginActivityListener(loginActivityListener: LoginActivityListener?) {
        mLoginActivityListener = loginActivityListener
    }

    private fun login(userID: String) {
        mCognitoUser = userPool!!.getUser(userID)
        mCognitoUser!!.getSessionInBackground(mAuthenticationHandler)
    }

    enum class LoginTypes {
        Email
    }

    interface LoginActivityListener {
        fun onRegistrationResponse(
            state: Boolean?,
            message: String?
        )

        fun onLoginResponse(state: Boolean?, message: String?)
        fun onNewPasswordResponse(state: Boolean?, message: String?)
        fun onForgotPasswordResponse(destination: String?)
        fun onUnconfirmResponse()
        fun onError(error: String?)
    }

    interface MainActivityListener {
        fun onChangePasswordResponse(
            state: Boolean?,
            message: String?
        )

        fun onUserNameChanged(state: Boolean?, message: String?)
    }


    fun getLocaleMessageFromAmazon(
        message: String?
    ): String? {
        if (null == message) return ""
        if (message.contains("Attempt limit exceeded, please try after some time")) {
            return ("Перевишено кількість спроб")
        } else if (message.contains("Incorrect username or password"))
            return ("Невірний логін або пароль")
        else if (message.contains("User was successfully confirmed"))
            return ("Користувач підтверджений")
        else if (message.contains("Failture"))
            return ("Помилка")
        else if (message.contains("Success"))
            return ("Успiшно")
        return message
    }

    fun isValidMail(email: String?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidMobile(phone: String?): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

    fun getToken(): String? {
        val t = instance!!.userToken
        return t
    }

    fun setToken(token: String) {
        instance!!.userToken = token
    }

}