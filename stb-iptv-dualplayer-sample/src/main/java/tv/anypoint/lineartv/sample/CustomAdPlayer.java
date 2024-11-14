package tv.anypoint.lineartv.sample;

import android.util.Log;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import tv.anypoint.api.ads.AnypointAdPlayer;
import tv.anypoint.sdk.comm.PlaySet;

import java.util.ArrayList;
import java.util.List;

class CustomAdPlayer implements AnypointAdPlayer, Player.Listener {
    private List<AnypointAdPlayerCallback> videoAdPlayerCallbacks = new ArrayList<>();

    private SimpleExoPlayer player;
    private DefaultDataSourceFactory dataSourceFactory;

    private List<PlaySet> playSets = new ArrayList<>();
    private List<String> mediaUrls = new ArrayList<>();
    private int currentMediaUrlIndex = 0;
    private long adTotalDuration = 0;

    public CustomAdPlayer() {

    }

    @Override
    public void play() {
        Log.i(TVApplication.TAG, "custom player play...");

        player.stop();

        ConcatenatingMediaSource mediaSource = new ConcatenatingMediaSource();

        for (String adUrl : mediaUrls) {
            mediaSource.addMediaSource(
                    new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(adUrl))
            );
        }

        // startDelay...

        player.setMediaSource(mediaSource);
        player.prepare();

        for (AnypointAdPlayerCallback videoAdPlayerCallback : videoAdPlayerCallbacks) {
            Log.d("TEST", "play url: " + mediaUrls.get(player.getCurrentPeriodIndex()));
            videoAdPlayerCallback.onPlay(mediaUrls.get(player.getCurrentPeriodIndex()));
        }
    }

    @Override
    public void addCallback(AnypointAdPlayerCallback videoAdPlayerCallback) {
        if (!videoAdPlayerCallbacks.contains(videoAdPlayerCallback)) {
            videoAdPlayerCallbacks.add(videoAdPlayerCallback);
        }
    }

    @Override
    public void removeCallback(AnypointAdPlayerCallback videoAdPlayerCallback) {
        videoAdPlayerCallbacks.remove(videoAdPlayerCallback);
    }


    @Override
    public AdProgress getProgress() {
        Log.i(TVApplication.TAG, "custom player getProgress...");

        long playTime = player.getContentPosition();
        if (player.getCurrentPeriodIndex() > 0) {
            for (int i = 0; i < player.getCurrentPeriodIndex(); i++) {
                playTime += player.getCurrentTimeline().getPeriod(i, new Timeline.Period()).getDurationMs();
            }
        }
        return new AdProgress(playTime, adTotalDuration, player.getCurrentTimeline().getPeriod(player.getCurrentPeriodIndex(), new Timeline.Period()).getDurationMs());
    }

    @Override
    public float getVolume() {
        return player.getVolume();
    }

    @Override
    public void load(PlaySet playSet) {
        Log.i(TVApplication.TAG, "custom player load...");

        playSets.clear();
        mediaUrls.clear();

        playSets.add(playSet);
        mediaUrls = playSet.toMediaUrls();
        adTotalDuration = playSet.getDuration();

        // TODO: load ad's playSet
        // player.setMediaSource(...);
        player.prepare();
    }

    @Override
    public void append(PlaySet playSet) {
        // TODO: implementation

        mediaUrls.addAll(playSet.toMediaUrls());
        adTotalDuration += playSet.getDuration();

        int lastIndex = playSets.size() - 1;
        playSets.add(playSet);
    }

    @Override
    public String currentMediaUrl() {
        return mediaUrls.get(currentMediaUrlIndex);
    }

    @Override
    public void pause() {
        Log.i(TVApplication.TAG, "pause...");
    }

    @Override
    public void release() {
        Log.i(TVApplication.TAG, "release...");

        //stopChannel();
    }

    @Override
    public void resume() {
    }

    @Override
    public void stop() {
        for (AnypointAdPlayerCallback videoAdPlayerCallback : videoAdPlayerCallbacks) {
            videoAdPlayerCallback.onStopped();
        }
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            stop();
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        for (AnypointAdPlayerCallback videoAdPlayerCallback : videoAdPlayerCallbacks) {
            videoAdPlayerCallback.onError(mediaUrls.get(player.getCurrentPeriodIndex()), error);
        }
    }

    @Override
    public boolean isAdPlaying() {
        // TODO: implementation
        return true;
    }

    @Override
    public int getCurrentMediaUnitIndex() {
        return player.getCurrentPeriodIndex();
    }

    @Override
    public boolean skipAd() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem();
        }
        return true;
    }
}


