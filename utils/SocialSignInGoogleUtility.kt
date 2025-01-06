package com.example.hashtex.util

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.example.hashtex.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import timber.log.Timber

object SocialSignInGoogleUtility {

    fun getSignInIntent(activity: Activity): Intent {
        val clientId = "ADD_ID"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestServerAuthCode(clientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso).apply {
            signOut()
        }.signInIntent
    }

    fun handleGoogleSignInActivityResult(
        activityResult: ActivityResult,
        onSignIn: (authCode: String, idToken: String) -> Unit,
        onError: () -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val authCode = account.serverAuthCode
            val idToken = account.idToken
            if (authCode == null || idToken == null) {
                onError()
            } else {
                onSignIn(authCode, idToken)
            }
        } catch (e: ApiException) {
            Timber.e(e, "handleSignInResult:error")
            onError()
        }
    }
}