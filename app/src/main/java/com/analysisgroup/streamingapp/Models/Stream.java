package com.analysisgroup.streamingapp.Models;

public class Stream {

    private String streamId;
    private String status;
    private final String publishType = "LiveApp";
    private String name;
    private String description;
    private final boolean publish = true;
    private final boolean publicStream = true;
    private final boolean is360 = false;
    private String streamUrl;
    private int hlsViewerCount;

    public Stream() {
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
        return publish;
    }

    public boolean isPublicStream() {
        return publicStream;
    }

    public boolean isIs360() {
        return is360;
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
