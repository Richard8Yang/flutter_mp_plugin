enum LandmarkType {
  holistic,
  face,
  pose,
  hand,
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
