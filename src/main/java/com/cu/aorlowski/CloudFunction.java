package com.cu.aorlowski;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.common.collect.ImmutableList;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.logging.Logger;

public class CloudFunction implements BackgroundFunction<PubSubMessage> {
    private static final Logger logger =
            Logger.getLogger(CloudFunction.class.getName());
    private static final TwitchClient twitchClient = new TwitchClient();
    private static final GcpClient gcpClient = new GcpClient();

    @Override
    public void accept(PubSubMessage pubSubMessage, Context context) throws InterruptedException, ParseException, IOException, IllegalAccessException {
        saveStreams();
    }

    private void saveStreams() throws InterruptedException, ParseException, IOException, IllegalAccessException {
        ImmutableList<TwitchStreamSnapshot> streams = twitchClient.getStreams();
        gcpClient.uploadStreams(streams);
    }
}
