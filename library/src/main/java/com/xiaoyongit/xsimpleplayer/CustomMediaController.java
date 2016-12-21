package com.xiaoyongit.xsimpleplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.vov.vitamio.R;
import io.vov.vitamio.utils.ScreenResolution;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 *
 * 自定义视频控制器
 */
public class CustomMediaController extends MediaController {
    /**
     * 控制提示窗口的显示
     */
    private static final int HIDEFRAM = 0;
    /**
     * 屏幕的宽度
     */
    private int mScreenWidth = 0;

    /**
     * 是否可以触摸控制
     */
    private static boolean mTouchController = false;

    /**
     * 手势识别器
     */
    private GestureDetector mGestureDetector;

    /**
     * 音频管理器
     */
    private AudioManager mAudioManager;


    /**
     * 视频播放控件
     */
    private VideoView mVideoView;
    private Context mContext;


    /**
     * 视频标题
     */
    private String videoname;


    //********************** 控件**********
    /**
     * 返回按钮
     */
    private ImageButton img_back;

    /**
     * 音量or亮度提示View
     */
    private View mVolumeBrightnessLayout;

    /**
     * 提示图片
     */
    private ImageView mOperationBg;

    /**
     * 音量or亮度
     */
    private TextView mOperationTv;

    /**
     * 视频标题控件
     */
    private TextView mVideoTitle;

    /**
     * 全屏按钮
     */
    private ImageButton mFullBtn;


    /**
     * 最大音量
     */
    private int mMaxVolume;

    /**
     * 当前音量
     */
    private int mVolume;

    /**
     * 当前亮度
     */
    private int mBrightness;

    /**
     * 快进or快退指示标签
     */
    private TextView mTvFast;

    /**
     * 快进or快退
     */
    private boolean mIntoSeek = false;


    public int mbutton_id_Back = 0;

    public int mbutton_id_FullScreen = 0;

    private OnClickListener mBtnOnClickListener;


    //videoview 用于对视频进行控制的等

    /**
     * @param mContext
     * @param mVideoView
     */
    public CustomMediaController(Context mContext, VideoView mVideoView,OnClickListener listener) {
        super(mContext);
        this.mContext = mContext;
        this.mVideoView = mVideoView;
        setMediaPlayer(mVideoView);
        this.getScreenWidth();
        this.mBtnOnClickListener = listener;

        mGestureDetector = new GestureDetector(mContext, new MyGestureListener());

        //设置视频控制器的依附的父控件
        setAnchorView(mVideoView);
    }

    /**
     * 重写初始化布局的方法
     * @return
     */
    @Override
    protected View makeControllerView() {
        return this.initFindView(R.layout.view_mediacontroller);
    }

    /**
     * 在这查找控件
     * @return
     */
    private View initFindView(int layoutResId) {
        View tempView = ((Activity) mContext).getLayoutInflater().inflate(layoutResId, this);

        //获取控件ID
        mbutton_id_Back = R.id.mediacontroller_top_back;
        img_back = (ImageButton) tempView.findViewById(mbutton_id_Back);

        mbutton_id_FullScreen = R.id.mediacontroller_full;
        mFullBtn = (ImageButton) tempView.findViewById(mbutton_id_FullScreen);

        mVideoTitle = (TextView) tempView.findViewById(R.id.mediacontroller_filename);
        mTvFast = (TextView) tempView.findViewById(R.id.tv_fast);
        mVolumeBrightnessLayout = (RelativeLayout) tempView.findViewById(R.id.operation_volume_brightness);
        mOperationBg = (ImageView) tempView.findViewById(R.id.operation_bg);
        mOperationTv = (TextView) tempView.findViewById(R.id.operation_tv);

        this.initControllerData(tempView);
        return tempView;
    }

    /**
     * 初始化布局数据,注意监听等layout内部控件需要在此初始化
     */
    private void initControllerData(View gropView) {
        if (mVideoTitle != null) {
            mVideoTitle.setText(videoname);
        }
        mOperationTv.setVisibility(View.GONE);
        //系统音频管理
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //获取系统最大音量
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / mMaxVolume;
        mBrightness = this.getScreenBrightness();

        img_back.setOnClickListener(mBtnOnClickListener);
        mFullBtn.setOnClickListener(mBtnOnClickListener);
    }

    /**
     * 设置是否可触摸控制
     * @param b
     */
    public void setTouchController(boolean b){
        mTouchController = b;
    }

    /**
     * 设置是否可触摸flag
     */
    public boolean getTouchController(){
        return mTouchController;
    }

    /**
     * 获取屏幕宽度
     */
    private void getScreenWidth() {
        Pair<Integer, Integer> screenPair = ScreenResolution.getResolution(mContext);
        mScreenWidth = screenPair.first;
    }



