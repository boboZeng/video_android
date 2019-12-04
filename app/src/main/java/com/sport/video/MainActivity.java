package com.sport.video;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.video.live.ClarityModel;
import com.video.live.IjkPlayerStatus;
import com.video.live.NetWorkBroadcastReceiver;
import com.video.live.OnBackListener;
import com.video.live.OnCustomInfoListener;
import com.video.live.SimpleVideoLayoutController;
import com.video.live.VideoConstants;
import com.video.live.VideoLayout;
import com.video.live.VideoUtils;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private VideoLayout videoLayout;
    private SimpleVideoLayoutController controller;
    //    private String path = "rtmp://wslive.undemonstrable.cn/wslive1/5759_push_5ddda0f46684e?wsTime=1575009617&wsSecret=d4323e657297dd55680d808bb29c0775";
    private String path = "rtmp://wslive.undemonstrable.cn/wslive1/6390_pull_5de86b5979f62?wsTime=1575513019&wsSecret=1691ce2824fce2dbadcbad088b77e6a3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VideoUtils.setEnableDebug(true);

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
                    Toast.makeText(MainActivity.this, "网络不稳定，切换低清晰度播放更流畅", Toast.LENGTH_SHORT).show();
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

        if (VideoUtils.isNetworkConnected(this)
                && VideoUtils.isMobileConnected(this)) {
            Toast.makeText(this, "当前为非wifi环境，请注意流量消耗", Toast.LENGTH_SHORT).show();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkBroadcastReceiver, filter);
    }

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

    @Override
    public void onStop() {
        super.onStop();
        if (videoLayout != null) {
            videoLayout.pause(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netWorkBroadcastReceiver);
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

    private NetWorkBroadcastReceiver netWorkBroadcastReceiver = new NetWorkBroadcastReceiver() {
        @Override
        public void onConnect(Context context) {
            if (VideoUtils.isNetworkConnected(context)
                    && VideoUtils.isMobileConnected(context)) {
                Toast.makeText(context, "当前为非wifi环境，请注意流量消耗", Toast.LENGTH_SHORT).show();
            }
            if (videoLayout == null) {
                return;
            }
            if (videoLayout.getCurrentState() == VideoLayout.STATE_AUTO_PAUSED
                    || videoLayout.getCurrentState() == VideoLayout.STATE_ERROR) {
                videoLayout.load();
            }
        }
    };
}
