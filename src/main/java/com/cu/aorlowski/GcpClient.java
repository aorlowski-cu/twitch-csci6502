package com.cu.aorlowski;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Acl;
import com.google.common.collect.ImmutableList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GcpClient {
    private static final String GCP_PROJECT_ID = "molten-castle-305417";
    private static final String GCP_STREAM_DATASET = "twitch_streams";
    private static final String GCP_BUCKET_NAME = "twitch-streams";

    private static final BigQuery bq = BigQueryOptions.newBuilder()
            .setCredentials(getCredentials())
            .setProjectId(GCP_PROJECT_ID)
            .build()
            .getService();

    private static final Storage storage = StorageOptions.newBuilder()
            .setCredentials(getCredentials())
            .setProjectId(GCP_PROJECT_ID)
            .build()
            .getService();

    public void uploadStreams(ImmutableList<TwitchStreamSnapshot> streams) throws IllegalAccessException {
        Date date = new Date();
        String today = new SimpleDateFormat("yyyyMMdd").format(date);
        String tableName = "snapshot$" + today;
        ArrayList<Object> content = new ArrayList<>();
        streams.forEach(stream -> content.add((Object)stream));
        Blob blob = saveToCloudStorage(content);
        loadBqFromGcs(GCP_STREAM_DATASET, tableName, "gs://" + blob.getBucket() + "/" + blob.getName());
        blob.delete();
    }

    public ArrayList<String> getDistinctStreamers(){
        String query = "SELECT DISTINCT user_id FROM `molten-castle-305417.twitch_streams.snapshot`";
        ArrayList<String> result = new ArrayList<>();
        try{
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult queryResult = bq.query(queryConfig);

            queryResult.iterateAll()
                    .forEach(rows ->
                            rows.forEach(row ->
                                    result.add((String) row.getValue())));


        } catch (InterruptedException e) {
            System.out.println("Query did not run \n" + e.toString());
        }
        return result;
    }

    public void uploadStreamers(ImmutableList<Streamer> streamers){
        ArrayList<Object> content = new ArrayList<>();
        streamers.forEach(streamer -> content.add((Object)streamer));
        Blob blob = saveToCloudStorage(content);
        loadBqFromGcs(GCP_STREAM_DATASET, "streamers", "gs://" + blob.getBucket() + "/" + blob.getName());
        blob.delete();
    }

    private static boolean loadBqFromGcs(String datasetName, String tableName, String sourceUri){
        try{
            TableId tableId = TableId.of(datasetName, tableName);
            LoadJobConfiguration loadConfig =
                    LoadJobConfiguration.newBuilder(tableId, sourceUri)
                            .setFormatOptions(FormatOptions.json())
                            .setTimePartitioning(TimePartitioning.of(TimePartitioning.Type.DAY))
                            .setAutodetect(true)
                            .build();
            Job job = bq.create(JobInfo.of(loadConfig));
            job = job.waitFor();
            if(job.isDone()){
                System.out.println("Streams successfully loaded into big query");
                return true;
            } else{
                System.out.println( "BigQuery was unable to load into the table due to an error:"
                        + job.getStatus().getError());
            }
        } catch (BigQueryException | InterruptedException e){
            System.out.println(e);
        }
        return false;
    }

    private Blob saveToCloudStorage(ArrayList<Object> objects) {
        Bucket bucket = getOrCreateStorageBucket(GCP_BUCKET_NAME);
        StringBuilder str = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        for (var obj : objects) {
            try {
                str.append(mapper.writeValueAsString(obj) + "\n");
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        Blob result = bucket.create("temp-twitch.json", str.toString().getBytes(Charset.forName("UTF-8")));
        return result;
    }
    private Bucket getOrCreateStorageBucket(String bucketName) {
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            bucket = storage.create(BucketInfo.newBuilder(bucketName)
                    .setAcl(ImmutableList.of(
                            Acl.of(Acl.User.ofAllAuthenticatedUsers(), Acl.Role.OWNER))).build());
        }
        return bucket;
    }

    private void createDatasetIfNotExists(String datasetName) {
        try {
            DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();
            Dataset newDataset = bq.create(datasetInfo);
            newDataset.getDatasetId().getDataset();
        } catch (Exception e) {
        }

    }

    private void createTableIfNotExists(String tableName) {
        try {
            TableId tableId = TableId.of(GCP_STREAM_DATASET, tableName);
            TableDefinition tableDefinition = StandardTableDefinition.of(Schema.of());
            TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
            var response = bq.create(tableInfo);
            System.out.println(response.getTableId());
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private Map<String, Object> streamToMap(TwitchStreamSnapshot stream) throws IllegalAccessException {
        HashMap<String, Object> map = new HashMap<>();
        Field[] allFields = stream.getClass().getDeclaredFields();
        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = field.get(stream);
            map.put(field.getName(), value);
        }
        return map;
    }

    private static GoogleCredentials getCredentials() {
        GoogleCredentials creds = null;
        try {
            creds = GoogleCredentials
                    .fromStream(new FileInputStream("C:\\Users\\aorlowski\\Downloads\\molten-castle-305417-bc02648f1bb2.json"));
        } catch (IOException e) {
            try{
                creds = GoogleCredentials.getApplicationDefault();
            } catch(IOException ex){
                System.out.println(ex);
            }
        }
        return creds;
    }
}
