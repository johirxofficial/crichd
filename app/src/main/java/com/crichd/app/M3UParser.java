package com.crichd.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class M3UParser {
    public static List<Channel> parse(String m3uUrl) {
        List<Channel> channels = new ArrayList<>();
        try {
            URL url = new URL(m3uUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            String line;
            Channel currentChannel = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("#EXTINF")) {
                    currentChannel = new Channel();
                    // Extract Logo
                    if (line.contains("tvg-logo=\"")) {
                        int start = line.indexOf("tvg-logo=\"") + 10;
                        int end = line.indexOf("\"", start);
                        if(end > start) currentChannel.logoUrl = line.substring(start, end);
                    }
                    // Extract Name
                    int commaIndex = line.lastIndexOf(",");
                    if (commaIndex != -1) {
                        currentChannel.name = line.substring(commaIndex + 1).trim();
                    }
                } else if (line.startsWith("#EXTVLCOPT:http-referrer=")) {
                    if (currentChannel != null) {
                        currentChannel.referrer = line.replace("#EXTVLCOPT:http-referrer=", "").trim();
                    }
                } else if (line.startsWith("#EXTVLCOPT:http-origin=")) {
                    if (currentChannel != null) {
                        currentChannel.origin = line.replace("#EXTVLCOPT:http-origin=", "").trim();
                    }
                } else if (line.startsWith("http")) {
                    if (currentChannel != null) {
                        currentChannel.streamUrl = line;
                        channels.add(currentChannel);
                        currentChannel = null; // Reset for next
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channels;
    }
}
