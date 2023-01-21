import 'dart:async';

import 'package:flutter/services.dart';

import 'flutter_mp_plugin_platform_interface.dart';
import 'landmark_event.dart';

final FlutterMpPluginPlatform _mpPluginPlatform =
    FlutterMpPluginPlatform.instance;

class FlutterMpPlugin {
  Future<String?> getPlatformVersion() {
    return FlutterMpPluginPlatform.instance.getPlatformVersion();
  }

  Future<int> init(
          {String trackingType = "holistic", Map<String, dynamic>? options}) =>
      _mpPluginPlatform.init(trackingType: trackingType, options: options);

  // source info can be:
  // 1. camera: "camera::front/high_resolution"
  // 2. TODO: video: "video::http://url.to.video" or "video::/sdcard/path/to/video"
  // 3. TODO: image: "image::http://url.to.image" or "image::/sdcard/path/to/image"
  // 4. TODO: screen capture "screen::region/0.0.1080.1920"
  Future<bool> start({required String sourceInfo}) {
    return _mpPluginPlatform.start(sourceInfo);
  }

  Future<void> pause() => _mpPluginPlatform.pause();

  Future<void> resume() => _mpPluginPlatform.resume();

  Future<void> stop() => _mpPluginPlatform.stop();

  // Surface texture id used by Texture widget
  Future<int?> getTextureId() => _mpPluginPlatform.getTextureId();
}

class LandmarkEventSubscriber {
  StreamSubscription<LandmarkEvent>? _subscriber;

  bool subscribe({
    required int textureId,
    required Function(LandmarkEvent) onEvent,
    Function? onError,
  }) {
    if (textureId < 0) return false;
    print("Start listening on channel: landmarks_$textureId");
    _subscriber = EventChannel("landmarks_$textureId")
        .receiveBroadcastStream()
        .map(landmarksToEvent)
        .listen(onEvent, onError: onError);
    return _subscriber != null;
  }

  void unsubscribe() async {
    if (_subscriber != null) {
      await _subscriber!.cancel();
    }
  }

  LandmarkEvent landmarksToEvent(dynamic event) {
    final typeMap = {
      'multi_holistic_landmarks_array': LandmarkType.holistic,
      'multi_face_landmarks': LandmarkType.face,
      'multi_pose_landmarks': LandmarkType.pose,
      'multi_left_hand_landmarks': LandmarkType.lefthand,
      'multi_right_hand_landmarks': LandmarkType.righthand,
      'multi_pose_world_landmarks': LandmarkType.poseworld,
    };
    if (typeMap.containsKey(event['type'])) {
      return LandmarkEvent(
        landmarkType: typeMap[event['type']]!,
        landmarkList: event['landmarks'],
        timestamp: event['timestamp'],
      );
    } else {
      return LandmarkEvent(landmarkType: LandmarkType.unknown);
    }
  }
}
