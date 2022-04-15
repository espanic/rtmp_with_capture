package com.example.rtmp_with_capture;

// CameraUtils.java

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CameraUtils {
    @NonNull
    public static final CameraUtils INSTANCE;

    @RequiresApi(21)
    @NonNull
    public final Size computeBestPreviewSize(@NonNull String cameraName, @NonNull ResolutionPreset preset) {
//        ResolutionPreset preset = preset;
        if (preset.ordinal() > ResolutionPreset.high.ordinal()) {
            preset = ResolutionPreset.high;
        }

        CamcorderProfile profile = this.getBestAvailableCamcorderProfileForResolutionPreset(cameraName, preset);
        return new Size(profile.videoFrameWidth, profile.videoFrameHeight);
    }

    @RequiresApi(21)
    @NonNull
    public final Size computeBestCaptureSize(@NonNull StreamConfigurationMap streamConfigurationMap) {
        Size[] outputSizes = streamConfigurationMap.getOutputSizes(256);
        Object maxSize = Collections.max((Collection)Arrays.asList((Size[])Arrays.copyOf(outputSizes, outputSizes.length)), (Comparator)(new CameraUtils.CompareSizesByArea()));
        return (Size)maxSize;
    }

    @RequiresApi(21)
    @NonNull
    public final List getAvailableCameras(@NonNull Activity activity) throws CameraAccessException {
        Object cameraservice = activity.getSystemService(Context.CAMERA_SERVICE);
        if (cameraservice == null) {
            throw new NullPointerException("camera service null!!");
        } else {
            CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            String[] cameraNames = cameraManager.getCameraIdList();
            List cameras = new ArrayList();

            for(int i = 0; i < cameraNames.length; ++i) {
                String cameraName = cameraNames[i];
                HashMap details = new HashMap();
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraName);
                details.put("name", cameraName);
                Integer sensorOrientation = (Integer)characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                label44: {
                    details.put("sensorOrientation", sensorOrientation);
                    Integer lensFacing = (Integer)characteristics.get(CameraCharacteristics.LENS_FACING);
                    boolean var14 = false;
                    if (lensFacing != null) {
                        if (lensFacing == 0) {
                            details.put("lensFacing", "front");
                            break label44;
                        }
                    }
                    if (lensFacing != null) {
                        if (lensFacing == 1) {
                            ((Map)details).put("lensFacing", "back");
                            break label44;
                        }
                    }
                    if (lensFacing != null) {
                        if (lensFacing == 2) {
                            ((Map)details).put("lensFacing", "external");
                        }
                    }
                }

                cameras.add(details);
            }

            return cameras;
        }
    }

    @NonNull
    public final CamcorderProfile getBestAvailableCamcorderProfileForResolutionPreset(@NonNull String cameraName, @Nullable ResolutionPreset preset) {
        int cameraId = Integer.parseInt(cameraName);
        switch (preset)
        {
            case max:
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_2160P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                else throw  new IllegalArgumentException("No capture session available for current capture session");
            case ultraHigh:
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_2160P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                else throw  new IllegalArgumentException("No capture session available for current capture session");
            case veryHigh:
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                else throw  new IllegalArgumentException("No capture session available for current capture session");
            case high:
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                else throw  new IllegalArgumentException("No capture session available for current capture session");
            case  medium:
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                else throw  new IllegalArgumentException("No capture session available for current capture session");
            case low:
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                else throw  new IllegalArgumentException("No capture session available for current capture session");
            default:
                if(CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) return  CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                else throw  new IllegalArgumentException("No capture session available for current capture session");
        }
    }

    private CameraUtils() {
    }

    static {
        INSTANCE = new CameraUtils();
    }

    private static final class CompareSizesByArea implements Comparator {
        public int compare(@NonNull Size lhs, @NonNull Size rhs) {
            return Long.signum((long)lhs.getWidth() * (long)lhs.getHeight() - (long)rhs.getWidth() * (long)rhs.getHeight());
        }

        // $FF: synthetic method
        // $FF: bridge method
        public int compare(Object var1, Object var2) {
            return this.compare((Size)var1, (Size)var2);
        }

        public CompareSizesByArea() {
        }
    }
}



