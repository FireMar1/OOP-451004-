package com.library.plugin;

import com.library.serialization.ItemDeserializer;

/**
 * SPI interface for library item plugins.
 * Every plugin JAR must implement this interface and register it in
 *   META-INF/services/com.library.plugin.PluginDescriptor
 *
 * Two responsibilities per plugin:
 *   1. Provide a deserializer (for BSON → model)
 *   2. Register a UI form  (for Add / Edit dialogs)
 *
 * Adding a new item type requires ZERO changes to the base program.
 */
public interface PluginDescriptor {

    /** Must match the "_type" field stored in BSON. */
    String getTypeName();

    /** Returns the deserializer used by ItemRegistry. */
    ItemDeserializer getDeserializer();

    /**
     * Called once at startup by ItemFormDialog static initialiser.
     * The plugin must call ItemFormDialog.registerForm(...) here.
     */
    void registerForm();
}