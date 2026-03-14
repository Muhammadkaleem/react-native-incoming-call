# @connect-code/react-native-incoming-call

> Full-screen incoming call UI for React Native — works on foreground, background, and lock screen on Android. Built on [Nitro Modules](https://nitro.margelo.com/) for maximum native performance.

---

## Features

- Full-screen incoming call activity shown above the lock screen
- Wakes the device screen and dismisses the keyguard automatically
- Works in foreground, background, and killed-app states
- Foreground service keeps the call alive when the app is not running
- Answer / Reject buttons with event callbacks to JavaScript
- Auto-timeout with configurable duration
- Customisable caller name, avatar, background colour, and call type
- Inline `IncomingCallView` Nitro View for embedding in your own React Native screen
- Event-driven API — subscribe with a simple `addEventListener` / cleanup function

---

## Platform support

| Platform | Support |
|----------|---------|
| Android | ✅ Full support |
| iOS | 🔜 Planned (CallKit) |

---

## Installation

```bash
# npm
npm install @connect-code/react-native-incoming-call react-native-nitro-modules

# yarn
yarn add @connect-code/react-native-incoming-call react-native-nitro-modules
```

[react-native-nitro-modules](https://nitro.margelo.com/) is a required peer dependency.

### Android permissions

The library merges its own `AndroidManifest.xml` automatically. If you need to declare them manually, add the following to your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_PHONE_CALL" />
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## Expo

| Workflow | Support |
|----------|---------|
| Expo Go | ❌ Not supported (native modules required) |
| Development Build (`expo-dev-client`) | ✅ Supported |
| Bare workflow | ✅ Supported |
| EAS Build | ✅ Supported |

This library uses **Nitro Modules** (C++ + Kotlin/Swift native code) and cannot run in Expo Go. You must use a [development build](https://docs.expo.dev/develop/development-builds/introduction/) or bare workflow.

### Setup with Expo Dev Client

**1. Install dependencies**

```bash
npx expo install @connect-code/react-native-incoming-call react-native-nitro-modules expo-dev-client
```

**2. Rebuild your native app**

```bash
# Android
npx expo run:android

# or with EAS Build
eas build --profile development --platform android
```

> No Expo config plugin is required — permissions and the foreground service are merged automatically via React Native autolinking.

### Android 14+ note (`USE_FULL_SCREEN_INTENT`)

On Android 14 (API 34) and above, `USE_FULL_SCREEN_INTENT` is a **restricted permission**. It is granted automatically only for apps that have the `ROLE_DIALER` or `ROLE_EMERGENCY` role. For all other apps you must direct the user to grant it manually:

```typescript
import { Linking } from 'react-native';

// Open the system settings page for your app so the user can allow
// "Display over other apps / full-screen notifications"
Linking.openSettings();
```

Or use [`expo-intent-launcher`](https://docs.expo.dev/versions/latest/sdk/intent-launcher/) to open the exact settings screen:

```typescript
import * as IntentLauncher from 'expo-intent-launcher';

IntentLauncher.startActivityAsync(
  'android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT',
  { data: 'package:com.yourapp' }
);
```

> For development builds the permission is typically auto-granted. This only matters for production apps targeting API 34+.

### EAS Build

No special configuration is needed. Add the library to your project and EAS Build will handle the rest through autolinking:

```bash
eas build --platform android
```

---

## Quick start

```typescript
import IncomingCall from '@connect-code/react-native-incoming-call';

// Show the incoming call screen (from a push notification handler, etc.)
await IncomingCall.display({
  uuid: 'call-abc-123',
  callerName: 'John Doe',
  avatar: 'https://example.com/avatar.jpg',
  callType: 'audio',          // 'audio' | 'video'
  backgroundColor: '#1A1A2E',
  timeout: 20000,             // auto-reject after 20 s
});
```

---

## API reference

### `IncomingCall.display(options)`

Starts the foreground service and launches the full-screen call activity.

```typescript
IncomingCall.display({
  uuid: string;             // unique call ID (required)
  callerName: string;       // caller display name (required)
  avatar?: string;          // remote avatar URL
  callType?: 'audio' | 'video';  // defaults to 'audio'
  backgroundColor?: string; // hex colour, e.g. '#1A1A2E'
  timeout?: number;         // ms before auto-reject, default 30000
}): Promise<void>
```

### `IncomingCall.answer(uuid)`

Programmatically answer the call and emit `onAnswer`.

```typescript
await IncomingCall.answer('call-abc-123');
```

### `IncomingCall.reject(uuid)`

Programmatically reject the call and emit `onReject`.

```typescript
await IncomingCall.reject('call-abc-123');
```

### `IncomingCall.end(uuid)`

End an active call and stop the foreground service.

```typescript
await IncomingCall.end('call-abc-123');
```

### `IncomingCall.addEventListener(event, listener)`

Subscribe to a call lifecycle event. Returns an unsubscribe function.

```typescript
const unsubscribe = IncomingCall.addEventListener('onAnswer', (data) => {
  console.log('Answered:', data.uuid, data.timestamp);
});

// later…
unsubscribe();
```

| Event | When fired |
|-------|-----------|
| `onAnswer` | User taps Accept or `answer()` is called |
| `onReject` | User taps Decline or `reject()` is called |
| `onTimeout` | Timeout elapsed with no user action |

### `IncomingCall.removeEventListener(event, listener)`

Alternative to calling the returned unsubscribe function.

```typescript
IncomingCall.removeEventListener('onAnswer', myListener);
```

---

## Event data shape

```typescript
interface IncomingCallEventData {
  uuid: string;       // the call ID passed to display()
  timestamp: number;  // Unix epoch ms
}
```

---

## IncomingCallView (Nitro View)

Embed an inline call UI inside your own React Native screen (foreground use-case).

```tsx
import { IncomingCallView } from '@connect-code/react-native-incoming-call';

<IncomingCallView
  color="#1A1A2E"
  callerName="John Doe"
  callType="audio"
  style={{ width: '100%', height: 220, borderRadius: 16 }}
/>
```

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `color` | `string` | `"#1A1A2E"` | Background colour (hex) |
| `callerName` | `string` | — | Caller display name |
| `avatar` | `string` | — | Avatar URL |
| `callType` | `string` | `"audio"` | `"audio"` or `"video"` |
| `timeout` | `number` | `30000` | Auto-reject timeout (ms) |

**Methods** (via ref):

```tsx
const ref = useRef<IncomingCallViewRef>(null);

ref.current?.answerCall();
ref.current?.rejectCall();
```

---

## Full example

```tsx
import React, { useEffect } from 'react';
import { Button } from 'react-native';
import IncomingCall from '@connect-code/react-native-incoming-call';

export default function App() {
  useEffect(() => {
    const unsubAnswer  = IncomingCall.addEventListener('onAnswer',  (d) => console.log('answered', d.uuid));
    const unsubReject  = IncomingCall.addEventListener('onReject',  (d) => console.log('rejected', d.uuid));
    const unsubTimeout = IncomingCall.addEventListener('onTimeout', (d) => console.log('timeout',  d.uuid));

    return () => {
      unsubAnswer();
      unsubReject();
      unsubTimeout();
    };
  }, []);

  const call = () =>
    IncomingCall.display({
      uuid: `call-${Date.now()}`,
      callerName: 'Jane Smith',
      callType: 'video',
      backgroundColor: '#0D1B2A',
      timeout: 15000,
    });

  return <Button title="Simulate Incoming Call" onPress={call} />;
}
```

See the [`example/`](example/) directory for the full working demo app.

---

## Architecture

```
react-native-incoming-call
├── src/
│   ├── index.tsx                 JS/TS public API + event manager
│   └── IncomingCall.nitro.ts     Nitro spec (props & methods)
├── android/
│   └── src/main/java/…
│       ├── IncomingCallModule.kt     RN bridge — display/answer/reject/end
│       ├── IncomingCallActivity.kt   Full-screen lock-screen activity
│       ├── IncomingCallService.kt    Foreground service (phoneCall type)
│       ├── IncomingCall.kt           HybridIncomingCall Nitro View
│       └── IncomingCallPackage.kt    Package registration
└── example/                      Demo app
```

**Call flow:**

```
JS: IncomingCall.display()
  → IncomingCallModule (Kotlin)
    → IncomingCallService (foreground, keeps alive in background/killed)
      → IncomingCallActivity (lock screen, wake, full-screen UI)
        → user taps Accept / Decline
          → broadcast → IncomingCallModule → JS event (onAnswer / onReject)
```

---

## Requirements

| Requirement | Version |
|-------------|---------|
| React Native | 0.73+ |
| react-native-nitro-modules | 0.35+ |
| Android minSdk | 24 (Android 7.0) |
| compileSdk | 36 |
| Kotlin | 2.0+ |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

Pull requests are welcome. For major changes, please open an issue first.

## License

MIT © [MuhammadKaleem](https://github.com/Muhammadkaleem)

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob) + [Nitro Modules](https://nitro.margelo.com/)
