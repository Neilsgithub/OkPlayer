package org.succlz123.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;

import org.succlz123.okplayer.OkPlayer;
import org.succlz123.okplayer.listener.CaptionListener;
import org.succlz123.okplayer.listener.OkPlayerListener;
import org.succlz123.okplayer.utils.EventLogger;
import org.succlz123.okplayer.utils.OkPlayerUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        OkPlayerListener,
        CaptionListener,
        SurfaceHolder.Callback,
        AudioCapabilitiesReceiver.Listener {

    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private View root;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

    private long playerPosition;
    private boolean playerNeedsPrepare;
    private boolean enableBackgroundAudio;

    private Surface surface;
    private OkPlayer player;
    private MediaController mediaController;
    private EventLogger eventLogger;
    private Uri uri;
    private int contentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //http://stackoverflow.com/questions/16939814/android-util-androidruntimeexception-requestfeature-must-be-called-before-add
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        root = findViewById(R.id.root);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surface = surfaceView.getHolder().getSurface();
        surfaceView.getHolder().addCallback(this);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(root);

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
        audioCapabilitiesReceiver.register();
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        playerPosition = 0;
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
//        uri = intent.getData();
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        uri = Uri.parse(storagePath + File.separator + "0.mp4");
        contentType = intent.getIntExtra(OkPlayerUtils.CONTENT_TYPE,
                OkPlayerUtils.inferContentType(uri, intent.getStringExtra(OkPlayerUtils.TYPE)));

        if (player == null) {
            preparePlayer(true);
        } else {
//            player.setBackgrounded(false);
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (false) {
            releasePlayer();
        } else {
            //只暂停播放器视频
//            player.setBackgrounded(true);
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioCapabilitiesReceiver.unregister();
        releasePlayer();
    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new OkPlayer(OkPlayerUtils.getRendererBuilder(this, uri, contentType));
            player.addListener(this);
            player.setCaptionListener(this);
            player.setId3MetadataListener(null);
            player.seekTo(playerPosition);

            playerNeedsPrepare = true;

            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);

            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
    }

    /**
     * {@link OkPlayerListener}
     */
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            String string = Util.SDK_INT < 18 ? "Protected content not supported on API levels below 18"
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? "This device does not support the required DRM scheme" : "An unknown DRM error occurred";
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
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
     * {@link com.google.android.exoplayer.audio.AudioCapabilitiesReceiver.Listener}
     */
    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        player.setBackgrounded(backgrounded);
    }
}
