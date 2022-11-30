import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_mp_plugin_platform_interface.dart';

/// An implementation of [FlutterMpPluginPlatform] that uses method channels.
class MethodChannelFlutterMpPlugin extends FlutterMpPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_mp_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
