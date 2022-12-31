#import <Flutter/Flutter.h>

@class SwiftMpPlugin;

@interface FlutterMpPlugin : NSObject<FlutterPlugin>

@property(nonatomic, readonly) SwiftMpPlugin* mpPlugin;

@end
