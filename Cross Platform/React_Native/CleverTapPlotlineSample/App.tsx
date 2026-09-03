/**
 * Sample React Native App
 * CleverTap + Plotline SDK Integration Demo
 *
 * @format
 */

import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  View,
  useColorScheme,
} from 'react-native';

type SdkStatus = 'not-integrated';

type SdkCard = {
  name: string;
  description: string;
  status: SdkStatus;
};

const SDKS: SdkCard[] = [
  {
    name: 'CleverTap',
    description: 'User engagement, push notifications & rich media',
    status: 'not-integrated',
  },
  {
    name: 'Plotline',
    description: 'Product onboarding, surveys & user experience',
    status: 'not-integrated',
  },
];

function App() {
  const isDarkMode = useColorScheme() === 'dark';

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
              <View style={styles.badge}>
                <Text style={styles.badgeText}>NOT INTEGRATED</Text>
              </View>
            </View>
            <Text style={styles.cardDescription}>{sdk.description}</Text>
          </View>
        ))}

        <Text style={styles.footer}>
          Phase 1 — Base app ready. SDKs will be wired up in Phase 2 & 3.
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
    backgroundColor: '#e8eaed',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#5a6472',
  },
  cardDescription: {
    marginTop: 8,
    fontSize: 14,
    color: '#5a6472',
  },
  footer: {
    marginTop: 8,
    fontSize: 13,
    color: '#8a93a0',
    textAlign: 'center',
  },
});

export default App;