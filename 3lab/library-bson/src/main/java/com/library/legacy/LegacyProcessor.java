package com.library.legacy;

import com.library.model.LibraryItem;

import java.util.List;

/**
 * A "foreign" processing plugin interface (simulates a plugin written by another team/student).
 *
 * Lab #6 requires adapting a plugin that does not follow our SPI.
 * This interface intentionally differs from {@code ProcessingPlugin}:
 * - no stage concept
 * - different naming method
 *
 * The application adapts implementations of this interface via the Adapter pattern.
 */
public interface LegacyProcessor {

    /** Human-readable name of the legacy processor. */
    String name();

    /**
     * Processes loaded items.
     * Legacy processors are assumed to run AFTER_LOAD.
     */
    List<LibraryItem> process(List<LibraryItem> items);
}

