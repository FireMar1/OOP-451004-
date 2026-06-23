package com.library.plugin.builtin;

import com.library.model.LibraryItem;
import com.library.plugin.ProcessingPlugin;
import com.library.plugin.ProcessingStage;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Uppercases titles after loading.
 * Demonstrates a post-load processing plugin.
 */
public final class UppercaseTitlesProcessor implements ProcessingPlugin {

    @Override
    public String getId() {
        return "builtin.uppercase-titles.v1";
    }

    @Override
    public String getDisplayName() {
        return "Uppercase titles (after load)";
    }

    @Override
    public EnumSet<ProcessingStage> supportedStages() {
        return EnumSet.of(ProcessingStage.AFTER_LOAD);
    }

    @Override
    public List<LibraryItem> process(List<LibraryItem> items, ProcessingStage stage) {
        for (LibraryItem item : items) {
            if (item.getTitle() != null) item.setTitle(item.getTitle().toUpperCase(Locale.ROOT));
        }
        return items;
    }
}

