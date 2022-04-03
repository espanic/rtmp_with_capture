package com.example.rtmp_with_capture;


import android.content.Context;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.pedro.encoder.Frame;
import com.pedro.encoder.audio.AudioEncoder;
import com.pedro.encoder.audio.GetAacData;
import com.pedro.encoder.input.audio.CustomAudioEffect;
import com.pedro.encoder.input.audio.GetMicrophoneData;
import com.pedro.encoder.input.audio.MicrophoneManager;
import com.pedro.encoder.utils.CodecUtil.Force;
import com.pedro.encoder.video.FormatVideoEncoder;
import com.pedro.encoder.video.GetVideoData;
import com.pedro.rtplibrary.util.FpsListener;
import com.pedro.rtplibrary.util.RecordController;
import com.pedro.rtplibrary.util.FpsListener.Callback;
import com.pedro.rtplibrary.util.RecordController.Listener;
import com.pedro.rtplibrary.util.RecordController.Status;
import com.pedro.rtplibrary.view.OffScreenGlThread;
import java.nio.ByteBuffer;
import kotlin.Metadata;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import net.ossrs.rtmp.ConnectCheckerRtmp;
import net.ossrs.rtmp.SrsFlvMuxer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
        mv = {1, 1, 18},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u0000È\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b#\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0005\n\u0002\b\u000f\b\u0007\u0018\u0000 ¢\u00012\u00020\u00012\u00020\u00022\u00020\u00032\u00020\u00042\u00020\u00052\u00020\u0006:\u0002¢\u0001B%\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\n\u0012\u0006\u0010\f\u001a\u00020\u0006¢\u0006\u0002\u0010\rJ\u0006\u0010=\u001a\u00020>J\u0006\u0010?\u001a\u00020>J\u0018\u0010@\u001a\u00020>2\u0006\u0010A\u001a\u00020B2\u0006\u0010C\u001a\u00020DH\u0016J\u0016\u0010E\u001a\u00020>2\u0006\u0010A\u001a\u00020B2\u0006\u0010C\u001a\u00020DJ\u0006\u0010F\u001a\u00020\u0013J\u0006\u0010G\u001a\u00020\u0013J\u0016\u0010H\u001a\u00020>2\u0006\u0010I\u001a\u00020B2\u0006\u0010C\u001a\u00020DJ\u0006\u0010J\u001a\u00020\u0013J\u0006\u0010K\u001a\u00020\u0013J\u0018\u0010L\u001a\u00020>2\u0006\u0010I\u001a\u00020B2\u0006\u0010C\u001a\u00020DH\u0016J\u0010\u0010M\u001a\u00020>2\u0006\u0010N\u001a\u00020OH\u0016J\u0010\u0010P\u001a\u00020>2\u0006\u0010Q\u001a\u00020RH\u0016J\b\u0010S\u001a\u00020>H\u0016J\b\u0010T\u001a\u00020>H\u0016J\u0010\u0010U\u001a\u00020>2\u0006\u0010V\u001a\u00020WH\u0016J\b\u0010X\u001a\u00020>H\u0016J\b\u0010Y\u001a\u00020>H\u0016J\u0010\u0010Z\u001a\u00020>2\u0006\u0010[\u001a\u00020\u0013H\u0016J\u0010\u0010\\\u001a\u00020>2\u0006\u0010]\u001a\u00020\u001cH\u0016J\u0018\u0010^\u001a\u00020>2\u0006\u0010_\u001a\u00020B2\u0006\u0010`\u001a\u00020BH\u0016J \u0010a\u001a\u00020>2\u0006\u0010_\u001a\u00020B2\u0006\u0010`\u001a\u00020B2\u0006\u0010b\u001a\u00020BH\u0016J \u0010c\u001a\u00020>2\u0006\u0010_\u001a\u00020B2\u0006\u0010`\u001a\u00020B2\b\u0010b\u001a\u0004\u0018\u00010BJ\u0010\u0010d\u001a\u00020>2\u0006\u0010e\u001a\u00020fH\u0016J\u0010\u0010g\u001a\u00020>2\u0006\u0010Q\u001a\u00020RH\u0016J\u0006\u0010h\u001a\u00020>J\u0006\u0010i\u001a\u00020>J:\u0010j\u001a\u00020\n2\b\b\u0002\u0010]\u001a\u00020\u00132\b\b\u0002\u0010k\u001a\u00020\u00132\b\b\u0002\u0010l\u001a\u00020\n2\b\b\u0002\u0010m\u001a\u00020\n2\b\b\u0002\u0010n\u001a\u00020\nH\u0007J\u0016\u0010o\u001a\u00020>2\u0006\u0010l\u001a\u00020\n2\u0006\u0010k\u001a\u00020\u0013J\u0010\u0010p\u001a\u00020>2\u0006\u0010q\u001a\u00020\u0013H\u0002J6\u0010r\u001a\u00020\n2\u0006\u0010s\u001a\u00020\u00132\u0006\u0010t\u001a\u00020\u00132\u0006\u0010[\u001a\u00020\u00132\u0006\u0010]\u001a\u00020\u00132\u0006\u0010u\u001a\u00020\n2\u0006\u0010q\u001a\u00020\u0013JT\u0010r\u001a\u00020\n2\u0006\u0010s\u001a\u00020\u00132\u0006\u0010t\u001a\u00020\u00132\u0006\u0010[\u001a\u00020\u00132\u0006\u0010]\u001a\u00020\u00132\u0006\u0010u\u001a\u00020\n2\u0006\u0010v\u001a\u00020\u00132\u0006\u0010q\u001a\u00020\u00132\b\b\u0002\u0010w\u001a\u00020\u00132\b\b\u0002\u0010x\u001a\u00020\u0013H\u0007J\u000e\u0010y\u001a\u00020>2\u0006\u0010z\u001a\u00020\u001cJ\u0016\u0010{\u001a\u00020\n2\u0006\u0010z\u001a\u00020\u001c2\u0006\u0010V\u001a\u00020WJ\u0006\u0010|\u001a\u00020>J\u0006\u0010}\u001a\u00020>J\u0006\u0010~\u001a\u00020>J\u0006\u0010\u007f\u001a\u00020>J\t\u0010\u0080\u0001\u001a\u00020>H\u0002J\u0010\u0010\u0081\u0001\u001a\u00020>2\u0007\u0010\u0082\u0001\u001a\u00020\u0013J\u0007\u0010\u0083\u0001\u001a\u00020>J\u0007\u0010\u0084\u0001\u001a\u00020>J\u0019\u0010\u0085\u0001\u001a\u00020>2\u0007\u0010\u0086\u0001\u001a\u00020W2\u0007\u0010\u0087\u0001\u001a\u00020WJ\u0013\u0010\u0088\u0001\u001a\u00020>2\n\u0010\u0089\u0001\u001a\u0005\u0018\u00010\u008a\u0001J\u001b\u0010\u008b\u0001\u001a\u00020>2\b\u0010\u008c\u0001\u001a\u00030\u008d\u00012\b\u0010\u008e\u0001\u001a\u00030\u008d\u0001J\u0012\u0010\u008f\u0001\u001a\u00020>2\t\u0010\u0090\u0001\u001a\u0004\u0018\u00010\u0004J\u000f\u0010\u0091\u0001\u001a\u00020>2\u0006\u0010[\u001a\u00020\u0013J\u0011\u0010\u0092\u0001\u001a\u00020>2\b\u0010\u0093\u0001\u001a\u00030\u0094\u0001J\u0010\u0010\u0095\u0001\u001a\u00020>2\u0007\u0010\u0096\u0001\u001a\u00020\u0013J\u000f\u0010\u0097\u0001\u001a\u00020>2\u0006\u0010]\u001a\u00020\u0013J\u000f\u0010\u0098\u0001\u001a\u00020\n2\u0006\u0010V\u001a\u00020WJ\u0007\u0010\u0099\u0001\u001a\u00020>J\u0010\u0010\u009a\u0001\u001a\u00020>2\u0007\u0010\u009b\u0001\u001a\u00020WJ\u0010\u0010\u009c\u0001\u001a\u00020>2\u0007\u0010\u009d\u0001\u001a\u00020WJ\u0010\u0010\u009e\u0001\u001a\u00020>2\u0007\u0010\u009d\u0001\u001a\u00020WJ\u0007\u0010\u009f\u0001\u001a\u00020>J\u0007\u0010 \u0001\u001a\u00020>J\u0007\u0010¡\u0001\u001a\u00020>R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e¢\u0006\u0002\n\u0000R\u0011\u0010\u0012\u001a\u00020\u00138F¢\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\f\u001a\u00020\u0006¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0007\u001a\u00020\b¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u000e\u0010\u001a\u001a\u00020\u0013X\u0082\u000e¢\u0006\u0002\n\u0000R\u0011\u0010\u001b\u001a\u00020\u001c8F¢\u0006\u0006\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u001f\u001a\u00020\u001c8F¢\u0006\u0006\u001a\u0004\b \u0010\u001eR\u000e\u0010!\u001a\u00020\"X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010%\u001a\u00020&8F¢\u0006\u0006\u001a\u0004\b'\u0010(R\u0011\u0010)\u001a\u00020\n8F¢\u0006\u0006\u001a\u0004\b)\u0010*R\u0011\u0010\u000b\u001a\u00020\n¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010*R\u001e\u0010,\u001a\u00020\n2\u0006\u0010+\u001a\u00020\n@BX\u0086\u000e¢\u0006\b\n\u0000\u001a\u0004\b,\u0010*R\u001e\u0010-\u001a\u00020\n2\u0006\u0010+\u001a\u00020\n@BX\u0086\u000e¢\u0006\b\n\u0000\u001a\u0004\b-\u0010*R\u000e\u0010.\u001a\u00020/X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u00100\u001a\u00020\nX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u00101\u001a\u00020\nX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u00102\u001a\u000203X\u0082\u000e¢\u0006\u0002\n\u0000R\u0011\u00104\u001a\u00020\u001c8F¢\u0006\u0006\u001a\u0004\b5\u0010\u001eR\u0011\u00106\u001a\u00020\u001c8F¢\u0006\u0006\u001a\u0004\b7\u0010\u001eR\u000e\u00108\u001a\u000209X\u0082\u000e¢\u0006\u0002\n\u0000R\u0011\u0010\t\u001a\u00020\n¢\u0006\b\n\u0000\u001a\u0004\b:\u0010*R\u0010\u0010;\u001a\u0004\u0018\u00010<X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006£\u0001"},
        d2 = {"Lcom/marshalltechnology/video_stream/RtmpCameraConnector;", "Lcom/pedro/encoder/audio/GetAacData;", "Lcom/pedro/encoder/video/GetVideoData;", "Lcom/pedro/encoder/input/audio/GetMicrophoneData;", "Lcom/pedro/rtplibrary/util/FpsListener$Callback;", "Lcom/pedro/rtplibrary/util/RecordController$Listener;", "Lnet/ossrs/rtmp/ConnectCheckerRtmp;", "context", "Landroid/content/Context;", "useOpenGL", "", "isPortrait", "connectChecker", "(Landroid/content/Context;ZZLnet/ossrs/rtmp/ConnectCheckerRtmp;)V", "ORIENTATIONS", "Landroid/util/SparseIntArray;", "audioEncoder", "Lcom/pedro/encoder/audio/AudioEncoder;", "cacheSize", "", "getCacheSize", "()I", "getConnectChecker", "()Lnet/ossrs/rtmp/ConnectCheckerRtmp;", "getContext", "()Landroid/content/Context;", "curFps", "droppedAudioFrames", "", "getDroppedAudioFrames", "()J", "droppedVideoFrames", "getDroppedVideoFrames", "fpsListener", "Lcom/pedro/rtplibrary/util/FpsListener;", "glInterface", "Lcom/pedro/rtplibrary/view/OffScreenGlThread;", "inputSurface", "Landroid/view/Surface;", "getInputSurface", "()Landroid/view/Surface;", "isAudioMuted", "()Z", "<set-?>", "isRecording", "isStreaming", "microphoneManager", "Lcom/pedro/encoder/input/audio/MicrophoneManager;", "pausedRecording", "pausedStreaming", "recordController", "Lcom/pedro/rtplibrary/util/RecordController;", "sentAudioFrames", "getSentAudioFrames", "sentVideoFrames", "getSentVideoFrames", "srsFlvMuxer", "Lnet/ossrs/rtmp/SrsFlvMuxer;", "getUseOpenGL", "videoEncoder", "Lcom/marshalltechnology/video_stream/VideoEncoder;", "disableAudio", "", "enableAudio", "getAacData", "aacBuffer", "Ljava/nio/ByteBuffer;", "info", "Landroid/media/MediaCodec$BufferInfo;", "getAacDataRtp", "getBitrate", "getFps", "getH264DataRtp", "h264Buffer", "getStreamHeight", "getStreamWidth", "getVideoData", "inputPCMData", "frame", "Lcom/pedro/encoder/Frame;", "onAudioFormat", "mediaFormat", "Landroid/media/MediaFormat;", "onAuthErrorRtmp", "onAuthSuccessRtmp", "onConnectionFailedRtmp", "reason", "", "onConnectionSuccessRtmp", "onDisconnectRtmp", "onFps", "fps", "onNewBitrateRtmp", "bitrate", "onSpsPps", "sps", "pps", "onSpsPpsVps", "vps", "onSpsPpsVpsRtp", "onStatusChange", "status", "Lcom/pedro/rtplibrary/util/RecordController$Status;", "onVideoFormat", "pauseRecord", "pauseStream", "prepareAudio", "sampleRate", "isStereo", "echoCanceler", "noiseSuppressor", "prepareAudioRtp", "prepareGlInterface", "rotation", "prepareVideo", "width", "height", "hardwareRotation", "iFrameInterval", "avcProfile", "avcProfileLevel", "reConnect", "delay", "reTry", "resetDroppedAudioFrames", "resetDroppedVideoFrames", "resetSentAudioFrames", "resetSentVideoFrames", "resetVideoEncoder", "resizeCache", "newSize", "resumeRecord", "resumeStream", "setAuthorization", "user", "password", "setCustomAudioEffect", "customAudioEffect", "Lcom/pedro/encoder/input/audio/CustomAudioEffect;", "setForce", "forceVideo", "Lcom/pedro/encoder/utils/CodecUtil$Force;", "forceAudio", "setFpsListener", "callback", "setLimitFPSOnFly", "setProfileIop", "profileIop", "", "setReTries", "reTries", "setVideoBitrateOnFly", "shouldRetry", "startEncoders", "startRecord", "path", "startStream", "url", "startStreamRtp", "stopRecord", "stopStream", "stopStreamRtp", "Companion", "android.video_stream"}
)
@RequiresApi(
        api = 21
)
public final class RtmpCameraConnector implements GetAacData, GetVideoData, GetMicrophoneData, Callback, Listener, ConnectCheckerRtmp {
    private VideoEncoder videoEncoder;
    private MicrophoneManager microphoneManager;
    private AudioEncoder audioEncoder;
    private SrsFlvMuxer srsFlvMuxer;
    private int curFps;
    private boolean pausedStreaming;
    private boolean pausedRecording;
    private final OffScreenGlThread glInterface;
    private RecordController recordController;
    private boolean isStreaming;
    private boolean isRecording;
    private final SparseIntArray ORIENTATIONS;
    private final FpsListener fpsListener;
    @NotNull
    private final Context context;
    private final boolean useOpenGL;
    private final boolean isPortrait;
    @NotNull
    private final ConnectCheckerRtmp connectChecker;
    private static final String TAG = "RtmpCameraConnector";
    @NotNull
    public static final RtmpCameraConnector.Companion Companion = new RtmpCameraConnector.Companion((DefaultConstructorMarker)null);

