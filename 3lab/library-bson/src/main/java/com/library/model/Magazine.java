package com.library.model;
import com.library.serialization.BsonDocument;
public class Magazine extends LibraryItem {
    private String publisher; private int issueNumber; private String category;
    public Magazine() {}
    public Magazine(String id, String title, int year, String desc, String publisher,
                    int issueNumber, String category) {
        super(id, title, year, desc);
        this.publisher=publisher; this.issueNumber=issueNumber; this.category=category;
    }
    @Override public String getTypeName() { return "Magazine"; }
    @Override public String getDisplayLabel() {
        return String.format("[Magazine] %s #%d (%d)", title, issueNumber, year);
    }
    @Override public BsonDocument toBson() {
        BsonDocument d = baseBson();
        d.putString("publisher", publisher!=null?publisher:"");
        d.putInt32("issueNumber", issueNumber);
        d.putString("category", category!=null?category:""); return d;
    }
    public static Magazine fromBson(BsonDocument d) {
        Magazine m = new Magazine(); m.fromBsonBase(d);
        m.publisher = d.getString("publisher"); m.issueNumber = d.getInt32("issueNumber");
        m.category = d.getString("category"); return m;
    }
    public String getPublisher() { return publisher; }
    public void setPublisher(String p) { publisher=p; }
    public int getIssueNumber() { return issueNumber; }
    public void setIssueNumber(int n) { issueNumber=n; }
    public String getCategory() { return category; }
    public void setCategory(String c) { category=c; }
}
