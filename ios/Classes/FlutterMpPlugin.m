#import "FlutterMpPlugin.h"

@implementation FlutterMpPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterGlPlugin* instance = [[FlutterGlPlugin alloc] initWithRegistrar:registrar];
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_mp_plugin"
            binaryMessenger:[registrar messenger]];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)initWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  self = [super init];
  NSAssert(self, @"super init cannot be nil");
  _mpPlugin = [[SwiftMpPlugin alloc] initializeWithRegistrar:registrar messenger: [registrar messenger]];
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
