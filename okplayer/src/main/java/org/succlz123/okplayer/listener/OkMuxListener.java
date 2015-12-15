package org.succlz123.okplayer.listener;

import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;

import java.util.List;

/**
 * Created by succlz123 on 15/12/6.
 */
public class OkMuxListener implements
        OkPlayerListener,
        CaptionListener,
        DefaultBandwidthMeter.EventListener,
        AudioCapabilitiesReceiver.Listener {

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {

    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {

    }
}
