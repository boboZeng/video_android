package com.video.live;

import java.io.IOException;

/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-10-21.
 **/
public interface VideoController {
    /**
     * 加载视频，加载完成直接播放；
     */
    void load() throws IOException;

    /**
     * 重新开始播放
     */
    void start();

    void pause();

    /**
     * @param isAutoPause true :自动暂停，回来要继续播放（如按home建)
     */
    void pause(boolean isAutoPause);

    void stop();

    void release();

    void reset();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long pos);

    boolean isPlaying();

    boolean isPausing();

    String getVideoPath();

    VideoLayout getVideoLayout();

    /**
     * 当前状态
     *
     * @return
     */
    int getCurrentState();
}
