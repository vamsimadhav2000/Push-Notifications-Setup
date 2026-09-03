/**
 * @format
 */

jest.mock('clevertap-react-native', () => ({
  CleverTapPushNotificationClicked: 'CleverTapPushNotificationClicked',
  CleverTapInAppNotificationButtonTapped: 'CleverTapInAppNotificationButtonTapped',
  CleverTapPushPermissionResponseReceived: 'CleverTapPushPermissionResponseReceived',
  addListener: jest.fn(),
  removeListener: jest.fn(),
  recordEvent: jest.fn(),
  onUserLogin: jest.fn(),
  promptPushPrimer: jest.fn(),
  registerForPush: jest.fn(),
  createNotificationChannel: jest.fn(),
  showInbox: jest.fn(),
  isPushPermissionGranted: jest.fn(),
  profileGetCleverTapID: jest.fn((cb: (err: any, res: any) => void) =>
    cb(null, 'test-ct-id'),
  ),
}));

import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import App from '../App';

test('renders correctly', async () => {
  await ReactTestRenderer.act(() => {
    ReactTestRenderer.create(<App />);
  });
});