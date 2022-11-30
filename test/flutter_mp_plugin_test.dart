import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_mp_plugin/flutter_mp_plugin.dart';
import 'package:flutter_mp_plugin/flutter_mp_plugin_platform_interface.dart';
import 'package:flutter_mp_plugin/flutter_mp_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterMpPluginPlatform
    with MockPlatformInterfaceMixin
    implements FlutterMpPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterMpPluginPlatform initialPlatform = FlutterMpPluginPlatform.instance;

  test('$MethodChannelFlutterMpPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterMpPlugin>());
  });

  test('getPlatformVersion', () async {
    FlutterMpPlugin flutterMpPlugin = FlutterMpPlugin();
    MockFlutterMpPluginPlatform fakePlatform = MockFlutterMpPluginPlatform();
    FlutterMpPluginPlatform.instance = fakePlatform;

    expect(await flutterMpPlugin.getPlatformVersion(), '42');
  });
}
