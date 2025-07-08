import Foundation
import React

struct SessionsResponse: Decodable {
  let sdkData: String
}


@objcMembers
class NativePaymentImplementation: NSObject {
  private var setupToken: String?
  private var authToken: String?
  private var storeId: String?
  private var apiUrl: String?
  
  
  func initialize(_ config: NSDictionary,
                  resolve: @escaping RCTPromiseResolveBlock,
                  reject: @escaping RCTPromiseRejectBlock) {
    // Extract configuration parameters
    guard let authToken = config["authToken"] as? String,
          let storeId = config["storeId"] as? String,
          let apiUrl = config["apiUrl"] as? String else {
      reject("invalid_config", "Missing required configuration: authToken, storeId, or apiUrl", nil)
      return
    }
    
    // Store configuration for later use
    self.authToken = authToken
    self.storeId = storeId
    self.apiUrl = apiUrl
    
    
    
    print("✅ INITIALIZED: ")
    
    Task {
      do {
        
        
        resolve([
          "status": "success",
          "message": "Module initialized",
          "installationId": "123"
        ])
        
      } catch {
        print("❌ Initialization failed: \(error.localizedDescription)")
        
        // Return error response with installationId for consistency
        resolve([
          "status": "error",
          "message": "Module is not initialized",
          "installationId": "UNKNOWN"
        ])
      }
    }
  }
  
  
  func startPayment(_ request: NSDictionary,
                    resolve: @escaping RCTPromiseResolveBlock,
                    reject: @escaping RCTPromiseRejectBlock) {
    print("🟡 startPayment called with request: \(request)")
    
    
    
    
    Task {
      do {
        
        resolve([
          "status": "success",
          "message": "Module initialized",
        ])
        
      } catch {
        print("❌ startPayment failed with error: \(error.localizedDescription)")
        
        // Handle errors with more detailed logging
        print("🔍 Error type: \(type(of: error))")
        print("🔍 Error domain: \(error as NSError).domain")
        print("🔍 Error code: \(error as NSError).code")
        
        reject("payment_failed", "Payment failed: \(error.localizedDescription)", error)
      }
    }
  }
  
  func cancelPayment(_ resolve: @escaping RCTPromiseResolveBlock,
                     reject: @escaping RCTPromiseRejectBlock) {
    resolve([
      "status": "success",
      "message": "Payment cancelled (mock)"
    ])
  }
  
  func isDeviceSupported(_ resolve: @escaping RCTPromiseResolveBlock,
                         reject: @escaping RCTPromiseRejectBlock) {
    resolve([
      "isSupported": true,
      "sdkVersion": "mock_version"
    ])
  }
  
  func closeSession(_ resolve: @escaping RCTPromiseResolveBlock,
                    reject: @escaping RCTPromiseRejectBlock) {
    print("🟡 closeSession called")
    
    
    Task {
      do {
        print("🟡 Closing payment session...")
        // Add your session closing logic here
        // For example: try await paymentService.closeSession()
        
        print("✅ Session closed successfully")
        resolve([
          "status": "success",
          "message": "Session closed successfully"
        ])
      } catch {
        print("❌ closeSession failed with error: \(error.localizedDescription)")
        reject("close_session_failed", "Failed to close session: \(error.localizedDescription)", error)
      }
    }
  }
  
}
