package com.library.model;
import com.library.serialization.BsonDocument;
public class ScientificArticle extends LibraryItem {
    private String author; private String journal; private String doi; private String field;
    public ScientificArticle() {}
    public ScientificArticle(String id, String title,
                             int year, String desc, String author, String journal,
                             String doi, String field) {
        super(id, title, year, desc); this.author=author;
        this.journal=journal; this.doi=doi; this.field=field;
    }
    @Override public String getTypeName() { return "ScientificArticle"; }
    @Override public String getDisplayLabel() {
        return String.format("[Article] %s — %s, %s (%d)", title, author, journal, year);
    }
    @Override public BsonDocument toBson() {
        BsonDocument d = baseBson();
        d.putString("author", author!=null?author:""); d.putString("journal", journal!=null?journal:"");
        d.putString("doi", doi!=null?doi:""); d.putString("field", field!=null?field:""); return d;
    }
    public static ScientificArticle fromBson(BsonDocument d) {
        ScientificArticle a = new ScientificArticle(); a.fromBsonBase(d);
        a.author = d.getString("author"); a.journal = d.getString("journal");
        a.doi = d.getString("doi"); a.field = d.getString("field"); return a;
    }
    public String getAuthor() { return author; }
    public void setAuthor(String a) { author=a; }
    public String getJournal() { return journal; }
    public void setJournal(String j) { journal=j; }
    public String getDoi() { return doi; }
    public void setDoi(String d) { doi=d; }
    public String getField() { return field; }
    public void setField(String f) { field=f; }
}
