package com.video.live;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;


/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-10-21.
 **/
public class VideoLayout extends FrameLayout implements VideoController {
    private static final int STATE_IDLE = 0;//闲置
    private static final int STATE_PREPARING = 1;//正在预加载
    private static final int STATE_PREPARED = 2;//预加载完成
    private static final int STATE_PLAYING = 3;//正在播放
    private static final int STATE_PAUSED = 4;//暂停
    private static final int STATE_STOP = 5;//停止
    private static final int STATE_PLAYBACK_COMPLETED = 6;//播放完成

    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private IjkMediaPlayer mMediaPlayer = null;
    /**
     * 视频文件地址
     */
    private String mPath;
    /**
     * 视频请求header
     */
    private Map<String, String> mHeader;

    private SurfaceView mSurfaceView;

    private Context mContext;
    private boolean mEnableMediaCodec;
    private AudioManager mAudioManager;
    private AudioFocusHelper mAudioFocusHelper;
    private int layout;
    private float aspectRatio = 9 / 16f;
    private boolean isDefaultFullScreen = false;
    private VideoLayoutController videoLayoutController;
    private int currentState = STATE_IDLE;

    public VideoLayout(@NonNull Context context) {
        this(context, null);

    }

    public VideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public VideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        if (context == null || attrs == null) {
            return;
        }
        setBackgroundColor(Color.BLACK);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VideoLayout,
                defStyleAttr, defStyleRes);
        layout = typedArray.getResourceId(R.styleable.VideoLayout_layout, R.layout.video_layout);
        aspectRatio = typedArray.getFloat(R.styleable.VideoLayout_aspectRatio, 9 / 16f);
        isDefaultFullScreen = typedArray.getBoolean(R.styleable.VideoLayout_isDefaultFullScreen, false);


        mAudioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper();
    }

    /**
     * 设置视频播放控制器
     *
     * @param controller
     */
    public void initVideoLayoutController(VideoLayoutController controller) {
        this.videoLayoutController = controller;
        if (videoLayoutController == null) {
            videoLayoutController = new SimpleVideoLayoutController(getContext(), null);
        }
        View view = LayoutInflater.from(getContext()).inflate(layout, null);
        videoLayoutController.initView(view, this, isDefaultFullScreen);
        createSurfaceView(videoLayoutController.getSurface_container());
    }

    public void setSurfaceViewVisibility(int visibility) {
        videoLayoutController.setSurfaceViewVisibility(visibility);
    }


    /**
     * 设置播放地址
     *
     * @param path
     */
    public void setPath(String path) {
        setPath(path, null);
    }

    public void setPath(String path, Map<String, String> header) {
        if (VideoUtils.isNullOrEmpty(path)) {
            return;
        }
        mPath = path;
        mHeader = header;
    }


    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            VideoUtils.d("VideoLayout createSurfaceView surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            VideoUtils.d("VideoLayout createSurfaceView surfaceChanged");
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(surfaceHolder);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            VideoUtils.d("VideoLayout createSurfaceView surfaceDestroyed");

        }
    };

    //创建surfaceView
    private void createSurfaceView(FrameLayout surface_container) {
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceView.getHolder().addCallback(callback);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT
                , LayoutParams.MATCH_PARENT, Gravity.CENTER);
        surface_container.addView(mSurfaceView, 0, layoutParams);
    }

    //创建一个新的player
    private IjkMediaPlayer createPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "http-detect-range-support", 1);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "min-frames", 100);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        //如果项目中同时使用了HTTP和HTTPS的视频源的话，要注意如果视频源刚好是相同域名，会导致播放失败，这是由于dns缓存造成的;
        //设置清除dns cache;
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "async,cache,crypto,file,http,https,ijkhttphook,ijkinject,ijklivehook,ijklongurl,ijksegment,ijktcphook,pipe,rtp,tcp,tls,udp,ijkurlhook,data,concat,subfile,udp,ffconcat");
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"safe",0);


        ijkMediaPlayer.setVolume(1.0f, 1.0f);

        setEnableMediaCodec(ijkMediaPlayer, mEnableMediaCodec);
        return ijkMediaPlayer;
    }

    //设置是否开启硬解码
    private void setEnableMediaCodec(IjkMediaPlayer ijkMediaPlayer, boolean isEnable) {
        int value = isEnable ? 1 : 0;
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", value);//开启硬解码
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", value);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", value);
    }

    public void setEnableMediaCodec(boolean isEnable) {
        mEnableMediaCodec = isEnable;
    }

    //设置ijkplayer的监听
    private void setListener(IjkMediaPlayer player) {
        player.setOnPreparedListener(mPreparedListener);
        player.setOnVideoSizeChangedListener(mVideoSizeChangedListener);
        player.setOnErrorListener(mErrorListener);
        player.setOnInfoListener(mInfoListener);
        player.setOnBufferingUpdateListener(mBufferingUpdateListener);
        player.setOnSeekCompleteListener(mSeekCompleteListener);
        player.setOnTimedTextListener(mTimedTextListener);
        player.setOnCompletionListener(mCompletionListener);
        player.setOnNativeInvokeListener(mNativeInvokeListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (videoLayoutController.isFullScreen() || getChildAt(0) == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = (int) (specWidth * aspectRatio);
        VideoUtils.d("VideoLayout onMeasure specWidth:" + specWidth
                + ",specHeight:" + specHeight);
        setMeasuredDimension(specWidth, specHeight);

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
        getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }


    //<editor-fold desc="VideoController">

    @Override
    public void load() {
        if (VideoUtils.isNullOrEmpty(mPath)) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        try {
            currentState = STATE_PREPARING;
            mMediaPlayer = createPlayer();
            setListener(mMediaPlayer);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());

            Uri mUri = Uri.parse(mPath);
            String scheme = mUri.getScheme();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
//                    mSettings.getUsingMediaDataSource() &&
                    (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                IMediaDataSource dataSource = new FileMediaDataSource(new File(mUri.toString()));
                mMediaPlayer.setDataSource(dataSource);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                    RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(getContext(), mUri);
                    mMediaPlayer.setDataSource(rawDataSourceProvider);
                } else {
                    mMediaPlayer.setDataSource(getContext(), mUri, mHeader);
                }
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }
//            mMediaPlayer.setDataSource(mContext, mUri, mHeader);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            currentState = STATE_IDLE;
            VideoUtils.d("VideoLayout load e:" + e.getMessage());
        }
    }

    @Override
    public void start() {
        if (mMediaPlayer != null) {
            currentState = STATE_PLAYING;
            videoLayoutController.setPlayImageResource(R.drawable.video_sel_pause);
            mMediaPlayer.start();
            mAudioFocusHelper.requestFocus();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            currentState = STATE_PAUSED;
            videoLayoutController.setPlayImageResource(R.drawable.video_sel_play);
            mMediaPlayer.pause();
            mAudioFocusHelper.abandonFocus();
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            currentState = STATE_STOP;
            videoLayoutController.setPlayImageResource(R.drawable.video_sel_play);
            mMediaPlayer.stop();
            mAudioFocusHelper.abandonFocus();
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            currentState = STATE_IDLE;
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioFocusHelper.abandonFocus();
        }
        if (videoLayoutController != null) {
            videoLayoutController.release();
        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            currentState = STATE_IDLE;
            videoLayoutController.setPlayImageResource(R.drawable.video_sel_play);
            mMediaPlayer.reset();
            mAudioFocusHelper.abandonFocus();
        }
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(long pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public boolean isPausing() {
        return currentState == STATE_PAUSED;
    }

    @Override
    public String getVideoPath() {
        return mPath;
    }

    @Override
    public VideoLayout getVideoLayout() {
        return this;
    }

    @Override
    public int getCurrentState() {
        return currentState;
    }

    //</editor-fold>

    //<editor-fold desc="音频监听 ">

    /**
     * 音频焦点改变监听
     */
    private class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
        boolean startRequested = false;
        boolean pausedForLoss = false;
        int currentFocus = 0;

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (currentFocus == focusChange) {
                return;
            }

            currentFocus = focusChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN://获得焦点
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT://暂时获得焦点
                    if (startRequested || pausedForLoss) {
                        start();
                        startRequested = false;
                        pausedForLoss = false;
                    }
                    if (mMediaPlayer != null)//恢复音量
                        mMediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS://焦点丢失
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://焦点暂时丢失
                    if (isPlaying()) {
                        pausedForLoss = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://此时需降低音量
                    if (mMediaPlayer != null && isPlaying()) {
                        mMediaPlayer.setVolume(0.1f, 0.1f);
                    }
                    break;
            }
        }

        boolean requestFocus() {
            if (currentFocus == AudioManager.AUDIOFOCUS_GAIN) {
                return true;
            }

            if (mAudioManager == null) {
                return false;
            }

            int status = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
                currentFocus = AudioManager.AUDIOFOCUS_GAIN;
                return true;
            }

            startRequested = true;
            return false;
        }

        boolean abandonFocus() {

            if (mAudioManager == null) {
                return false;
            }

            startRequested = false;
            int status = mAudioManager.abandonAudioFocus(this);
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status;
        }
    }

    //</editor-fold>

    //<editor-fold desc="各种listener 赋值">
    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            VideoUtils.d("VideoLayout onPrepared:");
            currentState = STATE_PREPARED;
            start();
            if (onPreparedListener != null) {
                onPreparedListener.onPrepared(iMediaPlayer);
            }
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            VideoUtils.d("VideoLayout onVideoSizeChanged width:" + width + ",height:" + height);
            VideoUtils.d("VideoLayout onVideoSizeChanged sar_num:" + sar_num + ",sar_den:" + sar_den);
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                mSurfaceView.getHolder().setFixedSize(videoWidth, videoHeight);
            }
            if (onVideoSizeChangedListener != null) {
                onVideoSizeChangedListener.onVideoSizeChanged(mp, width, height, sar_num, sar_den);
            }
        }
    };

    private IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {


        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            currentState = STATE_IDLE;
            VideoUtils.d("VideoLayout onError what:" + what + ",extra:" + extra);
            if (onErrorListener != null) {
                return onErrorListener.onError(mp, what, extra);
            }
            return true;
        }
    };


    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            VideoUtils.d("VideoLayout onInfo what:" + what + ",extra:" + extra);
            if (onInfoListener != null) {
                return onInfoListener.onInfo(mp, what, extra);
            }
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            VideoUtils.d("VideoLayout onBufferingUpdate percent:" + percent);
            if (onBufferingUpdateListener != null) {
                onBufferingUpdateListener.onBufferingUpdate(mp, percent);
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            VideoUtils.d("VideoLayout onSeekComplete:");
            currentState = STATE_IDLE;
            if (onSeekCompleteListener != null) {
                onSeekCompleteListener.onSeekComplete(mp);
            }
        }
    };

    private IMediaPlayer.OnTimedTextListener mTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
            VideoUtils.d("VideoLayout onTimedText rect:" + (text == null ? null : text.getText())
                    + ",bounds:" + (text == null ? null : text.getBounds()));
            if (onTimedTextListener != null) {
                onTimedTextListener.onTimedText(mp, text);
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            VideoUtils.d("VideoLayout onCompletion:");
            currentState = STATE_IDLE;
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion(mp);
            }
        }
    };

    IjkMediaPlayer.OnNativeInvokeListener mNativeInvokeListener = new IjkMediaPlayer.OnNativeInvokeListener() {
        @Override
        public boolean onNativeInvoke(int i, Bundle bundle) {
            VideoUtils.d("VideoLayout onNativeInvoke i:" + i);
            if (onNativeInvokeListener != null) {
                return onNativeInvokeListener.onNativeInvoke(i, bundle);
            }
            return true;
        }
    };

    private IMediaPlayer.OnPreparedListener onPreparedListener;
    private IMediaPlayer.OnCompletionListener onCompletionListener;
    private IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener;
    private IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener;
    private IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener;
    private IMediaPlayer.OnErrorListener onErrorListener;
    private IMediaPlayer.OnInfoListener onInfoListener;
    private IMediaPlayer.OnTimedTextListener onTimedTextListener;
    private IjkMediaPlayer.OnNativeInvokeListener onNativeInvokeListener;


    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
        this.onBufferingUpdateListener = onBufferingUpdateListener;
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        this.onSeekCompleteListener = onSeekCompleteListener;
    }

    public void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
    }

    public void setOnTimedTextListener(IMediaPlayer.OnTimedTextListener onTimedTextListener) {
        this.onTimedTextListener = onTimedTextListener;
    }

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setOnNativeInvokeListener(IjkMediaPlayer.OnNativeInvokeListener onNativeInvokeListener) {
        this.onNativeInvokeListener = onNativeInvokeListener;
    }

    public void setAspectRatio(float ratio) {
        this.aspectRatio = ratio;
    }
    //</editor-fold>
}
