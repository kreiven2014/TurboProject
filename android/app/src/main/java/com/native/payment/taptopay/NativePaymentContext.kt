package com.native.payment.taptopay

/**
 * Shared context for Native Payment configuration.
 * Stores authentication token and store ID that are shared between
 * NativePaymentModule and MyAuthenticationService.
 */
object NativePaymentContext {
    private var authToken: String? = null
    private var storeId: String? = null
    private var apiUrl: String? = null
    private var isInitialized: Boolean = false

    /**
     * Initialize the context with auth token, store ID, API key, and API URL
     */
    fun initialize(authToken: String, storeId: String, apiUrl: String) {
        this.authToken = authToken
        this.storeId = storeId
        this.apiUrl = apiUrl
        this.isInitialized = true
    }

    /**
     * Get the auth token, throws exception if not initialized
     */
    fun getAuthToken(): String {
        if (!isInitialized || authToken == null) {
            throw IllegalStateException("NativePaymentContext not initialized. Call initialize() with authToken, storeId, and apiUrl first.")
        }
        return authToken!!
    }

    /**
     * Get the store ID, throws exception if not initialized
     */
    fun getStoreId(): String {
        if (!isInitialized || storeId == null) {
            throw IllegalStateException("NativePaymentContext not initialized. Call initialize() with authToken, storeId, and apiUrl first.")
        }
        return storeId!!
    }

    /**
     * Get the API URL, throws exception if not initialized
     */
    fun getApiUrl(): String {
        if (!isInitialized || apiUrl == null) {
            throw IllegalStateException("NativePaymentContext not initialized. Call initialize() with authToken, storeId, and apiUrl first.")
        }
        return apiUrl!!
    }

    /**
     * Check if context is initialized
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Clear the context (useful for testing or logout)
     */
    fun clear() {
        authToken = null
        storeId = null
        apiUrl = null
        isInitialized = false
    }
}
