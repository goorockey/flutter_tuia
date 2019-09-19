import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class FlutterTuiaBannerView extends StatefulWidget {
  final String positionId;
  final int width;
  final int height;
  final Function onLoaded;
  final Function onError;
  final Function onClick;
  final Function onExposure;

  FlutterTuiaBannerView(
    this.positionId, {
    this.width,
    this.height,
    this.onLoaded,
    this.onError,
    this.onClick,
    this.onExposure,
  });

  @override
  _FlutterTuiaBannerViewState createState() => _FlutterTuiaBannerViewState();
}

class _FlutterTuiaBannerViewState extends State<FlutterTuiaBannerView> {
  MethodChannel _channel;
  int _channelId;
  bool loaded = false;

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return _androidView();
    }

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return _iosView();
    }

    print('Tuia Banner 不支持的平台');
    return Container(width: 0, height: 0);
  }

  Future<dynamic> _onMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'adClicked':
        {
          widget.onClick?.call(() {
            _loadView();
          });
          break;
        }
      case 'adLoaded':
        {
          widget.onLoaded?.call(() {
            _loadView();
          });
          break;
        }
      case 'adExposure':
        {
          widget.onExposure?.call(() {
            _loadView();
          });
          break;
        }
      case 'adError':
        {
          widget.onError?.call(() {
            _loadView();
          });
          break;
        }
      default:
        break;
    }
  }

  _loadView() async {
    if (_channel == null) {
      _channel =
          MethodChannel("flutter_tuia_banner_ad_view_" + _channelId.toString());
      _channel.setMethodCallHandler(_onMethodCall);
    }

    final result = await _channel.invokeMethod("renderBannerAd", {
      "positionId": widget.positionId,
    });

    if (mounted && loaded != result) {
      setState(() {
        loaded = result;
      });
    }

    if (result != true) {
      widget.onError?.call(() {
        _loadView();
      });
    }
  }

  Widget _androidView() {
    return Container(
      height: loaded ? widget.height.toDouble() : 1,
      width: loaded ? widget.width.toDouble() : 1,
      child: AndroidView(
        viewType: "flutter_tuia_banner_ad_view",
        onPlatformViewCreated: (id) async {
          _channelId = id;
          _loadView();
        },
      ),
    );
  }

  Widget _iosView() {
    return Container(
      height: loaded ? widget.height.toDouble() : 1,
      width: loaded ? widget.width.toDouble() : 1,
      child: UiKitView(
        viewType: "flutter_tuia_banner_ad_view",
        creationParams: <String, dynamic>{
          "positionId": widget.positionId,
        },
        creationParamsCodec: new StandardMessageCodec(),
        onPlatformViewCreated: (int id) async {
          _channelId = id;
          _loadView();
        },
      ),
    );
  }
}
