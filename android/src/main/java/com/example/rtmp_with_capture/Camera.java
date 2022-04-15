package com.example.rtmp_with_capture;

import com.example.rtmp_with_capture.DartMessenger.EventType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CaptureRequest.Builder;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.Image.Plane;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.pedro.rtplibrary.util.BitrateAdapter;
import com.pedro.rtplibrary.util.BitrateAdapter.Listener;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.view.TextureRegistry.SurfaceTextureEntry;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ossrs.rtmp.ConnectCheckerRtmp;


@RequiresApi(21)
public final class Camera implements ConnectCheckerRtmp {
    private final CameraManager cameraManager;
    private final OrientationEventListener orientationEventListener;
    private boolean isFrontFacing;
    private int sensorOrientation;
    private final Size captureSize;
    private final Size previewSize;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader pictureImageReader;
    private ImageReader imageStreamReader;
    private final CamcorderProfile recordingProfile;
    private final CamcorderProfile streamingProfile;
    private int currentOrientation;
    private RtmpCameraConnector rtmpCamera;
    private BitrateAdapter bitrateAdapter;
    private final int maxRetries;
    private int currentRetries;
    private String publishUrl;
    @Nullable
    private final Activity activity;
    @NonNull
    private final SurfaceTextureEntry flutterTexture;
    @NonNull
    private final DartMessenger dartMessenger;
    @NonNull
    private final String cameraName;
    @Nullable
    private final String resolutionPreset;
    @Nullable
    private final String streamingPreset;
    private final boolean enableAudio;
    private final boolean useOpenGL;
    private static final String TAG = "FlutterCamera";

    private final void prepareCameraForRecordAndStream(int fps, Integer bitrate) throws IOException {
        RtmpCameraConnector rtmpCamera;
        if (this.rtmpCamera != null) {
            rtmpCamera = this.rtmpCamera;
            rtmpCamera.stopStream();
            this.rtmpCamera = (RtmpCameraConnector) null;
        }

        Log.i(TAG, "prepareCameraForRecordAndStream(opengl=" + this.useOpenGL + ", portrait: " + this.isPortrait() + ", currentOrientation: " + this.currentOrientation + ", mediaOrientation: " + this.getMediaOrientation() + ", frontfacing: " + this.isFrontFacing + ")");
        rtmpCamera = new RtmpCameraConnector(activity.getApplicationContext(), useOpenGL, isPortrait(), this);
        Activity var10003 = this.activity;
        if (this.enableAudio) {
            rtmpCamera.prepareAudio();
        }
        Integer bitrateToUse = bitrate;
        if (bitrate == null) {
            bitrateToUse = 1228800;
        }
        rtmpCamera = this.rtmpCamera;
        rtmpCamera.prepareVideo(!this.isPortrait() ? this.streamingProfile.videoFrameWidth : this.streamingProfile.videoFrameHeight, !this.isPortrait() ? this.streamingProfile.videoFrameHeight : this.streamingProfile.videoFrameWidth, fps, bitrateToUse, !this.useOpenGL, this.getMediaOrientation());
    }

