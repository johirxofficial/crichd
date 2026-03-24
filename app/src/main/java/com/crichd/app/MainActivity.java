package com.crichd.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private final String M3U_URL = "https://iptv-scraper-zilla.pages.dev/CricHD.m3u";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        loadChannels();
    }

    private void loadChannels() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Channel> channels = M3UParser.parse(M3U_URL);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (channels != null) {
                    recyclerView.setAdapter(new ChannelAdapter(channels, channel -> {
                        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                        intent.putExtra("url", channel.streamUrl);
                        intent.putExtra("name", channel.name);
                        intent.putExtra("ref", channel.referrer);
                        intent.putExtra("origin", channel.origin);
                        startActivity(intent);
                    }));
                }
            });
        });
    }
}
