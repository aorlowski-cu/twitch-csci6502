package com.cu.aorlowski;

import com.google.common.collect.ImmutableList;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;

public class Main {

    private static final TwitchClient twitchClient = new TwitchClient();
    private static final GcpClient gcpClient = new GcpClient();

    public static void main(String[] args) throws InterruptedException, ParseException, IOException, IllegalAccessException {
        //ImmutableList<TwitchStreamSnapshot> streams = twitchClient.getStreams();
        //gcpClient.uploadStreams(streams);

        ArrayList<String> streamerList = gcpClient.getDistinctStreamers();
        ImmutableList<Streamer> streamerData = twitchClient.getSteamerInfos(ImmutableList.copyOf(streamerList));
        for(var streamer: streamerData){
            streamer.followerCount = twitchClient.getFollowerCount(streamer.userId);
        }
        gcpClient.uploadStreamers(streamerData);
    }
}
