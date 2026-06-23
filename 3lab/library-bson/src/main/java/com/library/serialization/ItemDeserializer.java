package com.library.serialization;
import com.library.model.LibraryItem;
@FunctionalInterface
public interface ItemDeserializer {
    LibraryItem deserialize(BsonDocument doc);
}
