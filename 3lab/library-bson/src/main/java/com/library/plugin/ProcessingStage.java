package com.library.plugin;

/**
 * Defines when a processing plugin is executed.
 * The assignment requires:
 * - processing before saving to file
 * - processing after loading from file
 */
public enum ProcessingStage {
    BEFORE_SAVE,
    AFTER_LOAD
}

