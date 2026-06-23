package com.library.model;
import com.library.serialization.BsonDocument;
public class Newspaper extends LibraryItem {
    private String publisher; private String frequency; private String region;
    public Newspaper() {}
    public Newspaper(String id, String title, int year,
                     String desc, String publisher, String frequency, String region) {
        super(id, title, year, desc); this.publisher=publisher;
        this.frequency=frequency; this.region=region;
    }
    @Override public String getTypeName() { return "Newspaper"; }
    @Override public String getDisplayLabel() {
        return String.format("[Newspaper] %s — %s (%d)", title, region, year);
    }
    @Override public BsonDocument toBson() {
        BsonDocument d = baseBson();
        d.putString("publisher", publisher!=null?publisher:"");
        d.putString("frequency", frequency!=null?frequency:"");
        d.putString("region", region!=null?region:""); return d;
    }
    public static Newspaper fromBson(BsonDocument d) {
        Newspaper n = new Newspaper(); n.fromBsonBase(d);
        n.publisher = d.getString("publisher"); n.frequency = d.getString("frequency");
        n.region = d.getString("region"); return n;
    }
    public String getPublisher() { return publisher; }
    public void setPublisher(String p) { publisher=p; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String f) { frequency=f; }
    public String getRegion() { return region; }
    public void setRegion(String r) { region=r; }
}
