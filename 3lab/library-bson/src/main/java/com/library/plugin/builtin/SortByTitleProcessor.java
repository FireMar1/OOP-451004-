package com.library.plugin.builtin;

import com.library.model.LibraryItem;
import com.library.plugin.ProcessingPlugin;
import com.library.plugin.ProcessingStage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Sorts items by title before saving.
 * This is a simple example of a deterministic pre-save processing step.
 */
public final class SortByTitleProcessor implements ProcessingPlugin {

    @Override
    public String getId() {
        return "builtin.sort-by-title.v1";
    }

    @Override
    public String getDisplayName() {
        return "Sort items by title (before save)";
    }

    @Override
    public EnumSet<ProcessingStage> supportedStages() {
        return EnumSet.of(ProcessingStage.BEFORE_SAVE);
    }

    @Override
    public List<LibraryItem> process(List<LibraryItem> items, ProcessingStage stage) {
        List<LibraryItem> copy = new ArrayList<>(items);
        copy.sort(Comparator.comparing(li -> li.getTitle() == null ? "" : li.getTitle(), String.CASE_INSENSITIVE_ORDER));
        return copy;
    }
}

