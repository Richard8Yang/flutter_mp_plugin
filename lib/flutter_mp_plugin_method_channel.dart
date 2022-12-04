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
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> init(
      {required String trackingType,
      LandMarksCallbackFunction? landMarksCallbackFun}) async {
    final ret = await methodChannel.invokeMethod<bool>(
        'init', <String, dynamic>{'trackingType': trackingType});
    // landMarksCallbackFun;  // TODO: callback, how?
    return ret ?? false;
  }

  @override
  Future<Map<String, dynamic>> getConfig() async {
    final ret =
        await methodChannel.invokeMethod<Map<String, dynamic>>('getConfig');
    return ret ?? {};
  }

  @override
  Future<bool> start(String sourceInfo, Map<String, dynamic>? config) async {
    final ret = await methodChannel.invokeMethod<bool>(
        'start', <String, dynamic>{'sourceInfo': sourceInfo, 'config': config});
    return ret ?? false;
  }

  @override
  Future<void> pause() async {
    await methodChannel.invokeMethod<void>('pause');
  }

  @override
  Future<void> resume() async {
    await methodChannel.invokeMethod<void>('resume');
  }

  @override
  Future<void> stop() async {
    await methodChannel.invokeMethod<void>('stop');
  }

  @override
  Future<int?> getTextureId() async {
    final ret = await methodChannel.invokeMethod<int>('getTextureId');
    return ret;
  }
}
