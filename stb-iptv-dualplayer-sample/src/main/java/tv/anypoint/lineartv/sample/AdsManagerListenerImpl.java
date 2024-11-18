package tv.anypoint.lineartv.sample;

import android.view.View;
import com.google.android.exoplayer2.ui.PlayerView;
import tv.anypoint.api.ads.AnypointAdsManager;

class AdsManagerListenerImpl implements AnypointAdsManager.AdsManagerListener {
    private final ChannelPlayer channelPlayer;
    private final PlayerView adPlayerView;
    public AdsManagerListenerImpl(ChannelPlayer channelPlayer, PlayerView adPlayerView) {
        this.channelPlayer = channelPlayer;
        this.adPlayerView = adPlayerView;
    }

    @Override
    public boolean onPrepare(int i, boolean b) {
        return false;
    }

    public void onPlay(boolean retainChannelStream) {
        channelPlayer.setVolume(0);
        adPlayerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void prepareStop(boolean b) {

    }

    public void onStopped(boolean retainChannelStream) {
        channelPlayer.setVolume(1);
        adPlayerView.setVisibility(View.GONE);
    }

    @Override
    public void onError(Throwable throwable, boolean b) {

    }
}