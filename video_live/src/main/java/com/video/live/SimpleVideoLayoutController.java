package com.video.live;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-10-22.
 **/
public class SimpleVideoLayoutController extends VideoLayoutController implements View.OnClickListener {
    private VideoController videoController;
    protected ImageView startButton, imgThumb;
    protected ImageView fullscreenButton, img_lock;
    protected TextView tv_status, tv_clarity;
    protected TextView tv_message;
    protected View parentLayout, layout_top, layout_bottom, statusBar;
    protected FrameLayout surface_container;
    protected LinearLayout ll_clarity;
    protected ProgressBar progressBar;
    private boolean isFullScreen = false, isShowCover = false;
    private OnBackListener onBackListener;
    private List<ClarityModel> clarityList = new ArrayList<>();//清晰度列表
    private int clarityPosition = -1;//选中播放清晰度

    public SimpleVideoLayoutController(Context context, OnBackListener onBackListener) {
        super(context);
        this.onBackListener = onBackListener;
    }

    @Override
    public void initView(View view, VideoController videoController) {
        this.parentLayout = view;
        this.videoController = videoController;
        tv_status = parentLayout.findViewById(R.id.tv_status);
        tv_message = parentLayout.findViewById(R.id.textView_message);
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

        ll_clarity = parentLayout.findViewById(R.id.ll_clarity);
        tv_clarity = parentLayout.findViewById(R.id.tv_clarity);
        tv_clarity.setOnClickListener(this);
        img_lock = parentLayout.findViewById(R.id.img_lock);
        img_lock.setOnClickListener(this);

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
        int count = surface_container.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = surface_container.getChildAt(i);
            view.setVisibility(visibility);
        }
    }

    @Override
    public void setLoadingVisibility(int visibility) {
        if (progressBar != null) {
            progressBar.setVisibility(visibility);
        }
    }

    @Override
    public void setMessage(String message) {
        if (tv_message != null)
            tv_message.setText(message);
    }

    @Override
    public void clearMessage() {
        setMessage("");
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
        setLoadingVisibility(View.GONE);
        clearMessage();
    }

    @Override
    public void changePath() {
        updateClarityChildView();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.img_lock) {
            boolean isSelect = img_lock.isSelected();
            img_lock.setSelected(!isSelect);
            img_lock.setImageResource(isSelect ? R.drawable.video_icon_unlock : R.drawable.video_icon_lock);
            showHideCover(true);
        } else if (id == R.id.start) {
            setMessage("");
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
        } else if (id == R.id.tv_clarity) {
            if (ll_clarity.getVisibility() == View.VISIBLE) {
                return;
            }
            ll_clarity.setVisibility(View.VISIBLE);
            clarityAnimator(true);
        }
    }

    private void clarityAnimator(final boolean isShow) {
        float x = ll_clarity.getResources().getDimension(R.dimen.clarity_width);
        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_clarity, "translationX",
                isShow ? x : 0, isShow ? 0 : x);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(100)
                .start();
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

    /**
     * @param flag true 隐藏
     */
    public void showHideCover(boolean flag) {
        VideoUtils.d("VideoLayout showHideCover flag:" + flag
                + ",path:" + videoController.getVideoPath()
                + ",img_lock.isSelected():" + img_lock.isSelected());
        if (VideoUtils.isNullOrEmpty(videoController.getVideoPath())) {
            return;
        }
        img_lock.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        isShowCover = !flag;
        if (img_lock.isSelected()) {
            flag = true;
        }
        setBottomVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        setTopVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        startButton.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        ll_clarity.setVisibility(View.INVISIBLE);

    }

    /**
     * 控制返回按钮显示
     *
     * @param visibility
     */
    public void setTopVisibility(int visibility) {
        layout_top.setVisibility(visibility);
        layout_bottom.invalidate();
    }

    public void setBottomVisibility(int visibility) {
        layout_bottom.setVisibility(visibility);
        layout_bottom.invalidate();
    }

    /**
     * 背景图片显示
     *
     * @param visibility
     */
    public void setThumbVisibility(int visibility) {
        imgThumb.setVisibility(visibility);
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

    /**
     * 设置支持的清晰度
     *
     * @param clarityList
     */
    @Override
    public void setClarityList(List<ClarityModel> clarityList) {
        this.clarityList.clear();
        clarityPosition = -1;
        if (clarityList == null || clarityList.size() <= 0) {
            ll_clarity.removeAllViews();
            tv_clarity.setVisibility(View.GONE);
            return;
        }
        tv_clarity.setVisibility(View.VISIBLE);
        this.clarityList.addAll(clarityList);
        initClarityChildView();
    }

    @Override
    public boolean isSupportChangeClarity() {
        return clarityList != null && clarityList.size() > 0;
    }

    /**
     * 初始化清晰度
     */
    private void initClarityChildView() {
        ll_clarity.removeAllViews();
        if (clarityList == null || clarityList.size() <= 0) {
            tv_clarity.setVisibility(View.GONE);
            return;
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        for (int i = 0; i < clarityList.size(); i++) {
            ClarityModel model = clarityList.get(i);
            if (clarityPosition != -1
                    && !VideoUtils.isNullOrEmpty(videoController.getVideoPath())
                    && videoController.getVideoPath().equals(model.getUrl())) {
                clarityPosition = i;
            }
            ll_clarity.addView(getClarityChildView(ll_clarity.getContext(), i, model.getName()),
                    i, params);
        }
    }

    private TextView getClarityChildView(Context context, int position, String text) {
        TextView tv = new TextView(context);
        tv.setTag(position);
        tv.setText(text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                ClarityModel model = clarityList.get(position);
                videoController.setPath(model.getUrl());
                try {
                    videoController.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(position == clarityPosition ? Color.rgb(0, 0xae, 0xef)
                : Color.rgb(0xff, 0xff, 0xff));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        return tv;
    }

    /**
     * 更新当前选中播放的清晰度
     */
    private void updateClarityChildView() {
        int position = -1;
        if (!VideoUtils.isNullOrEmpty(videoController.getVideoPath())) {
            for (int i = 0; i < clarityList.size(); i++) {
                ClarityModel model = clarityList.get(i);
                if (videoController.getVideoPath().equals(model.getUrl())) {
                    position = i;
                    break;
                }
            }
        }
        if (clarityPosition >= 0
                && clarityPosition < ll_clarity.getChildCount()) {
            TextView tv = (TextView) ll_clarity.getChildAt(clarityPosition);
            tv.setTextColor(Color.rgb(0xff, 0xff, 0xff));
        }
        if (position >= 0
                && position < ll_clarity.getChildCount()) {
            TextView tv = (TextView) ll_clarity.getChildAt(position);
            tv.setTextColor(Color.rgb(0, 0xae, 0xef));
            tv_clarity.setText(tv.getText());
        }

        clarityPosition = position;
        if (clarityPosition < 0
                || clarityPosition >= ll_clarity.getChildCount()) {
            tv_clarity.setText(R.string.clarity);
        }
    }
}
