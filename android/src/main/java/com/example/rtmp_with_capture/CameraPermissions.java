package com.example.rtmp_with_capture;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

public final class CameraPermissions {
    private boolean ongoing;
    private static final int CAMERA_REQUEST_ID = 9796;

//    public static final CameraPermissions.Companion Companion = new CameraPermissions.Companion((DefaultConstructorMarker)null);

    public final void requestPermissions(@NonNull Activity activity, @NonNull PermissionStuff permissionsRegistry, boolean enableAudio, @NonNull final CameraPermissions.ResultCallback callback) {
        if (this.ongoing) {
            callback.onResult("cameraPermission", "Camera permission request ongoing");
        }

        if (this.hasCameraPermission(activity) && (!enableAudio || this.hasAudioPermission(activity))) {
            callback.onResult((String)null, (String)null);
        } else {
            permissionsRegistry.addListener((RequestPermissionsResultListener)(new CameraPermissions.CameraRequestPermissionsListener((CameraPermissions.ResultCallback)(new CameraPermissions.ResultCallback() {
                public void onResult(@Nullable String errorCode, @Nullable String errorDescription) {
                    CameraPermissions.this.ongoing = false;
                    callback.onResult(errorCode, errorDescription);
                }
            }))));
            this.ongoing = true;
            ActivityCompat.requestPermissions(activity, enableAudio ? new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"} : new String[]{"android.permission.CAMERA"}, CAMERA_REQUEST_ID);
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


    public interface ResultCallback {
        void onResult(@Nullable String var1, @Nullable String var2);
    }


    @VisibleForTesting
    public static final class CameraRequestPermissionsListener implements RequestPermissionsResultListener {
        private boolean alreadyCalled;
        @NonNull
        private final CameraPermissions.ResultCallback callback;

        public final boolean getAlreadyCalled() {
            return this.alreadyCalled;
        }

        public final void setAlreadyCalled(boolean var1) {
            this.alreadyCalled = var1;
        }

        public boolean onRequestPermissionsResult(int id, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (!this.alreadyCalled && id == CAMERA_REQUEST_ID) {
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

        @NonNull
        public final CameraPermissions.ResultCallback getCallback() {
            return this.callback;
        }

        @VisibleForTesting
        public CameraRequestPermissionsListener(@NonNull CameraPermissions.ResultCallback callback) {
            super();
            this.callback = callback;
        }
    }
}
