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
  Future<int> init(
      {required String trackingType, Map<String, dynamic>? options}) async {
    final ret = await methodChannel.invokeMethod<int>('init',
        <String, dynamic>{'trackingType': trackingType, 'options': options});
    return ret ?? -1;
  }

  @override
  Future<bool> start(String sourceInfo) async {
    var args = <String, dynamic>{'sourceInfo': sourceInfo};
    final ret = await methodChannel.invokeMethod<bool>('start', args);
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
