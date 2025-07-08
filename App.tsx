/**
 * NativePayment Module Example
 * Demonstrates usage of the NativePayment Turbo Module
 */

import React, { useState, useEffect } from 'react';
import {
  StatusBar,
  StyleSheet,
  useColorScheme,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  Alert,
  SafeAreaView,
} from 'react-native';
import NativePayment from './specs/NativePayment';

function App() {
  const isDarkMode = useColorScheme() === 'dark';
  const [isInitialized, setIsInitialized] = useState(false);
  const [isDeviceSupported, setIsDeviceSupported] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  // Add event listeners for payment events
  useEffect(() => {
    // Note: Event listeners will be implemented when the native module is properly set up
    // For now, we'll handle events through the promise responses
    addLog('Event listeners will be set up when native module is ready');
  }, []);

  const addLog = (message: string) => {
    setLogs(prev => [...prev, `${new Date().toLocaleTimeString()}: ${message}`]);
  };

  const initializeModule = async () => {
    try {
      setIsProcessing(true);
      addLog('Initializing NativePayment module...');
      
      const config = {
        authToken: 'your_auth_token_here',
        storeId: 'your_store_id_here',
        apiUrl: 'https://your-api-url.com',
      };

      const result = await NativePayment.initialize(config);
      addLog(`Initialization result: ${JSON.stringify(result)}`);
      
      if (result.status === 'success') {
        setIsInitialized(true);
        Alert.alert('Success', 'NativePayment module initialized successfully!');
      } else {
        Alert.alert('Error', `Initialization failed: ${result.message}`);
      }
    } catch (error) {
      addLog(`Initialization error: ${error}`);
      Alert.alert('Error', `Failed to initialize: ${error}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const checkDeviceSupport = async () => {
    try {
      setIsProcessing(true);
      addLog('Checking device support...');
      
      const result = await NativePayment.isDeviceSupported();
      addLog(`Device support result: ${JSON.stringify(result)}`);
      
      setIsDeviceSupported(result.isSupported);
      
      if (result.isSupported) {
        Alert.alert('Device Supported', `This device supports NativePayment. SDK Version: ${result.sdkVersion}`);
      } else {
        Alert.alert('Device Not Supported', 'This device does not support NativePayment.');
      }
    } catch (error) {
      addLog(`Device support check error: ${error}`);
      Alert.alert('Error', `Failed to check device support: ${error}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const startPayment = async () => {
    try {
      setIsProcessing(true);
      addLog('Starting payment...');
      
      const paymentRequest = {
        nexoRequestBody: JSON.stringify({
          SaleToPOIRequest: {
            MessageHeader: {
              ProtocolVersion: "3.0",
              MessageClass: "Service",
              MessageCategory: "Payment",
              MessageType: "Request",
              ServiceID: "123456789",
              SaleID: "TurboProjectApp",
              POIID: "POI_001"
            },
            PaymentRequest: {
              SaleData: {
                SaleTransactionID: {
                  TransactionID: "TXN_001",
                  TimeStamp: new Date().toISOString()
                }
              },
              PaymentTransaction: {
                AmountsReq: {
                  Currency: "USD",
                  RequestedAmount: 10.00
                }
              }
            }
          }
        })
      };

      const result = await NativePayment.startPayment(paymentRequest);
      addLog(`Payment started: ${JSON.stringify(result)}`);
      
      if (result.status === 'initiated') {
        Alert.alert('Payment Initiated', `Transaction ID: ${result.transactionId}`);
      } else {
        Alert.alert('Error', `Payment failed to start: ${result.status}`);
        setIsProcessing(false);
      }
    } catch (error) {
      addLog(`Payment error: ${error}`);
      Alert.alert('Error', `Failed to start payment: ${error}`);
      setIsProcessing(false);
    }
  };

  const cancelPayment = async () => {
    try {
      setIsProcessing(true);
      addLog('Cancelling payment...');
      
      const result = await NativePayment.cancelPayment();
      addLog(`Payment cancellation result: ${JSON.stringify(result)}`);
      
      setIsProcessing(false);
      Alert.alert('Payment Cancelled', result.message || 'Payment was cancelled successfully');
    } catch (error) {
      addLog(`Payment cancellation error: ${error}`);
      Alert.alert('Error', `Failed to cancel payment: ${error}`);
      setIsProcessing(false);
    }
  };

  const closeSession = async () => {
    try {
      setIsProcessing(true);
      addLog('Closing session...');
      
      const result = await NativePayment.closeSession();
      addLog(`Session close result: ${JSON.stringify(result)}`);
      
      setIsInitialized(false);
      Alert.alert('Session Closed', result.message || 'Session closed successfully');
    } catch (error) {
      addLog(`Session close error: ${error}`);
      Alert.alert('Error', `Failed to close session: ${error}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const clearLogs = () => {
    setLogs([]);
  };

  const Button = ({ title, onPress, disabled, style }: any) => (
    <TouchableOpacity
      style={[
        styles.button,
        disabled && styles.buttonDisabled,
        style
      ]}
      onPress={onPress}
      disabled={disabled || isProcessing}
    >
      <Text style={[styles.buttonText, disabled && styles.buttonTextDisabled]}>
        {title}
      </Text>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: isDarkMode ? '#000' : '#fff' }]}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.content}>
        <Text style={[styles.title, { color: isDarkMode ? '#fff' : '#000' }]}>
          NativePayment Module Demo
        </Text>

        <View style={styles.statusContainer}>
          <Text style={[styles.statusText, { color: isDarkMode ? '#fff' : '#000' }]}>
            Status: {isInitialized ? '✅ Initialized' : '❌ Not Initialized'}
          </Text>
          <Text style={[styles.statusText, { color: isDarkMode ? '#fff' : '#000' }]}>
            Device: {isDeviceSupported ? '✅ Supported' : '❓ Unknown'}
          </Text>
          {isProcessing && (
            <Text style={[styles.statusText, styles.processingText]}>
              🔄 Processing...
            </Text>
          )}
        </View>

        <View style={styles.buttonContainer}>
          <Button
            title="Initialize Module"
            onPress={initializeModule}
            disabled={isInitialized}
            style={styles.primaryButton}
          />

          <Button
            title="Check Device Support"
            onPress={checkDeviceSupport}
            style={styles.secondaryButton}
          />

          <Button
            title="Start Payment"
            onPress={startPayment}
            disabled={!isInitialized || isProcessing}
            style={styles.successButton}
          />

          <Button
            title="Cancel Payment"
            onPress={cancelPayment}
            disabled={!isProcessing}
            style={styles.warningButton}
          />

          <Button
            title="Close Session"
            onPress={closeSession}
            disabled={!isInitialized}
            style={styles.dangerButton}
          />

          <Button
            title="Clear Logs"
            onPress={clearLogs}
            style={styles.infoButton}
          />
        </View>

        <View style={styles.logsContainer}>
          <Text style={[styles.logsTitle, { color: isDarkMode ? '#fff' : '#000' }]}>
            Event Logs ({logs.length})
          </Text>
          <ScrollView style={styles.logsScrollView}>
            {logs.map((log, index) => (
              <Text key={index} style={[styles.logEntry, { color: isDarkMode ? '#ccc' : '#666' }]}>
                {log}
              </Text>
            ))}
            {logs.length === 0 && (
              <Text style={[styles.logEntry, { color: isDarkMode ? '#ccc' : '#666' }]}>
                No logs yet. Try initializing the module or starting a payment.
              </Text>
            )}
          </ScrollView>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
  },
  statusContainer: {
    backgroundColor: '#f5f5f5',
    padding: 15,
    borderRadius: 10,
    marginBottom: 20,
  },
  statusText: {
    fontSize: 16,
    marginBottom: 5,
  },
  processingText: {
    color: '#007AFF',
    fontWeight: 'bold',
  },
  buttonContainer: {
    gap: 10,
    marginBottom: 20,
  },
  button: {
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonDisabled: {
    opacity: 0.5,
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
  buttonTextDisabled: {
    color: '#999',
  },
  primaryButton: {
    backgroundColor: '#007AFF',
  },
  secondaryButton: {
    backgroundColor: '#5856D6',
  },
  successButton: {
    backgroundColor: '#34C759',
  },
  warningButton: {
    backgroundColor: '#FF9500',
  },
  dangerButton: {
    backgroundColor: '#FF3B30',
  },
  infoButton: {
    backgroundColor: '#5AC8FA',
  },
  logsContainer: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    borderRadius: 10,
    padding: 15,
  },
  logsTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  logsScrollView: {
    flex: 1,
  },
  logEntry: {
    fontSize: 12,
    fontFamily: 'monospace',
    marginBottom: 5,
    lineHeight: 16,
  },
});

export default App;
