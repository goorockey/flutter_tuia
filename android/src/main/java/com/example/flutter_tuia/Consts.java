package com.example.flutter_tuia;

import android.content.Context;

public class Consts {
    public static final String TAG = "flutter_tuia";
    public static final int DEFAULT_PRELOAD_COUNT = 1;

    public static int dp2px(Context context, int dp) {
        return (int) ((float) dp * context.getResources().getDisplayMetrics().density);
    }

    public static class FunctionName {
        public static final String RENDER_SPLASH_AD = "renderSplashAd";
        public static final String RENDER_BANNER_AD = "renderBannerAd";
    }

    public static class ParamKey {
        public static final String APP_ID = "appId";
        public static final String POSITION_ID = "positionId";
        public static final String TIMEOUT = "timeout";
    }
}
