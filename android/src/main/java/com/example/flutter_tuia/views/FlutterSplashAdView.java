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
import com.lechuan.midunovel.view.FoxShView;
import com.lechuan.midunovel.view.FoxShListener;
import com.lechuan.midunovel.view.FoxSize;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class FlutterSplashAdView implements PlatformView, MethodChannel.MethodCallHandler {
    private LinearLayout mLinearLayout;
    private Activity mActivity;
    private MethodChannel methodChannel;
    private FoxShView mTMBrAdView;

    FlutterSplashAdView(Activity activity, BinaryMessenger messenger, int id) {
        methodChannel = new MethodChannel(messenger, "flutter_tuia_splash_ad_view_" + id);
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
        if (mLinearLayout != null) {
            mLinearLayout.removeAllViews();
            mLinearLayout = null;
        }

        if (mTMBrAdView != null) {
            mTMBrAdView.destroy();
            mTMBrAdView = null;
        }
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (Consts.FunctionName.RENDER_SPLASH_AD.equals(methodCall.method)) {
            renderSplashAd(methodCall, result);
        }
    }

    private void renderSplashAd(final MethodCall call, final MethodChannel.Result result) {
        try {
            Object positionIdVal = call.argument(Consts.ParamKey.POSITION_ID);
            if (positionIdVal == null) {
                Log.d(Consts.TAG, "Tuia splash ad empty positionId");
                try {
                    result.success(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            int positionId = Integer.parseInt((String)positionIdVal);

            int timeout = 3000;
            Object timeoutVal = call.argument(Consts.ParamKey.TIMEOUT);
            if (timeoutVal != null) {
                timeout = (int) timeoutVal;
            }

            if (mTMBrAdView != null) {
                mTMBrAdView.destroy();
                mTMBrAdView = null;
            }

            mTMBrAdView = new FoxShView(mActivity);
            mTMBrAdView.setTargetClass(mActivity, mActivity.getClass());
            mTMBrAdView.setCountTtime(timeout / 1000);
            mTMBrAdView.setAdListener(new FoxShListener() {
                @Override
                public void onTimeOut() {
                    Log.d(Consts.TAG, "Tuia splash ad onTimeOut");
                    mTMBrAdView.setTargetClass(null, mActivity.getClass());

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adFinish", null);
                        }
                    });
                }

                @Override
                public void onReceiveAd() {
                    Log.d(Consts.TAG, "Tuia splash ad onReceiveAd");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adLoaded", null);
                        }
                    });
                }
                @Override
                public void onFailedToReceiveAd() {
                    Log.d(Consts.TAG, "Tuia splash ad onFailedToReceiveAd");
                    mTMBrAdView.setTargetClass(null, mActivity.getClass());

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adError", null);
                        }
                    });
                }
                @Override
                public void onLoadFailed() {
                    Log.d(Consts.TAG, "Tuia splash ad onLoadFailed");
                    mTMBrAdView.setTargetClass(null, mActivity.getClass());

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adError", null);
                        }
                    });
                }
                @Override
                public void onCloseClick() {
                    Log.d(Consts.TAG, "Tuia splash ad onCloseClick");
                    mTMBrAdView.setTargetClass(null, mActivity.getClass());

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adFinish", null);
                        }
                    });
                }
                @Override
                public void onAdClick() {
                    Log.d(Consts.TAG, "Tuia splash ad onClick");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            methodChannel.invokeMethod("adClicked", null);
                        }
                    });
                }
                @Override
                public void onAdExposure() {
                    Log.d(Consts.TAG, "Tuia splash ad onAdExposure");
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
            Log.d(Consts.TAG, "Tuia splash ad failed");
            e.printStackTrace();
            try {
                result.success(false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
