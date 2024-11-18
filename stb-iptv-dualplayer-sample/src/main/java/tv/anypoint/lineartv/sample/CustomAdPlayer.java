package tv.anypoint.lineartv.sample;

import android.content.Context;
import android.util.Log;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
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
    private long adTotalDuration = 0;

    public CustomAdPlayer(Context context, PlayerView playerView) {
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        dataSourceFactory =
                new DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(context, "SampleChannelPlayer")
                );

        player = new SimpleExoPlayer.Builder(context, renderersFactory)
                .build();

        player.addListener(this);

        playerView.setUseController(false);
        playerView.setUseArtwork(false);
        playerView.setPlayer(player);
        playerView.setKeepContentOnPlayerReset(true);
    }

    @Override
    public void play() {
        Log.i(TVApplication.TAG, "custom player play...");

        player.stop();

        player.setPlayWhenReady(true);
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
            Log.d("TEST", "play url: " + mediaUrls.get(0));
            videoAdPlayerCallback.onPlay(mediaUrls.get(0));
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

        for (AnypointAdPlayer.AnypointAdPlayerCallback videoAdPlayerCallback : videoAdPlayerCallbacks) {
            Log.d("TEST", "loaded: " + mediaUrls.get(0));
            videoAdPlayerCallback.onLoaded(mediaUrls.get(0));
        }
    }

    @Override
    public void append(PlaySet playSet) {
        mediaUrls.addAll(playSet.toMediaUrls());
        adTotalDuration += playSet.getDuration();

        playSets.add(playSet);
    }

    @Override
    public String currentMediaUrl() {
        return mediaUrls.get(getCurrentMediaUnitIndex());
    }

    @Override
    public void pause() {
        Log.i(TVApplication.TAG, "pause...");
        player.pause();

        for (AnypointAdPlayer.AnypointAdPlayerCallback videoAdPlayerCallback : videoAdPlayerCallbacks) {
            videoAdPlayerCallback.onPause(currentMediaUrl());
        }
    }

    @Override
    public void release() {
        Log.i(TVApplication.TAG, "release...");
        player.release();
    }

    @Override
    public void resume() {
        Log.i(TVApplication.TAG, "resume...");

        player.play();
        for (AnypointAdPlayer.AnypointAdPlayerCallback videoAdPlayerCallback : videoAdPlayerCallbacks) {
            videoAdPlayerCallback.onResume(currentMediaUrl());
        }
    }

    @Override
    public void stop() {
        player.stop();
        for (AnypointAdPlayer.AnypointAdPlayerCallback videoAdPlayerCallback : videoAdPlayerCallbacks) {
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
        player.isPlaying();
        return true;
    }

    @Override
    public int getCurrentMediaUnitIndex() {
        return player.getCurrentMediaItemIndex();
    }

    @Override
    public boolean skipAd() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem();
        }
        return true;
    }
}


