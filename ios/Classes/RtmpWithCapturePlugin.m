#import "RtmpWithCapturePlugin.h"
#if __has_include(<rtmp_with_capture/rtmp_with_capture-Swift.h>)
#import <rtmp_with_capture/rtmp_with_capture-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "rtmp_with_capture-Swift.h"
#endif

@implementation RtmpWithCapturePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftRtmpWithCapturePlugin registerWithRegistrar:registrar];
}
@end