    public final boolean isStreaming() {
        return this.isStreaming;
    }

    public final boolean isRecording() {
        return this.isRecording;
    }

    public final void setCustomAudioEffect(@Nullable CustomAudioEffect customAudioEffect) {
        this.microphoneManager.setCustomAudioEffect(customAudioEffect);
    }

    public final void setFpsListener(@Nullable Callback callback) {
        this.fpsListener.setCallback(callback);
    }

    public final void setProfileIop(byte profileIop) {
        this.srsFlvMuxer.setProfileIop(profileIop);
    }

    @NotNull
    public final Surface getInputSurface() {
        Surface var1;
        if (this.useOpenGL) {
            var1 = this.glInterface.getSurface();
            Intrinsics.checkExpressionValueIsNotNull(var1, "glInterface.getSurface()");
            return var1;
        } else {
            VideoEncoder var10000 = this.videoEncoder;
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            var1 = var10000.getSurface();
            if (var1 == null) {
                Intrinsics.throwNpe();
            }

            return var1;
        }
    }


    public final boolean prepareVideo(int width, int height, int fps, int bitrate, boolean hardwareRotation, int iFrameInterval, int rotation, int avcProfile, int avcProfileLevel) {
        this.pausedStreaming = false;
        this.pausedRecording = false;
        this.videoEncoder = new VideoEncoder((GetVideoData)this, width, height, fps, bitrate, this.useOpenGL ? 0 : rotation, hardwareRotation, iFrameInterval, FormatVideoEncoder.SURFACE, avcProfile, avcProfileLevel);

        boolean result = videoEncoder.prepare();
        if (this.useOpenGL) {
            this.prepareGlInterface(this.ORIENTATIONS.get(rotation));

            glInterface.addMediaCodecSurface(videoEncoder.getSurface());
        }

        return result;
    }

