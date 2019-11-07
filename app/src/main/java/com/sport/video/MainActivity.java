package com.sport.video;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.video.live.OnBackListener;
import com.video.live.SimpleVideoLayoutController;
import com.video.live.VideoLayout;
import com.video.live.VideoUtils;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {
    private VideoLayout videoLayout;
    private SimpleVideoLayoutController controller;
    //    private String path ="rtmp://lssplay.bd666.cn/zbprod/2156_push_5d9dce3b4a83a?k=8fcc652ae16a21c6beef22b6c747b698&t=1571671666" ;
//    private String path = "rtmp://lssplay.bd666.cn/hdzb001/215_5d762821e5829?k=63154822b690063a5469bf196c43ab89&t=1571215911";
//    private String path = "rtmp://lssplay.bd666.cn/zbprod/2156_pull_5dafba8497b83?k=479692bb5a1397b50ad9b1dbf3ba490f&t=1571799301";
//    private String path ="rtmp://lssplay.bd666.cn/zbprod/2156_push_5d9dce3b4a83a?k=49c660e930b1e66b6f711a5f1354537a&t=1571807896";
    private String path ="http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
//    private String path ="rtmp://lssplay.undemonstrable.cn/zbprod/2728_pull_5dc3669738006?k=f731d665863d6dd1e3604dc24c8a7b05&t=1573086901";


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

//        controller.gotoScreenFullscreen();
    }

    @Override
    public void onBackPressed() {
        if (controller != null
                && controller.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
