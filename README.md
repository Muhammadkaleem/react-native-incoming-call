# React Native Incoming Call

A React Native package that displays a custom incoming call screen which works in foreground, background, and lock screen state on Android. Built with Nitro Modules for better performance and modern React Native compatibility.

## Features

- 📱 Full-screen incoming call UI
- 🔒 Lock screen display support
- ⚡ Works in foreground, background, and killed app states
- 🎨 Customizable caller info, avatar, and UI
- ⏰ Built-in timeout handling
- 📞 Answer and reject functionality
- 🎵 Customizable ringtone support
- 📡 Event-driven architecture

## Installation

```bash
npm install react-native-incoming-call react-native-nitro-modules
# or
yarn add react-native-incoming-call react-native-nitro-modules
```

> `react-native-nitro-modules` is required as this library relies on [Nitro Modules](https://nitro.margelo.com/).

## Requirements

- React Native 0.70+
- Nitro Modules
- Android API 21+
- Android permissions:
  - `USE_FULL_SCREEN_INTENT`
  - `WAKE_LOCK`
  - `FOREGROUND_SERVICE`

## Usage

### Basic Usage

```typescript
import IncomingCall from 'react-native-incoming-call';

// Display an incoming call
IncomingCall.display({
  uuid: 'call-123',
  callerName: 'John Doe',
  avatar: 'https://example.com/avatar.jpg',
  callType: 'video',
  backgroundColor: '#1a1a1a',
  timeout: 20000
});
```

### Using the IncomingCallPage Component

```typescript
import React, { useState } from 'react';
import IncomingCallPage from 'react-native-incoming-call/IncomingCallPage';

const MyComponent = () => {
  const [showCall, setShowCall] = useState(false);

  return (
    <>
      {showCall && (
        <IncomingCallPage
          uuid="call-123"
          callerName="John Doe"
          avatar="https://example.com/avatar.jpg"
          callType="video"
          backgroundColor="#1a1a1a"
          onAnswer={() => {
            console.log('Call answered');
            setShowCall(false);
          }}
          onReject={() => {
            console.log('Call rejected');
            setShowCall(false);
          }}
          timeout={20000}
        />
      )}
    </>
  );
};
```

### Event Listeners

```typescript
import IncomingCall from 'react-native-incoming-call';

// Set up event listeners
const unsubscribeAnswer = IncomingCall.addEventListener('onAnswer', (data) => {
  console.log('Call answered:', data.uuid);
});

const unsubscribeReject = IncomingCall.addEventListener('onReject', (data) => {
  console.log('Call rejected:', data.uuid);
});

const unsubscribeTimeout = IncomingCall.addEventListener('onTimeout', (data) => {
  console.log('Call timed out:', data.uuid);
});

// Clean up listeners
// unsubscribeAnswer();
// unsubscribeReject();
// unsubscribeTimeout();
```

### Call Management

```typescript
import IncomingCall from 'react-native-incoming-call';

// Answer a call
await IncomingCall.answer('call-123');

// Reject a call
await IncomingCall.reject('call-123');

// End an ongoing call
await IncomingCall.end('call-123');
```

## API Reference

### IncomingCall.display(options)

Display an incoming call screen.

**Parameters:**
- `options` (IncomingCallDisplayOptions): Configuration options

**IncomingCallDisplayOptions:**
- `uuid` (string): Unique identifier for the call
- `callerName` (string): Name of the caller
- `avatar` (string, optional): URL to caller's avatar image
- `callType` ('audio' | 'video', optional): Type of call (default: 'audio')
- `backgroundColor` (string, optional): Background color (default: '#0a0a0a')
- `timeout` (number, optional): Auto-reject timeout in ms (default: 20000)

### IncomingCall.answer(uuid)

Answer an incoming call.

**Parameters:**
- `uuid` (string): Unique identifier for the call

### IncomingCall.reject(uuid)

Reject an incoming call.

**Parameters:**
- `uuid` (string): Unique identifier for the call

### IncomingCall.end(uuid)

End an ongoing call.

**Parameters:**
- `uuid` (string): Unique identifier for the call

### Event Listeners

#### IncomingCall.addEventListener(event, listener)

Add an event listener for call events.

**Parameters:**
- `event` ('onAnswer' | 'onReject' | 'onTimeout'): Event type
- `listener` (function): Callback function

**Returns:** Unsubscribe function

#### IncomingCall.removeEventListener(event, listener)

Remove an event listener.

**Parameters:**
- `event` ('onAnswer' | 'onReject' | 'onTimeout'): Event type
- `listener` (function): Callback function to remove

## IncomingCallPage Component

A React component that renders the incoming call UI.

**Props:**
- `uuid` (string): Unique identifier for the call
- `callerName` (string): Name of the caller
- `avatar` (string, optional): URL to caller's avatar image
- `callType` ('audio' | 'video', optional): Type of call (default: 'audio')
- `backgroundColor` (string, optional): Background color (default: '#0a0a0a')
- `onAnswer` (function, optional): Callback when answer button is pressed
- `onReject` (function, optional): Callback when reject button is pressed
- `timeout` (number, optional): Auto-reject timeout in ms (default: 20000)

## Android Setup

### Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### Activities

The package automatically registers the necessary activities for displaying the incoming call screen above the lock screen.

## Example

See the `example/` directory for a complete working example.

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
