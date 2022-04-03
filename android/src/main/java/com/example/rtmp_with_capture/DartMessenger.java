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


@Metadata(
        mv = {1, 1, 18},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001:\u0001\u0010B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eJ\u0006\u0010\u000f\u001a\u00020\nR\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u0011"},
        d2 = {"Lcom/marshalltechnology/video_stream/DartMessenger;", "", "messenger", "Lio/flutter/plugin/common/BinaryMessenger;", "eventChannelId", "", "(Lio/flutter/plugin/common/BinaryMessenger;J)V", "eventSink", "Lio/flutter/plugin/common/EventChannel$EventSink;", "send", "", "eventType", "Lcom/marshalltechnology/video_stream/DartMessenger$EventType;", "description", "", "sendCameraClosingEvent", "EventType", "android.video_stream"}
)
public final class DartMessenger {
    private EventSink eventSink;

    public final void sendCameraClosingEvent() {
        this.send(DartMessenger.EventType.CAMERA_CLOSING, (String)null);
    }

    public final void send(@NonNull DartMessenger.EventType eventType, @Nullable String description) {
        Intrinsics.checkParameterIsNotNull(eventType, "eventType");
        if (this.eventSink != null) {
            Map event = (Map)(new HashMap());
            String var4 = eventType.toString();
            boolean var5 = false;
            if (var4 == null) {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            } else {
                String var10002 = var4.toLowerCase();
                Intrinsics.checkExpressionValueIsNotNull(var10002, "(this as java.lang.String).toLowerCase()");
                event.put("eventType", var10002);
                if (!TextUtils.isEmpty((CharSequence)description)) {
                    event.put("errorDescription", description);
                }

                EventSink var10000 = this.eventSink;
                if (var10000 == null) {
                    Intrinsics.throwNpe();
                }

                var10000.success(event);
            }
        }
    }

    public DartMessenger(@NotNull BinaryMessenger messenger, long eventChannelId) {
        Intrinsics.checkParameterIsNotNull(messenger, "messenger");
        super();
        boolean var4 = true;
        boolean var5 = false;
        boolean var6 = false;
        if (_Assertions.ENABLED) {
        }

        (new EventChannel(messenger, "video_stream/cameraEvents" + eventChannelId)).setStreamHandler((StreamHandler)(new StreamHandler() {
            public void onListen(@Nullable Object arguments, @NotNull EventSink sink) {
                Intrinsics.checkParameterIsNotNull(sink, "sink");
                DartMessenger.this.eventSink = sink;
            }

            public void onCancel(@Nullable Object arguments) {
                DartMessenger.this.eventSink = (EventSink)null;
            }
        }));
    }

    // $FF: synthetic method
    public static final EventSink access$getEventSink$p(DartMessenger $this) {
        return $this.eventSink;
    }

    @Metadata(
            mv = {1, 1, 18},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007¨\u0006\b"},
            d2 = {"Lcom/marshalltechnology/video_stream/DartMessenger$EventType;", "", "(Ljava/lang/String;I)V", "ERROR", "CAMERA_CLOSING", "RTMP_STOPPED", "RTMP_RETRY", "ROTATION_UPDATE", "android.video_stream"}
    )
    public static enum EventType {
        ERROR,
        CAMERA_CLOSING,
        RTMP_STOPPED,
        RTMP_RETRY,
        ROTATION_UPDATE;
    }
}
