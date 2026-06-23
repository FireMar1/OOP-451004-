package com.library.plugin.builtin;

import com.library.model.LibraryItem;
import com.library.plugin.ProcessingPlugin;
import com.library.plugin.ProcessingStage;

import java.util.EnumSet;
import java.util.List;

/**
 * Variant 1 processor: XML → JSON transformation.
 *
 * Implementation approach in this project:
 * - BEFORE_SAVE: if the item's description contains XML (starts with '<'),
 *   it is wrapped into a JSON object {"xml":"..."}.
 * - AFTER_LOAD: if the description looks like {"xml":"..."} it is unwrapped back to XML.
 *
 * This keeps the BSON file format unchanged while demonstrating the required conversion
 * on the data structure that is saved/loaded.
 */
public final class XmlToJsonVariantProcessor implements ProcessingPlugin {

    @Override
    public String getId() {
        return "variant1.xml-json.description.v1";
    }

    @Override
    public String getDisplayName() {
        return "Variant 1: XML ↔ JSON (description field)";
    }

    @Override
    public EnumSet<ProcessingStage> supportedStages() {
        return EnumSet.of(ProcessingStage.BEFORE_SAVE, ProcessingStage.AFTER_LOAD);
    }

    @Override
    public List<LibraryItem> process(List<LibraryItem> items, ProcessingStage stage) {
        for (LibraryItem item : items) {
            String d = item.getDescription();
            if (d == null) continue;

            if (stage == ProcessingStage.BEFORE_SAVE) {
                String trimmed = d.stripLeading();
                if (trimmed.startsWith("<")) {
                    item.setDescription(toJsonXmlWrapper(d));
                }
            } else {
                String trimmed = d.stripLeading();
                if (trimmed.startsWith("{\"xml\":\"") && trimmed.endsWith("\"}")) {
                    item.setDescription(fromJsonXmlWrapper(trimmed));
                }
            }
        }
        return items;
    }

    private static String toJsonXmlWrapper(String xml) {
        return "{\"xml\":\"" + escapeJsonString(xml) + "\"}";
    }

    private static String fromJsonXmlWrapper(String json) {
        // Expected exact format: {"xml":"..."}
        int start = "{\"xml\":\"".length();
        int end = json.length() - "\"}".length();
        if (end < start) return json;
        String payload = json.substring(start, end);
        return unescapeJsonString(payload);
    }

    private static String escapeJsonString(String s) {
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private static String unescapeJsonString(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                out.append(c);
                continue;
            }
            char n = s.charAt(++i);
            switch (n) {
                case '\\' -> out.append('\\');
                case '"' -> out.append('"');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                default -> out.append(n);
            }
        }
        return out.toString();
    }
}

