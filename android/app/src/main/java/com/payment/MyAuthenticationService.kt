package com.payment

import android.util.Log

/**
 * Mocked authentication service for NativePayment module.
 * Provides authentication state management without making real API calls.
 */
class MyAuthenticationService {
    
    private var isAuthenticated = false
    private var currentToken: String? = null
    
    /**
     * Mock authentication - always succeeds with demo token
     */
    fun authenticate(setupToken: String): Boolean {
        try {
            Log.d("NativePayment", "Mock authentication with setup token: ${setupToken.take(10)}...")
            
            // Check if context is initialized
            if (!NativePaymentContext.isInitialized()) {
                Log.e("NativePayment", "Context not initialized")
                return false
            }
            
            // Mock successful authentication
            currentToken = "DEMO_AUTH_${System.currentTimeMillis()}"
            isAuthenticated = true
            
            Log.d("NativePayment", "Mock authentication successful")
            return true
            
        } catch (e: Exception) {
            Log.e("NativePayment", "Mock authentication failed", e)
            isAuthenticated = false
            currentToken = null
            return false
        }
    }
    
    /**
     * Check if currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return isAuthenticated && currentToken != null
    }
    
    /**
     * Get current authentication token
     */
    fun getCurrentToken(): String? {
        return if (isAuthenticated) currentToken else null
    }
    
    /**
     * Logout and clear authentication state
     */
    fun logout() {
        Log.d("NativePayment", "Logging out")
        isAuthenticated = false
        currentToken = null
    }
    
    /**
     * Check if the payment service is ready (authenticated and context initialized)
     */
    fun isReady(): Boolean {
        return isAuthenticated() && NativePaymentContext.isInitialized()
    }
    
    /**
     * Validate authentication for payment operations
     */
    fun validateForPayment(): Boolean {
        return isAuthenticated() && isReady()
    }
    
    /**
     * Get mock SDK data for demo purposes
     */
    fun getMockSdkData(): String {
        return "DEMO_SDK_DATA_${System.currentTimeMillis()}"
    }
}
