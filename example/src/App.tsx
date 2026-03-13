import { useEffect, useRef, useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
} from 'react-native';
import { IncomingCall, IncomingCallView } from 'react-native-incoming-call';

export default function App() {
  const [log, setLog] = useState<string[]>([]);
  const unsubscribeRef = useRef<(() => void)[]>([]);

  const addLog = (msg: string) =>
    setLog((prev) => [`[${new Date().toLocaleTimeString()}] ${msg}`, ...prev]);

  useEffect(() => {
    // Listen for call events emitted from the native module
    const unsub1 = IncomingCall.addEventListener('onAnswer', (data) => {
      addLog(`✅ Answered  uuid=${data.uuid}`);
    });
    const unsub2 = IncomingCall.addEventListener('onReject', (data) => {
      addLog(`❌ Rejected  uuid=${data.uuid}`);
    });
    const unsub3 = IncomingCall.addEventListener('onTimeout', (data) => {
      addLog(`⏱ Timeout   uuid=${data.uuid}`);
    });

    unsubscribeRef.current = [unsub1, unsub2, unsub3];
    return () => unsubscribeRef.current.forEach((fn) => fn());
  }, []);

  const simulateIncomingCall = async () => {
    try {
      await IncomingCall.display({
        uuid: `call-${Date.now()}`,
        callerName: 'John Doe',
        avatar: 'https://i.pravatar.cc/150?img=3',
        callType: 'audio',
        backgroundColor: '#1A1A2E',
        timeout: 20000,
      });
      addLog('📲 Incoming call displayed');
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'Failed to display call');
    }
  };

  const simulateVideoCall = async () => {
    try {
      await IncomingCall.display({
        uuid: `video-${Date.now()}`,
        callerName: 'Jane Smith',
        callType: 'video',
        backgroundColor: '#0D1B2A',
        timeout: 15000,
      });
      addLog('🎥 Incoming video call displayed');
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'Failed to display call');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>react-native-incoming-call</Text>
      <Text style={styles.subtitle}>Nitro Modules Demo</Text>

      {/* Inline NitroView preview */}
      <IncomingCallView
        color="#1A1A2E"
        callerName="Preview User"
        callType="audio"
        style={styles.previewView}
      />

      {/* Action buttons */}
      <View style={styles.buttonsRow}>
        <TouchableOpacity style={styles.btnGreen} onPress={simulateIncomingCall}>
          <Text style={styles.btnText}>📲 Audio Call</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.btnBlue} onPress={simulateVideoCall}>
          <Text style={styles.btnText}>🎥 Video Call</Text>
        </TouchableOpacity>
      </View>

      {/* Event log */}
      <Text style={styles.logTitle}>Event Log</Text>
      <ScrollView style={styles.logBox}>
        {log.length === 0 ? (
          <Text style={styles.logEmpty}>No events yet…</Text>
        ) : (
          log.map((line, i) => (
            <Text key={i} style={styles.logLine}>
              {line}
            </Text>
          ))
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0D0D1A',
    padding: 20,
    paddingTop: 60,
  },
  title: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: '700',
    textAlign: 'center',
  },
  subtitle: {
    color: '#888',
    fontSize: 13,
    textAlign: 'center',
    marginBottom: 20,
  },
  previewView: {
    width: '100%',
    height: 220,
    borderRadius: 16,
    overflow: 'hidden',
    marginBottom: 20,
  },
  buttonsRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 24,
  },
  btnGreen: {
    flex: 1,
    backgroundColor: '#43A047',
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
  },
  btnBlue: {
    flex: 1,
    backgroundColor: '#1565C0',
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
  },
  btnText: {
    color: '#FFF',
    fontWeight: '600',
    fontSize: 15,
  },
  logTitle: {
    color: '#AAA',
    fontSize: 12,
    fontWeight: '600',
    marginBottom: 8,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  logBox: {
    flex: 1,
    backgroundColor: '#111122',
    borderRadius: 10,
    padding: 12,
  },
  logEmpty: {
    color: '#555',
    fontSize: 13,
    fontStyle: 'italic',
  },
  logLine: {
    color: '#CCC',
    fontSize: 12,
    marginBottom: 4,
    fontFamily: 'monospace',
  },
});
