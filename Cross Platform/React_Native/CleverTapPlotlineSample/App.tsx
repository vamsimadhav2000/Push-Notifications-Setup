/**
 * Sample React Native App
 * CleverTap + Plotline SDK Integration Demo
 *
 * @format
 */

import React, { useEffect, useState } from 'react';
import {
  Alert,
  Platform,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  useColorScheme,
} from 'react-native';
import * as CleverTap from 'clevertap-react-native';

type SdkCard = {
  name: string;
  description: string;
  integrated: boolean;
};

const SDKS: SdkCard[] = [
  {
    name: 'CleverTap',
    description: 'User engagement, push notifications & rich media',
    integrated: true,
  },
  {
    name: 'Plotline',
    description: 'Product onboarding, surveys & user experience',
    integrated: false,
  },
];

type ActionButton = {
  title: string;
  onPress: () => void;
};

function App() {
  const isDarkMode = useColorScheme() === 'dark';
  const [cleverTapId, setCleverTapId] = useState<string | null>(null);

  useEffect(() => {
    // CleverTap listeners
    CleverTap.addListener(
      CleverTap.CleverTapPushNotificationClicked,
      (event: any) => {
        Alert.alert('Push Clicked', JSON.stringify(event));
      },
    );
    CleverTap.addListener(
      CleverTap.CleverTapInAppNotificationButtonTapped,
      (event: any) => {
        Alert.alert('InApp Button Tapped', JSON.stringify(event));
      },
    );
    CleverTap.addListener(
      CleverTap.CleverTapPushPermissionResponseReceived,
      (event: any) => {
        Alert.alert('Push Permission', JSON.stringify(event));
      },
    );

    // Fetch CleverTap ID to prove the SDK is initialized.
    CleverTap.profileGetCleverTapID((err: any, res: any) => {
      if (!err && res) {
        setCleverTapId(res);
      }
    });

    return () => {
      CleverTap.removeListener(CleverTap.CleverTapPushNotificationClicked);
      CleverTap.removeListener(CleverTap.CleverTapInAppNotificationButtonTapped);
      CleverTap.removeListener(
        CleverTap.CleverTapPushPermissionResponseReceived,
      );
    };
  }, []);

  const recordEvent = () => {
    CleverTap.recordEvent('Product Viewed', {
      'Product Name': 'CleverTap Demo',
      Category: 'React Native',
      Amount: 99.99,
    });
    Alert.alert('CleverTap', 'Event "Product Viewed" recorded');
  };

  const updateProfile = () => {
    CleverTap.onUserLogin({
      Name: 'RN Demo User',
      Identity: 'rn-demo-user-001',
      Email: 'rn.demo@example.com',
      custom1: 42,
    });
    Alert.alert('CleverTap', 'User profile updated via onUserLogin');
  };

  const promptPushPrimer = () => {
    CleverTap.promptPushPrimer({
      inAppType: 'half-interstitial',
      titleText: 'Get Notified',
      messageText:
        'Please enable notifications on your device to receive offers and updates.',
      followDeviceOrientation: true,
      positiveBtnText: 'Allow',
      negativeBtnText: 'Cancel',
      fallbackToSettings: true,
    });
  };

  const registerForPush = () => {
    CleverTap.registerForPush();
    Alert.alert('CleverTap', 'registerForPush() called');
  };

  const createChannel = () => {
    CleverTap.createNotificationChannel(
      'CtRNS',
      'CleverTap React Native',
      'CleverTap React Native notifications',
      5,
      true,
    );
    Alert.alert('CleverTap', 'Notification channel created');
  };

  const showInbox = () => {
    CleverTap.showInbox({});
  };

  const checkPermission = () => {
    CleverTap.isPushPermissionGranted((err: any, res: any) => {
      Alert.alert(
        'Push Permission',
        err ? `Error: ${err}` : `Granted: ${JSON.stringify(res)}`,
      );
    });
  };

  const cleverTapActions: ActionButton[] = [
    { title: 'Record Event', onPress: recordEvent },
    { title: 'Update Profile', onPress: updateProfile },
    { title: 'Push Primer', onPress: promptPushPrimer },
    { title: 'Register for Push', onPress: registerForPush },
    ...(Platform.OS === 'android'
      ? [{ title: 'Create Channel', onPress: createChannel }]
      : []),
    { title: 'App Inbox', onPress: showInbox },
    { title: 'Push Permission', onPress: checkPermission },
  ];

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.title}>SDK Integration Lab</Text>
          <Text style={styles.subtitle}>React Native 0.87.1</Text>
        </View>

        {SDKS.map(sdk => (
          <View key={sdk.name} style={styles.card}>
            <View style={styles.cardHeader}>
              <Text style={styles.cardName}>{sdk.name}</Text>
              <View
                style={[
                  styles.badge,
                  sdk.integrated ? styles.badgeActive : styles.badgeInactive,
                ]}>
                <Text
                  style={[
                    styles.badgeText,
                    sdk.integrated && styles.badgeTextActive,
                  ]}>
                  {sdk.integrated ? 'INTEGRATED' : 'NOT INTEGRATED'}
                </Text>
              </View>
            </View>
            <Text style={styles.cardDescription}>{sdk.description}</Text>
          </View>
        ))}

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>CleverTap SDK Status</Text>
          <Text style={styles.statusText}>
            CleverTap ID: {cleverTapId ?? 'fetching…'}
          </Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>CleverTap Actions</Text>
          {cleverTapActions.map(action => (
            <TouchableOpacity
              key={action.title}
              style={styles.button}
              onPress={action.onPress}>
              <Text style={styles.buttonText}>{action.title}</Text>
            </TouchableOpacity>
          ))}
        </View>

        <Text style={styles.footer}>
          Phase 2 — CleverTap auto-integrated (push + rich media). Plotline next.
        </Text>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f4f5f7',
  },
  scrollContent: {
    padding: 20,
  },
  header: {
    marginTop: 16,
    marginBottom: 24,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#0b1f3a',
  },
  subtitle: {
    marginTop: 4,
    fontSize: 14,
    color: '#5a6472',
  },
  card: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOpacity: 0.06,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  cardName: {
    fontSize: 18,
    fontWeight: '600',
    color: '#0b1f3a',
  },
  badge: {
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeActive: {
    backgroundColor: '#e6f6ec',
  },
  badgeInactive: {
    backgroundColor: '#e8eaed',
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#5a6472',
  },
  badgeTextActive: {
    color: '#1a7f4b',
  },
  cardDescription: {
    marginTop: 8,
    fontSize: 14,
    color: '#5a6472',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#0b1f3a',
    marginBottom: 12,
  },
  statusText: {
    fontSize: 14,
    color: '#5a6472',
  },
  button: {
    backgroundColor: '#1a73e8',
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 16,
    marginBottom: 10,
    alignItems: 'center',
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 15,
    fontWeight: '600',
  },
  footer: {
    marginTop: 8,
    fontSize: 13,
    color: '#8a93a0',
    textAlign: 'center',
  },
});

export default App;