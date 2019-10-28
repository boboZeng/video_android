package com.video.live;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;

/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-10-22.
 **/
public abstract class VideoLayoutController {
    protected Context context;

    public VideoLayoutController(Context context) {
        this.context = context;
    }

    public abstract void initView(View view, VideoController videoController);

    public abstract FrameLayout getSurface_container();

    /**
     * 显示视频画面的SurfaceView
     * @param visibility
     */
    public abstract void setSurfaceViewVisibility(int visibility);

    /**
     * 加载动画显示
     * @param visibility
     */
    public abstract void setLoadingVisibility(int visibility);


    /**
     * 暂停播放按钮
     *
     * @param resId
     */
    public abstract void setPlayImageResource(@DrawableRes int resId);

    public abstract boolean isFullScreen();

    public abstract void release();

}
