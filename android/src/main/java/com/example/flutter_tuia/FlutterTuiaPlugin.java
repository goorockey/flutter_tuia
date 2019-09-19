package com.example.flutter_tuia;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.example.flutter_tuia.views.FlutterBannerAdViewFactory;
import com.example.flutter_tuia.views.FlutterSplashAdViewFactory;
import com.lechuan.midunovel.view.FoxSDK;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.StandardMessageCodec;

/** FlutterTuiaPlugin */
public class FlutterTuiaPlugin implements MethodCallHandler {
  private Activity mActivity;

  public FlutterTuiaPlugin(Activity activity) {
      mActivity = activity;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_tuia");
    channel.setMethodCallHandler(new FlutterTuiaPlugin(registrar.activity()));

    registrar.platformViewRegistry().registerViewFactory("flutter_tuia_banner_ad_view",
        new FlutterBannerAdViewFactory(new StandardMessageCodec(), registrar.activity(),
          registrar.messenger()));

    registrar.platformViewRegistry().registerViewFactory("flutter_tuia_splash_ad_view",
            new FlutterSplashAdViewFactory(new StandardMessageCodec(), registrar.activity(),
                    registrar.messenger()));
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("initSDK")) {
      this.initSDK(call, result);
    } else if (call.method.equals("checkPermissions")) {
      this.checkPermission(call, result);
    } else {
      result.notImplemented();
    }
  }

  private void initSDK(MethodCall call, Result result) {
    try {
      FoxSDK.init(mActivity.getApplication());
      Log.d(Consts.TAG, "Tuia sdk init success");
      result.success(true);
    } catch (Exception e) {
      Log.d(Consts.TAG, "Tuia sdk init fail");
      e.printStackTrace();
      result.success(false);
    }
  }

  private void checkPermission(MethodCall call, Result result) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.checkAndRequestPermission(result);
    } else {
        try {
            result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

private ArrayList<String> getNeedPermissionList() {
    ArrayList<String> lackedPermission = new ArrayList<String>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if ((mActivity.checkSelfPermission(
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        if ((mActivity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if ((mActivity.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    return lackedPermission;
}

@TargetApi(Build.VERSION_CODES.M)
private void checkAndRequestPermission(Result result) {
    List<String> lackedPermission = getNeedPermissionList();

    // 权限都已经有了，那么直接调用SDK
    if (lackedPermission.size() == 0) {
        try {
            result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } else {
        // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
        try {
            result.success(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] requestPermissions = new String[lackedPermission.size()];
        lackedPermission.toArray(requestPermissions);
        mActivity.requestPermissions(requestPermissions, 1024);
    }
}
}
