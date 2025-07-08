package com.native.payment.taptopay

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.util.Log
import androidx.startup.AppInitializer
import com.adyen.ipp.api.InPersonPayments
import com.adyen.ipp.api.InPersonPaymentsInitializer
import com.adyen.ipp.api.diagnosis.DiagnosisRequest
import com.adyen.ipp.api.initialization.InitializationState
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Arguments
import com.native.payment.taptopay.NativePaymentSpec
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.adyen.ipp.api.payment.PaymentInterface
import com.adyen.ipp.api.payment.PaymentInterfaceType
import com.adyen.ipp.api.payment.PaymentResult
import com.adyen.ipp.api.payment.TransactionRequest
import com.adyen.ipp.api.ui.MerchantUiParameters
import com.managementnativeapp.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.single
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.Date
import java.util.Locale


class NativePaymentModule(reactContext: ReactApplicationContext) : NativePaymentSpec(reactContext) {

    private val reactContext: ReactApplicationContext = reactContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        instance = this
    }

    override fun getName() = NAME

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize(config: ReadableMap, promise: Promise) {
        try {
            // Get the configuration values directly without additional mapping:
            val authToken = config.getString("authToken") ?: throw Error("authToken is required")
            val storeId = config.getString("storeId") ?: throw Error("storeId is required")
            val apiUrl = config.getString("apiUrl") ?: throw Error("apiUrl is required")

            // Initialize the shared context with authToken, storeId, apiKey, and apiUrl
            NativePaymentContext.initialize(authToken, storeId, apiUrl)

            val result = AppInitializer.getInstance(reactContext)
                .initializeComponent(InPersonPaymentsInitializer::class.java)
            coroutineScope.launch {
                // Wait for any initialization state (success or failure)
                InPersonPayments.initialised
                    .take(1) // Take the first state change
                    .collect { state ->
                        when (state) {
                            InitializationState.SuccessfulInitialization -> {
                                InPersonPayments.warmUp()
                                val response = Arguments.createMap().apply {
                                    putString("status", "success")
                                    putString("message", "Module initialized")
                                    putString("installationId", InPersonPayments.getInstallationId().getOrNull() ?: "UNKNOWN")
                                }

                                promise.resolve(response)
                            }
                            else -> {
                                val failedResponse = Arguments.createMap().apply {
                                    putString("status", "error")
                                    putString("message", "Module is not initialized")
                                    putString("installationId", "UNKNOWN")

                                }
                                promise.resolve(failedResponse)
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            promise.reject("INITIALIZATION_ERROR", e.message, e)
        }
    }

    override fun startPayment(request: ReadableMap, promise: Promise) {
        try {
            val nexoRequestBody = request.getString("nexoRequestBody") ?: ""
            Log.d("tapp: nexoRequest", "${nexoRequestBody}")

            // Emit processing status
            emitPaymentStatus("processing", "Payment initiated")

            coroutineScope.launch {
                InPersonPayments.getPaymentInterface(PaymentInterfaceType.createTapToPayType())
                    .fold(
                        onSuccess = { t2pInterface ->
                            startPaymentOnSDK(t2pInterface, nexoRequestBody, promise)
                        },
                        onFailure = { error ->

                            // Emit failure event
                            emitPaymentFailure(
                                error = "Failed to get payment interface: ${error.message}",
                            )

                            promise.reject("PAYMENT_ERROR", "Failed to get payment interface: ${error.message}")
                        }
                    )
            }
        } catch (e: Exception) {
            // Emit failure event
            emitPaymentFailure(
                error = e.message ?: "Unknown error",
            )

            promise.reject("PAYMENT_ERROR", e.message, e)
        }
    }

    override fun cancelPayment(promise: Promise) {
        try {
            // Emit cancellation status
            emitPaymentStatus("cancelled", "Payment cancelled")

            // TODO: Implement actual payment cancellation with Native Payment SDK
            // For now, return a mock response
            val response = Arguments.createMap().apply {
                putString("status", "success")
                putString("message", "Payment cancelled (mock)")
            }

            promise.resolve(response)
        } catch (e: Exception) {
            promise.reject("CANCEL_ERROR", e.message, e)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun isDeviceSupported(promise: Promise) {
        try {
            coroutineScope.launch {
                try {
                    InPersonPayments.initialised
                        .filter { state -> state == InitializationState.SuccessfulInitialization }
                        .take(1)
                        .collect { state ->
                            if (state == InitializationState.SuccessfulInitialization) {
                                val id = InPersonPayments.getInstallationId()

                                val poiId = InPersonPayments.getInstallationId().getOrNull() ?: "UNKNOWN"

                                DiagnosisRequest.create(generateNexoDiagnosisRequest(poiId = poiId))
                                    .fold(
                                    onSuccess = { resulted ->
                                        val res = InPersonPayments.performDiagnosis(resulted).getOrNull()
                                        val initializedResponse = Arguments.createMap().apply {
                                            putBoolean("isSupported", res?.success ?: false)
                                            putString("sdkVersion", id.getOrNull())
                                        }
                                        promise.resolve(initializedResponse)
                                    },
                                    onFailure = { errored ->
                                        val notInitializedResponse = Arguments.createMap().apply {
                                            putBoolean("isSupported", false)
                                            putString("sdkVersion", id.getOrNull())
                                        }
                                        promise.resolve(notInitializedResponse)
                                    })

                            } else {
                                val notInitializedResponse = Arguments.createMap().apply {
                                    putBoolean("isSupported", false)
                                    putString("sdkVersion", "no data")
                                }
                                promise.resolve(notInitializedResponse)
                            }
                        }
                } catch (e: Exception) {
                    // Fallback to mock response if coroutine fails
                    val response = Arguments.createMap().apply {
                        putBoolean("isSupported", false)
                        putString("sdkVersion", "no data")
                    }
                    promise.resolve(response)
                }
            }


        } catch (e: Exception) {
            Log.e("tapp: ", "isDeviceSupported: Outer exception: ${e.message}", e)
            promise.reject("DEVICE_CHECK_ERROR", e.message, e)
        }
    }

    override fun closeSession(promise: Promise) {
        try {
            Log.d("NativePayment", "closeSession called")
            
            // Add your session closing logic here
            // For example: InPersonPayments.closeSession()
            
            val response = Arguments.createMap().apply {
                putString("status", "success")
                putString("message", "Session closed successfully")
            }
            
            promise.resolve(response)
        } catch (e: Exception) {
            Log.e("NativePayment", "closeSession failed", e)
            promise.reject("CLOSE_SESSION_ERROR", e.message, e)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun startPaymentOnSDK(paymentInterface: PaymentInterface<*>, nexoRequest: String, promise: Promise) {
        try {
            // Use a simpler approach for React Native Turbo Module
            InPersonPayments.performTransaction(
                context = reactContext.currentActivity as MainActivity,
                paymentInterface = paymentInterface,
                transactionRequest = TransactionRequest.create(nexoRequest).getOrThrow(),
                paymentLauncher = (reactContext.currentActivity as MainActivity).paymentLauncher,
                merchantUiParameters = MerchantUiParameters.create(
                    tapToPayUiParameters = MerchantUiParameters.TapToPayUiParameters.create(
                        animation = MerchantUiParameters.TapToPayUiParameters.TapToPayAnimationType.front(
                            MerchantUiParameters.TapToPayUiParameters.TapToPayAnimationType.Front.NfcFrontPosition.TopCenter),
                    ),
                ),
            )

            // Return immediate response - actual payment result will be handled via events
            val response = Arguments.createMap().apply {
                putString("status", "initiated")
                putString("message", "Payment transaction initiated")
                putString("transactionId", "transaction_${System.currentTimeMillis()}")
            }

            promise.resolve(response)

        } catch (e: Exception) {
            Log.e("NativePayment", "Payment failed", e)

            // Emit failure event
            emitPaymentFailure(
                error = "Failed to start payment: ${e.message}",
            )

            promise.reject("PAYMENT_ERROR", "Failed to start payment: ${e.message}")
        }
    }

    // Event emission methods called from MainActivity
    fun handlePaymentSuccess(paymentResult: PaymentResult) {
        emitPaymentSuccess(
            transactionId = "transaction_${System.currentTimeMillis()}",
            message = paymentResult.toString()
        )
    }

    fun handlePaymentFailure(error: Throwable) {
        emitPaymentFailure(
            error = error.message ?: "Payment failed",
        )
    }

    // Private helper methods for event emission
    private fun emitPaymentSuccess(transactionId: String, message: String) {
        val eventData = Arguments.createMap().apply {
            putString("status", "success")
            putString("transactionId", transactionId)
            putString("message", message)
        }
        emitOnPaymentSuccess(eventData)
    }

    private fun emitPaymentFailure(error: String) {
        val eventData = Arguments.createMap().apply {
            putString("status", "failed")
            putString("error", error)
        }
        emitOnPaymentFailure(eventData)
    }

    private fun emitPaymentStatus(status: String, message: String) {
        val eventData = Arguments.createMap().apply {
            putString("status", status)
            putString("message", message)
        }
        emitOnPaymentStatus(eventData)
    }

    companion object {
        const val NAME = "NativePayment"

        @Volatile
        private var instance: NativePaymentModule? = null

        fun getInstance(): NativePaymentModule? = instance

        private val DATE_FORMAT =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

        private fun generateNexoDiagnosisRequest(
            serviceId: String = "${(Math.random() * 1000000000).toInt()}",
            saleId: String = "AndroidSampleApp",
            poiId: String,
        ): String {

            val timeStamp = DATE_FORMAT.format(Date())
            val maxServiceIdSize = 10

            return """
                |{
                |  "SaleToPOIRequest": {
                |    "MessageHeader": {
                |      "ProtocolVersion": "3.0",
                |      "MessageClass": "Service",
                |      "MessageCategory": "Diagnosis",
                |      "MessageType": "Request",
                |      "ServiceID": "${serviceId.take(maxServiceIdSize)}",
                |      "SaleID": "$saleId",
                |      "POIID": "$poiId"
                |    },
                |    "DiagnosisRequest": {
                |      "HostDiagnosisFlag": true
                |    }
                |  }
                |}
            """.trimMargin("|")
        }


    }
}