    @SuppressLint({"MissingPermission"})
    public final void open(@NonNull final Result result) throws CameraAccessException {
        this.pictureImageReader = ImageReader.newInstance(this.captureSize.getWidth(), this.captureSize.getHeight(), ImageFormat.JPEG, 2);
        this.cameraManager.openCamera(this.cameraName, (StateCallback) (new StateCallback() {
            public void onOpened(@NonNull CameraDevice device) {
                Camera.this.cameraDevice = device;
                try {
                    Camera.this.startPreview();
                } catch (CameraAccessException e) {
                    result.error("CameraAccess", e.getMessage(), (Object) null);
                    Camera.this.close();
                    return;
                }

                Map reply = new HashMap();
                reply.put("textureId", Camera.this.getFlutterTexture().id());
                if (Camera.this.isPortrait()) {
                    reply.put("previewWidth", Camera.this.previewSize.getWidth());
                    reply.put("previewHeight", Camera.this.previewSize.getHeight());
                } else {
                    reply.put("previewWidth", Camera.this.previewSize.getHeight());
                    reply.put("previewHeight", Camera.this.previewSize.getWidth());
                }

                reply.put("previewQuarterTurns", Camera.this.currentOrientation / 90);
                Log.i(Camera.TAG, "open: width: " + reply.get("previewWidth") + " height: " + reply.get("previewHeight") + " currentOrientation: " + Camera.this.currentOrientation + " quarterTurns: " + reply.get("previewQuarterTurns"));
                result.success(reply);
            }

            public void onClosed(@NonNull CameraDevice camera) {
                Camera.this.getDartMessenger().sendCameraClosingEvent();
                super.onClosed(camera);
            }

            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                Log.v("Camera", "onDisconnected()");
                Camera.this.close();
                Camera.this.getDartMessenger().send(EventType.ERROR, "The camera was disconnected.");
            }

            public void onError(@NonNull CameraDevice cameraDevice, int errorCode) {
                Log.v("Camera", "onError(" + errorCode + ")");
                close();
                String errorDescription = "";
                switch (errorCode) {
                    case 1:
                        errorDescription = "The camera device is in use already.";
                        break;
                    case 2:
                        errorDescription = "Max cameras in use";
                        break;
                    case 3:
                        errorDescription = "The camera device could not be opened due to a device policy.";
                        break;
                    case 4:
                        errorDescription = "The camera device has encountered a fatal error";
                        break;
                    case 5:
                        errorDescription = "The camera service has encountered a fatal error.";
                        break;
                    default:
                        errorDescription = "Unknown camera error";
                }

                Camera.this.getDartMessenger().send(DartMessenger.EventType.ERROR, errorDescription);
            }
        }), (Handler) null);
    }

    private final void writeToFile(ByteBuffer buffer, File file) throws IOException {
        Closeable closeable = (Closeable) (new FileOutputStream(file));
        Throwable var5 = null;
        try {
            FileOutputStream outputStream = (FileOutputStream) closeable;
            while (0 < buffer.remaining()) {
                outputStream.getChannel().write(buffer);
            }

        } catch (Throwable var10) {
            var5 = var10;
            throw var10;
        } finally {
            closeable.close();
//            CloseableKt.closeFinally(closeable, var5);
        }
    }

    private final void createCaptureSession(int templateType, final Runnable onSuccessCallback, Surface surface) throws CameraAccessException {
        this.closeCaptureSession();
        Log.v("Camera", "createCaptureSession " + this.previewSize.getWidth() + "x" + this.previewSize.getHeight() + " mediaOrientation: " + this.getMediaOrientation() + " currentOrientation: " + this.currentOrientation + " sensorOrientation: " + this.sensorOrientation + " porteait: " + this.isPortrait());
        Builder requestBuilder = cameraDevice.createCaptureRequest(templateType);
        List surfaceList = new ArrayList();
        SurfaceTexture surfaceTexture = this.flutterTexture.surfaceTexture();
        if (this.isPortrait()) {
            surfaceTexture.setDefaultBufferSize(this.previewSize.getWidth(), this.previewSize.getHeight());
        } else {
            surfaceTexture.setDefaultBufferSize(this.previewSize.getHeight(), this.previewSize.getWidth());
        }

        Surface flutterSurface = new Surface(surfaceTexture);
        requestBuilder.addTarget(flutterSurface);
        if (templateType != 1) {
            requestBuilder.addTarget(surface);
        }

        surfaceList.add(flutterSurface);
        surfaceList.add(surface);
        CameraCaptureSession.StateCallback callback = new CameraCaptureSession.StateCallback() {
            @RequiresApi(26)
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    if (Camera.this.cameraDevice == null) {
                        Camera.this.getDartMessenger().send(EventType.ERROR, "The camera was closed during configuration.");
                        return;
                    }

                    Log.v("Camera", "open successful ");
                    requestBuilder.set(CaptureRequest.CONTROL_MODE, 1);
                    session.setRepeatingRequest(requestBuilder.build(), (CaptureCallback) null, (Handler) null);
                    Camera.this.cameraCaptureSession = session;
                    onSuccessCallback.run();
                } catch (CameraAccessException var3) {
                    Log.v("Camera", "Error CameraAccessException", (Throwable) var3);
                    Camera.this.getDartMessenger().send(EventType.ERROR, var3.getMessage());
                } catch (IllegalStateException var4) {
                    Log.v("Camera", "Error IllegalStateException", (Throwable) var4);
                    Camera.this.getDartMessenger().send(EventType.ERROR, var4.getMessage());
                } catch (IllegalArgumentException var5) {
                    Log.v("Camera", "Error IllegalArgumentException", (Throwable) var5);
                    Camera.this.getDartMessenger().send(DartMessenger.EventType.ERROR, var5.getMessage());
                }

            }

            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                Camera.this.getDartMessenger().send(EventType.ERROR, "Failed to configure camera session.");
            }
        };

        cameraDevice.createCaptureSession(surfaceList, callback, (Handler) null);
    }

    public final void stopVideoRecordingOrStreaming(@NonNull Result result) {
        Log.i("Camera", "stopVideoRecordingOrStreaming ");
        if (this.rtmpCamera == null) {
            result.success((Object) null);
        } else {
            try {
                this.currentRetries = 0;
                this.publishUrl = (String) null;
                if (this.rtmpCamera != null) {
                    rtmpCamera.stopRecord();
                    rtmpCamera.stopStream();
                    this.rtmpCamera = (RtmpCameraConnector) null;
                }

                this.startPreview();
                result.success((Object) null);
            } catch (CameraAccessException e) {
                result.error("videoRecordingFailed", e.getMessage(), (Object) null);
            } catch (IllegalStateException e) {
                result.error("videoRecordingFailed", e.getMessage(), (Object) null);
            }

        }
    }

    public final void stopVideoStreaming(@NonNull Result result) {
        Log.i("Camera", "stopVideoRecording");
        if (this.rtmpCamera == null) {
            result.success((Object) null);
        } else {
            try {
                this.currentRetries = 0;
                this.publishUrl = (String) null;
                if (this.rtmpCamera != null) {
                    rtmpCamera.stopStream();
                    this.rtmpCamera = (RtmpCameraConnector) null;
                }

                this.startPreview();
                result.success(null);
            } catch (CameraAccessException e) {
                result.error("videoRecordingFailed", e.getMessage(), (Object) null);
            } catch (IllegalStateException e) {
                result.error("videoRecordingFailed", e.getMessage(), (Object) null);
            }

        }
    }

    public final void startPreview() throws CameraAccessException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };
        Surface surface = pictureImageReader.getSurface();
        this.createCaptureSession(CameraDevice.TEMPLATE_PREVIEW, runnable, surface);
    }

    public final void startPreviewWithImageStream(@NonNull EventChannel imageStreamChannel) throws CameraAccessException {
        this.imageStreamReader = ImageReader.newInstance(this.previewSize.getWidth(), this.previewSize.getHeight(), ImageFormat.YUV_420_888, 2);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };
        Surface surface = imageStreamReader.getSurface();
        this.createCaptureSession(3, runnable, surface);
        imageStreamChannel.setStreamHandler((StreamHandler) (new StreamHandler() {
            public void onListen(@NonNull Object o, @NonNull EventSink imageStreamSink) {
                Camera.this.setImageStreamImageAvailableListener(imageStreamSink);
            }

            public void onCancel(@NonNull Object o) {
                imageStreamReader.setOnImageAvailableListener(null, null);
            }
        }));
    }

    private final void setImageStreamImageAvailableListener(final EventSink imageStreamSink) {

        imageStreamReader.setOnImageAvailableListener((OnImageAvailableListener) (new OnImageAvailableListener() {
            public final void onImageAvailable(@NonNull ImageReader reader) {
                Image img = reader.acquireLatestImage();
                if (img != null) {
                    List planes = new ArrayList();
                    Plane[] imgPlanes = img.getPlanes();
                    int var7 = imgPlanes.length;

                    for (Plane plane : imgPlanes) {
                        ByteBuffer buffer = plane.getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes, 0, bytes.length);
                        Map planeBuffer = new HashMap();
                        planeBuffer.put("bytesPerRow", plane.getRowStride());
                        planeBuffer.put("bytesPerPixel", plane.getPixelStride());
                        planeBuffer.put("bytes", bytes);
                        planes.add(planeBuffer);
                    }

                    Map imageBuffer = (Map) (new HashMap());
                    imageBuffer.put("width", img.getWidth());
                    imageBuffer.put("height", img.getHeight());
                    imageBuffer.put("format", img.getFormat());
                    imageBuffer.put("planes", planes);
                    imageStreamSink.success(imageBuffer);
                    img.close();
                }
            }
        }), null);
    }

    private final void closeCaptureSession() {
        if (this.cameraCaptureSession != null) {
            Log.v("Camera", "Close recoordingCaptureSession");

            try {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.abortCaptures();

                cameraCaptureSession.close();
            } catch (CameraAccessException e) {
                Log.w("RtmpCamera", "Error from camera", (Throwable) e);
            }

            cameraCaptureSession = null;
        } else {
            Log.v("Camera", "No recoordingCaptureSession to close");
        }

    }

    public final void close() {
        this.closeCaptureSession();
        if (this.cameraDevice != null) {
            cameraDevice.close();
            this.cameraDevice = null;
        }

        if (this.pictureImageReader != null) {

            pictureImageReader.close();
            this.pictureImageReader = null;
        }

        if (this.imageStreamReader != null) {

            imageStreamReader.close();
            this.imageStreamReader = null;
        }

        if (this.rtmpCamera != null) {

            rtmpCamera.stopStream();
            this.rtmpCamera = null;
            this.bitrateAdapter = null;
            this.publishUrl = null;
        }

    }

    public final void dispose() {
        this.close();
        this.flutterTexture.release();
        this.orientationEventListener.disable();
    }

    public final void startVideoStreaming(@Nullable final String url, @Nullable Integer bitrate, @NonNull Result result) {
        if (url == null) {
            result.error("fileExists", "Must specify a url.", null);
        } else {
            try {
                if (this.rtmpCamera == null) {
                    this.currentRetries = 0;
                    this.prepareCameraForRecordAndStream(this.streamingProfile.videoFrameRate, bitrate);
                    Runnable runnable = (Runnable) (new Runnable() {
                        public final void run() {
                            rtmpCamera.startStream(url);
                        }
                    });

                    this.createCaptureSession(3, runnable, rtmpCamera.getInputSurface());
                } else {

                    rtmpCamera.startStream(url);
                }
                result.success(null);
            } catch (CameraAccessException var5) {
                result.error("videoStreamingFailed", var5.getMessage(), (Object) null);
            } catch (IOException var6) {
                result.error("videoStreamingFailed", var6.getMessage(), (Object) null);
            }

        }
    }

    public final void startVideoRecordingAndStreaming(@NonNull final String filePath, @Nullable final String url, @Nullable Integer bitrate, @NonNull Result result) {

        if ((new File(filePath)).exists()) {
            result.error("fileExists", "File at path '" + filePath + "' already exists.", (Object) null);
        } else if (url == null) {
            result.error("fileExists", "Must specify a url.", (Object) null);
        } else {
            try {
                this.currentRetries = 0;
                this.prepareCameraForRecordAndStream(this.streamingProfile.videoFrameRate, bitrate);
                Runnable runnable = (Runnable) (new Runnable() {
                    public final void run() {
                        rtmpCamera.startStream(url);
                        rtmpCamera.startRecord(filePath);
                    }
                });

                this.createCaptureSession(3, runnable, rtmpCamera.getInputSurface());
                result.success(null);
            } catch (CameraAccessException e) {
                result.error("videoRecordingFailed", e.getMessage(), null);
            } catch (IOException e) {
                result.error("videoRecordingFailed", e.getMessage(), null);
            }

        }
    }

    public final void pauseVideoStreaming(@NonNull Result result) {
        if (this.rtmpCamera != null) {
            if (rtmpCamera.isStreaming()) {
                try {
                    this.currentRetries = 0;
                    rtmpCamera.pauseStream();
                } catch (IllegalStateException e) {
                    result.error("videoStreamingFailed", e.getMessage(),  null);
                    return;
                }
                result.success(null);
                return;
            }
        }
        result.success( null);
    }

    public final void getStreamStatistics(@NonNull Result result) {
        if (this.rtmpCamera != null) {
            boolean var3 = false;
            HashMap ret = new HashMap();

            ret.put("cacheSize", rtmpCamera.getCacheSize());
            ret.put("sentAudioFrames", rtmpCamera.getSentAudioFrames());
            ret.put("sentVideoFrames", rtmpCamera.getSentVideoFrames());
            ret.put("droppedAudioFrames", rtmpCamera.getDroppedAudioFrames());
            ret.put("droppedVideoFrames", rtmpCamera.getDroppedVideoFrames());
            ret.put("isAudioMuted", rtmpCamera.isAudioMuted());
            ret.put("bitrate", rtmpCamera.getBitrate());
            ret.put("width", rtmpCamera.getStreamWidth());
            ret.put("height", rtmpCamera.getStreamHeight());
            ret.put("fps", rtmpCamera.getFps());
            result.success(ret);
        } else {
            result.error("noStats", "Not streaming anything", null);
        }

    }

    public final void resumeVideoStreaming(@NonNull Result result) {
        if (this.rtmpCamera != null) {
            if (rtmpCamera.isStreaming()) {
                try {

                    rtmpCamera.resumeStream();
                } catch (IllegalStateException e) {
                    result.error("videoStreamingFailed", e.getMessage(),  null);
                    return;
                }
                result.success( null);
                return;
            }
        }

        result.success((Object) null);
    }

    private final int getMediaOrientation() {
        int sensorOrientationOffset = this.isFrontFacing ? -this.currentOrientation : this.currentOrientation;
        return (sensorOrientationOffset + this.sensorOrientation + 360) % 360;
    }

    private final boolean isPortrait() {
        Display getOrient = activity.getWindowManager().getDefaultDisplay();
        Point pt = new Point();
        getOrient.getSize(pt);
        if (pt.x == pt.y) {
            return true;
        } else {
            return pt.x < pt.y;
        }
    }

    @Override
    public void onAuthSuccessRtmp() {
    }

    @Override
    public void onNewBitrateRtmp(long bitrate) {
        if (this.bitrateAdapter != null) {
            bitrateAdapter.setMaxBitrate((int) bitrate);
        }

    }

    @Override
    public void onConnectionSuccessRtmp() {
        this.bitrateAdapter = new BitrateAdapter((Listener) (new Listener() {
            public void onBitrateAdapted(int bitrate) {
                rtmpCamera.setVideoBitrateOnFly(bitrate);
            }
        }));

        bitrateAdapter.setMaxBitrate(rtmpCamera.getBitrate());
    }

    @Override
    public void onConnectionFailedRtmp(@NonNull final String reason) {
        if (this.rtmpCamera != null) {
            int i = this.currentRetries;
            if (i <= this.maxRetries) {
                while (true) {
                    this.currentRetries = i;
                    if (this.rtmpCamera.reTry(5000L, reason)) {
                        activity.runOnUiThread((Runnable) (new Runnable() {
                            public final void run() {
                                Camera.this.getDartMessenger().send(EventType.RTMP_RETRY, reason);
                            }
                        }));
                        return;
                    }
                    if (i == maxRetries) {
                        break;
                    }
                    ++i;
                }
            }

            rtmpCamera.stopStream();
            this.rtmpCamera =  null;
            activity.runOnUiThread((Runnable) (new Runnable() {
                public final void run() {
                    Camera.this.getDartMessenger().send(EventType.RTMP_STOPPED, "Failed retry");
                }
            }));
        }

    }

    @Override
    public void onAuthErrorRtmp() {
        activity.runOnUiThread((Runnable) (new Runnable() {
            public final void run() {
                Camera.this.getDartMessenger().send(EventType.ERROR, "Auth error");
            }
        }));
    }

    @Override
    public void onDisconnectRtmp() {
        if (rtmpCamera != null) {
            rtmpCamera.stopStream();
            rtmpCamera =  null;
        }
        activity.runOnUiThread((Runnable) (new Runnable() {
            public final void run() {
                Camera.this.getDartMessenger().send(EventType.RTMP_STOPPED, "Disconnected");
            }
        }));
    }

    @Nullable
    public final Activity getActivity() {
        return this.activity;
    }

    @NonNull
    public final SurfaceTextureEntry getFlutterTexture() {
        return this.flutterTexture;
    }

    @NonNull
    public final DartMessenger getDartMessenger() {
        return this.dartMessenger;
    }

    @NonNull
    public final String getCameraName() {
        return this.cameraName;
    }

    @Nullable
    public final String getResolutionPreset() {
        return this.resolutionPreset;
    }

    @Nullable
    public final String getStreamingPreset() {
        return this.streamingPreset;
    }

    public final boolean getEnableAudio() {
        return this.enableAudio;
    }

    public final boolean getUseOpenGL() {
        return this.useOpenGL;
    }

    public Camera(@Nullable Activity activity, @NonNull SurfaceTextureEntry flutterTexture, @NonNull DartMessenger dartMessenger, @NonNull String cameraName, @Nullable String resolutionPreset, @Nullable String streamingPreset, boolean enableAudio, boolean useOpenGL) {
        super();
        this.activity = activity;
        this.flutterTexture = flutterTexture;
        this.dartMessenger = dartMessenger;
        this.cameraName = cameraName;
        this.resolutionPreset = resolutionPreset;
        this.streamingPreset = streamingPreset;
        this.enableAudio = enableAudio;
        this.useOpenGL = useOpenGL;
        this.currentOrientation = -1;
        this.maxRetries = 3;
        if (activity == null) {
            throw new IllegalStateException("No activity available!");
        } else {
            Object cameraManager = this.activity.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                throw new NullPointerException("null cannot be cast to non-null type android.hardware.camera2.CameraManager");
            } else {
                CameraCharacteristics characteristics;
                initialize:
                {
                    this.cameraManager = (CameraManager) cameraManager;
                    this.orientationEventListener = (OrientationEventListener) (new OrientationEventListener(this.activity.getApplicationContext()) {
                        public void onOrientationChanged(int i) {
                            if (i != -1) {
                                Camera.this.currentOrientation = (int) Math.round((double) i / 90.0D) * 90;
                                Camera.this.getDartMessenger().send(EventType.ROTATION_UPDATE, String.valueOf(Camera.this.currentOrientation / 90));
                                Log.i(Camera.TAG, "Updated Orientation (sent) " + Camera.this.currentOrientation + " -- " + Camera.this.currentOrientation / 90);
                            }
                        }
                    });
                    this.orientationEventListener.enable();
                    try {
                        characteristics = this.cameraManager.getCameraCharacteristics(this.cameraName);
                        Integer camdir = (Integer) characteristics.get(CameraCharacteristics.LENS_FACING);
                        if (camdir != null) {
                            if (camdir == 0) {
                                isFrontFacing = true;
                                break initialize;
                            }
                        }
                        isFrontFacing = false;
                        this.sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                        this.currentOrientation = (int) Math.round((double) activity.getResources().getConfiguration().orientation / 90.0D) * 90;
                    }catch (Exception e){
                        Log.e(TAG, "error in getting cameracharacteristics");
                    }

                }



                ResolutionPreset preset = ResolutionPreset.valueOf(resolutionPreset);
                this.recordingProfile = CameraUtils.INSTANCE.getBestAvailableCamcorderProfileForResolutionPreset(this.cameraName, preset);
                this.captureSize = new Size(this.recordingProfile.videoFrameWidth, this.recordingProfile.videoFrameHeight);
                this.previewSize = CameraUtils.INSTANCE.computeBestPreviewSize(this.cameraName, preset);
                ResolutionPreset streamPreset = ResolutionPreset.valueOf(streamingPreset);
                this.streamingProfile = CameraUtils.INSTANCE.getBestAvailableCamcorderProfileForResolutionPreset(this.cameraName, streamPreset);
            }
        }
    }

//    // $FF: synthetic method
//    public static final CameraCaptureSession f(Camera $this) {
//        return $this.cameraCaptureSession;
//    }
//
//    // $FF: synthetic method
//    public static final void access$setImageStreamReader$p(Camera $this, ImageReader var1) {
//        $this.imageStreamReader = var1;
//    }
//
//    // $FF: synthetic method
//    public static final void access$setRtmpCamera$p(Camera $this, RtmpCameraConnector var1) {
//        $this.rtmpCamera = var1;
//    }


//    public static final class Companion {
//        private Companion() {
//        }
//
//        // $FF: synthetic method
//        public Companion(DefaultConstructorMarker $constructor_marker) {
//            this();
//        }
//    }
}
enum ResolutionPreset {
    low, medium, high, veryHigh, ultraHigh, max
}