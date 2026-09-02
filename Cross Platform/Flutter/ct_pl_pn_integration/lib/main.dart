import 'dart:convert';
import 'dart:io';

import 'package:clevertap_plugin/clevertap_plugin.dart';
import 'package:flutter/material.dart';
import 'package:plotline_engage/plotline.dart';

// Secrets come from the local secrets.env file (gitignored) via:
//   flutter run --dart-define-from-file=secrets.env
const String _demoUserId = String.fromEnvironment('CT_DEMO_USER_ID');
const String _demoUserName = String.fromEnvironment('CT_DEMO_USER_NAME');
const String _demoUserEmail = String.fromEnvironment('CT_DEMO_USER_EMAIL');

const String _plotlineApiKey = String.fromEnvironment('PLOTLINE_API_KEY');
const String _plotlineUserId = String.fromEnvironment('PLOTLINE_USER_ID');

// Handlers fire outside the widget tree, so deeplinks are shown via this global
// navigator key attached to MaterialApp.
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

// Guards against showing the same payload twice back-to-back (e.g. the Android
// killed-state launch check and a CleverTap click both resolving the same tap).
String _lastShownPayload = '';
DateTime _lastShownAt = DateTime.fromMillisecondsSinceEpoch(0);

// Must be a top-level function (runs on a background isolate on Android).
@pragma('vm:entry-point')
void onCleverTapKilledStateNotificationClicked(Map<String, dynamic> payload) {
  // NOTE: this runs in the plugin's background isolate (clevertap_callback_dispatcher),
  // which has its own memory, so any globals set here are NOT visible in the main
  // isolate. Killed-state deeplinks are instead read via getAppLaunchNotification()
  // (Android) / getInitialUrl() (iOS) in _HomePageState.initState. This handler is
  // registered so the native side has a callback handle available if needed.
}

/// Pops a dialog showing every key-value in the deeplink/push payload, so
/// whatever deeplink value is configured is visible.
void showDeeplinkModal(String source, Map<String, dynamic> payload) {
  final context = navigatorKey.currentContext;
  if (context == null) return;

  // Drop immediate re-deliveries of the identical payload (one tap can reach the
  // modal through more than one path, e.g. native click + launch notification).
  final key = jsonEncode(payload);
  if (key == _lastShownPayload &&
      DateTime.now().difference(_lastShownAt).inSeconds < 2) {
    return;
  }
  _lastShownPayload = key;
  _lastShownAt = DateTime.now();

  showDialog(
    context: context,
    builder: (dialogContext) => AlertDialog(
      title: Text('Deeplink · $source'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            for (final entry in payload.entries)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 2),
                child: Text(
                  '${entry.key}: ${entry.value}',
                  style: const TextStyle(fontFamily: 'monospace'),
                ),
              ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(dialogContext).pop(),
          child: const Text('OK'),
        ),
      ],
    ),
  );
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // CleverTap auto-integration: the SDK initializes itself from the native
  // AndroidManifest.xml (Android) and Info.plist (iOS) credentials, so there is
  // no explicit initializeInstance() call needed here.
  CleverTapPlugin.setDebugLevel(3);
  CleverTapPlugin.registerForPush();

  // CleverTap deeplink / push-click handling.
  final clevertap = CleverTapPlugin();
  clevertap.setCleverTapPushClickedPayloadReceivedHandler((payload) {
    showDeeplinkModal('CleverTap Push', payload);
  });
  CleverTapPlugin.onKilledStateNotificationClicked(
      onCleverTapKilledStateNotificationClicked);

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

  // Plotline: init once per app open, request push permission, handle clicks/deeplinks.
  if (_plotlineApiKey.isNotEmpty) {
    Plotline.debug(true);
    Plotline.init(_plotlineApiKey, _plotlineUserId);
    // Confirm the native SDK actually initialized (also surfaces failures on the
    // console), so a missing dart-define / misconfigured App Group is visible.
    Plotline.registerInitCallback(
      () => debugPrint('Plotline.init: SDK initialized successfully'),
      (error) => debugPrint('Plotline.init FAILED: $error'),
    );
    Plotline.requestPushPermission();
    Plotline.setPlotlineNotificationClickListener((properties) {
      showDeeplinkModal('Plotline Notification', properties);
    });
    Plotline.setPlotlineRedirectListener((properties) {
      showDeeplinkModal('Plotline Redirect', properties);
    });
    Plotline.track('App Launched');
  } else {
    debugPrint(
        'Plotline.init skipped - PLOTLINE_API_KEY not set. Run with: flutter run --dart-define-from-file=secrets.env');
  }

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    // PlotlineWrapper suppresses in-app messages while the user is scrolling.
    return PlotlineWrapper(
      child: MaterialApp(
        title: 'CleverTap + Plotline Integration Demo',
        theme: ThemeData(
          colorScheme: .fromSeed(seedColor: Colors.deepPurple),
        ),
        navigatorKey: navigatorKey,
        home: const HomePage(),
      ),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  void initState() {
    super.initState();
    // Cold-start deeplinks: the navigator only exists after the first frame.
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      try {
        if (Platform.isIOS) {
          // iOS: the plugin only exposes the launch deeplink string (getAppLaunchNotification
          // is Android-only and throws MissingPluginException here).
          final url = await CleverTapPlugin.getInitialUrl();
          if (url != null) {
            showDeeplinkModal('CleverTap Push (launch)', {'wzrk_dl': url});
          }
        } else {
          // Android: full payload from the launch intent when launched from a
          // killed-state CleverTap notification.
          final launch = await CleverTapPlugin.getAppLaunchNotification();
          if (launch.didNotificationLaunchApp && launch.payload != null) {
            showDeeplinkModal('CleverTap Push (launch)', launch.payload!);
          }
        }
      } catch (e) {
        debugPrint('Cold-start deeplink check failed: $e');
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('CT + Plotline Demo'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: .center,
          children: [
            const Text('CleverTap + Plotline auto-integrated'),
            const SizedBox(height: 8),
            Text(
              _demoUserId.isNotEmpty ? 'User: $_demoUserId' : 'User: anonymous',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 32),
            FilledButton(
              onPressed: () {
                CleverTapPlugin.recordEvent('Demo Button Tapped', {});
                Plotline.track('Demo Button Tapped');
              },
              child: const Text('Record Demo Event'),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: () => CleverTapPlugin.showInbox({}),
              child: const Text('Show CleverTap Inbox'),
            ),
            const SizedBox(height: 12),
            TextButton(
              onPressed: () => showDeeplinkModal(
                    'Sample',
                    {'wzrk_dl': 'ctpl://home', 'source': 'demo'},
                  ),
              child: const Text('Show Sample Deeplink'),
            ),
          ],
        ),
      ),
    );
  }
}