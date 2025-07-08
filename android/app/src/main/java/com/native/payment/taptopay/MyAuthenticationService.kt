package com.native.payment.taptopay

import android.util.Log
import com.adyen.ipp.api.authentication.AuthenticationProvider
import com.adyen.ipp.api.authentication.AuthenticationResponse
import com.adyen.ipp.api.authentication.MerchantAuthenticationService
import java.io.IOException
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class MyAuthenticationService : MerchantAuthenticationService() {

    /**
     *  ------------
     * | IMPORTANT |
     *  ------------
     *
     * This part of the code sends the `setupToken` to authenticate you and your app with Native Payment.
     *
     * In this example, for simplicity and ease of use, we are using okhttp to connect directly to the payment provider.
     * This is NOT how your app should be implemented! Your credentials and API Key should be kept secret and safe
     * within your servers, and should only be used for direct server to server communication with the payment provider.
     *
     * In a production environment you should send the `setupToken` to your server and forward the authentication
     * request from there to the payment provider server, and then return the `sdkData` result here.
     *
     * More information on the Docs page.
     * https://docs.adyen.com/point-of-sale/ipp-mobile/card-reader-android/integration-reader#session
     */

    // API key and API URL will be retrieved from shared context (configured via .env)

    // You can also declare this implementation somewhere else and pass it using your Dependency Injection system.
    override val authenticationProvider: AuthenticationProvider
        get() = object : AuthenticationProvider {
            override suspend fun authenticate(setupToken: String): Result<AuthenticationResponse> {
                // Check if context is initialized before proceeding
                if (!NativePaymentContext.isInitialized()) {
                    return Result.failure(Throwable("Not initialized from react native app"))
                }
                val client = OkHttpClient()
                // Get authToken, storeId, apiKey, and apiUrl from shared context
                val authToken = NativePaymentContext.getAuthToken()
                val storeId = NativePaymentContext.getStoreId()
                val apiUrl = "${NativePaymentContext.getApiUrl()}/api/v1/payment/${storeId}/init-pay-with-tap"

                val jsonObject = JSONObject().apply {
                    put("token", setupToken)
                }
                val mediaType = "application/json".toMediaType()
                val requestBody = jsonObject.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer $authToken")
                    .post(requestBody)
                    .build()

                return suspendCancellableCoroutine { continuation ->
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            continuation.resume(Result.failure(Throwable(e)))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful && response.body != null) {
                                val json = JSONObject(response.body!!.string())
                                continuation.resume(
                                    Result.success(
                                        AuthenticationResponse.create(
                                            json.optString("sdkData")
                                        )
                                    )
                                )
                            } else {
                                continuation.resume(Result.failure(Throwable("error")))
                            }
                        }
                    })
                }
            }
        }
}
