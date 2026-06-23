package com.library.model;

import com.library.serialization.BsonDocument;

/**
 * Abstract base class for all library items.
 * Every subclass must implement toBson() and provide a type identifier.
 * Adding new subclasses requires NO changes to existing code.
 */
public abstract class LibraryItem {

    protected String id;
    protected String title;
    protected int    year;
    protected String description;

    public LibraryItem() {}

    public LibraryItem(String id, String title, int year, String description) {
        this.id          = id;
        this.title       = title;
        this.year        = year;
        this.description = description;
    }


    /** Serialize this item to BSON. Must include "_type" field. */
    public abstract BsonDocument toBson();

    /** Populate common fields from a BsonDocument. */
    protected void fromBsonBase(BsonDocument doc) {
        this.id          = doc.getString("id");
        this.title       = doc.getString("title");
        this.year        = doc.getInt32("year");
        this.description = doc.getString("description");
    }

    /** Write common fields into a new BsonDocument. */
    protected BsonDocument baseBson() {
        BsonDocument doc = new BsonDocument();
        doc.putString("_type",       getTypeName());
        doc.putString("id",          id          != null ? id          : "");
        doc.putString("title",       title       != null ? title       : "");
        doc.putInt32 ("year",        year);
        doc.putString("description", description != null ? description : "");
        return doc;
    }

    /** Unique string identifier stored in BSON "_type" field. */
    public abstract String getTypeName();

    /** Human-readable label shown in the UI list. */
    public abstract String getDisplayLabel();

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }
    public String getTitle()               { return title; }
    public void   setTitle(String t)       { this.title = t; }
    public int    getYear()                { return year; }
    public void   setYear(int y)           { this.year = y; }
    public String getDescription()         { return description; }
    public void   setDescription(String d) { this.description = d; }

    @Override
    public String toString() { return getDisplayLabel(); }
}
