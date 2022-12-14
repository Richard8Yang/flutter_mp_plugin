import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_mp_plugin_method_channel.dart';

typedef LandMarksCallbackFunction = void Function(
    Map<String, dynamic> landmarks);

abstract class FlutterMpPluginPlatform extends PlatformInterface {
  /// Constructs a FlutterMpPluginPlatform.
  FlutterMpPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterMpPluginPlatform _instance = MethodChannelFlutterMpPlugin();

  /// The default instance of [FlutterMpPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterMpPlugin].
  static FlutterMpPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterMpPluginPlatform] when
  /// they register themselves.
  static set instance(FlutterMpPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// This method is called when the plugin is first initialized
  /// and on every full restart.
  Future<int> init(
      {required String trackingType, Map<String, dynamic>? options}) {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future<bool> start(String sourceInfo) {
    throw UnimplementedError('start() has not been implemented.');
  }

  Future<void> pause() {
    throw UnimplementedError('pause() has not been implemented.');
  }

  Future<void> resume() {
    throw UnimplementedError('resume() has not been implemented.');
  }

  Future<void> stop() {
    throw UnimplementedError('stop() has not been implemented.');
  }

  // Surface texture id used by Texture widget
  Future<int?> getTextureId() {
    throw UnimplementedError('getTextureId() has not been implemented.');
  }
}
