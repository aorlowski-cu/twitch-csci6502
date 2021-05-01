package com.cu.aorlowski;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

public class Streamer {
    @JsonProperty("id")
    public String userId;

    @JsonProperty("login")
    public String login;

    @JsonProperty("display_name")
    public String displayName;

    @JsonProperty("type")
    public String type;

    @JsonProperty("broadcaster_type")
    public String broadcasterType;

    @JsonProperty("description")
    public String description;

    @JsonProperty("profile_image_url")
    public String profileImageUrl;

    @JsonProperty("offline_image_url")
    public String offlineImageUrl;

    @JsonProperty("view_count")
    public BigInteger viewCount;

    @JsonProperty("email")
    public String email;

    @JsonProperty("created_at")
    public String createdAt;

    public long followerCount;
}
