package com.example.rtmp_with_capture;



import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import androidx.annotation.RequiresApi;
import com.marshalltechnology.video_stream.CameraPermissions.ResultCallback;
import io.flutter.Log;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.view.TextureRegistry;
import io.flutter.view.TextureRegistry.SurfaceTextureEntry;
import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
        mv = {1, 1, 18},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0000\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b¢\u0006\u0002\u0010\fJ\u001c\u0010\u0013\u001a\u00020\u00142\n\u0010\u0015\u001a\u00060\u0016j\u0002`\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0003J\u0018\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0018\u001a\u00020\u0019H\u0003J\u0018\u0010\u001d\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0018\u001a\u00020\u0019H\u0017J\u0006\u0010\u001e\u001a\u00020\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001f"},
        d2 = {"Lcom/marshalltechnology/video_stream/MethodCallHandlerImpl;", "Lio/flutter/plugin/common/MethodChannel$MethodCallHandler;", "activity", "Landroid/app/Activity;", "messenger", "Lio/flutter/plugin/common/BinaryMessenger;", "cameraPermissions", "Lcom/marshalltechnology/video_stream/CameraPermissions;", "permissionsRegistry", "Lcom/marshalltechnology/video_stream/PermissionStuff;", "textureRegistry", "Lio/flutter/view/TextureRegistry;", "(Landroid/app/Activity;Lio/flutter/plugin/common/BinaryMessenger;Lcom/marshalltechnology/video_stream/CameraPermissions;Lcom/marshalltechnology/video_stream/PermissionStuff;Lio/flutter/view/TextureRegistry;)V", "camera", "Lcom/marshalltechnology/video_stream/Camera;", "imageStreamChannel", "Lio/flutter/plugin/common/EventChannel;", "methodChannel", "Lio/flutter/plugin/common/MethodChannel;", "handleException", "", "exception", "Ljava/lang/Exception;", "Lkotlin/Exception;", "result", "Lio/flutter/plugin/common/MethodChannel$Result;", "instantiateCamera", "call", "Lio/flutter/plugin/common/MethodCall;", "onMethodCall", "stopListening", "android.video_stream"}
)
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
    public void onMethodCall(@NotNull final MethodCall call, @NotNull final Result result) {
        Intrinsics.checkParameterIsNotNull(call, "call");
        Intrinsics.checkParameterIsNotNull(result, "result");
        String var10000 = call.method;
        if (var10000 != null) {
            String var3 = var10000;
            Integer bitrate;
            Camera var9;
            switch(var3.hashCode()) {
                case -2037208347:
                    if (var3.equals("availableCameras")) {
                        try {
                            result.success(CameraUtils.INSTANCE.getAvailableCameras(this.activity));
                        } catch (Exception var8) {
                            this.handleException(var8, result);
                        }

                        return;
                    }
                    break;
                case -1199947852:
                    if (var3.equals("resumeVideoStreaming")) {
                        var9 = this.camera;
                        if (var9 == null) {
                            Intrinsics.throwNpe();
                        }

                        var9.resumeVideoStreaming(result);
                        return;
                    }
                    break;
                case -1157944680:
                    if (var3.equals("prepareForVideoRecording")) {
                        result.success((Object)null);
                        return;
                    }
                    break;
                case -538687043:
                    if (var3.equals("pauseVideoStreaming")) {
                        var9 = this.camera;
                        if (var9 == null) {
                            Intrinsics.throwNpe();
                        }

                        var9.pauseVideoStreaming(result);
                        return;
                    }
                    break;
                case -477693392:
                    if (var3.equals("stopRecordingOrStreaming")) {
                        var9 = this.camera;
                        if (var9 == null) {
                            Intrinsics.throwNpe();
                        }

                        var9.stopVideoRecordingOrStreaming(result);
                        return;
                    }
                    break;
                case -189056215:
                    if (var3.equals("startVideoStreaming")) {
                        Log.i("Stuff", call.arguments.toString());
                        bitrate = (Integer)null;
                        if (call.hasArgument("bitrate")) {
                            bitrate = (Integer)call.argument("bitrate");
                        }

                        var9 = this.camera;
                        if (var9 == null) {
                            Intrinsics.throwNpe();
                        }

                        var9.startVideoStreaming((String)call.argument("url"), bitrate, result);
                        return;
                    }
                    break;
                case 589227065:
                    if (var3.equals("getStreamStatistics")) {
                        try {
                            var9 = this.camera;
                            if (var9 == null) {
                                Intrinsics.throwNpe();
                            }

                            var9.getStreamStatistics(result);
                        } catch (Exception var5) {
                            this.handleException(var5, result);
                        }

                        return;
                    }
                    break;
                case 871091088:
                    if (var3.equals("initialize")) {
                        if (this.camera != null) {
                            var9 = this.camera;
                            if (var9 == null) {
                                Intrinsics.throwNpe();
                            }

                            var9.close();
                        }

                        CameraPermissions var11 = this.cameraPermissions;
                        Activity var10 = this.activity;
                        PermissionStuff var10002 = this.permissionsRegistry;
                        Object var10003 = call.argument("enableAudio");
                        if (var10003 == null) {
                            Intrinsics.throwNpe();
                        }

                        var11.requestPermissions(var10, var10002, (Boolean)var10003, (ResultCallback)(new ResultCallback() {
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
                    break;
                case 954656505:
                    if (var3.equals("startImageStream")) {
                        try {
                            var9 = this.camera;
                            if (var9 == null) {
                                Intrinsics.throwNpe();
                            }

                            var9.startPreviewWithImageStream(this.imageStreamChannel);
                            result.success((Object)null);
                        } catch (Exception var7) {
                            this.handleException(var7, result);
                        }

                        return;
                    }
                    break;
                case 1211269411:
                    if (var3.equals("startVideoRecordingAndStreaming")) {
                        Log.i("Stuff", call.arguments.toString());
                        bitrate = (Integer)null;
                        if (call.hasArgument("bitrate")) {
                            bitrate = (Integer)call.argument("bitrate");
                        }

                        var9 = this.camera;
                        if (var9 == null) {
                            Intrinsics.throwNpe();
                        }

                        Object var10001 = call.argument("filePath");
                        if (var10001 == null) {
                            Intrinsics.throwNpe();
                        }

                        var9.startVideoRecordingAndStreaming((String)var10001, (String)call.argument("url"), bitrate, result);
                        return;
                    }
                    break;
                case 1671767583:
                    if (var3.equals("dispose")) {
                        if (this.camera != null) {
                            var9 = this.camera;
                            if (var9 == null) {
                                Intrinsics.throwNpe();
                            }

                            var9.dispose();
                        }

                        result.success((Object)null);
                        return;
                    }
                    break;
                case 1672159065:
                    if (var3.equals("stopImageStream")) {
                        try {
                            var9 = this.camera;
                            if (var9 == null) {
                                Intrinsics.throwNpe();
                            }

                            var9.startPreview();
                            result.success((Object)null);
                        } catch (Exception var6) {
                            this.handleException(var6, result);
                        }

                        return;
                    }
                    break;
                case 1967657600:
                    if (var3.equals("stopStreaming")) {
                        var9 = this.camera;
                        if (var9 == null) {
                            Intrinsics.throwNpe();
                        }

                        var9.stopVideoStreaming(result);
                        return;
                    }
            }
        }

        result.notImplemented();
    }

    public final void stopListening() {
        this.methodChannel.setMethodCallHandler((MethodCallHandler)null);
    }

    @RequiresApi(21)
    private final void instantiateCamera(MethodCall call, Result result) throws CameraAccessException {
        String cameraName = (String)call.argument("cameraName");
        String resolutionPreset = (String)call.argument("resolutionPreset");
        String streamingPreset = (String)call.argument("streamingPreset");
        Object var10000 = call.argument("enableAudio");
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        Intrinsics.checkExpressionValueIsNotNull(var10000, "call.argument<Boolean>(\"enableAudio\")!!");
        boolean enableAudio = (Boolean)var10000;
        boolean enableOpenGL = false;
        if (call.hasArgument("enableAndroidOpenGL")) {
            var10000 = call.argument("enableAndroidOpenGL");
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            enableOpenGL = (Boolean)var10000;
        }

        SurfaceTextureEntry flutterSurfaceTexture = this.textureRegistry.createSurfaceTexture();
        DartMessenger dartMessenger = new DartMessenger(this.messenger, flutterSurfaceTexture.id());
        Camera var10001 = new Camera;
        Activity var10003 = this.activity;
        Intrinsics.checkExpressionValueIsNotNull(flutterSurfaceTexture, "flutterSurfaceTexture");
        if (cameraName == null) {
            Intrinsics.throwNpe();
        }

        var10001.<init>(var10003, flutterSurfaceTexture, dartMessenger, cameraName, resolutionPreset, streamingPreset, enableAudio, enableOpenGL);
        this.camera = var10001;
        Camera var10 = this.camera;
        if (var10 == null) {
            Intrinsics.throwNpe();
        }

        var10.open(result);
    }

    @RequiresApi(21)
    private final void handleException(Exception exception, Result result) {
        if (exception instanceof CameraAccessException) {
            result.error("CameraAccess", exception.getMessage(), (Object)null);
        }

        if (exception == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.RuntimeException /* = java.lang.RuntimeException */");
        } else {
            throw (Throwable)((RuntimeException)exception);
        }
    }

    public MethodCallHandlerImpl(@NotNull Activity activity, @NotNull BinaryMessenger messenger, @NotNull CameraPermissions cameraPermissions, @NotNull PermissionStuff permissionsRegistry, @NotNull TextureRegistry textureRegistry) {
        Intrinsics.checkParameterIsNotNull(activity, "activity");
        Intrinsics.checkParameterIsNotNull(messenger, "messenger");
        Intrinsics.checkParameterIsNotNull(cameraPermissions, "cameraPermissions");
        Intrinsics.checkParameterIsNotNull(permissionsRegistry, "permissionsRegistry");
        Intrinsics.checkParameterIsNotNull(textureRegistry, "textureRegistry");
        super();
        this.activity = activity;
        this.messenger = messenger;
        this.cameraPermissions = cameraPermissions;
        this.permissionsRegistry = permissionsRegistry;
        this.textureRegistry = textureRegistry;
        this.methodChannel = new MethodChannel(this.messenger, "video_stream");
        this.imageStreamChannel = new EventChannel(this.messenger, "video_stream/imageStream");
        this.methodChannel.setMethodCallHandler((MethodCallHandler)this);
    }
}

