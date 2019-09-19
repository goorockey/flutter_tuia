package com.example.flutter_tuia.views;

import android.app.Activity;
import android.content.Context;

import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MessageCodec;
import io.flutter.plugin.platform.PlatformView;


public class FlutterBannerAdViewFactory extends PlatformViewFactory {
    private Activity mActivity;
    private BinaryMessenger mMessenger;

    public FlutterBannerAdViewFactory(MessageCodec<Object> createArgsCodec, Activity activity,
            BinaryMessenger messenger) {
        super(createArgsCodec);
        this.mActivity = activity;
        this.mMessenger = messenger;
    }

    @Override
    public PlatformView create(Context context, int id, Object o) {
        return new FlutterBannerAdView(mActivity, mMessenger, id);
    }
}
