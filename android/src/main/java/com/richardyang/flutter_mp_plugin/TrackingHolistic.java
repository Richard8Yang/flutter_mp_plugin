package com.richardyang.flutter_mp_plugin;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Size;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.framework.PacketCallback;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.formats.proto.LandmarkProto.Landmark;
import com.google.mediapipe.formats.proto.LandmarkProto.LandmarkList;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;

import java.util.Map;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class LandmarksHandler implements PacketCallback {
  protected boolean _enabled = true;
  protected QueuingEventSink _eventSink = null;

  public void enable(boolean enabled) { _enabled = enabled; }
  public void setEventSink(QueuingEventSink eventSink) { _eventSink = eventSink; }

  @Override
  public void process(Packet packet) {
    if (!_enabled) return;

    try {
      // vector<mediapipe::NormalizedLandmarkList>
      List<NormalizedLandmarkList> arrayLandmarks = PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());
      for (NormalizedLandmarkList landmarks : arrayLandmarks) {
        for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
          //Log.d("Holistic", " \"" + k + "\": " + landmark.getX() + " " + landmark.getY() + " " + landmark.getZ());
        }
      }
    } catch (Exception e) {
      Log.e("LM", "Couldn't parse landmarks packet, error: " + e);
      return;
    }
  }
}

final class WorldLandmarksHandler extends LandmarksHandler {
  @Override
  public void process(Packet packet) {
    if (!_enabled) return;

    try {
      // vector<mediapipe::NormalizedLandmarkList>
      List<LandmarkList> arrayLandmarks = PacketGetter.getProtoVector(packet, LandmarkList.parser());
      for (LandmarkList landmarks : arrayLandmarks) {
        for (Landmark landmark : landmarks.getLandmarkList()) {
          //Log.d("Holistic", " \"" + k + "\": " + landmark.getX() + " " + landmark.getY() + " " + landmark.getZ());
        }
      }
    } catch (Exception e) {
      Log.e("WorldLM", "Couldn't parse landmarks packet, error: " + e);
      return;
    }
  }
}

// For each output landmark stream we must assign corresponding callback
// Refer to the link for available streams: https://github.com/google/mediapipe/blob/master/mediapipe/graphs/holistic_tracking/holistic_tracking_gpu.pbtxt
final class HolisticLandmarksHandler extends LandmarksHandler {
  static List<String> HOLISTIC_COMPONENTS = Arrays.asList("face", "pose", "lefthand", "righthand");

  @Override
  public void process(Packet packet) {
    if (!_enabled) return;

    try {
      List<Object> landmarksArray = new ArrayList<>();
      // vector<vector<mediapipe::NormalizedLandmarkList>> in the order of < face->pose->lefthand->righthand > landmarks
      List<List<NormalizedLandmarkList>> multiInstLandmarks = PacketGetter.getProtoVectorVector(packet, NormalizedLandmarkList.parser());
      for (List<NormalizedLandmarkList> oneInstLandmarks : multiInstLandmarks) {
        Map<String, Object> holisticComponents = new HashMap<>();
        for (int i = 0; i < oneInstLandmarks.size(); i++) {
          List<Float> flattenedCoords = new ArrayList<Float>();
          NormalizedLandmarkList landmarks = oneInstLandmarks.get(i);
          for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
            flattenedCoords.add(landmark.getX());
            flattenedCoords.add(landmark.getY());
            flattenedCoords.add(landmark.getZ());
          }
          holisticComponents.put(HOLISTIC_COMPONENTS.get(i), flattenedCoords);
        }
        landmarksArray.add(holisticComponents);
      }
      Map<String, Object> event = new HashMap<>();
      event.put("type", "holistic");
      event.put("landmarks", landmarksArray);
      _eventSink.success(event);
    } catch (Exception e) {
      Log.e("Holistic", "Couldn't parse landmarks packet, error: " + e);
      return;
    }
  }
}

final class HolisticTrackingOptions {
  private boolean enableSegmentation = false;
  private boolean refineFaceLandmarks = false;
  private boolean enableFaceLandmarks = false;
  private boolean enablePoseLandmarks = false;
  private boolean enableLeftHandLandmarks = false;
  private boolean enableRightHandLandmarks = false;
  private boolean enableHolisticLandmarks = false;
  private boolean enablePoseWorldLandmarks = false;
  private boolean enableLandmarksOverlay = true;
  //private boolean enableVideoFrameOutput = true;
  private int maxPersonsToTrack = 0;

  public boolean enableSegmentation() {
    return enableSegmentation;
  }
  public HolisticTrackingOptions enableSegmentation(boolean enabled) {
    enableSegmentation = enabled;
    return this;
  }

  public boolean refineFaceLandmarks() {
    return refineFaceLandmarks;
  }
  public HolisticTrackingOptions refineFaceLandmarks(boolean enabled) {
    refineFaceLandmarks = enabled;
    return this;
  }

  public boolean enableFaceLandmarks() {
    return enableFaceLandmarks;
  }
  public HolisticTrackingOptions enableFaceLandmarks(boolean enabled) {
    enableFaceLandmarks = enabled;
    return this;
  }

  public boolean enablePoseLandmarks() {
    return enablePoseLandmarks;
  }
  public HolisticTrackingOptions enablePoseLandmarks(boolean enabled) {
    enablePoseLandmarks = enabled;
    return this;
  }

