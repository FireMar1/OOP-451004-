package com.library.serialization;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Supported types used in this project:
 *   0x02  - UTF-8 string
 *   0x10  - int32
 *   0x04  - array
 *   0x03  - document (embedded)
 */
public final class BsonDocument {

    // Ordered map preserves field insertion order
    private final LinkedHashMap<String, Object> fields = new LinkedHashMap<>();

    public void putString(String key, String value) {
        fields.put(key, value != null ? value : "");
    }

    public void putInt32(String key, int value) {
        fields.put(key, value);
    }

    public void putArray(String key, List<BsonDocument> docs) {
        fields.put(key, docs);
    }

    public String getString(String key) {
        Object v = fields.get(key);
        return v instanceof String s ? s : "";
    }

    public int getInt32(String key) {
        Object v = fields.get(key);
        return v instanceof Integer i ? i : 0;
    }

    @SuppressWarnings("unchecked")
    public List<BsonDocument> getArray(String key) {
        Object v = fields.get(key);
        return v instanceof List<?> l ? (List<BsonDocument>) l : new ArrayList<>();
    }

    public boolean containsKey(String key) { return fields.containsKey(key); }

    // ── Encoding ──────────────────────────────────────────────────────────────

    /** Encode this document to a byte array following BSON spec. */
    public byte[] encode() {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key   = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String s) {
                body.write(0x02);                        // type: string
                writeCString(body, key);
                byte[] strBytes = s.getBytes(StandardCharsets.UTF_8);
                writeInt32(body, strBytes.length + 1);   // length includes null terminator
                body.writeBytes(strBytes);
                body.write(0x00);                        // null terminator

            } else if (value instanceof Integer i) {
                body.write(0x10);                        // type: int32
                writeCString(body, key);
                writeInt32(body, i);

            } else if (value instanceof List<?> list) {
                body.write(0x04);                        // type: array
                writeCString(body, key);
                // Build array document with "0","1",... keys
                BsonDocument arrDoc = new BsonDocument();
                @SuppressWarnings("unchecked")
                List<BsonDocument> docs = (List<BsonDocument>) list;
                for (int idx = 0; idx < docs.size(); idx++) {
                    // Inline the sub-doc as embedded document type 0x03
                    arrDoc.fields.put(String.valueOf(idx), docs.get(idx));
                }
                body.writeBytes(arrDoc.encode());
            } else if (value instanceof BsonDocument nested) {
                // Inline embedded document (used internally for array elements)
                body.write(0x03);
                writeCString(body, key);
                body.writeBytes(nested.encode());
            }
        }

        // Final document: int32 size + body + 0x00 terminator
        int totalSize = 4 + body.size() + 1;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInt32(out, totalSize);
        out.writeBytes(body.toByteArray());
        out.write(0x00);
        return out.toByteArray();
    }

    // ── Decoding ──────────────────────────────────────────────────────────────

    /** Decode a BSON document from a ByteBuffer (position must be at start of doc). */
    public static BsonDocument decode(ByteBuffer buf) {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int startPos = buf.position();
        int docSize  = buf.getInt();           // total size including self

        BsonDocument doc = new BsonDocument();

        while (buf.position() < startPos + docSize - 1) { // -1 for trailing 0x00
            byte type = buf.get();

            String key = readCString(buf);

            if (type == 0x02) {                // string
                int len = buf.getInt();        // includes null terminator
                byte[] strBytes = new byte[len - 1];
                buf.get(strBytes);
                buf.get();                     // consume null terminator
                doc.fields.put(key, new String(strBytes, StandardCharsets.UTF_8));

            } else if (type == 0x10) {         // int32
                doc.fields.put(key, buf.getInt());

            } else if (type == 0x04) {         // array
                BsonDocument arrDoc = decode(buf);
                List<BsonDocument> list = new ArrayList<>();
                for (int i = 0; arrDoc.fields.containsKey(String.valueOf(i)); i++) {
                    Object val = arrDoc.fields.get(String.valueOf(i));
                    if (val instanceof BsonDocument bd) list.add(bd);
                }
                doc.fields.put(key, list);

            } else if (type == 0x03) {         // embedded document
                BsonDocument nested = decode(buf);
                doc.fields.put(key, nested);
            }
            // Unknown types: skip — makes format forward-compatible
        }

        buf.position(startPos + docSize);      // jump to end of this doc
        return doc;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void writeInt32(OutputStream out, int value) {
        try {
            out.write(value & 0xFF);
            out.write((value >> 8) & 0xFF);
            out.write((value >> 16) & 0xFF);
            out.write((value >> 24) & 0xFF);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private static void writeCString(OutputStream out, String s) {
        try {
            out.write(s.getBytes(StandardCharsets.UTF_8));
            out.write(0x00);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private static String readCString(ByteBuffer buf) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b;
        while ((b = buf.get()) != 0x00) baos.write(b);
        return baos.toString(StandardCharsets.UTF_8);
    }

    /** Simple JSON-like representation for the detail panel. */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{\n");
        fields.forEach((k, v) -> {
            sb.append("  \"").append(k).append("\": ");
            if (v instanceof String s)           sb.append('"').append(s).append('"');
            else if (v instanceof Integer i)     sb.append(i);
            else if (v instanceof List<?> list)  sb.append("[... ").append(list.size()).append(" items]");
            else if (v instanceof BsonDocument d) sb.append(d.toJson());
            sb.append(",\n");
        });
        if (sb.charAt(sb.length() - 2) == ',') sb.deleteCharAt(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}
