package com.example.rtmp_with_capture;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
        mv = {1, 1, 18},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0000\u0018\u0000 \u00112\u00020\u0001:\u0003\u0010\u0011\u0012B\u0005¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007H\u0002J\u0010\u0010\b\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007H\u0002J&\u0010\t\u001a\u00020\n2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u000fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u0013"},
        d2 = {"Lcom/marshalltechnology/video_stream/CameraPermissions;", "", "()V", "ongoing", "", "hasAudioPermission", "activity", "Landroid/app/Activity;", "hasCameraPermission", "requestPermissions", "", "permissionsRegistry", "Lcom/marshalltechnology/video_stream/PermissionStuff;", "enableAudio", "callback", "Lcom/marshalltechnology/video_stream/CameraPermissions$ResultCallback;", "CameraRequestPermissionsListener", "Companion", "ResultCallback", "android.video_stream"}
)
public final class CameraPermissions {
    private boolean ongoing;
    private static final int CAMERA_REQUEST_ID = 9796;
    @NotNull
    public static final CameraPermissions.Companion Companion = new CameraPermissions.Companion((DefaultConstructorMarker)null);

    public final void requestPermissions(@NotNull Activity activity, @NotNull PermissionStuff permissionsRegistry, boolean enableAudio, @NotNull final CameraPermissions.ResultCallback callback) {
        Intrinsics.checkParameterIsNotNull(activity, "activity");
        Intrinsics.checkParameterIsNotNull(permissionsRegistry, "permissionsRegistry");
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        if (this.ongoing) {
            callback.onResult("cameraPermission", "Camera permission request ongoing");
        }

        if (this.hasCameraPermission(activity) && (!enableAudio || this.hasAudioPermission(activity))) {
            callback.onResult((String)null, (String)null);
        } else {
            permissionsRegistry.adddListener((RequestPermissionsResultListener)(new CameraPermissions.CameraRequestPermissionsListener((CameraPermissions.ResultCallback)(new CameraPermissions.ResultCallback() {
                public void onResult(@Nullable String errorCode, @Nullable String errorDescription) {
                    CameraPermissions.this.ongoing = false;
                    callback.onResult(errorCode, errorDescription);
                }
            }))));
            this.ongoing = true;
            ActivityCompat.requestPermissions(activity, enableAudio ? new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"} : new String[]{"android.permission.CAMERA"}, 9796);
        }

    }

    private final boolean hasCameraPermission(Activity activity) {
        return ContextCompat.checkSelfPermission((Context)activity, "android.permission.CAMERA") == 0;
    }

    private final boolean hasAudioPermission(Activity activity) {
        return ContextCompat.checkSelfPermission((Context)activity, "android.permission.RECORD_AUDIO") == 0;
    }

    // $FF: synthetic method
    public static final boolean access$getOngoing$p(CameraPermissions $this) {
        return $this.ongoing;
    }

    @Metadata(
            mv = {1, 1, 18},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b`\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005H&¨\u0006\u0007"},
            d2 = {"Lcom/marshalltechnology/video_stream/CameraPermissions$ResultCallback;", "", "onResult", "", "errorCode", "", "errorDescription", "android.video_stream"}
    )
    public interface ResultCallback {
        void onResult(@Nullable String var1, @Nullable String var2);
    }

    @Metadata(
            mv = {1, 1, 18},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0002\b\u0001\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004J+\u0010\r\u001a\u00020\u00062\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u00112\u0006\u0010\u0013\u001a\u00020\u0014H\u0016¢\u0006\u0002\u0010\u0015R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f¨\u0006\u0016"},
            d2 = {"Lcom/marshalltechnology/video_stream/CameraPermissions$CameraRequestPermissionsListener;", "Lio/flutter/plugin/common/PluginRegistry$RequestPermissionsResultListener;", "callback", "Lcom/marshalltechnology/video_stream/CameraPermissions$ResultCallback;", "(Lcom/marshalltechnology/video_stream/CameraPermissions$ResultCallback;)V", "alreadyCalled", "", "getAlreadyCalled", "()Z", "setAlreadyCalled", "(Z)V", "getCallback", "()Lcom/marshalltechnology/video_stream/CameraPermissions$ResultCallback;", "onRequestPermissionsResult", "id", "", "permissions", "", "", "grantResults", "", "(I[Ljava/lang/String;[I)Z", "android.video_stream"}
    )
    @VisibleForTesting
    public static final class CameraRequestPermissionsListener implements RequestPermissionsResultListener {
        private boolean alreadyCalled;
        @NotNull
        private final CameraPermissions.ResultCallback callback;

        public final boolean getAlreadyCalled() {
            return this.alreadyCalled;
        }

        public final void setAlreadyCalled(boolean var1) {
            this.alreadyCalled = var1;
        }

        public boolean onRequestPermissionsResult(int id, @NotNull String[] permissions, @NotNull int[] grantResults) {
            Intrinsics.checkParameterIsNotNull(permissions, "permissions");
            Intrinsics.checkParameterIsNotNull(grantResults, "grantResults");
            if (!this.alreadyCalled && id == 9796) {
                this.alreadyCalled = true;
                if (grantResults.length != 0 && grantResults[0] == 0) {
                    if (grantResults.length > 1 && grantResults[1] != 0) {
                        this.callback.onResult("cameraPermission", "MediaRecorderAudio permission not granted");
                    } else {
                        this.callback.onResult((String)null, (String)null);
                    }
                } else {
                    this.callback.onResult("cameraPermission", "MediaRecorderCamera permission not granted");
                }

                return true;
            } else {
                return false;
            }
        }

        @NotNull
        public final CameraPermissions.ResultCallback getCallback() {
            return this.callback;
        }

        @VisibleForTesting
        public CameraRequestPermissionsListener(@NotNull CameraPermissions.ResultCallback callback) {
            Intrinsics.checkParameterIsNotNull(callback, "callback");
            super();
            this.callback = callback;
        }
    }

    @Metadata(
            mv = {1, 1, 18},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0005"},
            d2 = {"Lcom/marshalltechnology/video_stream/CameraPermissions$Companion;", "", "()V", "CAMERA_REQUEST_ID", "", "android.video_stream"}
    )
    public static final class Companion {
        private Companion() {
        }

        // $FF: synthetic method
        public Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }
    }
}