    // $FF: synthetic method
    public static boolean prepareVideo$default(RtmpCameraConnector var0, int var1, int var2, int var3, int var4, boolean var5, int var6, int var7, int var8, int var9, int var10, Object var11) {
        if ((var10 & 128) != 0) {
            var8 = -1;
        }

        if ((var10 & 256) != 0) {
            var9 = -1;
        }

        return var0.prepareVideo(var1, var2, var3, var4, var5, var6, var7, var8, var9);
    }


    public final boolean prepareVideo(int width, int height, int fps, int bitrate, boolean hardwareRotation, int rotation) {
        return prepareVideo$default(this, width, height, fps, bitrate, hardwareRotation, 2, rotation, 0, 0, 384, (Object)null);
    }

    private final void prepareGlInterface(int rotation) {
        Log.i(TAG, "prepareGlInterface " + rotation + " " + this.isPortrait);
        OffScreenGlThread var10000 = this.glInterface;
        VideoEncoder var10001 = this.videoEncoder;
        if (var10001 == null) {
            Intrinsics.throwNpe();
        }

        int var2 = var10001.getWidth();
        VideoEncoder var10002 = this.videoEncoder;
        if (var10002 == null) {
            Intrinsics.throwNpe();
        }

        var10000.setEncoderSize(var2, var10002.getHeight());
        this.glInterface.setRotation(rotation);
        this.glInterface.start();
    }

