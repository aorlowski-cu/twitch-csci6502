package com.cu.aorlowski;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitchStreamSnapshot {
    public String id;

    @JsonProperty("user_id")
    public String userId;

    @JsonProperty("user_login")
    public String userLogin;

    @JsonProperty("user_name")
    public String userName;

    @JsonProperty("game_id")
    public String gameId;

    @JsonProperty("game_name")
    public String gameName;
    public String type;
    public String title;

    @JsonProperty("viewer_count")
    public String viewerCount;

    @JsonProperty("started_at")
    public String startedAt;

    public String language;

    @JsonProperty("tag_ids")
    public String[] tagIds;
    public String snapshotDateTime;
}
