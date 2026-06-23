package com.library.serialization;

import com.library.model.*;
import java.util.*;

public class ItemRegistry {

    private static final Map<String, ItemDeserializer> REGISTRY = new HashMap<>();

    static {
        /**
         * Built-in types.
         * Plugins are registered dynamically by {@code PluginManager} at runtime.
         */
        register("Book",              Book::fromBson);
        register("Magazine",          Magazine::fromBson);
        register("Newspaper",         Newspaper::fromBson);
        register("ScientificArticle", ScientificArticle::fromBson);
        register("Textbook",          Textbook::fromBson);
        register("AudioBook",         AudioBook::fromBson);
    }

    public static void register(String typeName, ItemDeserializer d) {
        REGISTRY.put(typeName, d);
    }

    public static LibraryItem deserialize(BsonDocument doc) {
        String type = doc.getString("_type");
        ItemDeserializer d = REGISTRY.get(type);
        if (d == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return d.deserialize(doc);
    }
}