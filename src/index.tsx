import { getHostComponent } from 'react-native-nitro-modules';
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
const IncomingCallConfig = require('../nitrogen/generated/shared/json/IncomingCallConfig.json');
import type {
  IncomingCallMethods,
  IncomingCallProps,
} from './IncomingCall.nitro';

export const IncomingCallView = getHostComponent<
  IncomingCallProps,
  IncomingCallMethods
>('IncomingCall', () => IncomingCallConfig);

// Types for the API
export interface IncomingCallDisplayOptions {
  uuid: string;
  callerName: string;
  avatar?: string;
  callType?: 'audio' | 'video';
  backgroundColor?: string;
  timeout?: number;
}

export type IncomingCallEventType = 'onAnswer' | 'onReject' | 'onTimeout';

export interface IncomingCallEventData {
  uuid: string;
  timestamp: number;
}

// Event emitter for handling incoming call events
class IncomingCallEventManager {
  private eventEmitter: NativeEventEmitter | undefined;
  private listeners: Map<
    IncomingCallEventType,
    Set<(data: IncomingCallEventData) => void>
  > = new Map();

  constructor() {
    if (NativeModules.IncomingCallModule) {
      this.eventEmitter = new NativeEventEmitter(
        NativeModules.IncomingCallModule
      );
      this.setupNativeListeners();
    }
  }

  private setupNativeListeners() {
    if (!this.eventEmitter) return;

    this.eventEmitter.addListener('onAnswer', (data: any) => {
      this.emit('onAnswer', data as IncomingCallEventData);
    });

    this.eventEmitter.addListener('onReject', (data: any) => {
      this.emit('onReject', data as IncomingCallEventData);
    });

    this.eventEmitter.addListener('onTimeout', (data: any) => {
      this.emit('onTimeout', data as IncomingCallEventData);
    });
  }

  private emit(event: IncomingCallEventType, data: IncomingCallEventData) {
    const listeners = this.listeners.get(event);
    if (listeners) {
      listeners.forEach((listener) => listener(data));
    }
  }

  addEventListener(
    event: IncomingCallEventType,
    listener: (data: IncomingCallEventData) => void
  ) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    this.listeners.get(event)!.add(listener);

    // Return unsubscribe function
    return () => {
      this.listeners.get(event)?.delete(listener);
    };
  }

  removeEventListener(
    event: IncomingCallEventType,
    listener: (data: IncomingCallEventData) => void
  ) {
    this.listeners.get(event)?.delete(listener);
  }
}

const eventManager = new IncomingCallEventManager();

// Main API class
export class IncomingCall {
  /**
   * Display an incoming call screen
   */
  static async display(options: IncomingCallDisplayOptions): Promise<void> {
    try {
      if (NativeModules.IncomingCallModule) {
        return await NativeModules.IncomingCallModule.displayIncomingCall(
          options
        );
      } else {
        console.warn('IncomingCall: Native module not available');
      }
    } catch (error) {
      console.error('IncomingCall: Failed to display call', error);
      throw error;
    }
  }

  /**
   * Answer the current incoming call
   */
  static async answer(uuid: string): Promise<void> {
    try {
      if (NativeModules.IncomingCallModule) {
        return await NativeModules.IncomingCallModule.answerCall(uuid);
      } else {
        console.warn('IncomingCall: Native module not available');
      }
    } catch (error) {
      console.error('IncomingCall: Failed to answer call', error);
      throw error;
    }
  }

  /**
   * Reject the current incoming call
   */
  static async reject(uuid: string): Promise<void> {
    try {
      if (NativeModules.IncomingCallModule) {
        return await NativeModules.IncomingCallModule.rejectCall(uuid);
      } else {
        console.warn('IncomingCall: Native module not available');
      }
    } catch (error) {
      console.error('IncomingCall: Failed to reject call', error);
      throw error;
    }
  }

  /**
   * End an ongoing call
   */
  static async end(uuid: string): Promise<void> {
    try {
      if (NativeModules.IncomingCallModule) {
        return await NativeModules.IncomingCallModule.endCall(uuid);
      } else {
        console.warn('IncomingCall: Native module not available');
      }
    } catch (error) {
      console.error('IncomingCall: Failed to end call', error);
      throw error;
    }
  }

  /**
   * Check if the app can show full-screen intents (Android 14+ only).
   * Always returns true on iOS and Android < 14.
   */
  static async canShowFullScreen(): Promise<boolean> {
    if (Platform.OS === 'android' && NativeModules.IncomingCallModule) {
      return await NativeModules.IncomingCallModule.canShowFullScreen();
    }
    return true;
  }

  /**
   * Open system settings so the user can grant USE_FULL_SCREEN_INTENT
   * (Android 14+ only). No-op on other platforms / API levels.
   */
  static async requestFullScreenPermission(): Promise<void> {
    if (Platform.OS === 'android' && NativeModules.IncomingCallModule) {
      return await NativeModules.IncomingCallModule.requestFullScreenPermission();
    }
  }

  /**
   * Add event listener for call events
   */
  static addEventListener(
    event: IncomingCallEventType,
    listener: (data: IncomingCallEventData) => void
  ): () => void {
    return eventManager.addEventListener(event, listener);
  }

  /**
   * Remove event listener for call events
   */
  static removeEventListener(
    event: IncomingCallEventType,
    listener: (data: IncomingCallEventData) => void
  ): void {
    eventManager.removeEventListener(event, listener);
  }
}

// Default export
export default IncomingCall;
