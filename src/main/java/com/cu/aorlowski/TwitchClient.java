package com.cu.aorlowski;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TwitchClient {
    private static final String TOKEN_URI = "https://id.twitch.tv/oauth2/token";
    private static final String CLIENT_ID = "9ss76dt92bgblscgnjo3vqv6vx8maa";
    private static final String SECRET = "lfq27aqoparqskv3t2un3si4mdv7cv";
    private static final String TWITCH_URL = "https://api.twitch.tv/helix/";
    private static String accessToken = "";


    public void refreshAccessToken() throws IOException, InterruptedException, ParseException {
        //https://www.javatpoint.com/java-string-format
        String uriString = String.format("%s?client_id=%s&client_secret=%s&grant_type=client_credentials",
                TOKEN_URI, CLIENT_ID, SECRET);
        HttpClient client = HttpClient.newHttpClient();
        //https://docs.oracle.com/en/java/javase/15/docs/api/java.net.http/java/net/http/HttpClient.html
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriString))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // https://dzone.com/articles/how-to-parse-json-data-from-a-rest-api-using-simpl
        // https://code.google.com/archive/p/json-simple/downloads
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(response.body());
        accessToken = (String)obj.get("access_token");
    }

    public ImmutableList<TwitchStreamSnapshot> getStreams() throws InterruptedException, ParseException, IOException {
        final String STREAMS_PATH = "streams?first=100";
        HttpResponse<String> response = callTwitch(STREAMS_PATH);
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(response.body());
        ArrayList streams = (ArrayList)obj.get("data");
        ArrayList returnStreams = new ArrayList<TwitchStreamSnapshot>();
        Date date = new Date();
        String time = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(date);
        for(var stream: streams){
            ObjectMapper mapper = new ObjectMapper();
            TwitchStreamSnapshot snapshot = mapper.readValue(mapper.writeValueAsString(stream), TwitchStreamSnapshot.class);
            snapshot.snapshotDateTime = time;
            returnStreams.add(snapshot);
        }
        return ImmutableList.copyOf(returnStreams);
    }

    public ImmutableList<Streamer> getSteamerInfos(ImmutableList<String> userIds) throws InterruptedException, ParseException, IOException {
        ArrayList returnStreamers = new ArrayList<Streamer>();
        final String USERS_PATH = "users?";
        int numRequestsRemaining = userIds.size();
        while(numRequestsRemaining > 0){
            StringBuilder stringBuilder = new StringBuilder();
            int batchSize = numRequestsRemaining > 100 ? 100 : numRequestsRemaining;
            for(int i = 0; i < batchSize; i++){
                String currentId = userIds.get(numRequestsRemaining-1);
                numRequestsRemaining--;
                stringBuilder.append("id="+currentId);
                if(numRequestsRemaining>0){
                    stringBuilder.append("&");
                }
            }
            HttpResponse<String> response = callTwitch(USERS_PATH+stringBuilder.toString());
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject)parser.parse(response.body());
            ArrayList users = (ArrayList)obj.get("data");
            for(var user: users){
                ObjectMapper mapper = new ObjectMapper();
                Streamer streamer = mapper.readValue(mapper.writeValueAsString(user), Streamer.class);
                returnStreamers.add(streamer);
            }
        }
        return ImmutableList.copyOf(returnStreamers);
    }

    public long getFollowerCount(String userId) throws InterruptedException, ParseException, IOException {
        final String USERS_FOLLOWS_PATH = "users/follows?to_id=" + userId;
        HttpResponse<String> response = callTwitch(USERS_FOLLOWS_PATH);
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(response.body());
        Long count = (long)obj.get("total");
        return count;
    }

    private HttpResponse<String> callTwitch(String path) throws InterruptedException, ParseException, IOException {
        if(accessToken.isBlank()){
            refreshAccessToken();
        }

        String uriString = String.format("%s%s", TWITCH_URL, path);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriString))
                .header("Authorization", "Bearer " + accessToken)
                .header("Client-Id", CLIENT_ID)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
