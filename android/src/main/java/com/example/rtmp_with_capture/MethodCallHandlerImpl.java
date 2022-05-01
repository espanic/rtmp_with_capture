package com.example.rtmp_with_capture;



import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.nfc.Tag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import io.flutter.Log;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.view.TextureRegistry;
import io.flutter.view.TextureRegistry.SurfaceTextureEntry;



public final class MethodCallHandlerImpl implements MethodCallHandler {
    private final MethodChannel methodChannel;
    private final EventChannel imageStreamChannel;
    private Camera camera;
    private final Activity activity;
    private final BinaryMessenger messenger;
    private final CameraPermissions cameraPermissions;
    private final PermissionStuff permissionsRegistry;
    private final TextureRegistry textureRegistry;

    @RequiresApi(21)
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {

        String method = call.method;
        Log.i("methodcallhandlerimpl", method);
        if (method != null) {
            Integer bitrate;
                    if (method.equals("availableCameras")) {
                        try {
                            result.success(CameraUtils.INSTANCE.getAvailableCameras(this.activity));
                        } catch (Exception e) {
                            this.handleException(e, result);
                        }
                        return;
                    }
                    if (method.equals("resumeVideoStreaming")) {
                        camera.resumeVideoStreaming(result);
                        return;
                    }
                    if (method.equals("prepareForVideoRecording")) {
                        result.success((Object)null);
                        return;
                    }
                    if (method.equals("pauseVideoStreaming")) {
                        camera.pauseVideoStreaming(result);
                        return;
                    }


                    if (method.equals("stopRecordingOrStreaming")) {
                        camera.stopVideoRecordingOrStreaming(result);
                        return;
                    }

                    if (method.equals("startVideoStreaming")) {
                        Log.i("Stuff", call.arguments.toString());
                        bitrate = null;
                        if (call.hasArgument("bitrate")) {
                            bitrate = call.argument("bitrate");
                        }


                        camera.startVideoStreaming((String)call.argument("url"), bitrate, result, (int) call.argument("width"), (int) call.argument("height"));
                        return;
                    }

                    if (method.equals("getStreamStatistics")) {
                        try {
                            camera.getStreamStatistics(result);
                        } catch (Exception e) {
                            this.handleException(e, result);
                        }

                        return;
                    }

                    if (method.equals("initialize")) {
                        if (this.camera != null) {
                            camera.close();
                        }



                        cameraPermissions.requestPermissions(activity, permissionsRegistry, call.argument("enableAudio"), (CameraPermissions.ResultCallback) (new CameraPermissions.ResultCallback() {
                            public void onResult(@Nullable String errorCode, @Nullable String errorDescription) {
                                if (errorCode == null) {
                                    try {
                                        MethodCallHandlerImpl.this.instantiateCamera(call, result);
                                    } catch (Exception var4) {
                                        MethodCallHandlerImpl.this.handleException(var4, result);
                                    }
                                } else {
                                    result.error(errorCode, errorDescription, (Object)null);
                                }

                            }
                        }));
                        return;
                    }

                    if (method.equals("startImageStream")) {
                        try {


                            camera.startPreviewWithImageStream(this.imageStreamChannel);
                            result.success((Object)null);
                        } catch (Exception e) {
                            this.handleException(e, result);
                        }

                        return;
                    }

                    if (method.equals("startVideoRecordingAndStreaming")) {
                        Log.i("Stuff", call.arguments.toString());
                        bitrate = null;
                        if (call.hasArgument("bitrate")) {
                            bitrate = call.argument("bitrate");
                        }


                        camera.startVideoRecordingAndStreaming(call.argument("filePath"), (String)call.argument("url"), bitrate, result);
                        return;
                    }

                    if (method.equals("dispose")) {
                        if (this.camera != null) {
                            camera.dispose();
                        }

                        result.success((Object)null);
                        return;
                    }

                    if (method.equals("stopImageStream")) {
                        try {

                            camera.startPreview();
                            result.success((Object)null);
                        } catch (Exception var6) {
                            this.handleException(var6, result);
                        }

                        return;
                    }

                    if (method.equals("stopStreaming")) {

                        camera.stopVideoStreaming(result);
                        return;
                    }

                    if(method.equals("takePhoto")){
                        byte[] byteArray = camera.takePhoto();
                        result.success(byteArray);
                        Log.i("mh", "sending byte array");
                    }

        }

        result.notImplemented();
    }

//    private void generateResults (Byte[] bytearray) {
//        methodChannel.invokeMethod("");
//    }

    public final void stopListening() {
        this.methodChannel.setMethodCallHandler((MethodCallHandler)null);
    }

    @RequiresApi(21)
    private final void instantiateCamera(MethodCall call, Result result) throws CameraAccessException {
        String cameraName = (String)call.argument("cameraName");
        String resolutionPreset = (String)call.argument("resolutionPreset");
        String streamingPreset = (String)call.argument("streamingPreset");
        boolean enableAudio = call.argument("enableAudio");
        boolean enableOpenGL = false;
        if (call.hasArgument("enableAndroidOpenGL")) {
            enableOpenGL = call.argument("enableAndroidOpenGL");
        }

        SurfaceTextureEntry flutterSurfaceTexture = this.textureRegistry.createSurfaceTexture();
        DartMessenger dartMessenger = new DartMessenger(this.messenger, flutterSurfaceTexture.id());
        camera = new Camera(activity, flutterSurfaceTexture, dartMessenger, cameraName, resolutionPreset, streamingPreset, enableAudio, enableOpenGL);
        camera.open(result);
    }

    @RequiresApi(21)
    private final void handleException(Exception exception, Result result) {
        if (exception instanceof CameraAccessException) {
            result.error("CameraAccess", exception.getMessage(), (Object)null);
        }

        if (exception == null) {
            throw new NullPointerException("null cannot be cast to non-null type kotlin.RuntimeException /* = java.lang.RuntimeException */");
        } else {
            throw new RuntimeException();
        }
    }

    public MethodCallHandlerImpl(@NonNull Activity activity, @NonNull BinaryMessenger messenger, @NonNull CameraPermissions cameraPermissions, @NonNull PermissionStuff permissionsRegistry, @NonNull TextureRegistry textureRegistry) {
        super();
        this.activity = activity;
        this.messenger = messenger;
        this.cameraPermissions = cameraPermissions;
        this.permissionsRegistry = permissionsRegistry;
        this.textureRegistry = textureRegistry;
        this.methodChannel = new MethodChannel(this.messenger, "rtmp_with_capture");
        this.imageStreamChannel = new EventChannel(this.messenger, "rtmp_with_capture/imageStream");
        this.methodChannel.setMethodCallHandler(this);
    }
}

