package org.succlz123.okplayer.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;

import org.succlz123.okplayer.OkPlayer;
import org.succlz123.okplayer.R;
import org.succlz123.okplayer.listener.CaptionListener;
import org.succlz123.okplayer.listener.OkPlayerListener;
import org.succlz123.okplayer.utils.OkPlayerUtils;

import java.util.List;

/**
 * Created by succlz123 on 15/12/1.
 */
public class OkVideoView extends RelativeLayout implements
        OkPlayerListener,
        CaptionListener,
        SurfaceHolder.Callback,
        AudioCapabilitiesReceiver.Listener {
    private LinearLayout titleBar;
    private ImageView backImageView;
    private ImageView hdImageView;
    private ImageView danmukuImageView;
    private ImageView previewImageView;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private OkPlayer player;
    private AspectRatioFrameLayout videoFrame;
    private CustomSurfaceView surfaceView;
    private RelativeLayout root;
    private MediaController mediaController;
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
        pause();
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
        View.inflate(context, R.layout.ok_exo_video_view, this);
//        addView(View.inflate(getContext(), R.layout.ok_exo_video_view, null));
        previewImageView = (ImageView) findViewById(R.id.preview_image);

        titleBar = (LinearLayout) findViewById(R.id.video_title_bar);
        backImageView = (ImageView) findViewById(R.id.video_back);
        hdImageView = (ImageView) findViewById(R.id.video_hd);
        danmukuImageView = (ImageView) findViewById(R.id.video_danmaku);

//        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        surfaceView = (CustomSurfaceView) findViewById(R.id.surface_view);
        root = (RelativeLayout) findViewById(R.id.root);

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
        player = new OkPlayer(null);

//        listenerMux = new EMListenerMux(new MuxNotifier());
        player.addListener(this);
        player.setId3MetadataListener(null);

        player.setSurface(surfaceView.getHolder().getSurface());
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
        setControlsEnabled(enableDefaultControls);

        typedArray.recycle();
    }

    public void setControlsEnabled(boolean enabled) {
        if (enabled) {
            mediaController = new MediaController(getContext());
            mediaController.setAnchorView(root);
            mediaController.setMediaPlayer(player.getPlayerControl());
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
    public void setVideoUri(Uri videoUri, int contentType) {
        uri = videoUri;

        if (uri == null) {
            return;
        }

        if (player == null) {
            initExoPlayer();
        }

        player.replaceRenderBuilder(OkPlayerUtils.getRendererBuilder(getContext(), uri, OkPlayerUtils.TYPE_OTHER));
        player.prepare();
        player.pushSurface(true);
        player.setPlayWhenReady(true);

//        if (!useExo) {
//            videoView.setVideoURI(uri);
//        } else {
//            if (uri == null) {
//                player.replaceRenderBuilder(null);
//            } else {
//                emExoPlayer.replaceRenderBuilder(getRendererBuilder(VideoType.get(uri), uri, defaultMediaType));
//                listenerMux.setNotifiedCompleted(false);
//            }
//
//            //Makes sure the listeners get the onPrepared callback
//            listenerMux.setNotifiedPrepared(false);
//            emExoPlayer.seekTo(0);
//        }
    }

    public void onNewIntent() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
//            eventLogger.endSession();
//            eventLogger = null;
        }
        playerPosition = 0;
    }

    public void onResume() {
//        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        uri = Uri.parse(storagePath + File.separator + "0.mp4");
        if (player == null) {
            initExoPlayer();
//            setVideoUri(uri,OkPlayerUtils.TYPE_OTHER);
        } else {
//            player.setBackgrounded(false);
            player.setPlayWhenReady(true);
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
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    /**
     * 释放
     */
    public void release() {
        if (player != null) {
            player.release();
        }

        if (audioCapabilitiesReceiver != null) {
            audioCapabilitiesReceiver.unregister();
            audioCapabilitiesReceiver = null;
        }
    }

    /**
     * 设置loading界面
     */
    public void setPreviewImage(Drawable drawable) {
        if (previewImageView != null) {
            previewImageView.setImageDrawable(drawable);
        }
    }

    public void setPreviewImage(int resourceId) {
        if (previewImageView != null) {
            previewImageView.setImageResource(resourceId);
        }
    }

    public void setPreviewImage(Bitmap bitmap) {
        if (previewImageView != null) {
            previewImageView.setImageBitmap(bitmap);
        }
    }

    public void setPreviewImage(Uri uri) {
        if (previewImageView != null) {
            previewImageView.setImageURI(uri);
        }
    }

    /**
     * {@link OkPlayerListener}
     */
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
//            showControls();
        }
//        playerStateTextView.setText(text);
//        updateButtonVisibilities();
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            String string = Util.SDK_INT < 18 ? "Protected content not supported on API levels below 18"
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? "This device does not support the required DRM scheme" : "An unknown DRM error occurred";
//            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        }
//        playerNeedsPrepare = true;
//        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//        shutterView.setVisibility(View.GONE);
        //视频比例改变时,同时改变videoFrame的高宽比例
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    /**
     * {@link SurfaceHolder.Callback}
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
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
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
//        releasePlayer();
//        preparePlayer(playWhenReady);
        player.setBackgrounded(backgrounded);
    }

    /**
     * Monitors the view click events to show the default controls if they are enabled.
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
            if (mediaController != null && titleBar != null) {
                toggleShowController();
            }
//            if (bus != null) {
//                bus.post(new EMVideoViewClickedEvent());
//            }
            return true;
        }
    }


    private void toggleShowController() {
        if (mediaController.isShowing()) {
            titleBar.setVisibility(GONE);
            mediaController.hide();
//            mHandler.removeMessages(1);
        } else {
            mediaController.show(5000);
            titleBar.setVisibility(VISIBLE);

            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    titleBar.setVisibility(VISIBLE);
                }
            },5000);
//            Message message = mHandler.obtainMessage(1);

//            mHandler.sendMessageDelayed(message, 5000);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case 1:
                    mediaController.hide();
                    titleBar.setVisibility(GONE);
                    break;
                default:
                    break;
            }
        }
    };


}
