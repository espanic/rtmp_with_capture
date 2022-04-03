package com.example.rtmp_with_capture;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.Callback;
import android.media.MediaCodec.CodecException;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Build.VERSION;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import androidx.annotation.RequiresApi;
import com.pedro.encoder.input.video.FpsLimiter;
import com.pedro.encoder.utils.CodecUtil;
import com.pedro.encoder.utils.CodecUtil.Force;
import com.pedro.encoder.utils.yuv.YUVUtil;
import com.pedro.encoder.video.FormatVideoEncoder;
import com.pedro.encoder.video.GetVideoData;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
        mv = {1, 1, 18},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u0000\u009a\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u000e\u0018\u0000 u2\u00020\u0001:\u0001uBa\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\u0006\u0010\t\u001a\u00020\u0005\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\u0005\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0005¢\u0006\u0002\u0010\u0011J\u0018\u0010S\u001a\u00020T2\u0006\u0010U\u001a\u00020V2\u0006\u0010\u0018\u001a\u00020\u0019H\u0004J\u0012\u0010W\u001a\u0004\u0018\u00010\u000e2\u0006\u0010X\u001a\u00020YH\u0002J\u0012\u0010Z\u001a\u0004\u0018\u00010Y2\u0006\u0010[\u001a\u00020MH\u0004J\b\u0010\\\u001a\u00020TH\u0003J&\u0010]\u001a\u0010\u0012\u0004\u0012\u00020V\u0012\u0004\u0012\u00020V\u0018\u00010^2\u0006\u0010_\u001a\u00020V2\u0006\u0010`\u001a\u00020\u0005H\u0002J\u0016\u0010a\u001a\b\u0012\u0004\u0012\u00020V0b2\u0006\u0010c\u001a\u00020VH\u0002J\b\u0010d\u001a\u00020TH\u0007J\u0016\u0010e\u001a\u00020T2\u0006\u0010f\u001a\u00020\u001d2\u0006\u0010g\u001a\u00020hJ\b\u0010i\u001a\u00020TH\u0004J\u001e\u0010j\u001a\u00020T2\u0006\u0010f\u001a\u00020\u001d2\u0006\u0010k\u001a\u00020\u00052\u0006\u0010\u0018\u001a\u00020\u0019J\u0006\u0010l\u001a\u00020\u000bJ(\u0010m\u001a\u00020T2\u0006\u0010U\u001a\u00020V2\u0006\u0010f\u001a\u00020\u001d2\u0006\u0010k\u001a\u00020\u00052\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u0006\u0010n\u001a\u00020TJ\u0018\u0010o\u001a\u00020T2\u0006\u0010U\u001a\u00020V2\u0006\u0010\u0018\u001a\u00020\u0019H\u0004J\u0010\u0010p\u001a\u00020T2\u0006\u0010g\u001a\u00020hH\u0002J\u0010\u0010q\u001a\u00020T2\u0006\u0010\b\u001a\u00020\u0005H\u0007J\u0006\u0010r\u001a\u00020TJ\u0006\u0010s\u001a\u00020TJ\b\u0010t\u001a\u00020TH\u0004R\u0011\u0010\u000f\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0010\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0013R\u001a\u0010\b\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0013\"\u0004\b\u0016\u0010\u0017R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0082\u000e¢\u0006\u0002\n\u0000R\u001c\u0010\u001c\u001a\u0004\u0018\u00010\u001dX\u0084\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u001f\"\u0004\b \u0010!R\u0011\u0010\n\u001a\u00020\u000b¢\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u001a\u0010$\u001a\u00020%X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b&\u0010'\"\u0004\b(\u0010)R\u0011\u0010\r\u001a\u00020\u000e¢\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u001a\u0010\u0007\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b,\u0010\u0013\"\u0004\b-\u0010\u0017R\u000e\u0010.\u001a\u00020/X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u000e\u00102\u001a\u000203X\u0082\u000e¢\u0006\u0002\n\u0000R\u0011\u0010\u0006\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b4\u0010\u0013R\u0011\u0010\f\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b5\u0010\u0013R\u000e\u00106\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u001a\u00107\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b8\u0010\u0013\"\u0004\b9\u0010\u0017R\u001a\u0010:\u001a\u00020;X\u0084\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b<\u0010=\"\u0004\b>\u0010?R\u0011\u0010\t\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b@\u0010\u0013R\u001a\u0010A\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bB\u0010#\"\u0004\bC\u0010DR\u000e\u0010E\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u001c\u0010F\u001a\u0004\u0018\u00010GX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bH\u0010I\"\u0004\bJ\u0010KR\u001a\u0010L\u001a\u00020MX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\bN\u0010O\"\u0004\bP\u0010QR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\bR\u0010\u0013¨\u0006v"},
        d2 = {"Lcom/marshalltechnology/video_stream/VideoEncoder;", "", "getVideoData", "Lcom/pedro/encoder/video/GetVideoData;", "width", "", "height", "fps", "bitrate", "rotation", "doRotation", "", "iFrameInterval", "formatVideoEncoder", "Lcom/pedro/encoder/video/FormatVideoEncoder;", "avcProfile", "avcProfileLevel", "(Lcom/pedro/encoder/video/GetVideoData;IIIIIZILcom/pedro/encoder/video/FormatVideoEncoder;II)V", "getAvcProfile", "()I", "getAvcProfileLevel", "getBitrate", "setBitrate", "(I)V", "bufferInfo", "Landroid/media/MediaCodec$BufferInfo;", "callback", "Landroid/media/MediaCodec$Callback;", "codec", "Landroid/media/MediaCodec;", "getCodec", "()Landroid/media/MediaCodec;", "setCodec", "(Landroid/media/MediaCodec;)V", "getDoRotation", "()Z", "force", "Lcom/pedro/encoder/utils/CodecUtil$Force;", "getForce", "()Lcom/pedro/encoder/utils/CodecUtil$Force;", "setForce", "(Lcom/pedro/encoder/utils/CodecUtil$Force;)V", "getFormatVideoEncoder", "()Lcom/pedro/encoder/video/FormatVideoEncoder;", "getFps", "setFps", "fpsLimiter", "Lcom/pedro/encoder/input/video/FpsLimiter;", "getGetVideoData", "()Lcom/pedro/encoder/video/GetVideoData;", "handlerThread", "Landroid/os/HandlerThread;", "getHeight", "getIFrameInterval", "isBufferMode", "limitFps", "getLimitFps", "setLimitFps", "presentTimeUs", "", "getPresentTimeUs", "()J", "setPresentTimeUs", "(J)V", "getRotation", "running", "getRunning", "setRunning", "(Z)V", "spsPpsSetted", "surface", "Landroid/view/Surface;", "getSurface", "()Landroid/view/Surface;", "setSurface", "(Landroid/view/Surface;)V", "type", "", "getType", "()Ljava/lang/String;", "setType", "(Ljava/lang/String;)V", "getWidth", "checkBuffer", "", "byteBuffer", "Ljava/nio/ByteBuffer;", "chooseColorDynamically", "mediaCodecInfo", "Landroid/media/MediaCodecInfo;", "chooseEncoder", "mime", "createAsyncCallback", "decodeSpsPpsFromBuffer", "Landroid/util/Pair;", "outputBuffer", "length", "extractVpsSpsPpsFromH265", "Ljava/util/List;", "csd0byteBuffer", "forceSyncFrame", "formatChanged", "mediaCodec", "mediaFormat", "Landroid/media/MediaFormat;", "getDataFromEncoder", "outputAvailable", "outBufferIndex", "prepare", "processOutput", "reset", "sendBuffer", "sendSPSandPPS", "setVideoBitrateOnFly", "start", "stop", "stopImp", "Companion", "android.video_stream"}
)
public final class VideoEncoder {
    private boolean spsPpsSetted;
    @Nullable
    private Surface surface;
    private final FpsLimiter fpsLimiter;
    @NotNull
    private String type;
    private HandlerThread handlerThread;
    @Nullable
    private MediaCodec codec;
    private Callback callback;
    private boolean isBufferMode;
    private long presentTimeUs;
    @NotNull
    private Force force;
    private final BufferInfo bufferInfo;
    private volatile boolean running;
    private int limitFps;
    @NotNull
    private final GetVideoData getVideoData;
    private final int width;
    private final int height;
    private int fps;
    private int bitrate;
    private final int rotation;
    private final boolean doRotation;
    private final int iFrameInterval;
    @NotNull
    private final FormatVideoEncoder formatVideoEncoder;
    private final int avcProfile;
    private final int avcProfileLevel;
    private static final String TAG = "VideoEncoder";
    @NotNull
    public static final VideoEncoder.Companion Companion = new VideoEncoder.Companion((DefaultConstructorMarker)null);

    @Nullable
    public final Surface getSurface() {
        return this.surface;
    }

    public final void setSurface(@Nullable Surface var1) {
        this.surface = var1;
    }

    @NotNull
    public final String getType() {
        return this.type;
    }

    public final void setType(@NotNull String var1) {
        Intrinsics.checkParameterIsNotNull(var1, "<set-?>");
        this.type = var1;
    }

    @Nullable
    protected final MediaCodec getCodec() {
        return this.codec;
    }

    protected final void setCodec(@Nullable MediaCodec var1) {
        this.codec = var1;
    }

    protected final long getPresentTimeUs() {
        return this.presentTimeUs;
    }

    protected final void setPresentTimeUs(long var1) {
        this.presentTimeUs = var1;
    }

    @NotNull
    public final Force getForce() {
        return this.force;
    }

    public final void setForce(@NotNull Force var1) {
        Intrinsics.checkParameterIsNotNull(var1, "<set-?>");
        this.force = var1;
    }

    public final boolean getRunning() {
        return this.running;
    }

    public final void setRunning(boolean var1) {
        this.running = var1;
    }

    public final int getLimitFps() {
        return this.limitFps;
    }

    public final void setLimitFps(int var1) {
        this.limitFps = var1;
    }

    public final boolean prepare() {
        MediaCodecInfo encoder = this.chooseEncoder(this.type);
        FormatVideoEncoder videoEncoder = this.formatVideoEncoder;

        boolean var3;
        try {
            if (encoder == null) {
                Log.e(TAG, "Valid encoder not found");
                return false;
            }

            this.codec = MediaCodec.createByCodecName(encoder.getName());
            if (videoEncoder == FormatVideoEncoder.YUV420Dynamical) {
                videoEncoder = this.chooseColorDynamically(encoder);
                if (videoEncoder == null) {
                    Log.e(TAG, "YUV420 dynamical choose failed");
                    return false;
                }
            }

            MediaFormat videoFormat = null;
            String resolution = "" + this.width + "x" + this.height;
            MediaFormat var10000 = MediaFormat.createVideoFormat(this.type, this.width, this.height);
            Intrinsics.checkExpressionValueIsNotNull(var10000, "MediaFormat.createVideoFormat(type, width, height)");
            videoFormat = var10000;
            String var8 = TAG;
            StringBuilder var10001 = (new StringBuilder()).append("Prepare video info: ");
            if (videoEncoder == null) {
                Intrinsics.throwNpe();
            }

            Log.i(var8, var10001.append(videoEncoder.name().toString()).append(", ").append(resolution).toString());
            videoFormat.setInteger("color-format", videoEncoder.getFormatCodec());
            videoFormat.setInteger("max-input-size", 0);
            videoFormat.setInteger("bitrate", this.bitrate);
            videoFormat.setInteger("frame-rate", this.fps);
            videoFormat.setInteger("i-frame-interval", this.iFrameInterval);
            videoFormat.setInteger("rotation-degrees", this.rotation);
            if (this.avcProfile > 0 && this.avcProfileLevel > 0) {
                videoFormat.setInteger("profile", this.avcProfile);
                videoFormat.setInteger("level", this.avcProfileLevel);
            }

            MediaCodec var9 = this.codec;
            if (var9 == null) {
                Intrinsics.throwNpe();
            }

            var9.configure(videoFormat, (Surface)null, (MediaCrypto)null, 1);
            this.running = false;
            this.isBufferMode = false;
            MediaCodec var10 = this.codec;
            if (var10 == null) {
                Intrinsics.throwNpe();
            }

            this.surface = var10.createInputSurface();
            Log.i(TAG, "prepared");
            var3 = true;
        } catch (IOException var5) {
            Log.e(TAG, "Create VideoEncoder failed.", (Throwable)var5);
            var3 = false;
        } catch (IllegalStateException var6) {
            Log.e(TAG, "Create VideoEncoder failed.", (Throwable)var6);
            var3 = false;
        }

        return var3;
    }

    public final void start() {
        this.spsPpsSetted = false;
        this.presentTimeUs = System.nanoTime() / (long)1000;
        this.fpsLimiter.setFPS(this.limitFps);
        if (this.formatVideoEncoder != FormatVideoEncoder.SURFACE) {
            YUVUtil.preAllocateBuffers(this.width * this.height * 3 / 2);
        }

        this.handlerThread.start();
        Handler handler = new Handler(this.handlerThread.getLooper());
        MediaCodec var10000;
        if (VERSION.SDK_INT >= 23) {
            this.createAsyncCallback();
            var10000 = this.codec;
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            var10000.setCallback(this.callback, handler);
            var10000 = this.codec;
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            var10000.start();
        } else {
            var10000 = this.codec;
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            var10000.start();
            handler.post((Runnable)(new Runnable() {
                public final void run() {
                    while(VideoEncoder.this.getRunning()) {
                        try {
                            VideoEncoder.this.getDataFromEncoder();
                        } catch (IllegalStateException var2) {
                            Log.i(VideoEncoder.TAG, "Encoding error", (Throwable)var2);
                        }
                    }

                }
            }));
        }

        this.running = true;
        Log.i(TAG, "started");
    }

    protected final void stopImp() {
        if (this.handlerThread != null) {
            if (VERSION.SDK_INT >= 18) {
                this.handlerThread.quitSafely();
            } else {
                this.handlerThread.quit();
            }
        }

        this.spsPpsSetted = false;
        this.surface = (Surface)null;
        Log.i(TAG, "stopped");
    }

    public final void stop() {
        this.running = false;
        VideoEncoder var3 = this;

        VideoEncoder var10000;
        Object var1;
        try {
            var10000 = var3;
            MediaCodec var10001 = this.codec;
            if (var10001 == null) {
                Intrinsics.throwNpe();
            }

            var10001.stop();
            var10001 = this.codec;
            if (var10001 == null) {
                Intrinsics.throwNpe();
            }

            var10001.release();
            this.stopImp();
            var1 = null;
        } catch (IllegalStateException var4) {
            var10000 = this;
            var1 = null;
        } catch (NullPointerException var5) {
            var10000 = this;
            var1 = null;
        }

        var10000.codec = (MediaCodec)var1;
    }

    public final void reset() {
        this.stop();
        this.prepare();
        this.start();
    }

    private final FormatVideoEncoder chooseColorDynamically(MediaCodecInfo mediaCodecInfo) {
        int[] var4 = mediaCodecInfo.getCapabilitiesForType(this.type).colorFormats;
        int var5 = var4.length;

        for(int var3 = 0; var3 < var5; ++var3) {
            int color = var4[var3];
            if (color == FormatVideoEncoder.YUV420PLANAR.getFormatCodec()) {
                return FormatVideoEncoder.YUV420PLANAR;
            }

            if (color == FormatVideoEncoder.YUV420SEMIPLANAR.getFormatCodec()) {
                return FormatVideoEncoder.YUV420SEMIPLANAR;
            }
        }

        return null;
    }

    @RequiresApi(
            api = 19
    )
    public final void setVideoBitrateOnFly(int bitrate) {
        if (this.running) {
            this.bitrate = bitrate;
            Bundle bundle = new Bundle();
            bundle.putInt("video-bitrate", bitrate);

            try {
                MediaCodec var10000 = this.codec;
                if (var10000 == null) {
                    Intrinsics.throwNpe();
                }

                var10000.setParameters(bundle);
            } catch (IllegalStateException var4) {
                Log.e(TAG, "encoder need be running", (Throwable)var4);
            }
        }

    }

    @RequiresApi(
            api = 19
    )
    public final void forceSyncFrame() {
        if (this.running) {
            Bundle bundle = new Bundle();
            bundle.putInt("request-sync", 0);

            try {
                MediaCodec var10000 = this.codec;
                if (var10000 == null) {
                    Intrinsics.throwNpe();
                }

                var10000.setParameters(bundle);
            } catch (IllegalStateException var3) {
                Log.e(TAG, "encoder need be running", (Throwable)var3);
            }
        }

    }

    private final void sendSPSandPPS(MediaFormat mediaFormat) {
        String var10000 = this.type;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        if (var10000.equals("video/hevc")) {
            ByteBuffer var10001 = mediaFormat.getByteBuffer("csd-0");
            if (var10001 == null) {
                Intrinsics.throwNpe();
            }

            Intrinsics.checkExpressionValueIsNotNull(var10001, "mediaFormat.getByteBuffer(\"csd-0\")!!");
            List byteBufferList = this.extractVpsSpsPpsFromH265(var10001);
            GetVideoData var3 = this.getVideoData;
            if (byteBufferList == null) {
                Intrinsics.throwNpe();
            }

            var3.onSpsPpsVps((ByteBuffer)byteBufferList.get(1), (ByteBuffer)byteBufferList.get(2), (ByteBuffer)byteBufferList.get(0));
        } else {
            this.getVideoData.onSpsPps(mediaFormat.getByteBuffer("csd-0"), mediaFormat.getByteBuffer("csd-1"));
        }

    }

    @Nullable
    protected final MediaCodecInfo chooseEncoder(@NotNull String mime) {
        Intrinsics.checkParameterIsNotNull(mime, "mime");
        List var10000;
        if (this.force == Force.HARDWARE) {
            var10000 = CodecUtil.getAllHardwareEncoders(mime);
            if (var10000 == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.util.List<android.media.MediaCodecInfo>");
            }
        } else if (this.force == Force.SOFTWARE) {
            var10000 = CodecUtil.getAllSoftwareEncoders(mime);
            if (var10000 == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.util.List<android.media.MediaCodecInfo>");
            }
        } else {
            var10000 = CodecUtil.getAllEncoders(mime);
            if (var10000 == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.util.List<android.media.MediaCodecInfo>");
            }
        }

        List mediaCodecInfoList = var10000;
        if (mediaCodecInfoList == null) {
            Intrinsics.throwNpe();
        }

        Iterator var4 = mediaCodecInfoList.iterator();

        while(var4.hasNext()) {
            MediaCodecInfo mci = (MediaCodecInfo)var4.next();
            String var12 = TAG;
            StringCompanionObject var5 = StringCompanionObject.INSTANCE;
            String var6 = "VideoEncoder %s";
            Object[] var7 = new Object[]{mci.getName()};
            boolean var8 = false;
            String var10001 = String.format(var6, Arrays.copyOf(var7, var7.length));
            Intrinsics.checkExpressionValueIsNotNull(var10001, "java.lang.String.format(format, *args)");
            Log.i(var12, var10001);
            CodecCapabilities var13 = mci.getCapabilitiesForType(mime);
            Intrinsics.checkExpressionValueIsNotNull(var13, "mci.getCapabilitiesForType(mime)");
            CodecCapabilities codecCapabilities = var13;
            int[] var15 = codecCapabilities.colorFormats;
            int var9 = var15.length;

            for(int var14 = 0; var14 < var9; ++var14) {
                int color = var15[var14];
                Log.i(TAG, "Color supported: " + color);
                if (this.formatVideoEncoder == FormatVideoEncoder.SURFACE) {
                    if (color == FormatVideoEncoder.SURFACE.getFormatCodec()) {
                        return mci;
                    }
                } else if (color == FormatVideoEncoder.YUV420PLANAR.getFormatCodec() || color == FormatVideoEncoder.YUV420SEMIPLANAR.getFormatCodec()) {
                    return mci;
                }
            }
        }

        return null;
    }

    private final Pair decodeSpsPpsFromBuffer(ByteBuffer outputBuffer, int length) {
        byte[] mSPS = (byte[])null;
        byte[] mPPS = (byte[])null;
        byte[] csd = new byte[length];
        outputBuffer.get(csd, 0, length);
        int i = 0;
        int spsIndex = -1;

        int ppsIndex;
        for(ppsIndex = -1; i < length - 4; ++i) {
            if (csd[i] == 0 && csd[i + 1] == 0 && csd[i + 2] == 0 && csd[i + 3] == 1) {
                if (spsIndex != -1) {
                    ppsIndex = i;
                    break;
                }

                spsIndex = i;
            }
        }

        if (spsIndex != -1 && ppsIndex != -1) {
            mSPS = new byte[ppsIndex];
            System.arraycopy(csd, spsIndex, mSPS, 0, ppsIndex);
            mPPS = new byte[length - ppsIndex];
            System.arraycopy(csd, ppsIndex, mPPS, 0, length - ppsIndex);
        }

        return mSPS != null && mPPS != null ? new Pair(ByteBuffer.wrap(mSPS), ByteBuffer.wrap(mPPS)) : null;
    }

    private final List extractVpsSpsPpsFromH265(ByteBuffer csd0byteBuffer) {
        boolean var3 = false;
        List byteBufferList = (List)(new ArrayList());
        int vpsPosition = -1;
        int spsPosition = -1;
        int ppsPosition = -1;
        int contBufferInitiation = 0;
        byte[] var10000 = csd0byteBuffer.array();
        Intrinsics.checkExpressionValueIsNotNull(var10000, "csd0byteBuffer.array()");
        byte[] csdArray = var10000;
        int i = 0;

        for(int var9 = csdArray.length; i < var9; ++i) {
            if (contBufferInitiation == 3 && csdArray[i] == 1) {
                if (vpsPosition == -1) {
                    vpsPosition = i - 3;
                } else if (spsPosition == -1) {
                    spsPosition = i - 3;
                } else {
                    ppsPosition = i - 3;
                }
            }

            if (csdArray[i] == 0) {
                ++contBufferInitiation;
            } else {
                contBufferInitiation = 0;
            }
        }

        byte[] vps = new byte[spsPosition];
        byte[] sps = new byte[ppsPosition - spsPosition];
        byte[] pps = new byte[csdArray.length - ppsPosition];
        int i = 0;

        for(int var12 = csdArray.length; i < var12; ++i) {
            if (i < spsPosition) {
                vps[i] = csdArray[i];
            } else if (i < ppsPosition) {
                sps[i - spsPosition] = csdArray[i];
            } else {
                pps[i - ppsPosition] = csdArray[i];
            }
        }

        ByteBuffer var10001 = ByteBuffer.wrap(vps);
        Intrinsics.checkExpressionValueIsNotNull(var10001, "ByteBuffer.wrap(vps)");
        byteBufferList.add(var10001);
        var10001 = ByteBuffer.wrap(sps);
        Intrinsics.checkExpressionValueIsNotNull(var10001, "ByteBuffer.wrap(sps)");
        byteBufferList.add(var10001);
        var10001 = ByteBuffer.wrap(pps);
        Intrinsics.checkExpressionValueIsNotNull(var10001, "ByteBuffer.wrap(pps)");
        byteBufferList.add(var10001);
        return byteBufferList;
    }

    protected final void getDataFromEncoder() throws IllegalStateException {
        Log.i(TAG, "getDataFromEncoder");

        while(this.running) {
            MediaCodec var10000 = this.codec;
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            int outBufferIndex = var10000.dequeueOutputBuffer(this.bufferInfo, 1L);
            MediaCodec var10001;
            if (outBufferIndex == -2) {
                var10000 = this.codec;
                if (var10000 == null) {
                    Intrinsics.throwNpe();
                }

                MediaFormat var3 = var10000.getOutputFormat();
                Intrinsics.checkExpressionValueIsNotNull(var3, "codec!!.getOutputFormat()");
                MediaFormat mediaFormat = var3;
                var10001 = this.codec;
                if (var10001 == null) {
                    Intrinsics.throwNpe();
                }

                this.formatChanged(var10001, mediaFormat);
            } else {
                if (outBufferIndex < 0) {
                    break;
                }

                var10001 = this.codec;
                if (var10001 == null) {
                    Intrinsics.throwNpe();
                }

                this.outputAvailable(var10001, outBufferIndex, this.bufferInfo);
            }
        }

    }

    public final void formatChanged(@NotNull MediaCodec mediaCodec, @NotNull MediaFormat mediaFormat) {
        Intrinsics.checkParameterIsNotNull(mediaCodec, "mediaCodec");
        Intrinsics.checkParameterIsNotNull(mediaFormat, "mediaFormat");
        this.getVideoData.onVideoFormat(mediaFormat);
        this.sendSPSandPPS(mediaFormat);
        this.spsPpsSetted = true;
    }

    protected final void checkBuffer(@NotNull ByteBuffer byteBuffer, @NotNull BufferInfo bufferInfo) {
        Intrinsics.checkParameterIsNotNull(byteBuffer, "byteBuffer");
        Intrinsics.checkParameterIsNotNull(bufferInfo, "bufferInfo");
        if ((bufferInfo.flags & 2) != 0 && !this.spsPpsSetted) {
            ByteBuffer var10001 = byteBuffer.duplicate();
            Intrinsics.checkExpressionValueIsNotNull(var10001, "byteBuffer.duplicate()");
            Pair buffers = this.decodeSpsPpsFromBuffer(var10001, bufferInfo.size);
            if (buffers != null) {
                this.getVideoData.onSpsPps((ByteBuffer)buffers.first, (ByteBuffer)buffers.second);
                this.spsPpsSetted = true;
            }
        }

    }

    protected final void sendBuffer(@NotNull ByteBuffer byteBuffer, @NotNull BufferInfo bufferInfo) {
        Intrinsics.checkParameterIsNotNull(byteBuffer, "byteBuffer");
        Intrinsics.checkParameterIsNotNull(bufferInfo, "bufferInfo");
        bufferInfo.presentationTimeUs = System.nanoTime() / (long)1000 - this.presentTimeUs;
        this.getVideoData.getVideoData(byteBuffer, bufferInfo);
    }

    private final void processOutput(ByteBuffer byteBuffer, MediaCodec mediaCodec, int outBufferIndex, BufferInfo bufferInfo) throws IllegalStateException {
        if (this.running) {
            this.checkBuffer(byteBuffer, bufferInfo);
            this.sendBuffer(byteBuffer, bufferInfo);
        }

        Log.e(TAG, "releaseOutputBuffer " + outBufferIndex);
        mediaCodec.releaseOutputBuffer(outBufferIndex, false);
    }

    @RequiresApi(
            api = 23
    )
    private final void createAsyncCallback() {
        Log.i(TAG, "createAsyncCallback");
        this.callback = (Callback)(new Callback() {
            public void onInputBufferAvailable(@NotNull MediaCodec mediaCodec, int inBufferIndex) {
                Intrinsics.checkParameterIsNotNull(mediaCodec, "mediaCodec");
                Log.i(VideoEncoder.TAG, "onInputBufferAvailable ignored");
            }

            public void onOutputBufferAvailable(@NotNull MediaCodec mediaCodec, int outBufferIndex, @NotNull BufferInfo bufferInfo) {
                Intrinsics.checkParameterIsNotNull(mediaCodec, "mediaCodec");
                Intrinsics.checkParameterIsNotNull(bufferInfo, "bufferInfo");

                try {
                    VideoEncoder.this.outputAvailable(mediaCodec, outBufferIndex, bufferInfo);
                } catch (IllegalStateException var5) {
                    Log.i(VideoEncoder.TAG, "Encoding error", (Throwable)var5);
                }

            }

            public void onError(@NotNull MediaCodec mediaCodec, @NotNull CodecException e) {
                Intrinsics.checkParameterIsNotNull(mediaCodec, "mediaCodec");
                Intrinsics.checkParameterIsNotNull(e, "e");
                Log.e(VideoEncoder.TAG, "Error", (Throwable)e);
            }

            public void onOutputFormatChanged(@NotNull MediaCodec mediaCodec, @NotNull MediaFormat mediaFormat) {
                Intrinsics.checkParameterIsNotNull(mediaCodec, "mediaCodec");
                Intrinsics.checkParameterIsNotNull(mediaFormat, "mediaFormat");
                VideoEncoder.this.formatChanged(mediaCodec, mediaFormat);
            }
        });
    }

    public final void outputAvailable(@NotNull MediaCodec mediaCodec, int outBufferIndex, @NotNull BufferInfo bufferInfo) {
        Intrinsics.checkParameterIsNotNull(mediaCodec, "mediaCodec");
        Intrinsics.checkParameterIsNotNull(bufferInfo, "bufferInfo");
        Log.e(TAG, "outputAvailable " + outBufferIndex);
        ByteBuffer byteBuffer = VERSION.SDK_INT >= 21 ? mediaCodec.getOutputBuffer(outBufferIndex) : mediaCodec.getOutputBuffers()[outBufferIndex];
        if (byteBuffer == null) {
            Intrinsics.throwNpe();
        }

        this.processOutput(byteBuffer, mediaCodec, outBufferIndex, bufferInfo);
    }

    @NotNull
    public final GetVideoData getGetVideoData() {
        return this.getVideoData;
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    public final int getFps() {
        return this.fps;
    }

    public final void setFps(int var1) {
        this.fps = var1;
    }

    public final int getBitrate() {
        return this.bitrate;
    }

    public final void setBitrate(int var1) {
        this.bitrate = var1;
    }

    public final int getRotation() {
        return this.rotation;
    }

    public final boolean getDoRotation() {
        return this.doRotation;
    }

    public final int getIFrameInterval() {
        return this.iFrameInterval;
    }

    @NotNull
    public final FormatVideoEncoder getFormatVideoEncoder() {
        return this.formatVideoEncoder;
    }

    public final int getAvcProfile() {
        return this.avcProfile;
    }

    public final int getAvcProfileLevel() {
        return this.avcProfileLevel;
    }

    public VideoEncoder(@NotNull GetVideoData getVideoData, int width, int height, int fps, int bitrate, int rotation, boolean doRotation, int iFrameInterval, @NotNull FormatVideoEncoder formatVideoEncoder, int avcProfile, int avcProfileLevel) {
        Intrinsics.checkParameterIsNotNull(getVideoData, "getVideoData");
        Intrinsics.checkParameterIsNotNull(formatVideoEncoder, "formatVideoEncoder");
        super();
        this.getVideoData = getVideoData;
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.bitrate = bitrate;
        this.rotation = rotation;
        this.doRotation = doRotation;
        this.iFrameInterval = iFrameInterval;
        this.formatVideoEncoder = formatVideoEncoder;
        this.avcProfile = avcProfile;
        this.avcProfileLevel = avcProfileLevel;
        this.fpsLimiter = new FpsLimiter();
        this.type = "video/avc";
        this.handlerThread = new HandlerThread(TAG);
        this.force = Force.FIRST_COMPATIBLE_FOUND;
        this.bufferInfo = new BufferInfo();
        this.limitFps = this.fps;
    }

    // $FF: synthetic method
    public VideoEncoder(GetVideoData var1, int var2, int var3, int var4, int var5, int var6, boolean var7, int var8, FormatVideoEncoder var9, int var10, int var11, int var12, DefaultConstructorMarker var13) {
        if ((var12 & 512) != 0) {
            var10 = -1;
        }

        if ((var12 & 1024) != 0) {
            var11 = -1;
        }

        this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11);
    }

    @Metadata(
            mv = {1, 1, 18},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082D¢\u0006\u0002\n\u0000¨\u0006\u0005"},
            d2 = {"Lcom/marshalltechnology/video_stream/VideoEncoder$Companion;", "", "()V", "TAG", "", "android.video_stream"}
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