    @JvmOverloads
    public final boolean prepareAudio(int bitrate, int sampleRate, boolean isStereo, boolean echoCanceler, boolean noiseSuppressor) {
        MicrophoneManager var10000 = this.microphoneManager;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.createMicrophone(sampleRate, isStereo, echoCanceler, noiseSuppressor);
        this.prepareAudioRtp(isStereo, sampleRate);
        AudioEncoder var6 = this.audioEncoder;
        if (var6 == null) {
            Intrinsics.throwNpe();
        }

        MicrophoneManager var10004 = this.microphoneManager;
        if (var10004 == null) {
            Intrinsics.throwNpe();
        }

        return var6.prepareAudioEncoder(bitrate, sampleRate, isStereo, var10004.getMaxInputSize());
    }

    // $FF: synthetic method
    public static boolean prepareAudio$default(RtmpCameraConnector var0, int bitrate, int sampleRate, boolean isStereo, boolean echoCancer, boolean noiseSuppressor, int var6, Object var7) {
        if ((var6 & 1) != 0) {
            bitrate = 65536;
        }

        if ((var6 & 2) != 0) {
            sampleRate = 32000;
        }

        if ((var6 & 4) != 0) {
            isStereo = true;
        }

        if ((var6 & 8) != 0) {
            echoCancer = false;
        }

        if ((var6 & 16) != 0) {
            noiseSuppressor = false;
        }

        return var0.prepareAudio(bitrate, sampleRate, isStereo, echoCancer, noiseSuppressor);
    }