  public boolean enableLeftHandLandmarks() {
    return enableLeftHandLandmarks;
  }
  public HolisticTrackingOptions enableLeftHandLandmarks(boolean enabled) {
    enableLeftHandLandmarks = enabled;
    return this;
  }

  public boolean enableRightHandLandmarks() {
    return enableRightHandLandmarks;
  }
  public HolisticTrackingOptions enableRightHandLandmarks(boolean enabled) {
    enableRightHandLandmarks = enabled;
    return this;
  }

  public boolean enableHolisticLandmarks() {
    return enableHolisticLandmarks;
  }
  public HolisticTrackingOptions enableHolisticLandmarks(boolean enabled) {
    enableHolisticLandmarks = enabled;
    return this;
  }

  public boolean enablePoseWorldLandmarks() {
    return enablePoseWorldLandmarks;
  }
  public HolisticTrackingOptions enablePoseWorldLandmarks(boolean enabled) {
    enablePoseWorldLandmarks = enabled;
    return this;
  }

  public boolean enableLandmarksOverlay() {
    return enableLandmarksOverlay;
  }
  public HolisticTrackingOptions enableLandmarksOverlay(boolean enabled) {
    enableLandmarksOverlay = enabled;
    return this;
  }

  public int maxPersonsToTrack() {
    return maxPersonsToTrack;
  }
  public HolisticTrackingOptions maxPersonsToTrack(int num) {
    maxPersonsToTrack = num;
    return this;
  }
}

final class TrackingHolistic implements TrackingType {
  private HolisticTrackingOptions _options;
  private FrameProcessor _processor;
  private Map<String, LandmarksHandler> _landmarksHandlers;
  private QueuingEventSink _eventSink;

  TrackingHolistic(HolisticTrackingOptions options, QueuingEventSink eventSink) {
    _options = options;
    _eventSink = eventSink;
    _landmarksHandlers = new HashMap<>();
  }

  public String graphName() { return "multi_holistic_tracking_gpu.binarypb"; }
  public String inputStreamName() { return "input_video"; }
  public String outputStreamName() { return "output_video"; }

  public void attachProcessor(FrameProcessor processor) {
    _processor = processor;

    // vector<vector<mediapipe::NormalizedLandmarkList>> in the order of < face->pose->lefthand->righthand > landmarks
    _landmarksHandlers.put("multi_holistic_landmarks_array", new HolisticLandmarksHandler());
    _processor.addPacketCallback("multi_holistic_landmarks_array", _landmarksHandlers.get("multi_holistic_landmarks_array"));
    _landmarksHandlers.get("multi_holistic_landmarks_array").enable(_options.enableHolisticLandmarks());
    _landmarksHandlers.get("multi_holistic_landmarks_array").setEventSink(_eventSink);

    if (!_options.enableHolisticLandmarks()) {
      // vector<mediapipe::NormalizedLandmarkList>
      final String landmarkTypes[] = {
        "multi_face_landmarks",
        "multi_pose_landmarks",
        "multi_left_hand_landmarks",
        "multi_right_hand_landmarks",
        //"multi_pose_world_landmarks"
      };
      final boolean landmarkEnabled[] = {
        _options.enableFaceLandmarks(),
        _options.enablePoseLandmarks(),
        _options.enableLeftHandLandmarks(),
        _options.enableRightHandLandmarks(),
        //_options.enablePoseWorldLandmarks()
      };
      for (int i = 0; i < landmarkTypes.length; ++i) {
        _landmarksHandlers.put(landmarkTypes[i], new LandmarksHandler());
        _processor.addPacketCallback(landmarkTypes[i], _landmarksHandlers.get(landmarkTypes[i]));
        _landmarksHandlers.get(landmarkTypes[i]).enable(landmarkEnabled[i]);
        _landmarksHandlers.get(landmarkTypes[i]).setEventSink(_eventSink);
      }
    }
  }

  public Map<String, Object> getLandmarks() {
    // TODO:
    return new HashMap<>();
  }

  public Boolean applyConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("enable_segmentation", _options.enableSegmentation());
    config.put("refine_face_landmarks", _options.refineFaceLandmarks());
    config.put("enable_face_landmarks", _options.enableFaceLandmarks());
    config.put("enable_hands_landmarks", _options.enableLeftHandLandmarks() || _options.enableRightHandLandmarks());
    config.put("enable_landmark_overlay", _options.enableLandmarksOverlay());
    config.put("num_poses", _options.maxPersonsToTrack());
    config.put("smooth_landmarks", false);
    Map<String, Packet> inputSidePackets = new HashMap<>();
    for (String key : config.keySet()) {
      final String valType = config.get(key).getClass().getSimpleName();
      final Packet packet;
      if (valType.equals("Boolean")) {
        packet = _processor.getPacketCreator().createBool((Boolean)config.get(key));
      } else if (valType.equals("Integer")) {
        packet = _processor.getPacketCreator().createInt32((Integer)config.get(key));
      } else if (valType.equals("Long")) {
        packet = _processor.getPacketCreator().createInt64((Long)config.get(key));
      } else if (valType.equals("Double")) {
        packet = _processor.getPacketCreator().createFloat64((Double)config.get(key));
      } else if (valType.equals("Float")) {
        packet = _processor.getPacketCreator().createFloat32((Float)config.get(key));
      } else {
        // unsupported type
        return false;
      }
      inputSidePackets.put(key, packet);
    }
    _processor.setInputSidePackets(inputSidePackets);
    return true;
  }
}
