package com.richardyang.flutter_mp_plugin;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Size;
import io.flutter.view.TextureRegistry;
import io.flutter.plugin.common.EventChannel;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.glutil.EglManager;

import java.util.Map;
import java.util.HashMap;

interface TrackingType {
  public String graphName();
  public String inputStreamName();
  public String outputStreamName();
  public void attachProcessor(FrameProcessor processor);
  public void enableLandmarkCallbacks(Boolean enable);
  public Map<String, Object> getLandmarks();
  public Map<String, Object> getConfig();
  public Boolean applyConfig(Map<String, Object> config);
}

interface SourceType {
  public Boolean init(String config, Activity activity, SurfaceTexture surfaceTexture, FrameProcessor processor, ExternalTextureConverter converter);
  public Boolean start();
  public void pause();
  public void resume();
  public void stop();
  public void dispose();
}

final class MpTracking {
  private final TextureRegistry.SurfaceTextureEntry textureEntry;
  private QueuingEventSink eventSink;
  private final EventChannel eventChannel;

  // Flips the camera-preview frames vertically by default, before sending them into FrameProcessor
  // to be processed in a MediaPipe graph, and flips the processed frames back when they are
  // displayed. This maybe needed because OpenGL represents images assuming the image origin is at
  // the bottom-left corner, whereas MediaPipe in general assumes the image origin is at the
  // top-left corner.
  // NOTE: use "flipFramesVertically" in manifest metadata to override this behavior.
  private static final boolean FLIP_FRAMES_VERTICALLY = true;

  static {
    // Load all native libraries needed by the app.
    System.loadLibrary("mediapipe_jni");
    try {
      System.loadLibrary("opencv_java3");
    } catch (UnsatisfiedLinkError e) {
      // Some example apps (e.g. template matching) require OpenCV 4.
      System.loadLibrary("opencv_java4");
    }
  }

  // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
  // frames onto a {@link Surface}.
  protected FrameProcessor processor;
  // Handles camera access via the {@link CameraX} Jetpack support library.
  protected CameraXPreviewHelper cameraHelper;

  // Creates and manages an {@link EGLContext}.
  private EglManager eglManager;

  // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
  // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
  private ExternalTextureConverter converter;

  private TrackingType tracker;
  private SourceType source;
  private Activity activity;

  MpTracking(String type, Context context, Activity activity, EventChannel eventChannel, TextureRegistry.SurfaceTextureEntry textureEntry) {
    this.eventChannel = eventChannel;
    this.textureEntry = textureEntry;
    this.activity = activity;

    if (type == "holistic") {
      this.tracker = new TrackingHolistic();
    } else if (type == "face") {
      // TODO:
    } else if (type == "body") {
      // TODO:
    } else if (type == "hand") {
      // TODO:
    } else {
      throw new Exception("Unsupported tracking type!");
    }

    // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
    // binary graphs.
    AndroidAssetUtil.initializeNativeAssetManager(context);
    eglManager = new EglManager(null);
    processor = new FrameProcessor(
      context, eglManager.getNativeContext(), this.tracker.graphName(),
      this.tracker.inputStreamName(), this.tracker.outputStreamName());
    processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);

    tracker.attachProcessor(processor);

    // Texture converter to convert OES texture to gl texture, used by camera source
    converter = new ExternalTextureConverter(eglManager.getContext());
    converter.setFlipY(FLIP_FRAMES_VERTICALLY);
    converter.setConsumer(processor);

    this.eventSink = new QueuingEventSink();
    this.eventChannel.setStreamHandler(
      new EventChannel.StreamHandler() {
        @Override
        public void onListen(Object o, EventChannel.EventSink sink) {
          eventSink.setDelegate(sink);
          tracker.enableLandmarkCallbacks(true);
        }
        @Override
        public void onCancel(Object o) {
          eventSink.setDelegate(null);
          tracker.enableLandmarkCallbacks(false);
        }
      }
    );
  }

  Boolean start(String inputInfo, Map<String, Object> trackingConfig) {
    if (!tracker.applyConfig(trackingConfig)) {
      return false;
    }

    // source info can be:
    // 1. camera: "camera::front/high_resolution"
    // 2. TODO: video: "video::http://url.to.video" or "video::/sdcard/path/to/video"
    // 3. TODO: image: "image::http://url.to.image" or "image::/sdcard/path/to/image"
    // 4. TODO: screen capture "screen::region/0.0.1080.1920"
    String[] info = inputInfo.split("::", 2);
    if (info.length < 2) return false;
    source.dispose();
    if (info[0] == "camera") {
      source = new SourceCamera();
      if (!source.init(info[1], this.activity, this.textureEntry.surfaceTexture(), processor, converter)) {
        source.dispose();
        return false;
      }
      source.start();
    } else if (info[0] == "video") {
      // TODO:
    } else if (info[0] == "image") {
      // TODO:
    } else if (info[0] == "screen") {
      // TODO:
    }

    return true;
  }
}
