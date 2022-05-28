package com.analysisgroup.streamingapp.Models;

public class LiveStream {

    private String streamId;
    private String status;
    private String username;
    private String name;
    private String description;
    private String image;
    private String streamUrl;
    private int hlsViewerCount;

    public LiveStream() {
    }

    public LiveStream(String username, String name) {
        this.username = username;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPublishType() {
        String publishType = "LiveApp";
        return publishType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublish() {
        return true;
    }

    public boolean isPublicStream() {
        return true;
    }

    public boolean isIs360() {
        return false;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public int getHlsViewerCount() {
        return hlsViewerCount;
    }

    public void setHlsViewerCount(int hlsViewerCount) {
        this.hlsViewerCount = hlsViewerCount;
    }
}
