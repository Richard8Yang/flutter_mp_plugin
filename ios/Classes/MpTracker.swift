import Flutter
import UIKit
import AVFoundation

class MpTracker: NSObject, FlutterTexture, AVCaptureVideoDataOutputSampleBufferDelegate, TrackerDelegate {
    var tracker: HolisticTracker?
    var eventChannel: FlutterEventChannel?
    var textureRegistry: FlutterTextureRegistry?
    var textureId: Int64 = -1
    var targetBuffer: CVPixelBuffer?

    init(_ textureRegistry: FlutterTextureRegistry?, messenger: FlutterBinaryMessenger?, options: Dictionary<String, Any>?) {
        super.init()

        if (options != nil) {
            let trackerParams: HolisticTrackerConfig = 
                HolisticTrackerConfig(options!["enableSegmentation", default: false] as! Bool, 
                    enableRefinedFace: options!["refineFaceLandmarks", default: false] as! Bool, 
                    maxPersonsToTrack: Int32(options!["maxPersonsToTrack", default: 0] as! Int),
                    enableFaceLandmarks: options!["enableFaceLandmarks", default: false] as! Bool, 
                    enablePoseLandmarks: options!["enablePoseLandmarks", default: false] as! Bool, 
                    enableLeftHandLandmarks: options!["enableLeftHandLandmarks", default: false] as! Bool, 
                    enableRightHandLandmarks: options!["enableRightHandLandmarks", default: false] as! Bool, 
                    enableHolisticLandmarks: options!["enableHolisticLandmarks", default: false] as! Bool, 
                    enablePoseWorldLandmarks: options!["enablePoseWorldLandmarks", default: false] as! Bool, 
                    enableLandmarksOverlay: options!["enableLandmarksOverlay", default: false] as! Bool,
                    enablePixelBufferOutput: true)
            self.tracker = HolisticTracker(trackerParams)!
        } else {
            let trackerParams: HolisticTrackerConfig = 
                HolisticTrackerConfig(false, 
                    enableRefinedFace: false, 
                    maxPersonsToTrack: 0, 
                    enableFaceLandmarks: false, 
                    enablePoseLandmarks: false, 
                    enableLeftHandLandmarks: false, 
                    enableRightHandLandmarks: false, 
                    enableHolisticLandmarks: false, 
                    enablePoseWorldLandmarks: false, 
                    enableLandmarksOverlay: true,
                    enablePixelBufferOutput: true)
            self.tracker = HolisticTracker(trackerParams)!
        }
        
        self.tracker!.delegate = self

        self.textureRegistry = textureRegistry
        self.textureId = self.textureRegistry!.register(self);
        let eventChannelName: String = "landmarks_" + String(self.textureId)
        self.eventChannel = FlutterEventChannel(name: eventChannelName, binaryMessenger: messenger!)
    }

    public func getTextureId() -> Int64 {
        return self.textureId
    }

    public func start() {
        self.tracker!.startGraph()
    }

    // FlutterTexture interface. Called by textureRegistry.textureFrameAvailable when a new frame is available
    public func copyPixelBuffer() -> Unmanaged<CVPixelBuffer>? {
        var pixelBuffer: CVPixelBuffer? = nil;
        pixelBuffer = self.targetBuffer;
        if(pixelBuffer != nil) {
            return Unmanaged.passRetained(pixelBuffer!);
        } else {
            print("pixelBuffer is nil.... ");
            return nil;
        }
    }

    // AVCaptureVideoDataOutputSampleBufferDelegate interface. Called when new frame from camera/video arrives 
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)
        self.tracker!.processVideoFrame(pixelBuffer)
    }
    
    // TrackerDelegate interface. Called when a new frame from tracker's output arrives
    func holisticTracker(_ holisticTracker: HolisticTracker!, didOutputPixelBuffer pixelBuffer: CVPixelBuffer!) {
        // Holistic tracker delegate output function
        DispatchQueue.main.async {
            // Notify flutter texture to update content
            self.targetBuffer = pixelBuffer
            self.textureRegistry!.textureFrameAvailable(self.textureId)
            //self.imageView.image = UIImage(ciImage: CIImage(cvPixelBuffer: pixelBuffer))
        }
    }
    
    // TrackerDelegate interface.
    func holisticTracker(_ holisticTracker: HolisticTracker!, didOutputLandmarks name: String!, packetData packet: [AnyHashable : Any]!) {
        // Landmarks handling
        name.withCString { nameStr in
            if (0 == memcmp(nameStr, kMultiHolisticStream, strlen(kMultiHolisticStream))) {
                processHolisticLandmarks(packet)
            } else if (0 == memcmp(nameStr, kMultiFaceStream, strlen(kMultiFaceStream)) ||
                       0 == memcmp(nameStr, kMultiPoseStream, strlen(kMultiPoseStream)) ||
                       0 == memcmp(nameStr, kMultiLeftHandStream, strlen(kMultiLeftHandStream)) ||
                       0 == memcmp(nameStr, kMultiRightHandStream, strlen(kMultiRightHandStream)) ||
                       0 == memcmp(nameStr, kMultiPoseWorldStream, strlen(kMultiPoseWorldStream))) {
                processLandmarksByType(packet, landmarkType: name)
            }
        }
    }
    
    func processHolisticLandmarks(_ landmarkData: [AnyHashable : Any]!) {
        // Holistic landmarks, Dict<Dict<Array<Landmark>>>
        //NSLog("==== Got new holistic packet ====")
        for (idx, data) in landmarkData {
            let index = idx as! Int
            let holisticDict = data as! [AnyHashable : Any]
            //NSLog("Holistic landmark #%d count %d", index, holisticDict.count)
            for (lmKey, lmVal) in holisticDict {
                let landmarkType = lmKey as! Int
                let landmarkList = lmVal as! [Landmark]
                //NSLog("#%d landmarks count %d", landmarkType, landmarkList.count)
                for landmark in landmarkList {
                    //NSLog("\t\"%d\": %.6f %.6f %.6f", landmarkType, landmark.x, landmark.y, landmark.z)
                }
            }
        }
    }
    
    func processLandmarksByType(_ landmarkData: [AnyHashable : Any]!, landmarkType: String) {
        // Landmarks of a single component, Dict<Array<Landmark>>
        //NSLog("==== Got new %s packet ====", landmarkType)
        for (idx, data) in landmarkData {
            let index = idx as! Int
            let landmarkList = data as! [Landmark]
            //NSLog("#%d landmarks count %d", index, landmarkList.count)
            for landmark in landmarkList {
                //NSLog("\t\"%d\": %.6f %.6f %.6f", index, landmark.x, landmark.y, landmark.z)
            }
        }
    }
}
