import type {
  HybridView,
  HybridViewMethods,
  HybridViewProps,
} from 'react-native-nitro-modules';

export interface IncomingCallProps extends HybridViewProps {
  /** Background color of the call screen (CSS hex, e.g. "#1A1A2E") */
  color: string;
  /** Caller's display name */
  callerName?: string;
  /** Remote avatar URL */
  avatar?: string;
  /** "audio" | "video" – defaults to "audio" */
  callType?: string;
  /** Auto-reject timeout in milliseconds – defaults to 30000 */
  timeout?: number;
}

export interface IncomingCallMethods extends HybridViewMethods {
  /** Programmatically answer the call rendered in this view */
  answerCall(): void;
  /** Programmatically reject the call rendered in this view */
  rejectCall(): void;
}

export type IncomingCall = HybridView<IncomingCallProps, IncomingCallMethods>;
