package com.library.serialization;
import com.library.model.LibraryItem;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
/**
 * Serializes/deserializes a list of LibraryItem objects to/from a BSON file.
 * File format: single BSON document { "items": [ <doc>, <doc>, ... ] }
 */
public class BsonSerializer {
    public static void serialize(List<LibraryItem> items, File file) throws IOException {
        List<BsonDocument> docs = new ArrayList<>();
        for (LibraryItem item : items) docs.add(item.toBson());
        BsonDocument root = new BsonDocument();
        root.putArray("items", docs);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(root.encode());
        }
    }
    public static List<LibraryItem> deserialize(File file) throws IOException {
        byte[] bytes;
        try (FileInputStream fis = new FileInputStream(file)) { bytes = fis.readAllBytes(); }
        BsonDocument root = BsonDocument.decode(ByteBuffer.wrap(bytes));
        List<LibraryItem> items = new ArrayList<>();
        for (BsonDocument doc : root.getArray("items")) {
            items.add(ItemRegistry.deserialize(doc));
        }
        return items;
    }
}
