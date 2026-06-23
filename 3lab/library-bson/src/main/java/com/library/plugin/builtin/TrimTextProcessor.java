package com.library.plugin.builtin;

import com.library.model.LibraryItem;
import com.library.plugin.ProcessingPlugin;
import com.library.plugin.ProcessingStage;

import java.util.EnumSet;
import java.util.List;

/**
 * Trims leading/trailing whitespace in common text fields.
 * Useful as a "data normalization" processor.
 */
public final class TrimTextProcessor implements ProcessingPlugin {

    @Override
    public String getId() {
        return "builtin.trim-text.v1";
    }

    @Override
    public String getDisplayName() {
        return "Trim text fields";
    }

    @Override
    public EnumSet<ProcessingStage> supportedStages() {
        return EnumSet.of(ProcessingStage.BEFORE_SAVE, ProcessingStage.AFTER_LOAD);
    }

    @Override
    public List<LibraryItem> process(List<LibraryItem> items, ProcessingStage stage) {
        for (LibraryItem item : items) {
            if (item.getId() != null) item.setId(item.getId().trim());
            if (item.getTitle() != null) item.setTitle(item.getTitle().trim());
            if (item.getDescription() != null) item.setDescription(item.getDescription().trim());
        }
        return items;
    }
}