    /**
     * 处理提示框的隐藏
     */
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case HIDEFRAM://隐藏提示窗口
                    mVolumeBrightnessLayout.setVisibility(View.GONE);
                    mOperationTv.setVisibility(View.GONE);
                    break;
            }
        }
    };


    /**
     * 获得当前屏幕亮度值 0--255
     */
    private int getScreenBrightness() {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenBrightness;
    }


    /**
     * MediaController事件传递
     *
     * @param event
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("MYApp-MyMediaController-dispatchKeyEvent");
        return true;
    }


    /**
     * MediaController的触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 手势结束处理过程
     */
    private void endGesture() {
        myHandler.removeMessages(HIDEFRAM);
        myHandler.sendEmptyMessageDelayed(HIDEFRAM, 1);
        mTvFast.setVisibility(View.GONE);
        mIntoSeek = false;
    }


    /**
     * 手势监听
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        /**
         * 因为使用的是自定义的mediaController 当显示后，mediaController会铺满屏幕，
         * 所以VideoView的点击事件会被拦截，所以重写控制器的手势事件，
         * 将全部的操作全部写在控制器中，
         * 因为点击事件被控制器拦截，无法传递到下层的VideoView，
         * 所以 原来的单机隐藏会失效，作为代替，
         * 在手势监听中onSingleTapConfirmed（）添加自定义的隐藏/显示，
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            refReshView();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        //滑动事件监听
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isFullScreen()) {
                return true;
            }
            float x1 = e1.getX();
            float y1 = e1.getY();
            float x2 = e2.getX();
            float y2 = e2.getY();
            float absX = Math.abs(x1 - x2);
            float absY = Math.abs(y1 - y2);

            float absDistanceX = Math.abs(distanceX);// distanceX < 0 从左到右
            float absDistanceY = Math.abs(distanceY);// distanceY < 0 从上到下

            // Y方向的距离比X方向的大，即 上下 滑动
            if (absDistanceX < absDistanceY && !mIntoSeek) {
                if (distanceY > 0) {//向上滑动   递增音量或者亮度
                    if (x1 >= mScreenWidth * 0.65) {
                        onVolumeSlide(1);
                    } else {//调节亮度
                        onBrightnessSlide(2);
                    }
                } else {//向下滑动  递减音量或者亮度
                    if (x1 >= mScreenWidth * 0.65) {
                        onVolumeSlide(-1);

                    } else {//调节亮度
                        onBrightnessSlide(-2);
                    }
                }

            } else {// X方向的距离比Y方向的大，即：左右滑动  快进或者快退
                if (absX > absY) {
                    mIntoSeek = true;
                    onSeekChange(x1, x2);
                    return true;
                }
            }
            return false;
        }


        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //暂停or播放
            playOrPause();
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     * 是否全屏
     */
    public boolean isFullScreen() {
        return mTouchController;
    }

    /**
     * 滑动改变声音大小
     *
     * @param value
     */
    private void onVolumeSlide(int value) {
        mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        mOperationTv.setVisibility(VISIBLE);
        mVolume += value;
        if (mVolume > 100) {
            mVolume = 100;
        } else if (mVolume < 0) {
            mVolume = 0;
        }
        mOperationBg.setImageResource(R.drawable.volmn_100);
        mOperationTv.setText(mVolume + "%");
        int tagVolume = mVolume * mMaxVolume / 100;
        //tagVolume:音量绝对值
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, tagVolume, 0);

    }

    /**
     * 滑动改变亮度
     *
     * @param value
     */
    private void onBrightnessSlide(int value) {
        mBrightness += value;
        if (mBrightness > 255) {
            mBrightness = 255;
        } else if (mBrightness <= 0) {
            mBrightness = 0;
        }
        mOperationBg.setImageResource(R.drawable.light_100);
        mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        mOperationTv.setVisibility(VISIBLE);
        mOperationTv.setText(mBrightness * 100 / 255 + "%");

        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.screenBrightness = mBrightness / 255f;
        ((Activity) mContext).getWindow().setAttributes(lp);
    }

    /**
     * 左右滑动距离计算快进/快退时间
     */
    private void onSeekChange(float x1, float x2) {
        long currentPosition = mVideoView.getCurrentPosition();

        long mSeek = 0;
        ;
        if (x1 - x2 > 50) {//向左滑
            if (currentPosition < 10000) {
                currentPosition = 0;
                mSeek = 0;
                setFashText(mSeek);
                mSeek = currentPosition;
                mVideoView.seekTo(mSeek);
            } else {
                float ducation = (x1 - x2);
                mSeek = currentPosition - ((long) ducation * 10);
                mVideoView.seekTo(mSeek);
                setFashText(mSeek);
            }
        } else if (x2 - x1 > 50) { //向右滑动
            if (currentPosition + 10000 > mVideoView.getDuration()) {
                currentPosition = mVideoView.getDuration();
                mSeek = currentPosition;
                mVideoView.seekTo(currentPosition);
                setFashText(mSeek);
            } else {
                float ducation = x2 - x1;
                mSeek = currentPosition + ((long) ducation * 10);
                mVideoView.seekTo(mSeek);
                setFashText(mSeek);
            }
        }

    }

    /**
     * 设置快进快退时间信息
     *
     * @param seek
     */
    private void setFashText(long seek) {
        String showTime = StringUtils.generateTime(seek) +
                "/" + StringUtils.generateTime(mVideoView.getDuration());
        mTvFast.setText(showTime);

        if (mTvFast.getVisibility() != View.VISIBLE) {
            mTvFast.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置视频文件名
     *
     * @param name
     */
    public void setVideoName(String name) {
        videoname = name;
        if (mVideoTitle != null) {
            mVideoTitle.setText(name);
        }
    }

    /**
     * 隐藏或显示
     */
    public void refReshView() {
        if (isShowing()) {
            hide();
        } else {
            show();
        }
    }

    /**
     * 播放/暂停
     */
    private void playOrPause() {
        if (mVideoView != null)
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            } else {
                mVideoView.start();
            }
    }

}
