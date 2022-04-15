package com.example.rtmp_with_capture;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.renderer.FlutterRenderer;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.TextureRegistry;

/**
 * RtmpWithCapturePlugin
 */
public class RtmpWithCapturePlugin implements FlutterPlugin {

    private MethodCallHandlerImpl methodCallHandler;
    private FlutterPluginBinding flutterPluginBinding;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding;
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = null;
    }

    private final void maybeStartListening(Activity activity, BinaryMessenger messenger, PermissionStuff permissionsRegistry, TextureRegistry textureRegistry) {
        if (Build.VERSION.SDK_INT >= 21) {
            this.methodCallHandler = new MethodCallHandlerImpl(activity, messenger, new CameraPermissions(), permissionsRegistry, textureRegistry);
        }
    }

    public void onDetachedFromActivity() {
        if (this.methodCallHandler != null) {
            methodCallHandler.stopListening();
            this.methodCallHandler = null;
        }
    }

    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        this.onAttachedToActivity(binding);
    }

    public void onAttachedToActivity(@NonNull final ActivityPluginBinding binding) {
        Activity activity = binding.getActivity();
        BinaryMessenger binaryMessenger = flutterPluginBinding.getBinaryMessenger();

        PermissionStuff permissionStuff = (PermissionStuff) (new PermissionStuff() {
            public void addListener(@NonNull PluginRegistry.RequestPermissionsResultListener listener) {
                binding.addRequestPermissionsResultListener(listener);
            }
        });
        FlutterEngine flutterEngine = flutterPluginBinding.getFlutterEngine();
        FlutterRenderer renderer = flutterEngine.getRenderer();
        this.maybeStartListening(activity, binaryMessenger, permissionStuff, (TextureRegistry) renderer);
    }

    public void onDetachedFromActivityForConfigChanges() {
        this.onDetachedFromActivity();
    }


}

