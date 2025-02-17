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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.zip.CRC32;

public class MainActivity extends Activity {
    protected final String TAG = "ANYPOINT_SDK_SAMPLE";

    private ChannelPlayer channelPlayer;
    protected AnypointAdsManager adsManager;
    protected AnypointAdView anypointAdView;
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

        anypointAdView = findViewById(R.id.linearTvAdView);

        AnypointAdsManager.AdsManagerListener adsManagerListener = new AdsManagerListenerImpl(channelPlayer);
        adsManager = anypointAdView.getAnypointAdsManager();
        adsManager.addListener(adsManagerListener);

        channelPlayer.playChannel();

        scte35Decoder = anypointAdView.useScte35Decoder();
        AnypointSdk.createTvEventPublisher().publish(TvEvent.CHANNEL_CHANGE, 1);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "AnypointAD keyCode: " + keyCode);

        try {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    // ex) send CHANNEL_CHANGE event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.CHANNEL_CHANGE, 1, "set multicast url");
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    // ex) send CHANNEL_CHANGE event
                    AnypointSdk.createTvEventPublisher().publish(TvEvent.CHANNEL_CHANGE, 0);
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
                    long now = System.currentTimeMillis();
                    scte35Decoder.decode(makeScte35(now + 9000, 30000), now);
                    break;
                default:
                    Log.d(TAG, "not support key code");
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to process keycode\n", e);
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setByte(BitSet bitSet, int start, byte value) {
        for (int i = 0; i < 8; i++) {
            bitSet.set(start + i, (value & (1 << i)) != 0);
        }
    }

    private void setShort(BitSet bitSet, int start, short value) {
        for (int i = 0; i < 16; i++) {
            bitSet.set(start + i, (value & (1 << i)) != 0);
        }
    }


    private byte[] makeScte35(long adBreakTimeInMs, long durationInMs) {
        BitSetWriter writer = new BitSetWriter();

        writer.write(/*tableId*/ (byte) 0xFC);
        BitSet sectionHeader = new BitSet(16);
        sectionHeader.set(/*section_syntax_indicator*/ 0, false);
        sectionHeader.set(/*private_indicator*/ 1, false);
        setByte(sectionHeader, /*section_length*/ 8, (byte) 0x25);
        writer.write(sectionHeader, 16);

        writer.write(/*protocolVersion*/ (byte) 0x00);
        BitSet encryptedInfo = new BitSet(8);
        encryptedInfo.set(/*encrypted_packet*/ 0, false);
        writer.write(encryptedInfo, 8);

        writer.write(/*ptsAdjustment*/ new byte[]{0x00, 0x00, 0x00, 0x00});
        writer.write(/*cwIndex*/ (byte) 0x00);
        writer.write(/*tier*/new BitSet(12), 12);
        BitSet spliceCommandLength = new BitSet(12);
        setShort(spliceCommandLength, /*splice_command_length*/ 4, (short) 0x14);
        writer.write(spliceCommandLength, 12);

        writer.write(/*spliceCommandType*/ (byte) 0x05);

        // SpliceInsert command
        writer.write(/*spliceEventId*/ new byte[]{0x00, 0x00, 0x00, 0x00});
        writer.write(/*spliceEventCancelIndicator*/ new byte[]{0x00, 0x00});
        BitSet spliceInsertFlags = new BitSet(8);
        spliceInsertFlags.set(0, true); // out_of_network_indicator
        spliceInsertFlags.set(1, true); // program_splice_flag
        spliceInsertFlags.set(2, true); // duration_flag
        spliceInsertFlags.set(3, false); // splice_immediate_flag
        spliceInsertFlags.set(4, false); // event_id_compliance_flag
        writer.write(spliceInsertFlags, 8);

        // SpliceTime struct
        BitSet spliceTimeFlags = new BitSet(8);
        spliceTimeFlags.set(0, true); // time_specified_flag
        writer.write(spliceTimeFlags, 8);
        writer.write(/*ptsTime*/ adBreakTimeInMs * 90);

        // BreakDuration struct
        BitSet breakDurationFlags = new BitSet(8);
        breakDurationFlags.set(0, true); // auto_return
        writer.write(breakDurationFlags, 8);
        writer.write(/*breakDuration*/ durationInMs);

        writer.write(/*uniqueProgramId*/ new byte[]{0x00, 0x00});
        writer.write(/*availNum*/ (byte) 0x00);
        writer.write(/*availsExpected*/ (byte) 0x00);

        writer.write(/*descriptorLoopLength*/ new byte[]{0x00, 0x00});

        byte[] rawBytes = writer.getBytes();
        CRC32 crc32 = new CRC32();
        crc32.update(rawBytes);
        writer.write(crc32.getValue());

        return writer.getBytes();
    }
}

class BitSetWriter {
    private final List<Byte> byteList = new ArrayList<>();
    private int currentByte = 0;
    private int bitCount = 0;

    public void write(BitSet bits, int length) {
        for (int i = 0; i < length; i++) {
            writeBit(bits.get(i) ? 1 : 0);
        }
    }

    public void write(byte b) {
        if (bitCount == 0) {
            byteList.add(b);
        } else {
            for (int i = 7; i >= 0; i--) {
                writeBit((b >> i) & 1);
            }
        }
    }

    public void write(byte[] bytes) {
        for (byte b : bytes) {
            write(b);
        }
    }

    public void write(long l) {
        for (int i = 0; i < 8; i++) {
            write((byte) (l >> (8 * i)));
        }
    }

    private void writeBit(int bit) {
        currentByte = (currentByte << 1) | bit;
        bitCount++;
        if (bitCount == 8) {
            flushByte();
        }
    }

    private void flushByte() {
        byteList.add((byte) currentByte);
        currentByte = 0;
        bitCount = 0;
    }

    public byte[] getBytes() {
        if (bitCount > 0) {
            currentByte <<= (8 - bitCount); // Padding remaining bits with 0s
            flushByte();
        }
        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            result[i] = byteList.get(i);
        }
        return result;
    }
}