package com.phoenixflex.android.network.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.getSystemService
import com.phoenixflex.android.core.errors.*
import com.phoenixflex.android.core.network.ErrorGlobalHandlerObserver
import com.phoenixflex.android.network.models.NetworkError
import com.phoenixflex.android.translations.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import retrofit2.HttpException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkErrorConverterHelper(
    private val context: Context,
    private val gson: Gson
) : ErrorGlobalHandlerObserver {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()

    private var onUnauthorizedErrorObserver: MutableSharedFlow<Throwable> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getOnGlobalErrorObserver(): SharedFlow<Throwable> {
        return onUnauthorizedErrorObserver.asSharedFlow()
    }

    private suspend fun send401Error(error: Throwable) {
        onUnauthorizedErrorObserver.emit(error)
    }

    suspend fun parseError(throwable: Throwable): Throwable {
        return if (throwable is HttpException) {
            val errorType = object : TypeToken<NetworkError>() {}.type
            try {
                val parsedError: NetworkError =
                    gson.fromJson(throwable.response()?.errorBody()?.charStream(), errorType)

                val errorMsg = parsedError.error ?: parsedError.message
                val finalError = when {
                    parsedError.errors != null -> {
                        FieldErrorException(parsedError.message, parsedError.errors)
                    }
                    errorMsg != null -> {
                        when (throwable.code()) {
                            HttpURLConnection.HTTP_FORBIDDEN -> {
                                AccessForbiddenException(errorMsg)
                            }
                            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                UnauthorizedException(errorMsg)
                            }
                            HttpURLConnection.HTTP_NOT_FOUND -> {
                                NotFoundException(errorMsg)
                            }
                            else -> {
                                Exception(errorMsg)
                            }
                        }
                    }
                    else -> {
                        throwable
                    }
                }

                if (HttpURLConnection.HTTP_UNAUTHORIZED == throwable.code()) {
                    send401Error(finalError)
                }

                finalError
            } catch (e: Throwable) {
                val message = context.getString(
                    when (throwable.code()) {
                        HttpURLConnection.HTTP_BAD_REQUEST -> R.string.error_network_default
                        HttpURLConnection.HTTP_FORBIDDEN -> R.string.error_network_forbidden
                        HttpURLConnection.HTTP_UNAUTHORIZED -> R.string.error_network_invalid_session
                        else -> R.string.error_network_default
                    }
                )
                val exception = Exception(message)
                if (throwable.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    send401Error(exception)
                }
                exception
            }
        } else if (!isNetworkConnected()) {
            NetworkException(context.getString(R.string.error_network_no_internet))
        } else {
            when (throwable) {
                is ConnectException,
                is SocketTimeoutException,
                is UnknownHostException -> Exception(context.getString(R.string.error_network_connection))
                else -> throwable
            }
        }
    }

    private fun isNetworkConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager?.activeNetwork ?: return false
            return connectivityManager.getNetworkCapabilities(nw)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
        } else {
            @Suppress("DEPRECATION")
            return connectivityManager?.activeNetworkInfo?.isConnected == true
        }
    }
}