import 'dart:async';

import 'package:flutter/services.dart';

export 'flutter_tuia_banner_view.dart';

class FlutterTuia {
  static const MethodChannel _channel = const MethodChannel('flutter_tuia');

  static Future<bool> checkPermissions() async {
    return await _channel.invokeMethod('checkPermissions');
  }

  static Future<void> initSDK() async {
    await _channel.invokeMethod('initSDK');
  }
}