    @JvmOverloads
    public final boolean prepareAudio(int bitrate, int sampleRate, boolean isStereo, boolean echoCanceler) {
        return prepareAudio$default(this, bitrate, sampleRate, isStereo, echoCanceler, false, 16, (Object)null);
    }

    @JvmOverloads
    public final boolean prepareAudio(int bitrate, int sampleRate, boolean isStereo) {
        return prepareAudio$default(this, bitrate, sampleRate, isStereo, false, false, 24, (Object)null);
    }

    @JvmOverloads
    public final boolean prepareAudio(int bitrate, int sampleRate) {
        return prepareAudio$default(this, bitrate, sampleRate, false, false, false, 28, (Object)null);
    }

    @JvmOverloads
    public final boolean prepareAudio(int bitrate) {
        return prepareAudio$default(this, bitrate, 0, false, false, false, 30, (Object)null);
    }

    @JvmOverloads
    public final boolean prepareAudio() {
        return prepareAudio$default(this, 0, 0, false, false, false, 31, (Object)null);
    }

    public final void setForce(@NotNull Force forceVideo, @NotNull Force forceAudio) {
        Intrinsics.checkParameterIsNotNull(forceVideo, "forceVideo");
        Intrinsics.checkParameterIsNotNull(forceAudio, "forceAudio");
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.setForce(forceVideo);
        AudioEncoder var3 = this.audioEncoder;
        if (var3 == null) {
            Intrinsics.throwNpe();
        }

        var3.setForce(forceAudio);
    }

