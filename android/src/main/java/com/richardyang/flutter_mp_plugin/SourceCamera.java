package com.richardyang.flutter_mp_plugin;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import com.google.mediapipe.components.CameraHelper;
//import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.glutil.EglManager;

import java.util.Map;
import java.util.HashMap;

final class SourceCamera implements SourceType {
  private SurfaceTexture _surfaceTexture;
  private CameraXPreviewHelper cameraHelper;

  // config string format: "front/high_resolution"
  public Boolean init(String config, Activity activity, SurfaceTexture surfaceTexture) {
    PermissionHelper.checkAndRequestCameraPermissions(activity);
    String[] params = config.split("/");
    if (params.length > 0) {}
    cameraHelper = new CameraXPreviewHelper();
    cameraHelper.setOnCameraStartedListener(
            surfaceTexture -> {
                onCameraStarted(surfaceTexture);
            });
    CameraHelper.CameraFacing cameraFacing =
            applicationInfo.metaData.getBoolean("cameraFacingFront", false)
                    ? CameraHelper.CameraFacing.FRONT
                    : CameraHelper.CameraFacing.BACK;
    cameraHelper.startCamera(
            this, cameraFacing, /*surfaceTexture=*/ null, cameraTargetResolution());
  }

  public Boolean start() {

  }

  public void pause() {

  }

  public void resume() {

  }

  public void stop() {

  }

  public void dispose() {

  }
}
