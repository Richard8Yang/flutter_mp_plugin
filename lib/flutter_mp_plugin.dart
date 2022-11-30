
import 'flutter_mp_plugin_platform_interface.dart';

class FlutterMpPlugin {
  Future<String?> getPlatformVersion() {
    return FlutterMpPluginPlatform.instance.getPlatformVersion();
  }
}
