package com.payment

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Arguments
import com.payment.NativePaymentSpec
import java.util.UUID

class NativePaymentModule(reactContext: ReactApplicationContext) : NativePaymentSpec(reactContext) {

    private val reactContext: ReactApplicationContext = reactContext
    private var isProcessing = false

    init {
        instance = this
    }

    override fun getName() = NAME

    override fun initialize(config: ReadableMap, promise: Promise) {
        try {
            Log.d("NativePayment", "Initializing module with config: ${config.toString()}")
            
            // Get the configuration values
            val authToken = config.getString("authToken") ?: throw Error("authToken is required")
            val storeId = config.getString("storeId") ?: throw Error("storeId is required")
            val apiUrl = config.getString("apiUrl") ?: throw Error("apiUrl is required")

            // Initialize the shared context
            NativePaymentContext.initialize(authToken, storeId, apiUrl)

            // Mock successful initialization
            val response = Arguments.createMap().apply {
                putString("status", "success")
                putString("message", "Module initialized successfully")
                putString("installationId", "DEMO_INSTALL_${UUID.randomUUID().toString().substring(0, 8)}")
            }

            Log.d("NativePayment", "Module initialized successfully")
            promise.resolve(response)
            
        } catch (e: Exception) {
            Log.e("NativePayment", "Initialization failed", e)
            promise.reject("INITIALIZATION_ERROR", e.message, e)
        }
    }

    override fun startPayment(request: ReadableMap, promise: Promise) {
        try {
            if (isProcessing) {
                promise.reject("PAYMENT_IN_PROGRESS", "Another payment is already in progress")
                return
            }

            val nexoRequestBody = request.getString("nexoRequestBody") ?: ""
            Log.d("NativePayment", "Starting payment with request: ${request.toString()}")
            Log.d("NativePayment", "NEXO Request Body: $nexoRequestBody")

            // Emit processing status
            emitPaymentStatus("processing", "Payment initiated")

            // Simulate payment processing
            isProcessing = true
            val transactionId = "DEMO_TXN_${UUID.randomUUID().toString().substring(0, 8)}"
            
            val response = Arguments.createMap().apply {
                putString("status", "initiated")
                putString("transactionId", transactionId)
                putDouble("amount", 25.00)
                putString("currency", "USD")
            }

            Log.d("NativePayment", "Payment initiated successfully: $transactionId")
            promise.resolve(response)

            // Simulate payment completion after delay
            Thread {
                try {
                    Thread.sleep(3000) // Simulate processing time
                    
                    val successEvent = Arguments.createMap().apply {
                        putString("status", "success")
                        putString("transactionId", transactionId)
                        putDouble("amount", 25.00)
                        putString("currency", "USD")
                        putString("reference", "DEMO_REF_${UUID.randomUUID().toString().substring(0, 6)}")
                    }
                    
                    emitOnPaymentSuccess(successEvent)
                    
                    Log.d("NativePayment", "Payment completed successfully")
                } catch (e: Exception) {
                    Log.e("NativePayment", "Payment simulation failed", e)
                } finally {
                    isProcessing = false
                }
            }.start()

        } catch (e: Exception) {
            Log.e("NativePayment", "Payment failed", e)
            isProcessing = false
            
            val failureEvent = Arguments.createMap().apply {
                putString("status", "failed")
                putString("error", e.message ?: "Unknown error")
                putDouble("amount", 25.00)
                putString("currency", "USD")
                putString("reference", "DEMO_REF_${UUID.randomUUID().toString().substring(0, 6)}")
            }
            
            emitOnPaymentFailure(failureEvent)
            promise.reject("PAYMENT_ERROR", e.message, e)
        }
    }

    override fun cancelPayment(promise: Promise) {
        try {
            Log.d("NativePayment", "Cancelling payment")
            
            if (!isProcessing) {
                promise.reject("NO_PAYMENT_IN_PROGRESS", "No payment is currently in progress")
                return
            }

            // Emit cancellation status
            emitPaymentStatus("cancelled", "Payment cancelled")

            // Simulate cancellation delay
            Thread.sleep(1000)
            
            isProcessing = false
            
            val response = Arguments.createMap().apply {
                putString("status", "cancelled")
                putString("message", "Payment cancelled successfully")
            }

            Log.d("NativePayment", "Payment cancelled successfully")
            promise.resolve(response)
            
        } catch (e: Exception) {
            Log.e("NativePayment", "Payment cancellation failed", e)
            promise.reject("CANCEL_ERROR", e.message, e)
        }
    }

    override fun isDeviceSupported(promise: Promise) {
        try {
            Log.d("NativePayment", "Checking device support")
            
            // Simulate device check delay
            Thread.sleep(500)
            
            val response = Arguments.createMap().apply {
                putBoolean("isSupported", true)
                putString("sdkVersion", "1.0.0")
            }

            Log.d("NativePayment", "Device support check completed: supported=true")
            promise.resolve(response)
            
        } catch (e: Exception) {
            Log.e("NativePayment", "Device support check failed", e)
            promise.reject("DEVICE_CHECK_ERROR", e.message, e)
        }
    }

    override fun closeSession(promise: Promise) {
        try {
            Log.d("NativePayment", "Closing session")
            
            // Simulate session closing delay
            Thread.sleep(1000)
            
            // Reset processing state
            isProcessing = false
            
            val response = Arguments.createMap().apply {
                putString("status", "closed")
                putString("message", "Session closed successfully")
            }

            Log.d("NativePayment", "Session closed successfully")
            promise.resolve(response)
            
        } catch (e: Exception) {
            Log.e("NativePayment", "Session close failed", e)
            promise.reject("CLOSE_SESSION_ERROR", e.message, e)
        }
    }

    // Event emission methods
    fun handlePaymentSuccess(transactionId: String, message: String) {
        emitPaymentSuccess(transactionId, message)
    }

    fun handlePaymentFailure(error: String) {
        emitPaymentFailure(error)
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
    }
}
