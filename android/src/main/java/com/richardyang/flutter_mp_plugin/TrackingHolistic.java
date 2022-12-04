package com.richardyang.flutter_mp_plugin;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.glutil.EglManager;
import com.google.mediapipe.framework.AndroidPacketCreator;

import java.util.Map;
import java.util.HashMap;

final class TrackingHolistic extends PacketCallback implements TrackingType {
  private Boolean _enableLandmarkProcess = false;
  private Map<String, Object> _config;
  FrameProcessor _processor;

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
    _processor.addPacketCallback("pose_landmarks", this);
    _processor.addPacketCallback("face_landmarks", this);
    _enableLandmarkProcess = true;
  }

  public void enableLandmarkCallbacks(Boolean enable) {
    _enableLandmarkProcess = enable;
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
        packet = _processor.getPacketCreator().createBool(_config.get(key));
      } else if (valType == "Integer") {
        packet = _processor.getPacketCreator().createInt32(_config.get(key));
      } else if (valType == "Long") {
        packet = _processor.getPacketCreator().createInt64(_config.get(key));
      } else if (valType == "Double") {
        packet = _processor.getPacketCreator().createFloat64(_config.get(key));
      } else if (valType == "Float") {
        packet = _processor.getPacketCreator().createFloat32(_config.get(key));
      } else {
        // unsupported type
        return false;
      }
      inputSidePackets.put(key, packet);
    }
    _processor.setInputSidePackets(inputSidePackets);
    return true;
  }

  // For each output landmark stream we must assign corresponding callback
  // Refer to the link for available streams: https://github.com/google/mediapipe/blob/master/mediapipe/graphs/holistic_tracking/holistic_tracking_gpu.pbtxt
  @Override
  public void process(Packet packet) {
    if (!_enableLandmarkProcess) return;
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
