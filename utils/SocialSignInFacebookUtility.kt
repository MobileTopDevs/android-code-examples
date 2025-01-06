package com.example.hashtex.util

import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SocialSignInFacebookUtility {

    suspend fun getAccessToken(fragment: Fragment): String {
        val loginResult = getLoginResult(fragment)
        return loginResult.accessToken.token
    }

    private suspend fun getLoginResult(fragment: Fragment) = suspendCoroutine { continuation ->
        val callbackManager = CallbackManager.Factory.create()
        val loginManager = LoginManager.getInstance().apply {
            registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onCancel() {
                        continuation.resumeWithException(Throwable("onCancel"))
                    }

                    override fun onError(error: FacebookException) {
                        continuation.resumeWithException(error)
                    }

                    override fun onSuccess(result: LoginResult) {
                        continuation.resume(result)
                    }
                }
            )
        }
        loginManager.logInWithReadPermissions(
            fragment,
            callbackManager,
            listOf("email")
        )
    }
}