    public final void startStream(@NonNull String url) {
        if (!this.isStreaming) {
            this.isStreaming = true;
            this.startStreamRtp(url);
        }
    }

    public final void startRecord(@NotNull String path) {
        Intrinsics.checkParameterIsNotNull(path, "path");
        if (!this.isRecording) {
            this.recordController.startRecord(path, (Listener)this);
            this.isRecording = true;
            if (!this.isStreaming) {
                this.startEncoders();
            }

        }
    }

    public final void stopRecord() {
        this.isRecording = false;
        this.recordController.stopRecord();
        if (!this.isStreaming) {
            this.stopStream();
        }

    }

    public final void startEncoders() {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.start();
        AudioEncoder var1 = this.audioEncoder;
        if (var1 == null) {
            Intrinsics.throwNpe();
        }

        var1.start();
        MicrophoneManager var2 = this.microphoneManager;
        if (var2 == null) {
            Intrinsics.throwNpe();
        }

        var2.start();
    }

    private final void resetVideoEncoder() {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.reset();
    }

    public final void stopStream() {
        this.isStreaming = false;
        this.stopStreamRtp();
        if (!this.isRecording) {
            MicrophoneManager var10000 = this.microphoneManager;
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            var10000.stop();
            VideoEncoder var1 = this.videoEncoder;
            if (var1 == null) {
                Intrinsics.throwNpe();
            }

            var1.stop();
            AudioEncoder var2 = this.audioEncoder;
            if (var2 == null) {
                Intrinsics.throwNpe();
            }

            var2.stop();
            this.glInterface.stop();
        }

    }

    public final void pauseStream() {
        this.pausedStreaming = true;
    }

    public final void resumeStream() {
        this.pausedStreaming = false;
    }

    public final void pauseRecord() {
        this.pausedRecording = true;
    }

    public final void resumeRecord() {
        this.pausedRecording = false;
    }

    public final boolean reTry(long delay, @NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        boolean result = this.shouldRetry(reason);
        if (result) {
            this.resetVideoEncoder();
            this.reConnect(delay);
        }

        return result;
    }

    public final void resizeCache(int newSize) throws RuntimeException {
        this.srsFlvMuxer.resizeFlvTagCache(newSize);
    }

    public final int getCacheSize() {
        return this.srsFlvMuxer.getFlvTagCacheSize();
    }

    public final long getSentAudioFrames() {
        return this.srsFlvMuxer.getSentAudioFrames();
    }

    public final long getSentVideoFrames() {
        return this.srsFlvMuxer.getSentVideoFrames();
    }

    public final long getDroppedAudioFrames() {
        return this.srsFlvMuxer.getDroppedAudioFrames();
    }

    public final long getDroppedVideoFrames() {
        return this.srsFlvMuxer.getDroppedVideoFrames();
    }

    public final void resetSentAudioFrames() {
        this.srsFlvMuxer.resetSentAudioFrames();
    }

    public final void resetSentVideoFrames() {
        this.srsFlvMuxer.resetSentVideoFrames();
    }

    public final void resetDroppedAudioFrames() {
        this.srsFlvMuxer.resetDroppedAudioFrames();
    }

    public final void resetDroppedVideoFrames() {
        this.srsFlvMuxer.resetDroppedVideoFrames();
    }

    public final void setAuthorization(@NotNull String user, @NotNull String password) {
        Intrinsics.checkParameterIsNotNull(user, "user");
        Intrinsics.checkParameterIsNotNull(password, "password");
        this.srsFlvMuxer.setAuthorization(user, password);
    }

    public final void prepareAudioRtp(boolean isStereo, int sampleRate) {
        this.srsFlvMuxer.setIsStereo(isStereo);
        this.srsFlvMuxer.setSampleRate(sampleRate);
    }

