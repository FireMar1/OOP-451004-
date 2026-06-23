package com.library.model;
import com.library.serialization.BsonDocument;
public class AudioBook extends LibraryItem {
    private String author;
    private String narrator;
    private int durationMinutes;
    private String format;
    public AudioBook() {}
    public AudioBook(String id, String title, int year, String desc, String author, String narrator, int durationMinutes, String format) {
        super(id, title, year, desc); this.author=author; this.narrator=narrator; this.durationMinutes=durationMinutes; this.format=format;
    }
    @Override public String getTypeName() {
        return "AudioBook";
    }
    @Override public String getDisplayLabel() {
        return String.format("[AudioBook] %s — %s, narrated by %s (%d min)", title, author,
                narrator, durationMinutes);
    }
    @Override public BsonDocument toBson() {
        BsonDocument d = baseBson();
        d.putString("author", author!=null?author:""); d.putString("narrator", narrator!=null?narrator:"");
        d.putInt32("durationMinutes", durationMinutes); d.putString("format", format!=null?format:"");
        return d;
    }
    public static AudioBook fromBson(BsonDocument d) {
        AudioBook a = new AudioBook(); a.fromBsonBase(d);
        a.author = d.getString("author"); a.narrator = d.getString("narrator");
        a.durationMinutes = d.getInt32("durationMinutes"); a.format = d.getString("format");
        return a;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String a) {
        author=a;
    }
    public String getNarrator() {
        return narrator;
    }
    public void setNarrator(String n) {
        narrator=n;
    }
    public int getDurationMinutes() {
        return durationMinutes;
    }
    public void setDurationMinutes(int d) {
        durationMinutes=d;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String f) { format=f; }
}
