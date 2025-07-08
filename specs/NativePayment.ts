// specs/NativePaymentSpec.ts
import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type { EventEmitter } from 'react-native/Libraries/Types/CodegenTypes';

export type AdyenConfig = {
  authToken: string;
  apiUrl: string;
  storeId: string;
};

export type InitializeResponse = {
  status: string;
  message: string;
  installationId: string;
};

export type PaymentRequest = {
  nexoRequestBody: string;
};

export type PaymentResponse = {
  status: string;
  transactionId?: string;
  amount: number;
  currency: string;
};

export type CancelResponse = {
  status: string;
  message: string;
};

export type DeviceSupportResponse = {
  isSupported: boolean;
  sdkVersion: string | null;
};

export type CloseSessionResponse = {
  status: string;
  message: string;
};

export type PaymentSuccessEvent = {
  status: 'success';
  transactionId: string;
  amount: number;
  currency: string;
  reference: string;
};

export type PaymentFailureEvent = {
  status: 'failed';
  error: string;
  amount?: number;
  currency?: string;
  reference?: string;
};

export type PaymentStatusEvent = {
  status: 'processing' | 'cancelled';
  message: string;
};

// ✅ This is the crucial part:
export interface Spec extends TurboModule {
  initialize(config: AdyenConfig): Promise<InitializeResponse>;
  startPayment(request: PaymentRequest): Promise<PaymentResponse>;
  cancelPayment(): Promise<CancelResponse>;
  isDeviceSupported(): Promise<DeviceSupportResponse>;
  closeSession(): Promise<CloseSessionResponse>;

  // Event emitters for payment events
  readonly onPaymentSuccess: EventEmitter<PaymentSuccessEvent>;
  readonly onPaymentFailure: EventEmitter<PaymentFailureEvent>;
  readonly onPaymentStatus: EventEmitter<PaymentStatusEvent>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('NativePayment');
