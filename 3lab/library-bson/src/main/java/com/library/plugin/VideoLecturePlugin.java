package com.library.plugin;

import com.library.model.VideoLecture;
import com.library.serialization.ItemDeserializer;
import com.library.ui.ItemFormDialog;

import java.util.UUID;

/**
 * Plugin that adds the VideoLecture type to the library application.
 *
 * Registered via:
 *   META-INF/services/com.library.plugin.PluginDescriptor
 *
 * No changes to base program source are required to activate this plugin.
 */
public class VideoLecturePlugin implements PluginDescriptor {

    @Override
    public String getTypeName() {
        return "VideoLecture";
    }

    @Override
    public ItemDeserializer getDeserializer() {
        return VideoLecture::fromBson;
    }

    /**
     * Registers the Add/Edit form for VideoLecture in ItemFormDialog.
     * Called automatically by ItemFormDialog's static initialiser via ServiceLoader.
     */
    @Override
    public void registerForm() {
        ItemFormDialog.registerForm(
                "VideoLecture",
                new String[]{"ID", "Title", "Year", "Description",
                        "Lecturer", "Duration (min)", "Platform", "Topic"},
                // populator: model → form fields
                (item, fields) -> {
                    VideoLecture v = (VideoLecture) item;
                    fields.get("ID").setText(v.getId());
                    fields.get("Title").setText(v.getTitle());
                    fields.get("Year").setText(String.valueOf(v.getYear()));
                    fields.get("Description").setText(v.getDescription());
                    fields.get("Lecturer").setText(v.getLecturer());
                    fields.get("Duration (min)").setText(String.valueOf(v.getDurationMinutes()));
                    fields.get("Platform").setText(v.getPlatform());
                    fields.get("Topic").setText(v.getTopic());
                },
                // applier: form fields → model
                (item, fields) -> {
                    VideoLecture v = (VideoLecture) item;
                    v.setId(fields.get("ID").getText());
                    v.setTitle(fields.get("Title").getText());
                    v.setYear(Integer.parseInt(fields.get("Year").getText()));
                    v.setDescription(fields.get("Description").getText());
                    v.setLecturer(fields.get("Lecturer").getText());
                    v.setDurationMinutes(Integer.parseInt(fields.get("Duration (min)").getText()));
                    v.setPlatform(fields.get("Platform").getText());
                    v.setTopic(fields.get("Topic").getText());
                },
                // factory: creates default new item
                () -> new VideoLecture(
                        UUID.randomUUID().toString().substring(0, 8),
                        "New Video Lecture", 2024, "",
                        "Lecturer", 60, "YouTube", "Topic")
        );
    }
}