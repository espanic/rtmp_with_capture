package com.example.rtmp_with_capture;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.pedro.rtplibrary.view.TakePhotoCallback;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import net.ossrs.rtmp.ConnectCheckerRtmp;
import net.ossrs.rtmp.SrsFlvMuxer;



@RequiresApi(api = 21)
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
    @NonNull
    private final Context context;
    private final boolean useOpenGL;
    private final boolean isPortrait;
    private Bitmap captureImage;
    @NonNull
    private final ConnectCheckerRtmp connectChecker;
    private static final String TAG = "RtmpCameraConnector";


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

    @NonNull
    public final Surface getInputSurface() {
        Surface var1;
        if (this.useOpenGL) {
            return glInterface.getSurface();
        } else {
            return videoEncoder.getSurface();
        }
    }


    public final boolean prepareVideo(int width, int height, int fps, int bitrate, boolean hardwareRotation, int iFrameInterval, int rotation, int avcProfile, int avcProfileLevel) {
        this.pausedStreaming = false;
        this.pausedRecording = false;
        this.videoEncoder = new VideoEncoder(this, width, height, fps, bitrate, this.useOpenGL ? 0 : rotation, hardwareRotation, iFrameInterval, FormatVideoEncoder.SURFACE, avcProfile, avcProfileLevel);
        boolean result = videoEncoder.prepare();
        if (this.useOpenGL) {
            this.prepareGlInterface(this.ORIENTATIONS.get(rotation));
            glInterface.addMediaCodecSurface(videoEncoder.getSurface());
        }
        return result;
    }

    public final boolean prepareVideo(int width, int height, int fps, int bitrate, boolean hardwareRotation, int rotation) {
        return prepareVideo(width, height, fps, bitrate, hardwareRotation, 2, rotation, -1, -1);
    }


    private final void prepareGlInterface(int rotation) {
        Log.i(TAG, "prepareGlInterface " + rotation + " " + this.isPortrait);

        glInterface.setEncoderSize(videoEncoder.getWidth(), videoEncoder.getHeight());
        this.glInterface.setRotation(rotation);
        this.glInterface.start();
    }

    public final boolean prepareAudio(int bitrate, int sampleRate, boolean isStereo, boolean echoCanceler, boolean noiseSuppressor) {
        microphoneManager.createMicrophone(sampleRate, isStereo, echoCanceler, noiseSuppressor);
        this.prepareAudioRtp(isStereo, sampleRate);
        return audioEncoder.prepareAudioEncoder(bitrate, sampleRate, isStereo, microphoneManager.getMaxInputSize());
    }


    public final boolean prepareAudio() {
        return prepareAudio(64 * 1024, 32000, true,false,false);
    }

    public final void setForce(@NonNull Force forceVideo, @NonNull Force forceAudio) {
        videoEncoder.setForce(forceVideo);
        audioEncoder.setForce(forceAudio);
    }

    public final void startStream(@NonNull String url) {
        if (!this.isStreaming) {
            this.isStreaming = true;
            this.startStreamRtp(url);
        }
    }

    public final void startRecord(@NonNull String path) {
        if (!this.isRecording) {
            try {
                this.recordController.startRecord(path, this);
                this.isRecording = true;
                if (!this.isStreaming) {
                    this.startEncoders();
                }
            }catch (Exception e){
                e.printStackTrace();
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
        videoEncoder.start();
        audioEncoder.start();
        microphoneManager.start();
    }

    private final void resetVideoEncoder() {
        videoEncoder.reset();
    }

    public final void stopStream() {
        this.isStreaming = false;
        this.stopStreamRtp();
        if (!this.isRecording) {
            microphoneManager.stop();


            videoEncoder.stop();


            audioEncoder.stop();
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


    public byte[] takePhoto() {
        glInterface.takePhoto(new TakePhotoCallback() {
            @Override
            public void onTakePhoto(Bitmap bitmap) {
                Log.i(TAG, "onTakePhoto bitmap width : " +bitmap.getWidth() + " height : " + bitmap.getHeight());
                captureImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Log.i(TAG, "" + bitmap.getHeight());
            }
        });
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(captureImage == null){
            return null;
        }
        else {
            Log.i(TAG, "capturing image succeeded");
            captureImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            captureImage = null;
            return  byteArray;
        }
    }

    public final boolean reTry(long delay, @NonNull String reason) {
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

    public final void setAuthorization(@NonNull String user, @NonNull String password) {
        this.srsFlvMuxer.setAuthorization(user, password);
    }

    public final void prepareAudioRtp(boolean isStereo, int sampleRate) {
        this.srsFlvMuxer.setIsStereo(isStereo);
        this.srsFlvMuxer.setSampleRate(sampleRate);
    }

    public final void startStreamRtp(@NonNull String url) {
        if(videoEncoder.getRotation() == 90 || videoEncoder.getRotation() == 270) srsFlvMuxer.setVideoResolution(videoEncoder.getHeight(), videoEncoder.getWidth());
        else srsFlvMuxer.setVideoResolution(videoEncoder.getWidth(), videoEncoder.getHeight());
        this.srsFlvMuxer.start(url);
    }

    public final void stopStreamRtp() {
        this.srsFlvMuxer.stop();
    }

    public final void setReTries(int reTries) {
        this.srsFlvMuxer.setReTries(reTries);
    }

    public final boolean shouldRetry(@NonNull String reason) {
        return this.srsFlvMuxer.shouldRetry(reason);
    }

    public final void reConnect(long delay) {
        this.srsFlvMuxer.reConnect(delay);
    }

    public final void disableAudio() {
        microphoneManager.mute();
    }

    public final void enableAudio() {
        microphoneManager.unMute();
    }

    public final boolean isAudioMuted() {
        return microphoneManager.isMuted();
    }

    public final int getBitrate() {
        int rate = videoEncoder.getBitrate();
        return rate;
    }

    public final int getStreamWidth() {

        return videoEncoder.getWidth();
    }

    public final int getStreamHeight() {
        return videoEncoder.getHeight();
    }

    public final int getFps() {
        return this.curFps;
    }

    public final void setVideoBitrateOnFly(int bitrate) {
        videoEncoder.setVideoBitrateOnFly(bitrate);
    }

    public final void setLimitFPSOnFly(int fps) {
        videoEncoder.setLimitFps(fps);
    }

    public void getAacData(@NonNull ByteBuffer aacBuffer, @NonNull BufferInfo info) {

        if (this.isStreaming && !this.pausedStreaming) {
            this.getAacDataRtp(aacBuffer, info);
        }

        if (this.isRecording && !this.pausedRecording) {
            this.recordController.recordAudio(aacBuffer, info);
        }

    }

    public void onSpsPps(@NonNull ByteBuffer sps, @NonNull ByteBuffer pps) {
        if (this.isStreaming && !this.pausedStreaming) {
            this.onSpsPpsVpsRtp(sps, pps, (ByteBuffer)null);
        }

    }

    public void onSpsPpsVps(@NonNull ByteBuffer sps, @NonNull ByteBuffer pps, @NonNull ByteBuffer vps) {
        if (this.isStreaming && !this.pausedStreaming) {
            this.onSpsPpsVpsRtp(sps, pps, vps);
        }

    }

    public void getVideoData(@NonNull ByteBuffer h264Buffer, @NonNull BufferInfo info) {
        this.fpsListener.calculateFps();
        if (this.isStreaming && !this.pausedStreaming) {
            this.getH264DataRtp(h264Buffer, info);
        }

        if (this.isRecording && !this.pausedRecording) {
            this.recordController.recordVideo(h264Buffer, info);
        }

    }

    public void inputPCMData(@NonNull Frame frame) {
        audioEncoder.inputPCMData(frame);
    }

    public void onVideoFormat(@NonNull MediaFormat mediaFormat) {

    }

    public void onAudioFormat(@NonNull MediaFormat mediaFormat) {

    }

    public final void getAacDataRtp(@NonNull ByteBuffer aacBuffer, @NonNull BufferInfo info) {

        this.srsFlvMuxer.sendAudio(aacBuffer, info);
    }

    public final void onSpsPpsVpsRtp(@NonNull ByteBuffer sps, @NonNull ByteBuffer pps, @Nullable ByteBuffer vps) {

        this.srsFlvMuxer.setSpsPPs(sps, pps);
    }

    public final void getH264DataRtp(@NonNull ByteBuffer h264Buffer, @NonNull BufferInfo info) {
        this.srsFlvMuxer.sendVideo(h264Buffer, info);
    }

    public void onFps(int fps) {
        this.curFps = fps;
    }

    public void onStatusChange(@NonNull Status status) {
    }

    public void onConnectionSuccessRtmp() {

        if (!videoEncoder.getRunning()) {
            this.startEncoders();
        }

        this.connectChecker.onConnectionSuccessRtmp();
    }

    public void onConnectionFailedRtmp(@NonNull String reason) {
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

    @NonNull
    public final Context getContext() {
        return this.context;
    }

    public final boolean getUseOpenGL() {
        return this.useOpenGL;
    }

    public final boolean isPortrait() {
        return this.isPortrait;
    }

    @NonNull
    public final ConnectCheckerRtmp getConnectChecker() {
        return this.connectChecker;
    }

    public RtmpCameraConnector(@NonNull Context context, boolean useOpenGL, boolean isPortrait, @NonNull ConnectCheckerRtmp connectChecker) {

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

}