    public final void startStreamRtp(@NotNull String url) {
        Intrinsics.checkParameterIsNotNull(url, "url");
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        label39: {
            VideoEncoder var10001;
            SrsFlvMuxer var2;
            VideoEncoder var10002;
            int var3;
            if (var10000.getRotation() != 90) {
                var10000 = this.videoEncoder;
                if (var10000 == null) {
                    Intrinsics.throwNpe();
                }

                if (var10000.getRotation() != 270) {
                    var2 = this.srsFlvMuxer;
                    var10001 = this.videoEncoder;
                    if (var10001 == null) {
                        Intrinsics.throwNpe();
                    }

                    var3 = var10001.getWidth();
                    var10002 = this.videoEncoder;
                    if (var10002 == null) {
                        Intrinsics.throwNpe();
                    }

                    var2.setVideoResolution(var3, var10002.getHeight());
                    break label39;
                }
            }

            var2 = this.srsFlvMuxer;
            var10001 = this.videoEncoder;
            if (var10001 == null) {
                Intrinsics.throwNpe();
            }

            var3 = var10001.getHeight();
            var10002 = this.videoEncoder;
            if (var10002 == null) {
                Intrinsics.throwNpe();
            }

            var2.setVideoResolution(var3, var10002.getWidth());
        }

        this.srsFlvMuxer.start(url);
    }

    public final void stopStreamRtp() {
        this.srsFlvMuxer.stop();
    }

    public final void setReTries(int reTries) {
        this.srsFlvMuxer.setReTries(reTries);
    }

    public final boolean shouldRetry(@NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        return this.srsFlvMuxer.shouldRetry(reason);
    }

    public final void reConnect(long delay) {
        this.srsFlvMuxer.reConnect(delay);
    }

    public final void disableAudio() {
        MicrophoneManager var10000 = this.microphoneManager;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.mute();
    }

    public final void enableAudio() {
        MicrophoneManager var10000 = this.microphoneManager;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.unMute();
    }

    public final boolean isAudioMuted() {
        MicrophoneManager var10000 = this.microphoneManager;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        return var10000.isMuted();
    }

    public final int getBitrate() {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        int rate = var10000.getBitrate();
        return rate;
    }

    public final int getStreamWidth() {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        return var10000.getWidth();
    }

    public final int getStreamHeight() {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        return var10000.getHeight();
    }

    public final int getFps() {
        return this.curFps;
    }

    public final void setVideoBitrateOnFly(int bitrate) {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.setVideoBitrateOnFly(bitrate);
    }

    public final void setLimitFPSOnFly(int fps) {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.setLimitFps(fps);
    }

    public void getAacData(@NotNull ByteBuffer aacBuffer, @NotNull BufferInfo info) {
        Intrinsics.checkParameterIsNotNull(aacBuffer, "aacBuffer");
        Intrinsics.checkParameterIsNotNull(info, "info");
        if (this.isStreaming && !this.pausedStreaming) {
            this.getAacDataRtp(aacBuffer, info);
        }

        if (this.isRecording && !this.pausedRecording) {
            this.recordController.recordAudio(aacBuffer, info);
        }

    }

    public void onSpsPps(@NotNull ByteBuffer sps, @NotNull ByteBuffer pps) {
        Intrinsics.checkParameterIsNotNull(sps, "sps");
        Intrinsics.checkParameterIsNotNull(pps, "pps");
        if (this.isStreaming && !this.pausedStreaming) {
            this.onSpsPpsVpsRtp(sps, pps, (ByteBuffer)null);
        }

    }

    public void onSpsPpsVps(@NotNull ByteBuffer sps, @NotNull ByteBuffer pps, @NotNull ByteBuffer vps) {
        Intrinsics.checkParameterIsNotNull(sps, "sps");
        Intrinsics.checkParameterIsNotNull(pps, "pps");
        Intrinsics.checkParameterIsNotNull(vps, "vps");
        if (this.isStreaming && !this.pausedStreaming) {
            this.onSpsPpsVpsRtp(sps, pps, vps);
        }

    }

    public void getVideoData(@NotNull ByteBuffer h264Buffer, @NotNull BufferInfo info) {
        Intrinsics.checkParameterIsNotNull(h264Buffer, "h264Buffer");
        Intrinsics.checkParameterIsNotNull(info, "info");
        this.fpsListener.calculateFps();
        if (this.isStreaming && !this.pausedStreaming) {
            this.getH264DataRtp(h264Buffer, info);
        }

        if (this.isRecording && !this.pausedRecording) {
            this.recordController.recordVideo(h264Buffer, info);
        }

    }

    public void inputPCMData(@NotNull Frame frame) {
        Intrinsics.checkParameterIsNotNull(frame, "frame");
        AudioEncoder var10000 = this.audioEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        var10000.inputPCMData(frame);
    }

