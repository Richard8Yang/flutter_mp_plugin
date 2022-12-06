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
import java.util.HashMap;

// For each output landmark stream we must assign corresponding callback
  // Refer to the link for available streams: https://github.com/google/mediapipe/blob/master/mediapipe/graphs/holistic_tracking/holistic_tracking_gpu.pbtxt
final class HolisticLandmarksHandler implements PacketCallback {
  private boolean _enabled = true;

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

final class TrackingHolistic implements TrackingType {
  private Map<String, Object> _config;
  private FrameProcessor _processor;

  private final HolisticLandmarksHandler _holisticLandmarksHandler = new HolisticLandmarksHandler();

  TrackingHolistic() {
    _config = new HashMap();
    _config.put("enable_segmentation", false);
    _config.put("refine_face_landmarks", false);
    _config.put("smooth_landmarks", false);
  }

  public String graphName() { return "multi_holistic_tracking_gpu.binarypb"; }
  public String inputStreamName() { return "input_video"; }
  public String outputStreamName() { return "output_video"; }

  public void attachProcessor(FrameProcessor processor) {
    _processor = processor;
    // TODO:
    _processor.addPacketCallback("holistic_landmarks", _holisticLandmarksHandler);
    _holisticLandmarksHandler.enable(true);
  }

  public void enableLandmarkCallbacks(Boolean enable) {
    _holisticLandmarksHandler.enable(enable);
  }

  public Map<String, Object> getLandmarks() {
    // TODO:
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
      if (valType == "Boolean") {
        packet = _processor.getPacketCreator().createBool((Boolean)_config.get(key));
      } else if (valType == "Integer") {
        packet = _processor.getPacketCreator().createInt32((Integer)_config.get(key));
      } else if (valType == "Long") {
        packet = _processor.getPacketCreator().createInt64((Long)_config.get(key));
      } else if (valType == "Double") {
        packet = _processor.getPacketCreator().createFloat64((Double)_config.get(key));
      } else if (valType == "Float") {
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
