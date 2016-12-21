package com.xiaoyongit.xsimpleplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.R;
import io.vov.vitamio.widget.VideoView;


/**
 * 视频播放器 by:xiaoyong
 */
public class CustomVideoPlayer extends LinearLayout implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    //*******************视频播放画质*******************
    public static final int VIDEO_QUALITY_LOW = -16;//低
    public static final int VIDEO_QUALITY_MEDIUM = 0;//中
    public static final int VIDEO_QUALITY_HIGH = 16;//高

    //*******************视频画面缩放模式***************
    public static final int VIDEO_LAYOUT_ORIGIN = 0;
    public static final int VIDEO_LAYOUT_SCALE = 1;
    public static final int VIDEO_LAYOUT_STRETCH = 2;
    public static final int VIDEO_LAYOUT_ZOOM = 3;
    public static final int VIDEO_LAYOUT_FIT_PARENT = 4;

    private Uri uri;

    private CustomMediaController mCustomMediaController;
    private VideoView mVideoView;
    private View mContent;

    //加载指示器
    private TextView mLoaddownloadRate, mLoadRate;
    private ProgressBar mLoadProg;

    private OnClickListener mOnBackBtnListener;

    private OnClickListener mOnFullBtnListener;

    /**
     * 原始高度 or 宽度
     */
    private int mheight = -1;
    private int mWidth = -1;


    public CustomVideoPlayer(Context context) {
        this(context, null);
    }

    public CustomVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_custom_videoplayer, this);
        mVideoView = (VideoView) findViewById(R.id.buffer);
        //加载指示器
        mLoadProg = (ProgressBar) findViewById(R.id.load_prog);
        mLoaddownloadRate = (TextView) findViewById(R.id.load_download_rate);
        mLoadRate = (TextView) findViewById(R.id.load_rate);
        mContent = (RelativeLayout) findViewById(R.id.content);

        mCustomMediaController = new CustomMediaController(context, mVideoView, this);


        mVideoView.setMediaController(mCustomMediaController);
        //默认缓存512kb
        mVideoView.setBufferSize(1024 * 512);
        mVideoView.setVideoQuality(VIDEO_QUALITY_HIGH);//高画质
        //设置视频缩放模式
        //mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE,0);

        //播放信息监听
        mVideoView.setOnInfoListener(this);
        //缓存更新进度监听
        mVideoView.setOnBufferingUpdateListener(this);

        //添加布局完成后的监听
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * 这里获取View原本高度和宽度并记录，在全屏恢复成半屏时候有用
     */
    public void onGlobalLayout() {
        if (mheight == -1) {
            mheight = getHeight();
        }
        if (mWidth == -1) {
            mWidth = getWidth();
        }
    }


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * 设置视频缓冲大小 如不设置则默认为512kb  1024*512
     * @param bufferSie
     */
    public void setBufferSie(int bufferSie){
        mVideoView.setBufferSize(bufferSie);
    }

    /**
     *设置视频播放画质 默认：VIDEO_QUALITY_HIGH  高画质
     * @param quality
     */
    public void setVideoQuality(int quality){
        mVideoView.setVideoQuality(quality);
    }

    /**
     * 设置播放完成事件监听
     *
     * @param listener
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mVideoView.setOnCompletionListener(listener);
    }

    /**
     * 错误事件监听
     *
     * @param listener
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        mVideoView.setOnErrorListener(listener);
    }


    /**
     * 设置视频标题
     *
     * @param title
     */
    public void setVideTitle(String title) {
        mCustomMediaController.setVideoName(title);
    }

    /**
     * 设置视频缩放方式  默认 VIDEO_LAYOUT_SCALE
     */
    public void setVideoLayout(int layout){
        mVideoView.setVideoLayout(layout, 0);
    }

    /**
     * 设置URL地址并且播放
     *
     * @param path
     */
    public void setUp(String path) {
        uri = Uri.parse(path);
        mVideoView.setVideoURI(uri);//设置视频播放地址
        mVideoView.requestFocus();
    }

    /**
     * 设置是否可触摸控制
     *
     */
    public void setIsTouchController(boolean isTouch) {
        mCustomMediaController.setTouchController(isTouch);
    }

    /**
     * 是否可以触摸控制
     */
    public boolean getIsTouchController() {
       return mCustomMediaController.getTouchController();
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            //缓存加载开始
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    mLoadProg.setVisibility(View.VISIBLE);
                    mLoaddownloadRate.setText("");
                    mLoadRate.setText("");
                    mLoaddownloadRate.setVisibility(View.VISIBLE);
                    mLoadRate.setVisibility(View.VISIBLE);

                }
                break;
            //缓存加载完成
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                mLoadProg.setVisibility(View.GONE);
                mLoaddownloadRate.setVisibility(View.GONE);
                mLoadRate.setVisibility(View.GONE);
                break;
            //缓存加载进度被改变
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                mLoaddownloadRate.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mLoadRate.setText(percent + "%");
    }


    /**
     * 恢复半屏
     */
    public void setNoFullScreen() {
        Activity activity = (Activity) getContext();
        Window window = activity.getWindow();

        WindowManager.LayoutParams attrs = window.getAttributes();
        //设置屏幕方向
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //清除屏幕参数
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setAttributes(attrs);

        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        float width = getResources().getDisplayMetrics().heightPixels;
        //float height = dp2px(300f);
        //这里一定要这样设置 要不视频的宽高比会变
        getLayoutParams().height = mheight;
        mContent.getLayoutParams().width = (int) width;
        mContent.getLayoutParams().height = mheight;
        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        mCustomMediaController.refReshView();
    }


    /**
     * 设置为全屏
     */
    public void setFullScreen() {
        Activity activity = (Activity) getContext();
        Window window = activity.getWindow();

        WindowManager.LayoutParams attrs = window.getAttributes();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setAttributes(attrs);

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        //获取屏幕尺寸
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);

        //设置Video布局尺寸
        getLayoutParams().height = metrics.heightPixels;
        mContent.getLayoutParams().width = metrics.widthPixels;
        mContent.getLayoutParams().height = metrics.heightPixels;
        //设置为全屏拉伸
        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        mCustomMediaController.refReshView();
    }


    /**
     * dp转px
     *
     * @param dpValue
     * @return
     */
    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return mVideoView.isPlaying();
    }


    /**
     * 获取当前播放位置
     *
     * @return
     */
    public long getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }


    /**
     * 跳转到指定播放进度
     *
     * @param index
     */
    public void setSeekTo(long index) {
        mVideoView.seekTo(index);
    }


    /**
     * 暂停播放
     */
    public void pause() {
        mVideoView.pause();
    }


    /**
     * 启动
     */
    public void start() {
        mVideoView.start();
    }

    /**
     * 释放资源
     */
    public void onDestroy() {
        mVideoView.stopPlayback();
    }

    /**
     * 设置返回按钮监听
     *
     * @param listener
     */
    public void setOnBackBtnClickListener(OnClickListener listener) {
        mOnBackBtnListener = listener;
    }

    /**
     * 设置全屏按钮监听
     *
     * @param listener
     */
    public void setOnFullBtnClickListener(OnClickListener listener) {
        mOnFullBtnListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mCustomMediaController.mbutton_id_Back) {
            if (mOnBackBtnListener != null) {
                mOnBackBtnListener.onClick(v);
            }


        } else if (v.getId() == mCustomMediaController.mbutton_id_FullScreen) {
            if (mOnFullBtnListener != null) {
                mOnFullBtnListener.onClick(v);
            }

        }
    }


}
