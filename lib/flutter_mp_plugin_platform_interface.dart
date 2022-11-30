import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_mp_plugin_method_channel.dart';

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
}
