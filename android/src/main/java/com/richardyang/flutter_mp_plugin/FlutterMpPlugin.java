package com.richardyang.flutter_mp_plugin;

import androidx.annotation.NonNull;

import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.view.TextureRegistry;
import java.util.Map;
import android.util.LongSparseArray;
import android.content.Context;

/** FlutterMpPlugin */
public class FlutterMpPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel _channel;
  private FlutterState _flutterState;
  private Activity _activity;
  private final LongSparseArray<MpTracking> _trackers = new LongSparseArray<>();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    BinaryMessenger binaryMessenger = flutterPluginBinding.getBinaryMessenger();
    Context applicationContext = flutterPluginBinding.getApplicationContext();
    final FlutterInjector injector = FlutterInjector.instance();
    _channel = new MethodChannel(binaryMessenger, "flutter_mp_plugin");
    _channel.setMethodCallHandler(this);
    _flutterState = new FlutterState(
      applicationContext,
      binaryMessenger,
      injector.flutterLoader()::getLookupKeyForAsset,
      injector.flutterLoader()::getLookupKeyForAsset,
      flutterPluginBinding.getTextureRegistry());
    _flutterState.startListening(this, binaryMessenger);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("init")) {
      TextureRegistry.SurfaceTextureEntry handle =
        _flutterState.textureRegistry.createSurfaceTexture();
      EventChannel eventChannel =
        new EventChannel(_flutterState.binaryMessenger, "landmarks_" + handle.id());
      try {
        MpTracking tracker = new MpTracking(
          call.arguments("trackingType"),
          _flutterState.applicationContext,
          _activity, eventChannel, handle);
        _trackers.put(handle.id(), tracker);
        result.success(handle.id());
      } catch (String e) {
        result.error("Unsupported tracking type!");
      }
    } else if (call.method.equals("getConfig")) {

    } else if (call.method.equals("start")) {

    } else if (call.method.equals("pause")) {

    } else if (call.method.equals("resume")) {

    } else if (call.method.equals("stop")) {

    } else if (call.method.equals("getTextureId")) {

    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    _channel.setMethodCallHandler(null);
    _flutterState.stopListening(binding.getBinaryMessenger());
    _flutterState = null;
  }

  @Override
  void onAttachedToActivity​(@NonNull ActivityPluginBinding binding) {
    this._activity = binding.getActivity();
  }

  @Override
  void onReattachedToActivityForConfigChanges​(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity​(binding);
  }
}
