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
    this.landmarksList,
    this.landmarksVisibility,
    this.timestamp,
  });

  final LandmarkType landmarkType;
  final List? landmarksList;
  final List? landmarksVisibility;
  final int? timestamp;
}
