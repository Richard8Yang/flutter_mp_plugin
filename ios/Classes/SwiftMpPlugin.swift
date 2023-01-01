import Flutter
import UIKit

@objc public class SwiftMpPlugin: NSObject {
  
  var registry: FlutterTextureRegistry?
  var messenger: FlutterBinaryMessenger?

  var tracker: MpTracker?
  var camera: Camera?

  override init() {}
  
  @objc public func initialize(registrar: FlutterPluginRegistrar, messenger: FlutterBinaryMessenger) {
    self.registry = registrar.textures();
    self.messenger = messenger;
  }

  @objc public func handle(call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "init":
      guard let args = call.arguments as? [String : Any] else {
        result(" arguments error.... ")
        return;
      }

      //let renderToVideo = args["trackingType"] as! Int;
      if (args["options"] != nil) {
        let options = args["options"] as! Dictionary<String, Any>
        self.tracker = MpTracker(self.registry, messenger: self.messenger, options: options)
      } else {
        self.tracker = MpTracker(self.registry, messenger: self.messenger, options: nil)
      }
      
      result(self.tracker!.getTextureId());

    case "start":
      guard let args = call.arguments as? [String : Any] else {
        result("arguments error.... ")
        return;
      }

      let sourceInfo = args["sourceInfo"] as! String
      let srcParams = sourceInfo.components(separatedBy: "::")
      if (srcParams.count > 1) {
        switch srcParams[0] {
          case "camera":
            self.camera = Camera(srcParams[1])
            self.camera!.setSampleBufferDelegate(self.tracker!)
            self.camera!.start()
            self.tracker!.start();
          
          case "video":
            result("Video is not supported yet");

          case "image":
            result("Image is not supported yet");

          case "screen":
            result("Screen capture is not supported yet");

          default:
            result("Unsupported source type: " + srcParams[0]);
        }
      }

      result(true);

    case "stop":
      result(true);

    case "pause":
      result(true);

    case "resume":
      result(true);
   
    case "getTextureId":
      result(nil);

    default:
      result(nil);
    }
  }
}
