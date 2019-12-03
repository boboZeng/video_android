package com.sport.video;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.video.live.NetWorkBroadcastReceiver;
import com.video.live.OnBackListener;
import com.video.live.SimpleVideoLayoutController;
import com.video.live.VideoLayout;
import com.video.live.VideoUtils;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private VideoLayout videoLayout;
    private SimpleVideoLayoutController controller;
    //    private String path = "rtmp://wslive.undemonstrable.cn/wslive1/5759_push_5ddda0f46684e?wsTime=1575009617&wsSecret=d4323e657297dd55680d808bb29c0775";
    private String path = "rtmp://wslive.undemonstrable.cn/wslive1/6200_push_5de5c9b0f2b86?wsTime=1575341112&wsSecret=bc2e361c5af7a1a77700b3176c4b630b";

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

//        controller.gotoScreenFullscreen();

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
        public void onConnect() {
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
