package tv.anypoint.lineartv.sample;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class ChannelPlayer implements Player.Listener {
    private SimpleExoPlayer player;
    private DefaultDataSourceFactory dataSourceFactory;

    public ChannelPlayer(Context context, SimpleExoPlayer channelPlayer) {
        dataSourceFactory =
                new DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(context, "ChannelPlayer")
                );

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);

        player = channelPlayer;
    }

    public void playChannel() {
        player.stop();

        player.setPlayWhenReady(true);
        player.setVolume(1);

        // play LinearTV
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(Uri.parse("https://your-stream")));
        player.setMediaSource(mediaSource);
        player.prepare();
    }

    public void setVolume(float volume) {
        player.setVolume(volume);
    }

    public void stopChannel() {
        player.stop();
    }
}
