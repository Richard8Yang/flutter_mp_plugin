import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_mp_plugin/flutter_mp_plugin_method_channel.dart';

void main() {
  MethodChannelFlutterMpPlugin platform = MethodChannelFlutterMpPlugin();
  const MethodChannel channel = MethodChannel('flutter_mp_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
