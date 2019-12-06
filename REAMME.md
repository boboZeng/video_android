1。基于https://github.com/Bilibili/ijkplayer

2。修复支持https
        参考：https://www.imooc.com/article/33930



###一、提示用户4G还是Wifi

>需求：进入直播页面如果是非Wifi提示用户网络情况；
```
        if (VideoUtils.isNetworkConnected(this)
                && !VideoUtils.isWifiConnected(this)) {
            Toast.makeText(this, "当前为非wifi环境，请注意流量消耗", Toast.LENGTH_SHORT).show();
        }
````

###二、自动续播
>需求：1.切换到后台、按home键或另外页面打开,
```
    #需要调用方法保存状态
    @Override
    public void onStop() {
        super.onStop();
        if (videoLayout != null) {
            videoLayout.pause(true);
        }
    }

    #自动续播
    @Override
    protected void onResume() {
        super.onResume();
        if (videoLayout != null) {
            if (videoLayout.getCurrentState() == VideoLayout.STATE_AUTO_PAUSED
                    || videoLayout.getCurrentState() == VideoLayout.STATE_ERROR) {
                videoLayout.load();
            }
        }
    }
```

>需求：2.网络不好，播放出错，恢复网络情况
```
    private NetWorkBroadcastManager netWorkBroadcastManager;

    #注册
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
              netWorkBroadcastManager = new NetWorkBroadcastManager(netWorkBroadcastManagerObserver);
        netWorkBroadcastManager.registerReceiver(this);
    }

    #移除
    @Override
    protected void onDestroy() {
        super.onDestroy();
        netWorkBroadcastManager.unregisterReceiver(this);
    }

    #状态变化监听
    private Observer netWorkBroadcastManagerObserver =  new Observer<Boolean>() {
          @Override
          public void onChanged(Boolean aBoolean) {
                if (VideoUtils.isNetworkConnected(MainActivity.this)
                      && VideoUtils.isMobileConnected(MainActivity.this)) {
                       Toast.makeText(MainActivity.this, "当前为非wifi环境，请注意流量消耗", Toast.LENGTH_SHORT).show();
               }
               if (videoLayout == null || !isConnected || isFinishing()) {
                     return;
               }
               if (videoLayout.getCurrentState() == VideoLayout.STATE_AUTO_PAUSED
                       || videoLayout.getCurrentState() == VideoLayout.STATE_ERROR) {
                    MainActivity.this.runOnUiThread(() -> videoLayout.load());
               }
          }
    }

```


###三、自定义状态播放器提示
>需求：1、缓冲超过10s
```
   videoLayout.setOnCustomInfoListener(new OnCustomInfoListener() {
            @Override
            public void onCustomInfo(int what) {
                if (what == VideoConstants.VideoCustomStatus.BUFFERING_TIMEOUT) {
                    Toast.makeText(MainActivity.this, "网络不稳定，切换低清晰度播放更流畅", Toast.LENGTH_SHORT).show();
                }
            }
        });
```
###四、播放最新时间点
```
videoLayout.load()

```
###五、支持多清晰度
```
        List<ClarityModel> clarityList = new ArrayList<>();
        ClarityModel model_1 = new ClarityModel("1080P", "rtmp://58.200.131.2:1935/livetv/hunantv");
        clarityList.add(model_1);
        ClarityModel model_2 = new ClarityModel("720P",
                "rtmp://wslive.undemonstrable.cn/wslive1/328_push_5dcbd506de59c?wsTime=1575525184&wsSecret=34e467150476d3dc71a451a402e756a2");
        clarityList.add(model_2);
        ClarityModel model_3 = new ClarityModel("480P",
                "rtmp://fms.105.net/live/rmc1");
        clarityList.add(model_3);
        ClarityModel model_4 = new ClarityModel("360P", "rtmp://202.69.69.180:443/webcast/bshdlive-pc");
        clarityList.add(model_4);
        controller.setClarityList(clarityList);
```




