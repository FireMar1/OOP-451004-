package com.library.legacy.example;

import com.library.legacy.LegacyProcessor;
import com.library.model.LibraryItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Example of a "foreign" plugin that uses {@link LegacyProcessor} interface.
 * The base application will load it and adapt it via the Adapter pattern.
 */
public final class LegacyReverseOrderProcessor implements LegacyProcessor {

    @Override
    public String name() {
        return "Reverse item order (after load)";
    }

    @Override
    public List<LibraryItem> process(List<LibraryItem> items) {
        List<LibraryItem> copy = new ArrayList<>(items);
        Collections.reverse(copy);
        return copy;
    }
}

