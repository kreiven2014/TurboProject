#import "RCTNativePayment.h"
#import "TurboProject-Swift.h" // assuming this includes AdyenTapToPayImplementation

@implementation NativePayment {
  NativePaymentImplementation *implementation;
}

RCT_EXPORT_MODULE()

- (instancetype)init {
  if (self = [super init]) {
    implementation = [[NativePaymentImplementation alloc] init];
  }
  return self;
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativePaymentSpecJSI>(params);
}

#pragma mark - TurboModule Spec Methods

- (void)initialize:(JS::NativePayment::AdyenConfig &)config
           resolve:(RCTPromiseResolveBlock)resolve
            reject:(RCTPromiseRejectBlock)reject {
  @try {
    NSString *authToken = config.authToken();
    NSString *apiUrl = config.apiUrl();
    NSString *storeId = config.storeId();

    NSDictionary *configDict = @{
      @"authToken": authToken ?: @"",
      @"apiUrl": apiUrl ?: @"",
      @"storeId": storeId ?: @""
    };

    [implementation initialize:configDict resolve:resolve reject:reject];
  }
  @catch (NSException *exception) {
    reject(@"initialize_error", exception.reason, nil);
  }
}

- (void)startPayment:(JS::NativePayment::PaymentRequest &)request
             resolve:(RCTPromiseResolveBlock)resolve
              reject:(RCTPromiseRejectBlock)reject {
  @try {
    NSString *nexoRequestBody = request.nexoRequestBody();

    NSDictionary *requestDict = @{
      @"nexoRequestBody": nexoRequestBody ?: @""
    };

    [implementation startPayment:requestDict resolve:resolve reject:reject];
  }
  @catch (NSException *exception) {
    reject(@"start_payment_error", exception.reason, nil);
  }
}

- (void)cancelPayment:(RCTPromiseResolveBlock)resolve
               reject:(RCTPromiseRejectBlock)reject {
  [implementation cancelPayment:resolve reject:reject];
}

- (void)isDeviceSupported:(RCTPromiseResolveBlock)resolve
                   reject:(RCTPromiseRejectBlock)reject {
  [implementation isDeviceSupported:resolve reject:reject];
}

- (void)closeSession:(RCTPromiseResolveBlock)resolve
              reject:(RCTPromiseRejectBlock)reject {
  [implementation closeSession:resolve reject:reject];
}

@end
