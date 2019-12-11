package com.sport.video;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.video.live.ClarityModel;
import com.video.live.IjkPlayerStatus;
import com.video.live.NetWorkBroadcastManager;
import com.video.live.OnBackListener;
import com.video.live.OnCustomInfoListener;
import com.video.live.SimpleVideoLayoutController;
import com.video.live.VideoConstants;
import com.video.live.VideoLayout;
import com.video.live.VideoUtils;
import com.video.network.ConnectionClassManager;
import com.video.network.ConnectionQuality;
import com.video.network.DeviceBandwidthSampler;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private VideoLayout videoLayout;
    private SimpleVideoLayoutController controller;
    //    private String path = "rtmp://wslive.undemonstrable.cn/wslive1/5759_push_5ddda0f46684e?wsTime=1575009617&wsSecret=d4323e657297dd55680d808bb29c0775";
    private String path = "rtmp://58.200.131.2:1935/livetv/hunantv";
//    private String path = "rtmp://wslive.undemonstrable.cn/wslive1/7017_push_5deef2d814081?wsTime=1575941413&wsSecret=02225de3fa5a11e62128c86db9c76b8e";

    private NetWorkBroadcastManager netWorkBroadcastManager;
    private TextView tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoUtils.setEnableDebug(true);
        netWorkBroadcastManager = new NetWorkBroadcastManager(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                playerNetWortNotice();
                if (videoLayout == null || !isConnected || isFinishing()) {
                    return;
                }
                if (videoLayout.getCurrentState() == VideoLayout.STATE_AUTO_PAUSED
                        || videoLayout.getCurrentState() == VideoLayout.STATE_ERROR) {
                    MainActivity.this.runOnUiThread(() -> videoLayout.load());
                }
            }
        });
        tv_content = findViewById(R.id.tv_content);
        videoLayout = findViewById(R.id.videoLayout);
        videoLayout.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                controller.setThumbVisibility(View.GONE);
                controller.setLoadingVisibility(View.GONE);
            }
        });

        videoLayout.initVideoLayoutController(controller = new SimpleVideoLayoutController(this,
                new OnBackListener() {

                    @Override
                    public boolean onBack() {
                        onBackPressed();
                        return true;
                    }
                }));
        videoLayout.setOnCustomInfoListener(new OnCustomInfoListener() {
            @Override
            public void onCustomInfo(int what) {
                if (what == VideoConstants.VideoCustomStatus.BUFFERING_TIMEOUT) {
                    Toast.makeText(MainActivity.this, "您当前下载速度"
                                    + (int) ConnectionClassManager.getInstance().getDownloadKBytePerSecond()
                                    + "K/S,  请切换网络立享高清直播",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        videoLayout.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {

                return true;
            }
        });
        videoLayout.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                if (what == IjkPlayerStatus.MEDIA_INFO_VIDEO_INTERRUPT.getErrorCode()) {
                    Toast.makeText(MainActivity.this, "直播流加载出错,请检查网络", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        List<ClarityModel> clarityList = new ArrayList<>();
        ClarityModel model_1 = new ClarityModel("1080P", "rtmp://58.200.131.2:1935/livetv/hunantv");
        clarityList.add(model_1);
        ClarityModel model_2 = new ClarityModel("720P",
                "rtmp://wslive.undemonstrable.cn/wslive1/328_push_5dcbd506de59c?wsTime=1575525184&wsSecret=34e467150476d3dc71a451a402e756a2");
        clarityList.add(model_2);
        ClarityModel model_3 = new ClarityModel("480P",
                "rtmp://fms.105.net/live/rmc1");
//                "rtmp://wslive.undemonstrable.cn/wslive1/328_push_5dcbd506de59c_480p?wsTime=1575525184&wsSecret=2a60f7cc92a584301a9fe3e84711d35b");
        clarityList.add(model_3);
        ClarityModel model_4 = new ClarityModel("360P", "rtmp://202.69.69.180:443/webcast/bshdlive-pc");
        clarityList.add(model_4);
        controller.setClarityList(clarityList);
        videoLayout.setPath(path);
        controller.setThumbVisibility(View.VISIBLE);

        findViewById(R.id.tv_player).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoLayout.release();
            }
        });

        findViewById(R.id.load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoLayout.load();
            }
        });
        findViewById(R.id.tv_airplay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, com.androidupnpdemo.ui.MainActivity.class));
            }
        });

        playerNetWortNotice();

        netWorkBroadcastManager.registerReceiver(this);
        mHandler.sendEmptyMessageDelayed(1, 1000);

        DeviceBandwidthSampler.init(Process.myUid());
        DeviceBandwidthSampler.getInstance().startSampling();

        currentTime = System.currentTimeMillis();
        currentBytes = TrafficStats.getUidRxBytes(Process.myUid());
    }

    private void playerNetWortNotice() {
        if (VideoUtils.isNetworkConnected(this)
                && !VideoUtils.isWifiConnected(this)) {
            int type = VideoUtils.getMoblieNetWorkType(this);
            if (type == VideoConstants.NETWORK_CLASS.NETWORK_CLASS_2_G) {
                Toast.makeText(this, "您当前处于2G网络，请切换网络立享高清直播", Toast.LENGTH_SHORT).show();
            } else if (type == VideoConstants.NETWORK_CLASS.NETWORK_CLASS_3_G) {
                Toast.makeText(this, "您当前处于3G网络，请切换网络立享高清直播", Toast.LENGTH_SHORT).show();
            } else if (type == VideoConstants.NETWORK_CLASS.NETWORK_CLASS_4_G) {
                Toast.makeText(this, "您当前处于4G网络，请注意流量消耗", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "当前为非wifi环境，请注意流量消耗", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionClassManager.getInstance().register(listener);
        if (videoLayout != null) {
            if (videoLayout.getCurrentState() == VideoLayout.STATE_AUTO_PAUSED
                    || videoLayout.getCurrentState() == VideoLayout.STATE_ERROR) {
                videoLayout.load();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ConnectionClassManager.getInstance().remove(listener);
        if (videoLayout != null) {
            videoLayout.pause(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        netWorkBroadcastManager.unregisterReceiver(this);
        mHandler.removeMessages(1);
    }

    @Override
    public void onBackPressed() {
        if (controller != null
                && controller.onBackPressed()) {
            return;
        }
        if (videoLayout != null) {
            videoLayout.stop();
            videoLayout.release();
        }
        super.onBackPressed();
    }

    private ConnectionClassManager.ConnectionClassStateChangeListener listener
            = bandwidthState -> {
        System.out.println("Connection MainActivity bandwidthState:" + bandwidthState.toString());
        Log.e("onBandwidthStateChange", bandwidthState.toString());
    };

    private long currentTime = -1;
    private long currentBytes = -1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                long bytes = TrafficStats.getUidRxBytes(Process.myUid());
                long time = System.currentTimeMillis();

                double s = (bytes - currentBytes) / (time - currentTime);
                currentBytes = bytes;
                currentTime = time;

                ConnectionQuality connectionQuality = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
                String name = connectionQuality.name();
                if (name.equals("POOR")) {
                }
                tv_content.setText((int) ConnectionClassManager.getInstance().getDownloadKBytePerSecond() + "k/s " + name
                        + "\n " + s + "k/s"
                        + "\n 网络类型：" + VideoUtils.getMoblieNetWorkType(getApplicationContext()));
                mHandler.sendEmptyMessageDelayed(1, 1000);
            }

        }
    };
}
