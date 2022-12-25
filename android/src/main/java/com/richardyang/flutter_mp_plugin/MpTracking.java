package com.richardyang.flutter_mp_plugin;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import android.opengl.*;
import android.opengl.EGL14.*;

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
  public Map<String, Object> getLandmarks();
  public Map<String, Object> getConfig();
  public Boolean applyConfig(Map<String, Object> config);
}

interface SourceType {
  public Boolean init(String config, Activity activity, ExternalTextureConverter converter);
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

  private EglManager eglManager;
  private EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
  private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;

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

    Log.d("MpTracker", type, null);

    if (type.equals("holistic")) {
      this.tracker = new TrackingHolistic(false, false, false, false, true, false, true, 0);
    } else if (type.equals("face")) {
      // TODO:
    } else if (type.equals("body")) {
      // TODO:
    } else if (type.equals("hand")) {
      // TODO:
    } else {
      throw new RuntimeException("Unsupported tracking type!");
    }

    // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
    // binary graphs.
    AndroidAssetUtil.initializeNativeAssetManager(activity);

    if (!setupEGL(textureEntry.surfaceTexture())) {
      throw new RuntimeException("Failed to setup EGL context");
    }

    eglManager = new EglManager(eglContext);
    processor = new FrameProcessor(activity, eglContext.getNativeHandle(), this.tracker.graphName(),
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
          //tracker.enableLandmarkCallbacks(true);
        }
        @Override
        public void onCancel(Object o) {
          eventSink.setDelegate(null);
          //tracker.enableLandmarkCallbacks(false);
        }
      }
    );
  }

  public Map<String, Object> getConfig() {
    return tracker.getConfig();
  }

  public boolean start(String inputInfo, Map<String, Object> trackingConfig) {
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
    if (source != null) source.dispose();
    if (info[0].equals("camera")) {
      source = new SourceCamera();
      if (!source.init(info[1], this.activity, converter)) {
        source.dispose();
        return false;
      }
      if (source.start()) {
        processor.getVideoSurfaceOutput().setEglSurface(eglSurface.getNativeHandle());
      }
    } else if (info[0].equals("video")) {
      // TODO:
    } else if (info[0].equals("image")) {
      // TODO:
    } else if (info[0].equals("screen")) {
      // TODO:
    }

    return true;
  }

  boolean setupEGL(SurfaceTexture surfaceTexture) {
    surfaceTexture.setDefaultBufferSize(1280, 720);

    EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
      throw new RuntimeException("Unable to get EGL14 display");
      //Log.e("MpTracker", "Can't load EGL display", null);
    }

    final int[] version = new int[2];
    if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
      throw new RuntimeException("eglInitialize failed");
    }

    final int[] attribs = new int[] {
      EGL14.EGL_RENDERABLE_TYPE,
      EGL14.EGL_OPENGL_ES2_BIT,
      EGL14.EGL_RED_SIZE, 8,
      EGL14.EGL_GREEN_SIZE, 8,
      EGL14.EGL_BLUE_SIZE, 8,
      EGL14.EGL_ALPHA_SIZE, 8,
      EGL14.EGL_DEPTH_SIZE, 16,
      EGL14.EGL_STENCIL_SIZE, 8,
      EGL14.EGL_SAMPLE_BUFFERS, 1,
      EGL14.EGL_SAMPLES, 4,
      EGL14.EGL_NONE
    };

    final EGLConfig[] configs = new EGLConfig[1];
    final int[] numConfigs = new int[1];
    if (!EGL14.eglChooseConfig(eglDisplay, attribs, 0, configs, 0, 1, numConfigs, 0)) {
      throw new RuntimeException("EGL choose config failed");
    }

    // Try opengl 3, if failed try opengl 2
    final int[] attributes = new int[] {EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE};
    eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attributes, 0);
    if (eglContext == EGL14.EGL_NO_CONTEXT) {
      attributes[1] = 2;
      eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attributes, 0);
      if (eglContext == EGL14.EGL_NO_CONTEXT) {
        throw new RuntimeException("EGL create context failed");
      }
    }

    if (eglSurface != EGL14.EGL_NO_SURFACE) {
      throw new RuntimeException("EGL already configured surface");
    }
    final int[] surfaceAttribs = new int[] {EGL14.EGL_NONE};
    eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surfaceTexture, surfaceAttribs, 0);
    if (eglSurface == EGL14.EGL_NO_SURFACE) {
      throw new RuntimeException("EGL create window surface failed");
    }

    //if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
    //  throw new RuntimeException("EGL make current failed");
    //}

    return true;
  }
}
