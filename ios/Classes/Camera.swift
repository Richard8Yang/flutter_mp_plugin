import AVFoundation

class Camera: NSObject {
    var session: AVCaptureSession
    var input: AVCaptureDeviceInput
    var device: AVCaptureDevice
    var output: AVCaptureVideoDataOutput

    init(_ config: String) {
        let params = config.split(separator: Character("/"))
        if (params.count > 0 && params[0] == "front") {
            self.device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front)!
        } else {
            self.device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back)!
        }
        if (params.count > 1 && params[1] == "high_resolution") {
            // TODO: set resolution
        }
        self.input = try! AVCaptureDeviceInput(device: device)
        self.output = .init()
        self.session = .init()

        output.videoSettings = [kCVPixelBufferPixelFormatTypeKey as String : kCVPixelFormatType_32BGRA]
        session.addInput(input)
        session.addOutput(output)
        session.sessionPreset = .photo
        if #available(iOS 13, *) {
            session.connections[0].videoOrientation = .portrait
            session.connections[0].isVideoMirrored = false
        }
    }
    
    func setSampleBufferDelegate(_ delegate: AVCaptureVideoDataOutputSampleBufferDelegate) {
        output.setSampleBufferDelegate(delegate, queue: .main)
    }
    
    func start() {
        session.startRunning()
    }
    
    func stop() {
        session.stopRunning()
    }
}
