
import 'dart:async';

import 'package:flutter/services.dart';

class RtmpWithCapture {
  static const MethodChannel _channel = MethodChannel('rtmp_with_capture');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
