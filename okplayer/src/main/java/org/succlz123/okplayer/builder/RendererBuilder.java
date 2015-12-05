package org.succlz123.okplayer.builder;

import org.succlz123.okplayer.OkPlayer;

/**
 * Builds renderers for the player.
 * <p/>
 * Created by succlz123 on 15/12/1.
 */
public interface RendererBuilder {

    void buildRenderers(OkPlayer player);

    void cancel();
}