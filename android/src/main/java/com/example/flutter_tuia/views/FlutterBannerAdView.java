package com.example.flutter_tuia.views;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.LinearLayout;

import com.example.flutter_tuia.Consts;
import com.example.flutter_tuia.R;
import com.lechuan.midunovel.view.FoxListener;
import com.lechuan.midunovel.view.FoxStreamerView;
import com.lechuan.midunovel.view.FoxSize;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterBannerAdView implements PlatformView, MethodChannel.MethodCallHandler {
    private LinearLayout mLinearLayout;
    private Activity mActivity;
    private MethodChannel methodChannel;
    private FoxStreamerView mTMBrAdView;

    FlutterBannerAdView(Activity activity, BinaryMessenger messenger, int id) {
        methodChannel = new MethodChannel(messenger, "flutter_tuia_banner_ad_view_" + id);
        methodChannel.setMethodCallHandler(this);

        this.mActivity = activity;
        if (mLinearLayout == null) {
            mLinearLayout = new LinearLayout(activity);
        }
    }

    @Override
    public View getView() {
        // 为了让platformView的背景透明
        if (mLinearLayout != null) {
            mLinearLayout.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        ViewParent parent = mLinearLayout.getParent();
                        if (parent == null) {
                            return;
                        }
                        while (parent.getParent() != null) {
                            parent = parent.getParent();
                        }
                        Object decorView = parent.getClass().getDeclaredMethod("getView").invoke(parent);
                        final Field windowField = decorView.getClass().getDeclaredField("mWindow");
                        windowField.setAccessible(true);
                        final Window window = (Window) windowField.get(decorView);
                        windowField.setAccessible(false);
                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    } catch (Exception e) {
                        // log the exception
                    }
                }
            });
        }
        return mLinearLayout;
    }

    @Override
    public void dispose() {
        methodChannel.setMethodCallHandler(null);

        if (mTMBrAdView != null) {
            mTMBrAdView.destroy();
            mTMBrAdView = null;
        }

        if (mLinearLayout != null) {
            mLinearLayout.removeAllViews();
        }
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (Consts.FunctionName.RENDER_BANNER_AD.equals(methodCall.method)) {
            renderBannerAd(methodCall, result);
        }
    }

    private void renderBannerAd(final MethodCall call, final MethodChannel.Result result) {
        try {
            Object positionIdVal = call.argument(Consts.ParamKey.POSITION_ID);
            if (positionIdVal == null) {
                Log.d(Consts.TAG, "Tuia banner ad empty positionId");
                try {
                    result.success(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            int positionId = Integer.parseInt((String)positionIdVal);

            if (mTMBrAdView != null) {
                mTMBrAdView.destroy();
                mTMBrAdView = null;
            }

            mTMBrAdView = new FoxStreamerView(mActivity, FoxSize.LANDER_TMBr);

            mTMBrAdView.setAdListener(new FoxListener() {
                @Override
                public void onReceiveAd() {
                    Log.d(Consts.TAG, "Tuia banner ad onReceiveAd");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adLoaded", null);
                        }
                    });
                }
                @Override
                public void onFailedToReceiveAd() {
                    Log.d(Consts.TAG, "Tuia banner ad onFailedToReceiveAd");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adError", null);
                        }
                    });
                }
                @Override
                public void onLoadFailed() {
                    Log.d(Consts.TAG, "Tuia banner ad onLoadFailed");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adError", null);
                        }
                    });
                }
                @Override
                public void onCloseClick() {
                    Log.d(Consts.TAG, "Tuia banner ad onCloseClick");
                }
                @Override
                public void onAdClick() {
                    Log.d(Consts.TAG, "Tuia banner ad onClick");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adClicked", null);
                        }
                    });
                }
                @Override
                public void onAdExposure() {
                    Log.d(Consts.TAG, "Tuia banner ad onAdExposure");
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adExposure", null);
                        }
                    });
                }
            });

            mLinearLayout.removeAllViews();
            mLinearLayout.addView(mTMBrAdView);

            mTMBrAdView.loadAd(positionId);

            try {
                result.success(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.d(Consts.TAG, "Tuia banner ad failed");
            e.printStackTrace();
            try {
                result.success(false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
