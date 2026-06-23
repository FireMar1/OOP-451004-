package com.library.model;
import com.library.serialization.BsonDocument;
public class Textbook extends Book {
    private String subject; private int gradeLevel;
    public Textbook() {}
    public Textbook(String id, String title, int year, String desc, String author, String isbn,
                    int pages, String genre, String subject, int gradeLevel) {
        super(id, title, year, desc, author, isbn, pages, genre); this.subject=subject;
        this.gradeLevel=gradeLevel;
    }
    @Override public String getTypeName() { return "Textbook"; }
    @Override public String getDisplayLabel() {
        return String.format("[Textbook] %s — %s, Grade %d (%d)", title, subject, gradeLevel, year);
    }
    @Override public BsonDocument toBson() {
        BsonDocument d = super.toBson();
        d.putString("_type", getTypeName());
        d.putString("subject", subject!=null?subject:""); d.putInt32("gradeLevel", gradeLevel); return d;
    }
    public static Textbook fromBson(BsonDocument d) {
        Textbook t = new Textbook(); t.fromBsonBase(d);
        t.setAuthor(d.getString("author")); t.setIsbn(d.getString("isbn"));
        t.setPageCount(d.getInt32("pageCount")); t.setGenre(d.getString("genre"));
        t.subject = d.getString("subject"); t.gradeLevel = d.getInt32("gradeLevel"); return t;
    }
    public String getSubject() { return subject; }
    public void setSubject(String s) { subject=s; }
    public int getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(int g) { gradeLevel=g; }
}