    public void onVideoFormat(@NotNull MediaFormat mediaFormat) {
        Intrinsics.checkParameterIsNotNull(mediaFormat, "mediaFormat");
    }

    public void onAudioFormat(@NotNull MediaFormat mediaFormat) {
        Intrinsics.checkParameterIsNotNull(mediaFormat, "mediaFormat");
    }

    public final void getAacDataRtp(@NotNull ByteBuffer aacBuffer, @NotNull BufferInfo info) {
        Intrinsics.checkParameterIsNotNull(aacBuffer, "aacBuffer");
        Intrinsics.checkParameterIsNotNull(info, "info");
        this.srsFlvMuxer.sendAudio(aacBuffer, info);
    }

    public final void onSpsPpsVpsRtp(@NotNull ByteBuffer sps, @NotNull ByteBuffer pps, @Nullable ByteBuffer vps) {
        Intrinsics.checkParameterIsNotNull(sps, "sps");
        Intrinsics.checkParameterIsNotNull(pps, "pps");
        this.srsFlvMuxer.setSpsPPs(sps, pps);
    }

    public final void getH264DataRtp(@NotNull ByteBuffer h264Buffer, @NotNull BufferInfo info) {
        Intrinsics.checkParameterIsNotNull(h264Buffer, "h264Buffer");
        Intrinsics.checkParameterIsNotNull(info, "info");
        this.srsFlvMuxer.sendVideo(h264Buffer, info);
    }

    public void onFps(int fps) {
        this.curFps = fps;
    }

    public void onStatusChange(@NotNull Status status) {
        Intrinsics.checkParameterIsNotNull(status, "status");
    }

    public void onConnectionSuccessRtmp() {
        VideoEncoder var10000 = this.videoEncoder;
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        if (!var10000.getRunning()) {
            this.startEncoders();
        }

        this.connectChecker.onConnectionSuccessRtmp();
    }

    public void onConnectionFailedRtmp(@NotNull String reason) {
        Intrinsics.checkParameterIsNotNull(reason, "reason");
        this.connectChecker.onConnectionFailedRtmp(reason);
    }

    public void onNewBitrateRtmp(long bitrate) {
        this.connectChecker.onNewBitrateRtmp(bitrate);
    }

    public void onDisconnectRtmp() {
        this.connectChecker.onDisconnectRtmp();
    }

    public void onAuthErrorRtmp() {
        this.connectChecker.onAuthErrorRtmp();
    }

    public void onAuthSuccessRtmp() {
        this.connectChecker.onAuthSuccessRtmp();
    }

    @NotNull
    public final Context getContext() {
        return this.context;
    }

    public final boolean getUseOpenGL() {
        return this.useOpenGL;
    }

    public final boolean isPortrait() {
        return this.isPortrait;
    }

    @NotNull
    public final ConnectCheckerRtmp getConnectChecker() {
        return this.connectChecker;
    }

    public RtmpCameraConnector(@NotNull Context context, boolean useOpenGL, boolean isPortrait, @NotNull ConnectCheckerRtmp connectChecker) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(connectChecker, "connectChecker");
        super();
        this.context = context;
        this.useOpenGL = useOpenGL;
        this.isPortrait = isPortrait;
        this.connectChecker = connectChecker;
        this.glInterface = new OffScreenGlThread(this.context);
        this.recordController = new RecordController();
        this.ORIENTATIONS = new SparseIntArray(4);
        this.fpsListener = new FpsListener();
        this.microphoneManager = new MicrophoneManager((GetMicrophoneData)this);
        this.audioEncoder = new AudioEncoder((GetAacData)this);
        this.srsFlvMuxer = new SrsFlvMuxer((ConnectCheckerRtmp)this);
        this.fpsListener.setCallback((Callback)this);
        this.curFps = 0;
        if (this.useOpenGL) {
            this.glInterface.init();
        }

        this.ORIENTATIONS.append(0, 270);
        this.ORIENTATIONS.append(90, 0);
        this.ORIENTATIONS.append(180, 90);
        this.ORIENTATIONS.append(270, 0);
    }

    @Metadata(
            mv = {1, 1, 18},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082D¢\u0006\u0002\n\u0000¨\u0006\u0005"},
            d2 = {"Lcom/marshalltechnology/video_stream/RtmpCameraConnector$Companion;", "", "()V", "TAG", "", "android.video_stream"}
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
