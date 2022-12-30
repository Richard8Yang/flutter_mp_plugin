import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_mp_plugin/flutter_mp_plugin.dart';

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

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  startCameraTracking() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      _textureId = await _flutterMpPlugin.init(trackingType: "holistic");
      print("Initialized tracker $_textureId");
      if (_textureId >= 0) {
        Future.delayed(const Duration(milliseconds: 100), () async {
          bool succ = await _flutterMpPlugin.start(
            sourceInfo: "camera::front/medium_resolution",
          );
          if (!succ) {
            print("Failed to start the tracker!");
          }
        });
      }
    } catch (e) {
      print(e);
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
          //child: Text('Running on: $_platformVersion\n'),
          child: Texture(
            textureId: _textureId,
            filterQuality: FilterQuality.medium,
          ),
        ),
      ),
    );
  }
}
