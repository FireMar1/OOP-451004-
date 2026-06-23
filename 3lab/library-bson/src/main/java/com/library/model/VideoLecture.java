package com.library.model;

import com.library.serialization.BsonDocument;

public class VideoLecture extends LibraryItem {
    private String lecturer;
    private int durationMinutes;
    private String platform;   // YouTube, Coursera, etc.
    private String topic;

    public VideoLecture() {}

    public VideoLecture(String id, String title, int year, String desc,
                        String lecturer, int durationMinutes, String platform, String topic) {
        super(id, title, year, desc);
        this.lecturer = lecturer;
        this.durationMinutes = durationMinutes;
        this.platform = platform;
        this.topic = topic;
    }

    @Override
    public String getTypeName() {
        return "VideoLecture";
    }

    @Override
    public String getDisplayLabel() {
        return String.format("[VideoLecture] %s — %s (%d min, %s)", title, lecturer, durationMinutes, platform);
    }

    @Override
    public BsonDocument toBson() {
        BsonDocument d = baseBson();
        d.putString("lecturer", lecturer != null ? lecturer : "");
        d.putInt32("durationMinutes", durationMinutes);
        d.putString("platform", platform != null ? platform : "");
        d.putString("topic", topic != null ? topic : "");
        return d;
    }

    public static VideoLecture fromBson(BsonDocument d) {
        VideoLecture v = new VideoLecture();
        v.fromBsonBase(d);
        v.lecturer = d.getString("lecturer");
        v.durationMinutes = d.getInt32("durationMinutes");
        v.platform = d.getString("platform");
        v.topic = d.getString("topic");
        return v;
    }

    // getters & setters
    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}