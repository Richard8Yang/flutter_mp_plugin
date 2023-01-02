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
  });

  final LandmarkType landmarkType;
  final List? landmarkList;
}
