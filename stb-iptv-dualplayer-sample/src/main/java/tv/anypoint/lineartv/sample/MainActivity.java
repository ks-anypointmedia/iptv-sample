package tv.anypoint.lineartv.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import tv.anypoint.api.AnypointSdk;
import tv.anypoint.api.ads.AnypointAdRequest;
import tv.anypoint.api.ads.AnypointAdView;
import tv.anypoint.api.ads.AnypointAdsManager;
import tv.anypoint.api.scte35.Scte35Decoder;
import tv.anypoint.sdk.comm.TvEvent;

public class MainActivity extends Activity {
    protected final String TAG = "ANYPOINT_SDK_SAMPLE";

    private ChannelPlayer channelPlayer;
    protected AnypointAdsManager adsManager;
    protected Scte35Decoder scte35Decoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        PlayerView channelView = findViewById(R.id.channelPlayer);
        SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        channelView.setPlayer(simpleExoPlayer);

        channelPlayer = new ChannelPlayer(this, simpleExoPlayer);

        AnypointAdView adView = findViewById(R.id.linearTvAdView);

        // Register your custom player to the AnypointAdView.
        PlayerView adPlayerView = findViewById(R.id.adPlayer);
        CustomAdPlayer customAdPlayer = new CustomAdPlayer(this, adPlayerView);
        adView.setAdPlayer(customAdPlayer);

        adsManager = adView.getAnypointAdsManager();
        AnypointAdsManager.AdsManagerListener adsManagerListener = new AdsManagerListenerImpl(channelPlayer, adPlayerView);

        adsManager.addListener(adsManagerListener);

        channelPlayer.playChannel();

        findViewById(R.id.requestAd).setOnClickListener(v -> adsManager.request(new AnypointAdRequest(0, 45000, 30000, 1, null, "1")));

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "AnypointAD keyCode: " + keyCode);

        try {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    // ex) send CHANNEL_CHANGE event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.CHANNEL_CHANGE, 46, "set multicast url");
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    // ex) send CHANNEL_CHANGE event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.CHANNEL_CHANGE, 45);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    // ex) send APP_START event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.APP_START, "set package name");
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    // ex) send VOD_START event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.VOD_START, "set title");
                    break;
                case KeyEvent.KEYCODE_0:
                    // ex) send SLEEP_MODE_START event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.SLEEP_MODE_START, "set title");
                    break;
                case KeyEvent.KEYCODE_1:
                    // ex) send SLEEP_MODE_START event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.MISC, "set title");
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    // ex) ad request
                    adsManager.request(new AnypointAdRequest(0, 45000, 30000, 1, null, "1"));
                    break;
                default:
                    Log.d(TAG, "not support key code");
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to process keycode\n", e);
        }
        return super.onKeyUp(keyCode, event);
    }
}