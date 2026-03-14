import Foundation
import CallKit
import React

@objc(IncomingCallModule)
class IncomingCallModule: RCTEventEmitter {

  private var provider: CXProvider?
  private let callController = CXCallController()
  private var currentCallUUID: UUID?
  private var timeoutTimer: DispatchWorkItem?

  override init() {
    super.init()
    let config = CXProviderConfiguration()
    config.supportsVideo = true
    config.maximumCallsPerCallGroup = 1
    config.supportedHandleTypes = [.generic]
    provider = CXProvider(configuration: config)
    provider?.setDelegate(self, queue: nil)
  }

  override static func requiresMainQueueSetup() -> Bool {
    return false
  }

  override func supportedEvents() -> [String]! {
    return ["onAnswer", "onReject", "onTimeout"]
  }

  // Required by RCTEventEmitter
  @objc override func addListener(_ eventName: String) {
    super.addListener(eventName)
  }

  @objc override func removeListeners(_ count: Double) {
    super.removeListeners(count)
  }

  @objc func displayIncomingCall(
    _ options: [String: Any],
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let uuid = UUID()
    currentCallUUID = uuid

    let callerName = options["callerName"] as? String ?? "Unknown"
    let callType = options["callType"] as? String ?? "audio"
    let timeoutMs = options["timeout"] as? Double ?? 30000

    let handle = CXHandle(type: .generic, value: callerName)
    let callUpdate = CXCallUpdate()
    callUpdate.remoteHandle = handle
    callUpdate.hasVideo = callType == "video"
    callUpdate.localizedCallerName = callerName

    provider?.reportNewIncomingCall(with: uuid, update: callUpdate) { [weak self] error in
      guard let self = self else { return }
      if let error = error {
        self.currentCallUUID = nil
        reject("DISPLAY_ERROR", error.localizedDescription, error)
        return
      }

      // Schedule auto-timeout
      let timer = DispatchWorkItem { [weak self] in
        guard let self = self, self.currentCallUUID == uuid else { return }
        self.provider?.reportCall(with: uuid, endedAt: Date(), reason: .unanswered)
        self.currentCallUUID = nil
        self.sendEvent(withName: "onTimeout", body: [
          "uuid": uuid.uuidString,
          "timestamp": Date().timeIntervalSince1970 * 1000,
        ])
      }
      self.timeoutTimer = timer
      DispatchQueue.main.asyncAfter(deadline: .now() + timeoutMs / 1000, execute: timer)
      resolve(nil)
    }
  }

  @objc func answerCall(
    _ uuidString: String,
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    guard let uuid = currentCallUUID else {
      resolve(nil)
      return
    }
    timeoutTimer?.cancel()
    let action = CXAnswerCallAction(call: uuid)
    callController.request(CXTransaction(action: action)) { _ in }
    resolve(nil)
  }

  @objc func rejectCall(
    _ uuidString: String,
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    guard let uuid = currentCallUUID else {
      resolve(nil)
      return
    }
    timeoutTimer?.cancel()
    currentCallUUID = nil
    let action = CXEndCallAction(call: uuid)
    callController.request(CXTransaction(action: action)) { _ in }
    resolve(nil)
  }

  @objc func endCall(
    _ uuidString: String,
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    guard let uuid = currentCallUUID else {
      resolve(nil)
      return
    }
    timeoutTimer?.cancel()
    currentCallUUID = nil
    let action = CXEndCallAction(call: uuid)
    callController.request(CXTransaction(action: action)) { _ in }
    resolve(nil)
  }
}

// MARK: - CXProviderDelegate

extension IncomingCallModule: CXProviderDelegate {

  func providerDidReset(_ provider: CXProvider) {
    timeoutTimer?.cancel()
    currentCallUUID = nil
  }

  func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
    timeoutTimer?.cancel()
    sendEvent(withName: "onAnswer", body: [
      "uuid": action.callUUID.uuidString,
      "timestamp": Date().timeIntervalSince1970 * 1000,
    ])
    currentCallUUID = nil
    action.fulfill()
  }

  func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
    timeoutTimer?.cancel()
    // Only emit onReject if the call was still ringing (not yet answered)
    if currentCallUUID != nil {
      sendEvent(withName: "onReject", body: [
        "uuid": action.callUUID.uuidString,
        "timestamp": Date().timeIntervalSince1970 * 1000,
      ])
      currentCallUUID = nil
    }
    action.fulfill()
  }
}
