package org.succlz123.okplayer.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.text.Cue;

import org.succlz123.okplayer.OkPlayer;
import org.succlz123.okplayer.R;
import org.succlz123.okplayer.listener.CaptionListener;
import org.succlz123.okplayer.listener.OkPlayerListener;
import org.succlz123.okplayer.utils.OkPlayerUtils;

import java.util.List;

/**
 * Created by succlz123 on 15/12/1.
 */
public class OkVideoView extends FrameLayout implements
        OkPlayerListener,
        CaptionListener,
        SurfaceHolder.Callback,
        AudioCapabilitiesReceiver.Listener {

    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

    private MediaController mediaController;
    private OkPlayer okPlayer;

    private Uri uri;
    private long playerPosition;

    public OkVideoView(Context context) {
        super(context);
        setup(context, null);
    }

    public OkVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);

    }

    public OkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OkVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    private void setup(Context context, AttributeSet attrs) {
        if (context == null) {
            return;
        }
        initView(context);
        readAttributes(context, attrs);
    }

    /**
     * 初始化view
     */
    private void initView(Context context) {
        View.inflate(context, R.layout.ok_video_view, this);

        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);

        if (surfaceView != null) {
            initExoPlayer();
        }
    }

    /**
     * 初始化播放器
     */
    private void initExoPlayer() {
        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getContext().getApplicationContext(), this);
        audioCapabilitiesReceiver.register();
        okPlayer = new OkPlayer(null);

        okPlayer.addListener(this);
        okPlayer.setId3MetadataListener(null);

        okPlayer.setSurface(surfaceView.getHolder().getSurface());
        surfaceView.getHolder().addCallback(this);
    }

    /**
     * 读取自定义配置
     */
    private void readAttributes(Context context, AttributeSet attrs) {
        if (attrs == null || isInEditMode()) {
            return;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OkExoPlayerVideoView);
        if (typedArray == null) {
            return;
        }

        boolean enableDefaultControls = typedArray.getBoolean(R.styleable.OkExoPlayerVideoView_ControlsEnabled, false);
        setDefaultControlsEnabled(enableDefaultControls);

        typedArray.recycle();
    }

    public void setDefaultControlsEnabled(boolean enabled) {
        if (enabled) {
            mediaController = new MediaController(getContext());
            mediaController.setAnchorView(videoFrame);
            mediaController.setMediaPlayer(okPlayer.getPlayerControl());
            mediaController.setEnabled(true);
        }

        CustomTouchListener listener = new CustomTouchListener(getContext());
        setOnTouchListener(enabled ? listener : null);
    }

    public Uri getVideoUri() {
        return uri;
    }

    /**
     * 设置视频uri
     */
    public void setVideoUri(Uri videoUri) {
        uri = videoUri;

        if (uri == null) {
            return;
        }

        if (okPlayer == null) {
            initExoPlayer();
        }

        okPlayer.replaceRenderBuilder(OkPlayerUtils.getRendererBuilder(getContext(), uri, OkPlayerUtils.TYPE_OTHER));
        okPlayer.prepare();
        okPlayer.pushSurface(true);
        okPlayer.seekTo(0);
        okPlayer.setPlayWhenReady(true);
    }

    public void onNewIntent() {
        release();
    }

    public void onResume(Uri uri) {
        if (okPlayer == null) {
            setVideoUri(uri);
        } else {
            okPlayer.setPlayWhenReady(true);
        }
    }

    public void onPause() {
        pause();
    }

    public void onDestroy() {
        release();
    }

    /**
     * 暂停
     */
    public void pause() {
        if (okPlayer != null) {
            okPlayer.setPlayWhenReady(false);
        }
    }

    /**
     * 释放
     */
    public void release() {
        if (okPlayer != null) {
            okPlayer.release();
            okPlayer = null;
        }
        playerPosition = 0;

        if (audioCapabilitiesReceiver != null) {
            audioCapabilitiesReceiver.unregister();
            audioCapabilitiesReceiver = null;
        }
    }

    public void addListener(OkPlayerListener listener) {
        if (okPlayer == null) {
            return;
        }
        okPlayer.addListener(listener);
    }

    public int getPlaybackState() {
        if (okPlayer == null) {
            return 0;
        }
        return okPlayer.getPlaybackState();
    }

    public boolean getPlayWhenReady() {
        if (okPlayer == null) {
            return false;
        }
        return okPlayer.getPlayWhenReady();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        if (okPlayer == null) {
            return;
        }
        okPlayer.setPlayWhenReady(playWhenReady);
    }

    public void togglePlayback() {
        if (okPlayer == null) {
            return;
        }
        if (okPlayer.getPlaybackState() == OkPlayer.STATE_READY) {
            boolean playWhenReady = okPlayer.getPlayWhenReady();
            if (playWhenReady) {
                okPlayer.setPlayWhenReady(false);
            } else {
                okPlayer.setPlayWhenReady(true);
            }
        }
    }

    public long getDuration() {
        if (okPlayer == null) {
            return 0;
        }
        return okPlayer.getDuration();
    }

    public long getCurrentPosition() {
        if (okPlayer == null) {
            return 0;
        }
        return okPlayer.getCurrentPosition();
    }

    public void seekTo(long positionMs) {
        if (okPlayer == null) {
            return;
        }
        okPlayer.seekTo(positionMs);
    }

    public long getBufferedPosition() {
        if (okPlayer == null) {
            return 0;
        }
        return okPlayer.getBufferedPosition();
    }

    public int getBufferedPercentage() {
        if (okPlayer == null) {
            return 0;
        }
        return okPlayer.getBufferedPercentage();
    }

    /**
     * {@link OkPlayerListener}
     */
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//        //视频比例改变时,同时改变videoFrame的高宽比例
//        videoFrame.setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    /**
     * {@link SurfaceHolder.Callback}
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (okPlayer != null) {
            okPlayer.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (okPlayer != null) {
            okPlayer.blockingClearSurface();
        }
    }

    /**
     * {@link CaptionListener}
     */
    @Override
    public void onCues(List<Cue> cues) {

    }

    /**
     * {@link AudioCapabilitiesReceiver.Listener}
     */
    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (okPlayer == null) {
            return;
        }
        boolean backgrounded = okPlayer.getBackgrounded();
        boolean playWhenReady = okPlayer.getPlayWhenReady();
//        releasePlayer();
//        preparePlayer(playWhenReady);
        okPlayer.setBackgrounded(backgrounded);
    }

    /**
     * 手势监听
     */
    public class CustomTouchListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener {
        private GestureDetector gestureDetector;

        public CustomTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mediaController == null) {
                return false;
            }

            if (mediaController.isShowing()) {
                mediaController.hide();
            } else {
                mediaController.show(5000);
            }
            return true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }
}
