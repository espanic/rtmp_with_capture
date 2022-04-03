package com.example.rtmp_with_capture;

// CameraUtils.java

import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.util.Size;
import androidx.annotation.RequiresApi;
import com.marshalltechnology.video_stream.Camera.ResolutionPreset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CameraUtils {
    @NotNull
    public static final CameraUtils INSTANCE;

    @RequiresApi(21)
    @NotNull
    public final Size computeBestPreviewSize(@NotNull String cameraName, @NotNull ResolutionPreset preset) {
        Intrinsics.checkParameterIsNotNull(cameraName, "cameraName");
        Intrinsics.checkParameterIsNotNull(preset, "preset");
        ResolutionPreset preset = preset;
        if (preset.ordinal() > ResolutionPreset.high.ordinal()) {
            preset = ResolutionPreset.high;
        }

        CamcorderProfile profile = this.getBestAvailableCamcorderProfileForResolutionPreset(cameraName, preset);
        return new Size(profile.videoFrameWidth, profile.videoFrameHeight);
    }

    @RequiresApi(21)
    @NotNull
    public final Size computeBestCaptureSize(@NotNull StreamConfigurationMap streamConfigurationMap) {
        Intrinsics.checkParameterIsNotNull(streamConfigurationMap, "streamConfigurationMap");
        Size[] var10000 = streamConfigurationMap.getOutputSizes(256);
        Object var2 = Collections.max((Collection)Arrays.asList((Size[])Arrays.copyOf(var10000, var10000.length)), (Comparator)(new CameraUtils.CompareSizesByArea()));
        Intrinsics.checkExpressionValueIsNotNull(var2, "Collections.max(\n       …    CompareSizesByArea())");
        return (Size)var2;
    }

    @RequiresApi(21)
    @NotNull
    public final List getAvailableCameras(@NotNull Activity activity) throws CameraAccessException {
        Intrinsics.checkParameterIsNotNull(activity, "activity");
        Object var10000 = activity.getSystemService("camera");
        if (var10000 == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.hardware.camera2.CameraManager");
        } else {
            CameraManager cameraManager = (CameraManager)var10000;
            String[] var16 = cameraManager.getCameraIdList();
            Intrinsics.checkExpressionValueIsNotNull(var16, "cameraManager.cameraIdList");
            String[] cameraNames = var16;
            List cameras = (List)(new ArrayList());
            String[] var7 = cameraNames;
            int var8 = cameraNames.length;

            for(int var6 = 0; var6 < var8; ++var6) {
                String cameraName = var7[var6];
                HashMap details = new HashMap();
                CameraCharacteristics var17 = cameraManager.getCameraCharacteristics(cameraName);
                Intrinsics.checkExpressionValueIsNotNull(var17, "cameraManager.getCameraCharacteristics(cameraName)");
                CameraCharacteristics characteristics = var17;
                Map var18 = (Map)details;
                Intrinsics.checkExpressionValueIsNotNull(cameraName, "cameraName");
                var18.put("name", cameraName);
                Integer sensorOrientation = (Integer)characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                var18 = (Map)details;
                if (sensorOrientation == null) {
                    Intrinsics.throwNpe();
                }

                label44: {
                    var18.put("sensorOrientation", sensorOrientation);
                    Integer lensFacing = (Integer)characteristics.get(CameraCharacteristics.LENS_FACING);
                    boolean var14 = false;
                    if (lensFacing != null) {
                        if (lensFacing == 0) {
                            ((Map)details).put("lensFacing", "front");
                            break label44;
                        }
                    }

                    byte var15 = 1;
                    if (lensFacing != null) {
                        if (lensFacing == var15) {
                            ((Map)details).put("lensFacing", "back");
                            break label44;
                        }
                    }

                    var15 = 2;
                    if (lensFacing != null) {
                        if (lensFacing == var15) {
                            ((Map)details).put("lensFacing", "external");
                        }
                    }
                }

                cameras.add(details);
            }

            return cameras;
        }
    }

    @NotNull
    public final CamcorderProfile getBestAvailableCamcorderProfileForResolutionPreset(@NotNull String cameraName, @Nullable ResolutionPreset preset) {
        // $FF: Couldn't be decompiled
    }

    private CameraUtils() {
    }

    static {
        CameraUtils var0 = new CameraUtils();
        INSTANCE = var0;
    }

    @Metadata(
            mv = {1, 1, 18},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016¨\u0006\b"},
            d2 = {"Lcom/marshalltechnology/video_stream/CameraUtils$CompareSizesByArea;", "Ljava/util/Comparator;", "Landroid/util/Size;", "()V", "compare", "", "lhs", "rhs", "android.video_stream"}
    )
    private static final class CompareSizesByArea implements Comparator {
        public int compare(@NotNull Size lhs, @NotNull Size rhs) {
            Intrinsics.checkParameterIsNotNull(lhs, "lhs");
            Intrinsics.checkParameterIsNotNull(rhs, "rhs");
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
// CameraUtils$WhenMappings.java
package com.marshalltechnology.video_stream;

        import com.marshalltechnology.video_stream.Camera.ResolutionPreset;
        import kotlin.Metadata;

// $FF: synthetic class
@Metadata(
        mv = {1, 1, 18},
        bv = {1, 0, 3},
        k = 3
)
public final class CameraUtils$WhenMappings {
    // $FF: synthetic field
    public static final int[] $EnumSwitchMapping$0 = new int[ResolutionPreset.values().length];

    static {
        $EnumSwitchMapping$0[ResolutionPreset.max.ordinal()] = 1;
        $EnumSwitchMapping$0[ResolutionPreset.ultraHigh.ordinal()] = 2;
        $EnumSwitchMapping$0[ResolutionPreset.veryHigh.ordinal()] = 3;
        $EnumSwitchMapping$0[ResolutionPreset.high.ordinal()] = 4;
        $EnumSwitchMapping$0[ResolutionPreset.medium.ordinal()] = 5;
        $EnumSwitchMapping$0[ResolutionPreset.low.ordinal()] = 6;
    }
}
