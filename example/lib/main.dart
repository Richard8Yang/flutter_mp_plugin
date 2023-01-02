import 'dart:ffi';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_mp_plugin/flutter_mp_plugin.dart';
import 'package:flutter_mp_plugin/landmark_event.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  int _textureId = 0;

  final _flutterMpPlugin = FlutterMpPlugin();
  final _landmarksSubscriber = LandmarkEventSubscriber();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  startCameraTracking() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      _textureId =
          await _flutterMpPlugin.init(trackingType: "holistic", options: {
        "enableHolisticLandmarks": true,
        "refineFaceLandmarks": true,
        "enableFaceLandmarks": true,
        "enablePoseLandmarks": false,
        "enableLeftHandLandmarks": false,
        "enableRightHandLandmarks": false,
        "enableLandmarksOverlay": true,
      });
      print("Initialized tracker $_textureId");
      if (_textureId >= 0) {
        Future.delayed(const Duration(milliseconds: 100), () async {
          bool succ = await _flutterMpPlugin.start(
            sourceInfo: "camera::front/medium_resolution",
          );
          if (succ) {
            _landmarksSubscriber.subscribe(
              textureId: _textureId,
              onEvent: handleLandmarkEvent,
            );
          } else {
            print("Failed to start the tracker!");
          }
        });
      }
    } catch (e) {
      print(e);
    }
  }

  void handleLandmarkEvent(LandmarkEvent event) {
    switch (event.landmarkType) {
      case LandmarkType.holistic:
        // List<Map<String, List>>
        print("==== Got new holistic packet ====");
        int index = 0;
        for (final element in event.landmarkList!) {
          //final oneHolistic = element as Map<String, List>;
          print("Holistic landmark #$index count ${element.length}");
          element.forEach((type, list) {
            final int count = list.length ~/ 3;
            print("$type landmarks count $count");
            for (int i = 0; i < list.length; i += 3) {
              final x = list[i + 0];
              final y = list[i + 1];
              final z = list[i + 2];
              print(" \"$type\": $x $y $z");
            }
          });
          index++;
        }
        break;

      default:
        break;
    }
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    await startCameraTracking();

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    animate();
  }

  animate() {
    setState(() {});
    Future.delayed(const Duration(milliseconds: 33), () {
      animate();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Texture(
            textureId: _textureId,
            filterQuality: FilterQuality.medium,
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _landmarksSubscriber.unsubscribe();
    //_flutterMpPlugin.dispose();
    super.dispose();
  }
}
