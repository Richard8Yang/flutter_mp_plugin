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
import com.google.mediapipe.framework.PacketCallback;
import com.google.mediapipe.framework.AndroidPacketCreator;

import java.util.Map;
import java.security.Principal;
import java.util.HashMap;

class LandmarksHandler implements PacketCallback {
  protected boolean _enabled = true;

  public void enable(boolean enabled) { _enabled = enabled; }

  @Override
  public void process(Packet packet) {
    if (!_enabled) return;
    /*
    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
    try {
      NormalizedLandmarkList landmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
      if (landmarks == null) {
          Log.d(TAG, "[TS:" + packet.getTimestamp() + "] No hand landmarks.");
          return;
      }
      // Note: If hand_presence is false, these landmarks are useless.
      Log.d(TAG,
              "[TS:"
                      + packet.getTimestamp()
                      + "] #Landmarks for hand: "
                      + landmarks.getLandmarkCount());
      Log.d(TAG, getLandmarksDebugString(landmarks));
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Couldn't Exception received - " + e);
      return;
    }
    */
  }
}

// For each output landmark stream we must assign corresponding callback
// Refer to the link for available streams: https://github.com/google/mediapipe/blob/master/mediapipe/graphs/holistic_tracking/holistic_tracking_gpu.pbtxt
final class HolisticLandmarksHandler extends LandmarksHandler {
  @Override
  public void process(Packet packet) {
    if (!_enabled) return;
    Log.d("Holistic", "Received holistic landmarks packet");
    /*
    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
    try {
      NormalizedLandmarkList landmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
      if (landmarks == null) {
          Log.d(TAG, "[TS:" + packet.getTimestamp() + "] No hand landmarks.");
          return;
      }
      // Note: If hand_presence is false, these landmarks are useless.
      Log.d(TAG,
              "[TS:"
                      + packet.getTimestamp()
                      + "] #Landmarks for hand: "
                      + landmarks.getLandmarkCount());
      Log.d(TAG, getLandmarksDebugString(landmarks));
    } catch (InvalidProtocolBufferException e) {
      Log.e(TAG, "Couldn't Exception received - " + e);
      return;
    }
    */
  }
}

final class TrackingHolistic implements TrackingType {
  private Map<String, Object> _config;
  private FrameProcessor _processor;
  private Map<String, LandmarksHandler> _landmarksHandlers;

  private boolean _enableFaceLandmarks;
  private boolean _enablePoseLandmarks;
  private boolean _enableLeftHandLandmarks;
  private boolean _enableRightHandLandmarks;
  private boolean _enableHolisticLandmarks;
  private boolean _enablePoseWorldLandmarks;
  private boolean _enableVideoFrameOutput;
  private int _maxPersonsToTrack;

  TrackingHolistic(boolean enableFaceLandmarks,
                   boolean enablePoseLandmarks,
                   boolean enableLeftHandLandmarks,
                   boolean enableRightHandLandmarks,
                   boolean enableHolisticLandmarks,
                   boolean enablePoseWorldLandmarks,
                   boolean enableVideoFrameOutput,
                   int maxPersonsToTrack) {
    _config = new HashMap<>();
    _config.put("enable_segmentation", false);
    _config.put("refine_face_landmarks", true);
    _config.put("smooth_landmarks", false);

    _landmarksHandlers = new HashMap<>();

    _enableFaceLandmarks = enableFaceLandmarks;
    _enablePoseLandmarks = enablePoseLandmarks;
    _enableLeftHandLandmarks = enableLeftHandLandmarks;
    _enableRightHandLandmarks = enableRightHandLandmarks;
    _enableHolisticLandmarks = enableHolisticLandmarks;
    _enablePoseWorldLandmarks = enablePoseWorldLandmarks;
    _enableVideoFrameOutput = enableVideoFrameOutput;
    _maxPersonsToTrack = maxPersonsToTrack;
  }

  public String graphName() { return "multi_holistic_tracking_gpu.binarypb"; }
  public String inputStreamName() { return "input_video"; }
  public String outputStreamName() { return "output_video"; }

  public void attachProcessor(FrameProcessor processor) {
    _processor = processor;

    // vector<vector<mediapipe::NormalizedLandmarkList>> in the order of < face->pose->lefthand->righthand > landmarks
    _landmarksHandlers.put("multi_holistic_landmarks_array", new HolisticLandmarksHandler());
    _processor.addPacketCallback("multi_holistic_landmarks_array", _landmarksHandlers.get("multi_holistic_landmarks_array"));
    _landmarksHandlers.get("multi_holistic_landmarks_array").enable(_enableHolisticLandmarks);

    // vector<mediapipe::NormalizedLandmarkList>
    final String landmarkTypes[] = {
      "multi_face_landmarks",
      "multi_pose_landmarks",
      "multi_left_hand_landmarks",
      "multi_right_hand_landmarks",
      "multi_pose_world_landmarks"
    };
    final boolean landmarkEnabled[] = {
      _enableFaceLandmarks,
      _enablePoseLandmarks,
      _enableLeftHandLandmarks,
      _enableRightHandLandmarks,
      _enablePoseWorldLandmarks
    };
    for (int i = 0; i < landmarkTypes.length; ++i) {
      _landmarksHandlers.put(landmarkTypes[i], new LandmarksHandler());
      _processor.addPacketCallback(landmarkTypes[i], _landmarksHandlers.get(landmarkTypes[i]));
      _landmarksHandlers.get(landmarkTypes[i]).enable(landmarkEnabled[i]);
    }
  }

  public Map<String, Object> getLandmarks() {
    // TODO:
    return new HashMap<>();
  }

  public Map<String, Object> getConfig() {
    return _config;
  }

  public Boolean applyConfig(Map<String, Object> config) {
    _config = config;
    Map<String, Packet> inputSidePackets = new HashMap<>();
    for (String key : _config.keySet()) {
      final String valType = _config.get(key).getClass().getSimpleName();
      final Packet packet;
      if (valType.equals("Boolean")) {
        packet = _processor.getPacketCreator().createBool((Boolean)_config.get(key));
      } else if (valType.equals("Integer")) {
        packet = _processor.getPacketCreator().createInt32((Integer)_config.get(key));
      } else if (valType.equals("Long")) {
        packet = _processor.getPacketCreator().createInt64((Long)_config.get(key));
      } else if (valType.equals("Double")) {
        packet = _processor.getPacketCreator().createFloat64((Double)_config.get(key));
      } else if (valType.equals("Float")) {
        packet = _processor.getPacketCreator().createFloat32((Float)_config.get(key));
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
