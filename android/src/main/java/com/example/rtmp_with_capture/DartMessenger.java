package com.example.rtmp_with_capture;



import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import java.util.HashMap;
import java.util.Map;


public final class DartMessenger {
    private EventSink eventSink;

    public final void sendCameraClosingEvent() {
        this.send(DartMessenger.EventType.CAMERA_CLOSING, (String)null);
    }

    public final void send(@NonNull DartMessenger.EventType eventType, @Nullable String description) {
        if (this.eventSink != null) {
            Map event = new HashMap();
            String es = eventType.toString();
            if (es == null) {
                throw new NullPointerException("null cannot be cast to non-null type java.lang.String");
            } else {
                String s = es.toLowerCase();
                event.put("eventType", s);
                if (!TextUtils.isEmpty((CharSequence)description)) {
                    event.put("errorDescription", description);
                }

                eventSink.success(event);
            }
        }
    }

    public DartMessenger(@NonNull BinaryMessenger messenger, long eventChannelId) {
        super();

        (new EventChannel(messenger, "rtmp_with_capture/cameraEvents" + eventChannelId)).setStreamHandler((StreamHandler)(new StreamHandler() {
            public void onListen(@Nullable Object arguments, @NonNull EventSink sink) {
                DartMessenger.this.eventSink = sink;
            }
            public void onCancel(@Nullable Object arguments) {
                DartMessenger.this.eventSink = (EventSink)null;
            }
        }));
    }
    public enum EventType {
        ERROR,
        CAMERA_CLOSING,
        RTMP_STOPPED,
        RTMP_RETRY,
        ROTATION_UPDATE;
    }
}
