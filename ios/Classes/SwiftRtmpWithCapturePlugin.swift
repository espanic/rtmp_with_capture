import Flutter
import UIKit

public class SwiftRtmpWithCapturePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "rtmp_with_capture", binaryMessenger: registrar.messenger())
    let instance = SwiftRtmpWithCapturePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
