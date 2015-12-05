package org.succlz123.okplayer.listener;

import android.media.MediaCodec;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;

import java.io.IOException;

/**
 * 内部错误的监听
 * <p>
 * 内部错误对用户是不可见的,所以提供这个监听
 * 错误仅供参考,但是请注意内部错误有可能是致命的
 * <p>
 * 如果播放器未能恢复内部错误,就会调用{@link ExoPlayer.Listener#onPlayerError(ExoPlaybackException)}
 * <p>
 * Created by succlz123 on 15/11/29.
 */
public interface InternalErrorListener {

    void onRendererInitializationError(Exception e);

    void onAudioTrackInitializationError(AudioTrack.InitializationException e);

    void onAudioTrackWriteError(AudioTrack.WriteException e);

    void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e);

    void onCryptoError(MediaCodec.CryptoException e);

    void onLoadError(int sourceId, IOException e);

    void onDrmSessionManagerError(Exception e);
}
