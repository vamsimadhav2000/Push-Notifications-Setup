import 'package:clevertap_plugin/clevertap_plugin.dart';
import 'package:flutter/material.dart';

// Secrets come from the local secrets.env file (gitignored) via:
//   flutter run --dart-define-from-file=secrets.env
const String _demoUserId = String.fromEnvironment('CT_DEMO_USER_ID');
const String _demoUserName = String.fromEnvironment('CT_DEMO_USER_NAME');
const String _demoUserEmail = String.fromEnvironment('CT_DEMO_USER_EMAIL');

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // CleverTap auto-integration: the SDK initializes itself from the native
  // AndroidManifest.xml (Android) and Info.plist (iOS) credentials, so there is
  // no explicit initializeInstance() call needed here.
  CleverTapPlugin.setDebugLevel(3);
  CleverTapPlugin.registerForPush();

  // Associate this device with a demo user profile (from local secrets).
  final profile = <String, dynamic>{
    if (_demoUserName.isNotEmpty) 'Name': _demoUserName,
    if (_demoUserEmail.isNotEmpty) 'Email': _demoUserEmail,
  };
  if (_demoUserId.isNotEmpty) {
    profile['Identity'] = _demoUserId;
    CleverTapPlugin.onUserLogin(profile);
  } else {
    CleverTapPlugin.profileSet(profile);
  }

  CleverTapPlugin.recordEvent('App Launched', {});

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CleverTap Integration Demo',
      theme: ThemeData(
        colorScheme: .fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('CleverTap Integration Demo'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: .center,
          children: [
            const Text('CleverTap auto-integrated'),
            const SizedBox(height: 8),
            Text(
              _demoUserId.isNotEmpty ? 'User: $_demoUserId' : 'User: anonymous',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 32),
            FilledButton(
              onPressed: () =>
                  CleverTapPlugin.recordEvent('Demo Button Tapped', {}),
              child: const Text('Record Demo Event'),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: () => CleverTapPlugin.showInbox({}),
              child: const Text('Show App Inbox'),
            ),
          ],
        ),
      ),
    );
  }
}