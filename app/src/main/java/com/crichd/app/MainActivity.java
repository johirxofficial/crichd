package com.crichd.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private RecyclerView recyclerView;
    private final String M3U_URL = "https://iptv-scraper-zilla.pages.dev/CricHD.m3u";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.player_view);
        recyclerView = findViewById(R.id.recycler_view);
        
        // 4 Columns Grid Layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        loadChannels();
    }

    private void loadChannels() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<Channel> channels = M3UParser.parse(M3U_URL);
            handler.post(() -> {
                if (channels != null && !channels.isEmpty()) {
                    ChannelAdapter adapter = new ChannelAdapter(channels, this::playChannel);
                    recyclerView.setAdapter(adapter);
                    // Auto play first channel
                    playChannel(channels.get(0));
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load channels", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void playChannel(Channel channel) {
        if (player != null) {
            player.release();
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Inject Referrer and Origin headers dynamically based on M3U data
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true);
        
        Map<String, String> headers = new HashMap<>();
        if (channel.referrer != null) headers.put("Referer", channel.referrer);
        if (channel.origin != null) headers.put("Origin", channel.origin);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        
        dataSourceFactory.setDefaultRequestProperties(headers);

        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(channel.streamUrl));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
