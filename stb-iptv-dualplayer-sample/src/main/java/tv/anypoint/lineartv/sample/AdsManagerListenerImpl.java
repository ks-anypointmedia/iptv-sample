package tv.anypoint.lineartv.sample;

import tv.anypoint.api.ads.AnypointAdsManager;

class AdsManagerListenerImpl implements AnypointAdsManager.AdsManagerListener {
    private final ChannelPlayer channelPlayer;
    public AdsManagerListenerImpl(ChannelPlayer channelPlayer) {
        this.channelPlayer = channelPlayer;
    }

    @Override
    public boolean onPrepare(int i, boolean b) {
        return false;
    }

    public void onPlay(boolean retainChannelStream) {
        channelPlayer.setVolume(0);
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
    }

    @Override
    public void onError(Throwable throwable, boolean b) {

    }
}