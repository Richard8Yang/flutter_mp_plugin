import Flutter
import UIKit

@objc public class SwiftMpPlugin: NSObject {
  
  var registry: FlutterTextureRegistry?
  var messenger: FlutterBinaryMessenger?
  var textureId: Int64 = -1

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
        self.tracker = MpTracker(options)
      } else {
        self.tracker = MpTracker(nil)
      }
      self.textureId = self.registry!.register(self.tracker!);
      let eventChannelName: String = "landmarks_" + String(self.textureId)
      let eventChannel = FlutterEventChannel(name: eventChannelName, binaryMessenger: self.messenger!)
      self.tracker!.setEventChannel(eventChannel)
      
      result(self.textureId);

    case "start":
      guard let args = call.arguments as? [String : Any] else {
        result(" arguments error.... ")
        return;
      }

      let sourceInfo = args["sourceInfo"] as! String
      self.camera = Camera(sourceInfo)
      self.camera!.setSampleBufferDelegate(self.tracker!)
      self.camera!.start()
      self.tracker!.start()

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
