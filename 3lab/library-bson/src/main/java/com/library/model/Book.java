package com.library.model;
import com.library.serialization.BsonDocument;
public class Book extends LibraryItem {
    private String author; private String isbn; private int pageCount; private String genre;
    public Book() {}
    public Book(String id, String title, int year,
                String desc, String author, String isbn, int pageCount, String genre) {
        super(id, title, year, desc); this.author = author; this.isbn = isbn; this.pageCount = pageCount; this.genre = genre;
    }
    @Override
    public String getTypeName() { return "Book"; }
    @Override
    public String getDisplayLabel() {
        return String.format("[Book] %s — %s (%d)", title, author, year);
    }
    @Override
    public BsonDocument toBson() {
        BsonDocument d = baseBson();
        d.putString("author", author!=null?author:""); d.putString("isbn", isbn!=null?isbn:"");
        d.putInt32("pageCount", pageCount); d.putString("genre", genre!=null?genre:""); return d;
    }
    public static Book fromBson(BsonDocument d) {
        Book b = new Book(); b.fromBsonBase(d);
        b.author = d.getString("author"); b.isbn = d.getString("isbn");
        b.pageCount = d.getInt32("pageCount"); b.genre = d.getString("genre"); return b;
    }
    public String getAuthor() { return author;
    }
    public void setAuthor(String a) {
        author=a;
    }
    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String i) {
        isbn=i;
    }
    public int getPageCount() {
        return pageCount;
    }
    public void setPageCount(int p) {
        pageCount=p;
    }
    public String getGenre() {
        return genre;
    }
    public void setGenre(String g) {
        genre=g;
    }
}
