package com.richardyang.flutter_mp_plugin;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.view.TextureRegistry;
import java.util.Map;
import android.util.LongSparseArray;
import android.content.Context;
import android.app.Activity;

/** FlutterMpPlugin */
public class FlutterMpPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel _channel;
  private Context _applicationContext;
  private BinaryMessenger _binaryMessenger;
  private TextureRegistry _textureRegistry;
  private Activity _activity;
  private MpTracking _tracker;
  private long _textureId;
  //private final LongSparseArray<MpTracking> _trackers = new LongSparseArray<>();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    _binaryMessenger = flutterPluginBinding.getBinaryMessenger();
    _textureRegistry = flutterPluginBinding.getTextureRegistry();
    _applicationContext = flutterPluginBinding.getApplicationContext();
    _channel = new MethodChannel(_binaryMessenger, "flutter_mp_plugin");
    _channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("init")) {
      TextureRegistry.SurfaceTextureEntry handle = _textureRegistry.createSurfaceTexture();
      EventChannel eventChannel = new EventChannel(_binaryMessenger, "landmarks_" + handle.id());
      try {
        _tracker = new MpTracking(call.argument("trackingType"), _applicationContext,
          _activity, eventChannel, handle, call.argument("options"));
        _textureId = handle.id();
        result.success(handle.id());
      } catch (RuntimeException e) {
        result.error("Failed to create tracker", e.getMessage(), null);
      }
    } else if (call.method.equals("start")) {
      String sourceInfo = call.argument("sourceInfo");
      if (_tracker.start(sourceInfo))
        result.success(true);
      else
        result.success(false);
    } else if (call.method.equals("pause")) {

    } else if (call.method.equals("resume")) {

    } else if (call.method.equals("stop")) {

    } else if (call.method.equals("getTextureId")) {
      result.success(_textureId);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    _channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity​(@NonNull ActivityPluginBinding binding) {
    this._activity = binding.getActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges​(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity​(binding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
      // TODO: the Activity your plugin was attached to was
      // destroyed to change configuration.
      // This call will be followed by onReattachedToActivityForConfigChanges().
  }

  @Override
  public void onDetachedFromActivity() {
      // TODO: your plugin is no longer associated with an Activity.
      // Clean up references.
      _activity = null;
  }
}
