package com.video.live;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import java.io.IOException;

/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-10-22.
 **/
public class SimpleVideoLayoutController extends VideoLayoutController implements View.OnClickListener {
    private VideoController videoController;
    protected ImageView startButton, imgThumb;
    protected ImageView fullscreenButton;
    protected TextView tv_status;
    protected View parentLayout, layout_top, layout_bottom, statusBar;
    protected FrameLayout surface_container;
    protected ProgressBar progressBar;
    private boolean isFullScreen = false, isShowCover = false;
    private OnBackListener onBackListener;

    public SimpleVideoLayoutController(Context context, OnBackListener onBackListener) {
        super(context);
        this.onBackListener = onBackListener;
    }

    @Override
    public void initView(View view, VideoController videoController) {
        this.parentLayout = view;
        this.videoController = videoController;
        tv_status = parentLayout.findViewById(R.id.tv_status);
        startButton = parentLayout.findViewById(R.id.start);
        imgThumb = parentLayout.findViewById(R.id.thumb);
        fullscreenButton = parentLayout.findViewById(R.id.fullscreen);
        surface_container = parentLayout.findViewById(R.id.surface_container);
        layout_top = parentLayout.findViewById(R.id.layout_top);
        layout_bottom = parentLayout.findViewById(R.id.layout_bottom);
        statusBar = parentLayout.findViewById(R.id.statusBar);
        progressBar = parentLayout.findViewById(R.id.progressBar);

        parentLayout.findViewById(R.id.back).setOnClickListener(this);
        startButton.setOnClickListener(this);
        fullscreenButton.setOnClickListener(this);
        surface_container.setOnClickListener(this);

        //首次不调用该方法，会引起状态栏异常
//            clearFloatScreen();
        if (parentLayout.getParent() != null) {
            ViewGroup vg = (ViewGroup) (VideoUtils.scanForActivity(context)).getWindow().getDecorView();
            vg.removeView(parentLayout);
        }

        videoController.getVideoLayout().addView(parentLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public FrameLayout getSurface_container() {
        return surface_container;
    }

    @Override
    public void setSurfaceViewVisibility(int visibility) {
        surface_container.setVisibility(visibility);
        int count = surface_container.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = surface_container.getChildAt(i);
            view.setVisibility(visibility);
        }
    }

    /**
     * 暂停播放按钮
     *
     * @param resId
     */
    @Override
    public void setPlayImageResource(@DrawableRes int resId) {
        startButton.setImageResource(resId);
    }

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    @Override
    public void release() {
        setTopVisibility(View.GONE);
        setBottomVisibility(View.GONE);
        setSurfaceViewVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.start) {
            if (videoController == null) {
                return;
            }
            if (VideoUtils.isNullOrEmpty(videoController.getVideoPath())) {
                Toast.makeText(v.getContext(),
                        v.getResources().getString(R.string.no_url),
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (videoController.isPlaying()) {
                videoController.pause();
            } else {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    videoController.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (id == R.id.fullscreen) {
            if (!videoController.isPlaying()) {
                return;
            }
            if (isFullScreen) {
                clearFloatScreen();
            } else {
                gotoScreenFullscreen();
            }
        } else if (id == R.id.surface_container) {
            showHideCover(isShowCover);
        } else if (id == R.id.back) {
            if (!onBackPressed()) {
                if (onBackListener != null) {
                    onBackListener.onBack();
                }
            }
        }
    }


    public void clearFloatScreen() {
        isFullScreen = false;
        statusBar.setVisibility(View.VISIBLE);
        VideoUtils.showStatusBar(context);
        VideoUtils.setRequestedOrientation(context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        VideoUtils.showSystemUI(context);

        if (parentLayout.getParent() != null) {
            ViewGroup vg = (ViewGroup) (VideoUtils.scanForActivity(context)).getWindow().getDecorView();
            vg.removeView(parentLayout);
        }

        videoController.getVideoLayout().addView(parentLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    public void gotoScreenFullscreen() {
        isFullScreen = true;
        statusBar.setVisibility(View.GONE);
        VideoUtils.hideStatusBar(context);
        VideoUtils.setRequestedOrientation(context, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        VideoUtils.hideSystemUI(context);//华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326

        if (parentLayout.getParent() != null) {
            videoController.getVideoLayout().removeView(parentLayout);
        }
        ViewGroup vg = (ViewGroup) (VideoUtils.scanForActivity(context)).getWindow().getDecorView();//和他也没有关系
        vg.addView(parentLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    public void showHideCover(boolean flag) {
        if (VideoUtils.isNullOrEmpty(videoController.getVideoPath())) {
            return;
        }
        setBottomVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        setTopVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        startButton.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        isShowCover = !flag;
    }

    /**
     * 控制返回按钮显示
     *
     * @param visibility
     */
    public void setTopVisibility(int visibility) {
        layout_top.setVisibility(visibility);
    }

    public void setBottomVisibility(int visibility) {
        layout_bottom.setVisibility(visibility);
    }

    /**
     * 背景图片显示
     *
     * @param visibility
     */
    public void setThumbVisibility(int visibility) {
        imgThumb.setVisibility(visibility);
    }

    public void setProgressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }

    /**
     * 提示文字显示
     *
     * @param text
     */
    public void setStatusText(String text) {
        if (VideoUtils.isNullOrEmpty(text)) {
            tv_status.setVisibility(View.GONE);
        } else {
            tv_status.setVisibility(View.VISIBLE);
            tv_status.setText(text);
        }
    }

    /**
     * 覆盖背景图
     *
     * @return
     */
    public ImageView getImgThumb() {
        return imgThumb;
    }

    public boolean onBackPressed() {
        if (isFullScreen) {
            clearFloatScreen();
            return true;
        }
        return false;
    }

    public View getParentLayout() {
        return parentLayout;
    }
}
