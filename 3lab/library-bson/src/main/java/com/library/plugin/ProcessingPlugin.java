package com.library.plugin;

import com.library.model.LibraryItem;

import java.util.EnumSet;
import java.util.List;

/**
 * SPI interface for "functional" plugins that transform data structures
 * before saving and/or after loading.
 *
 * Pattern note: This is a Strategy interface — each plugin provides
 * a different processing strategy, and the application composes them
 * into a pipeline based on user settings.
 */
public interface ProcessingPlugin {

    /**
     * Stable unique identifier used in settings.
     * Must not change between versions of the same plugin.
     */
    String getId();

    /** Human-readable name used in the UI settings menu. */
    String getDisplayName();

    /** Defines when this plugin can be applied. */
    EnumSet<ProcessingStage> supportedStages();

    /**
     * Transforms items. Implementations may mutate the list and/or its elements.
     * Returning a new list is also allowed.
     */
    List<LibraryItem> process(List<LibraryItem> items, ProcessingStage stage);
}

