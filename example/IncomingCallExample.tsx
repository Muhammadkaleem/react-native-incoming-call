import React from 'react';
import { View, Button, StyleSheet } from 'react-native';
import IncomingCall from 'react-native-incoming-call';

const IncomingCallExample: React.FC = () => {
  const handleShowIncomingCall = async () => {
    try {
      await IncomingCall.display({
        uuid: `call-${Date.now()}`,
        callerName: 'John Doe',
        avatar: 'https://example.com/avatar.jpg',
        callType: 'video',
        backgroundColor: '#1a1a1a',
        timeout: 20000
      });
    } catch (error) {
      console.error('Failed to display incoming call:', error);
    }
  };

  // Set up event listeners
  React.useEffect(() => {
    const unsubscribeAnswer = IncomingCall.addEventListener('onAnswer', (data) => {
      console.log('Incoming call answered:', data);
    });

    const unsubscribeReject = IncomingCall.addEventListener('onReject', (data) => {
      console.log('Incoming call rejected:', data);
    });

    const unsubscribeTimeout = IncomingCall.addEventListener('onTimeout', (data) => {
      console.log('Incoming call timed out:', data);
    });

    return () => {
      unsubscribeAnswer();
      unsubscribeReject();
      unsubscribeTimeout();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Button
        title="Show Incoming Call"
        onPress={handleShowIncomingCall}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
});

export default IncomingCallExample;
