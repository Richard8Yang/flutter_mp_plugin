package com.richardyang.flutter_mp_plugin;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Size;
import android.app.Activity;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.glutil.EglManager;

final class SourceCamera implements SourceType {
  private SurfaceTexture _surfaceTexture;
  private CameraXPreviewHelper _cameraHelper;
  private Activity _activity;
  private CameraHelper.CameraFacing _cameraFacing;
  private Size _resolution;

  // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
  // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
  private ExternalTextureConverter _converter;

  // config string format: "front/high_resolution"
  public Boolean init(String config, Activity activity, SurfaceTexture surfaceTexture, FrameProcessor processor, ExternalTextureConverter converter) {
    PermissionHelper.checkAndRequestCameraPermissions(activity);

    _cameraFacing = CameraHelper.CameraFacing.BACK;
    _resolution = new Size(1280, 720);

    String[] params = config.split("/");
    if (params.length > 0 && params[0] == "front") {
      _cameraFacing = CameraHelper.CameraFacing.FRONT;
    }
    if (params.length > 1 && params[1] == "high_resolution") {
      _resolution = new Size(1920, 1080);
    }

    _converter = converter;
    _surfaceTexture = surfaceTexture;
    _activity = activity;

    _cameraHelper = new CameraXPreviewHelper();
    _cameraHelper.setOnCameraStartedListener(
      surfaceTex -> {
          // Connect the converter to the camera-preview frames as its input (via
          // previewFrameTexture), and configure the output width and height as the computed
          // display size.
          boolean isCameraRotated = _cameraHelper.isCameraRotated();
          _converter.setSurfaceTextureAndAttachToGLContext(surfaceTex,
            isCameraRotated ? _resolution.getHeight() : _resolution.getWidth(),
            isCameraRotated ? _resolution.getWidth() : _resolution.getHeight());
      }
    );
  }

  public Boolean start() {
    _cameraHelper.startCamera(_activity, _cameraFacing, _surfaceTexture, _resolution);
    return true;
  }

  public void pause() {

  }

  public void resume() {

  }

  public void stop() {

  }

  public void dispose() {
    _converter.close();
  }
}
