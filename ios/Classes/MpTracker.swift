import Flutter
import UIKit
import AVFoundation

class MpTracker: NSObject, FlutterTexture, AVCaptureVideoDataOutputSampleBufferDelegate, TrackerDelegate {
    var tracker: HolisticTracker?
    var eventChannel: FlutterEventChannel?

    init(_ options: Dictionary<String, Any>?) {
        let trackerParams: HolisticTrackerConfig = 
            HolisticTrackerConfig(false, 
                enableRefinedFace: true, 
                maxPersonsToTrack: 0, 
                enableFaceLandmarks: false, 
                enablePoseLandmarks: false, 
                enableLeftHandLandmarks: false, 
                enableRightHandLandmarks: false, 
                enableHolisticLandmarks: true, 
                enablePoseWorldLandmarks: false, 
                enablePixelBufferOutput: true)
        self.tracker = HolisticTracker(trackerParams)!
        self.tracker!.delegate = self
    }

    public func setEventChannel(_ eventChannel: FlutterEventChannel) {
        self.eventChannel = eventChannel;
    }

    public func start() {
        self.tracker!.startGraph()
    }

    // FlutterTexture interface. Called by textureRegistry.textureFrameAvailable when a new frame is available
    public func copyPixelBuffer() -> Unmanaged<CVPixelBuffer>? {
        let outputItemTime = playerItemVideoOutput.itemTime(forHostTime: CACurrentMediaTime())
        guard playerItemVideoOutput.hasNewPixelBuffer(forItemTime: outputItemTime),
              let buffer = playerItemVideoOutput.copyPixelBuffer(forItemTime: outputItemTime, itemTimeForDisplay: nil) else {
            return nil
        }
        return Unmanaged<CVPixelBuffer>.passRetained(buffer)
    }

    // AVCaptureVideoDataOutputSampleBufferDelegate interface. Called when new frame from camera/video arrives 
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)
        self.tracker!.processVideoFrame(pixelBuffer)
    }
    
    // TrackerDelegate interface.
    func holisticTracker(_ holisticTracker: HolisticTracker!, didOutputPixelBuffer pixelBuffer: CVPixelBuffer!) {
        // Holistic tracker delegate output function
        DispatchQueue.main.async {
            // TODO: notify flutter texture to update content
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
        NSLog("==== Got new holistic packet ====")
        for (idx, data) in landmarkData {
            let index = idx as! Int
            let holisticDict = data as! [AnyHashable : Any]
            NSLog("Holistic landmark #%d count %d", index, holisticDict.count)
            for (lmKey, lmVal) in holisticDict {
                let landmarkType = lmKey as! Int
                let landmarkList = lmVal as! [Landmark]
                NSLog("#%d landmarks count %d", landmarkType, landmarkList.count)
                for landmark in landmarkList {
                    NSLog("\t\"%d\": %.6f %.6f %.6f", landmarkType, landmark.x, landmark.y, landmark.z)
                }
            }
        }
    }
    
    func processLandmarksByType(_ landmarkData: [AnyHashable : Any]!, landmarkType: String) {
        // Landmarks of a single component, Dict<Array<Landmark>>
        NSLog("==== Got new %s packet ====", landmarkType)
        for (idx, data) in landmarkData {
            let index = idx as! Int
            let landmarkList = data as! [Landmark]
            NSLog("#%d landmarks count %d", index, landmarkList.count)
            for landmark in landmarkList {
                NSLog("\t\"%d\": %.6f %.6f %.6f", index, landmark.x, landmark.y, landmark.z)
            }
        }
    }
}