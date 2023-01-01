#import "FlutterMpPlugin.h"

#if __has_include(<flutter_mp_plugin/flutter_mp_plugin-Swift.h>)
#import <flutter_mp_plugin/flutter_mp_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_mp_plugin-Swift.h"
#endif

@implementation FlutterMpPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMpPlugin* instance = [[FlutterMpPlugin alloc] initWithRegistrar:registrar];
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_mp_plugin"
            binaryMessenger:[registrar messenger]];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  self = [super init];
  NSAssert(self, @"super init cannot be nil");
  _mpPlugin = [SwiftMpPlugin alloc];
  [_mpPlugin initializeWithRegistrar:registrar messenger: [registrar messenger]];
  return self;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
    [_mpPlugin handleWithCall:call result:result];
  }
}

@end
