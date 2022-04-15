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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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


public final class VideoEncoder {
    private boolean spsPpsSetted;
    @Nullable
    private Surface surface;
    private final FpsLimiter fpsLimiter;
    @NonNull
    private String type;
    private HandlerThread handlerThread;
    @Nullable
    private MediaCodec codec;
    private Callback callback;
    private boolean isBufferMode;
    private long presentTimeUs;
    @NonNull
    private Force force;
    private final BufferInfo bufferInfo;
    private volatile boolean running;
    private int limitFps;
    @NonNull
    private final GetVideoData getVideoData;
    private final int width;
    private final int height;
    private int fps;
    private int bitrate;
    private final int rotation;
    private final boolean doRotation;
    private final int iFrameInterval;
    @NonNull
    private final FormatVideoEncoder formatVideoEncoder;
    private final int avcProfile;
    private final int avcProfileLevel;
    private static final String TAG = "VideoEncoder";

    @Nullable
    public final Surface getSurface() {
        return this.surface;
    }

    public final void setSurface(@Nullable Surface var1) {
        this.surface = var1;
    }

    @NonNull
    public final String getType() {
        return this.type;
    }

    public final void setType(@NonNull String type) {
        this.type = type;
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

    @NonNull
    public final Force getForce() {
        return this.force;
    }

    public final void setForce(@NonNull Force force) {
        this.force = force;
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

        boolean success;
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


            String resolution = "" + this.width + "x" + this.height;
            MediaFormat videoFormat = MediaFormat.createVideoFormat(this.type, this.width, this.height);
            StringBuilder sb = (new StringBuilder()).append("Prepare video info: ");
            Log.i(TAG, sb.append(videoEncoder.name().toString()).append(", ").append(resolution).toString());
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


            codec.configure(videoFormat, (Surface)null, (MediaCrypto)null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            this.running = false;
            this.isBufferMode = false;
            this.surface = codec.createInputSurface();
            Log.i(TAG, "prepared");
            success = true;
        } catch (IOException var5) {
            Log.e(TAG, "Create VideoEncoder failed.", (Throwable)var5);
            success = false;
        } catch (IllegalStateException var6) {
            Log.e(TAG, "Create VideoEncoder failed.", (Throwable)var6);
            success = false;
        }

        return success;
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
        if (VERSION.SDK_INT >= 23) {
            this.createAsyncCallback();
            codec.setCallback(this.callback, handler);
            codec.start();
        } else {
            codec.start();
            handler.post((Runnable)(new Runnable() {
                public final void run() {
                    while(VideoEncoder.this.getRunning()) {
                        try {
                            VideoEncoder.this.getDataFromEncoder();
                        } catch (IllegalStateException e) {
                            Log.i(VideoEncoder.TAG, "Encoding error", e);
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


        try {
            codec.stop();
            codec.release();
            this.stopImp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        codec =null;
    }

    public final void reset() {
        this.stop();
        this.prepare();
        this.start();
    }

    private final FormatVideoEncoder chooseColorDynamically(MediaCodecInfo mediaCodecInfo) {
        int[] capabilities = mediaCodecInfo.getCapabilitiesForType(this.type).colorFormats;
        int length = capabilities.length;

        for(int i = 0; i < length; ++i) {
            int color = capabilities[i];
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
                codec.setParameters(bundle);
            } catch (IllegalStateException e) {
                Log.e(TAG, "encoder need be running", e);
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
                codec.setParameters(bundle);
            } catch (IllegalStateException var3) {
                Log.e(TAG, "encoder need be running", (Throwable)var3);
            }
        }

    }

    private final void sendSPSandPPS(MediaFormat mediaFormat) {


        if (type.equals("video/hevc")) {
            List byteBufferList = this.extractVpsSpsPpsFromH265(mediaFormat.getByteBuffer("csd-0"));
            getVideoData.onSpsPpsVps((ByteBuffer)byteBufferList.get(1), (ByteBuffer)byteBufferList.get(2), (ByteBuffer)byteBufferList.get(0));
        } else {
            this.getVideoData.onSpsPps(mediaFormat.getByteBuffer("csd-0"), mediaFormat.getByteBuffer("csd-1"));
        }

    }

    @Nullable
    protected final MediaCodecInfo chooseEncoder(@NonNull String mime) {
        List<MediaCodecInfo> mediaCodecInfoList;
        if (this.force == Force.HARDWARE) {
            mediaCodecInfoList = CodecUtil.getAllHardwareEncoders(mime);
            if (mediaCodecInfoList == null) {
                throw new NullPointerException("null cannot be cast to non-null type java.util.List<android.media.MediaCodecInfo>");
            }
        } else if (this.force == Force.SOFTWARE) {
            mediaCodecInfoList = CodecUtil.getAllSoftwareEncoders(mime);
            if (mediaCodecInfoList == null) {
                throw new NullPointerException("null cannot be cast to non-null type java.util.List<android.media.MediaCodecInfo>");
            }
        } else {
            mediaCodecInfoList = CodecUtil.getAllEncoders(mime);
            if (mediaCodecInfoList == null) {
                throw new NullPointerException("null cannot be cast to non-null type java.util.List<android.media.MediaCodecInfo>");
            }
        }


        for(MediaCodecInfo mci : mediaCodecInfoList){
            Log.i(TAG, String.format("VideoEncoder %s", mci.getName()));
            CodecCapabilities codecCapabilities = mci.getCapabilitiesForType(mime);
            for(int color : codecCapabilities.colorFormats){
                Log.i(TAG, "Color supported");
                if(formatVideoEncoder == FormatVideoEncoder.SURFACE){
                    if(color == FormatVideoEncoder.SURFACE.getFormatCodec()) return  mci;
                }else{
                    if(color == FormatVideoEncoder.YUV420PLANAR.getFormatCodec() || color == FormatVideoEncoder.YUV422SEMIPLANAR.getFormatCodec()) return  mci;
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
        byte[] csdArray = csd0byteBuffer.array();

        for(int i = 0; i < csdArray.length; i++) {
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
        for(int i = 0; i < csdArray.length; i++) {
            if (i < spsPosition) {
                vps[i] = csdArray[i];
            } else if (i < ppsPosition) {
                sps[i - spsPosition] = csdArray[i];
            } else {
                pps[i - ppsPosition] = csdArray[i];
            }
        }


        byteBufferList.add(ByteBuffer.wrap(vps));
        byteBufferList.add(ByteBuffer.wrap(sps));
        byteBufferList.add(ByteBuffer.wrap(pps));
        return byteBufferList;
    }

    protected final void getDataFromEncoder() throws IllegalStateException {
        Log.i(TAG, "getDataFromEncoder");

        while(this.running) {

            int outBufferIndex = codec.dequeueOutputBuffer(this.bufferInfo, 1L);
            MediaCodec var10001;
            if (outBufferIndex == -2) {
                this.formatChanged(codec, codec.getOutputFormat());
            } else {
                if (outBufferIndex < 0) {
                    break;
                }
                this.outputAvailable(codec, outBufferIndex, this.bufferInfo);
            }
        }

    }

    public final void formatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
        this.getVideoData.onVideoFormat(mediaFormat);
        this.sendSPSandPPS(mediaFormat);
        this.spsPpsSetted = true;
    }

    protected final void checkBuffer(@NonNull ByteBuffer byteBuffer, @NonNull BufferInfo bufferInfo) {
        if ((bufferInfo.flags & 2) != 0 && !this.spsPpsSetted) {
            ByteBuffer bb = byteBuffer.duplicate();
            Pair buffers = this.decodeSpsPpsFromBuffer(bb, bufferInfo.size);
            if (buffers != null) {
                this.getVideoData.onSpsPps((ByteBuffer)buffers.first, (ByteBuffer)buffers.second);
                this.spsPpsSetted = true;
            }
        }

    }

    protected final void sendBuffer(@NonNull ByteBuffer byteBuffer, @NonNull BufferInfo bufferInfo) {
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
            public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int inBufferIndex) {
                Log.i(VideoEncoder.TAG, "onInputBufferAvailable ignored");
            }

            public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int outBufferIndex, @NonNull BufferInfo bufferInfo) {

                try {
                    VideoEncoder.this.outputAvailable(mediaCodec, outBufferIndex, bufferInfo);
                } catch (IllegalStateException e) {
                    Log.i(VideoEncoder.TAG, "Encoding error", (Throwable)e);
                }

            }

            public void onError(@NonNull MediaCodec mediaCodec, @NonNull CodecException e) {
                Log.e(VideoEncoder.TAG, "Error", (Throwable)e);
            }

            public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
                VideoEncoder.this.formatChanged(mediaCodec, mediaFormat);
            }
        });
    }

    public final void outputAvailable(@NonNull MediaCodec mediaCodec, int outBufferIndex, @NonNull BufferInfo bufferInfo) {
        Log.e(TAG, "outputAvailable " + outBufferIndex);
        ByteBuffer byteBuffer = VERSION.SDK_INT >= 21 ? mediaCodec.getOutputBuffer(outBufferIndex) : mediaCodec.getOutputBuffers()[outBufferIndex];
        this.processOutput(byteBuffer, mediaCodec, outBufferIndex, bufferInfo);
    }

    @NonNull
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

    @NonNull
    public final FormatVideoEncoder getFormatVideoEncoder() {
        return this.formatVideoEncoder;
    }

    public final int getAvcProfile() {
        return this.avcProfile;
    }

    public final int getAvcProfileLevel() {
        return this.avcProfileLevel;
    }

    public VideoEncoder(@NonNull GetVideoData getVideoData, int width, int height, int fps, int bitrate, int rotation, boolean doRotation, int iFrameInterval, @NonNull FormatVideoEncoder formatVideoEncoder, int avcProfile, int avcProfileLevel) {
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

}
