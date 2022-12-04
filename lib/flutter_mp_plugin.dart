import 'flutter_mp_plugin_platform_interface.dart';

final FlutterMpPluginPlatform _mpPluginPlatform =
    FlutterMpPluginPlatform.instance;

class FlutterMpPlugin {
  Future<String?> getPlatformVersion() {
    return FlutterMpPluginPlatform.instance.getPlatformVersion();
  }

  Future<bool> init(
          {String trackingType = "holistic",
          LandMarksCallbackFunction? landMarksCallbackFun}) =>
      _mpPluginPlatform.init(
          trackingType: trackingType,
          landMarksCallbackFun: landMarksCallbackFun);

  Future<Map<String, dynamic>> getConfig() {
    return _mpPluginPlatform.getConfig();
  }

  // source info can be:
  // 1. camera: "camera::front/high_resolution"
  // 2. TODO: video: "video::http://url.to.video" or "video::/sdcard/path/to/video"
  // 3. TODO: image: "image::http://url.to.image" or "image::/sdcard/path/to/image"
  // 4. TODO: screen capture "screen::region/0.0.1080.1920"
  Future<bool> start(String sourceInfo, Map<String, dynamic>? config) {
    return _mpPluginPlatform.start(sourceInfo, config);
  }

  Future<void> pause() => _mpPluginPlatform.pause();

  Future<void> resume() => _mpPluginPlatform.resume();

  Future<void> stop() => _mpPluginPlatform.stop();

  // Surface texture id used by Texture widget
  Future<int?> getTextureId() => _mpPluginPlatform.getTextureId();
}
