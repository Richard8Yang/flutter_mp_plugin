import 'dart:ffi';

enum LandmarkType {
  holistic,
  face,
  pose,
  lefthand,
  righthand,
  poseworld,
  unknown,
}

class LandmarkEvent {
  LandmarkEvent({
    required this.landmarkType,
    this.landmarkList,
    this.timestamp,
  });

  final LandmarkType landmarkType;
  final List? landmarkList;
  final int? timestamp;
}
