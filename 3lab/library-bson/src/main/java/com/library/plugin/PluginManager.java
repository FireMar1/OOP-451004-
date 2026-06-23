package com.library.plugin;

import com.library.legacy.LegacyProcessor;
import com.library.serialization.ItemRegistry;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads plugin JARs dynamically and exposes discovered extensions.
 *
 * Responsibilities:
 * - Load item-type plugins (new subclasses in the hierarchy) via {@link PluginDescriptor}
 * - Load functional plugins via {@link ProcessingPlugin}
 * - Register discovered extensions in the base application (registry + UI forms)
 *
 * Pattern note: This class acts as a Facade for the plugin subsystem.
 */
public final class PluginManager {

    private final List<ClassLoader> pluginClassLoaders = new ArrayList<>();
    private final Map<String, PluginDescriptor> itemPluginsByType = new LinkedHashMap<>();
    private final Map<String, ProcessingPlugin> processingPluginsById = new LinkedHashMap<>();

    /**
     * Loads plugins from the application classpath (built-ins) and from a directory of JARs.
     * This method is safe to call multiple times — it clears previously loaded plugin state.
     */
    public synchronized void reload(Path pluginsDir) {
        itemPluginsByType.clear();
        processingPluginsById.clear();
        pluginClassLoaders.clear();

        // Always load from application classpath first.
        loadFromClassLoader(Thread.currentThread().getContextClassLoader());

        // Then load from external JARs.
        if (pluginsDir == null) return;
        if (!Files.isDirectory(pluginsDir)) return;

        try {
            List<URL> jarUrls = new ArrayList<>();
            try (var stream = Files.list(pluginsDir)) {
                stream
                        .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                        .sorted()
                        .forEach(p -> {
                            try { jarUrls.add(p.toUri().toURL()); }
                            catch (Exception ignored) {}
                        });
            }

            if (jarUrls.isEmpty()) return;

            URLClassLoader ucl = new URLClassLoader(
                    jarUrls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader()
            );
            pluginClassLoaders.add(ucl);
            loadFromClassLoader(ucl);
        } catch (Exception e) {
            // Keep the app running even if plugins fail.
            System.err.println("[Warning] Plugin reload failed: " + e.getMessage());
        }
    }

    /**
     * Loads plugins from a single classloader and registers their contributions.
     */
    private void loadFromClassLoader(ClassLoader cl) {
        // Item-type plugins: register deserializers and UI forms.
        ServiceLoader<PluginDescriptor> itemLoader = ServiceLoader.load(PluginDescriptor.class, cl);
        for (PluginDescriptor plugin : itemLoader) {
            try {
                itemPluginsByType.put(plugin.getTypeName(), plugin);
                ItemRegistry.register(plugin.getTypeName(), plugin.getDeserializer());
                plugin.registerForm();
                System.out.println("[Plugin] Loaded item type: " + plugin.getTypeName());
            } catch (Exception e) {
                System.err.println("[Warning] Failed to load item plugin: " + e.getMessage());
            }
        }

        // Functional processing plugins.
        ServiceLoader<ProcessingPlugin> procLoader = ServiceLoader.load(ProcessingPlugin.class, cl);
        for (ProcessingPlugin plugin : procLoader) {
            try {
                processingPluginsById.put(plugin.getId(), plugin);
                System.out.println("[Plugin] Loaded processor: " + plugin.getDisplayName());
            } catch (Exception e) {
                System.err.println("[Warning] Failed to load processing plugin: " + e.getMessage());
            }
        }

        /**
         * Adapter pattern:
         * Load "legacy" processors (e.g., received from another student) and adapt them
         * to the current application's {@link ProcessingPlugin} SPI without changing
         * the legacy plugin code.
         */
        ServiceLoader<LegacyProcessor> legacyLoader = ServiceLoader.load(LegacyProcessor.class, cl);
        for (LegacyProcessor legacy : legacyLoader) {
            try {
                ProcessingPlugin adapted = new LegacyProcessorAdapter(legacy);
                processingPluginsById.put(adapted.getId(), adapted);
                System.out.println("[Plugin] Adapted legacy processor: " + adapted.getDisplayName());
            } catch (Exception e) {
                System.err.println("[Warning] Failed to adapt legacy processor: " + e.getMessage());
            }
        }
    }

    /** Returns item plugins keyed by type name. */
    public synchronized Map<String, PluginDescriptor> getItemPluginsByType() {
        return Collections.unmodifiableMap(itemPluginsByType);
    }

    /** Returns processing plugins keyed by stable plugin id. */
    public synchronized Map<String, ProcessingPlugin> getProcessingPluginsById() {
        return Collections.unmodifiableMap(processingPluginsById);
    }

    /**
     * Attempts to resolve a default plugins directory:
     * - next to the running JAR (preferred when running packaged)
     * - otherwise a "plugins" folder in the current working directory
     */
    public static Path defaultPluginsDir() {
        try {
            URL url = PluginManager.class.getProtectionDomain().getCodeSource().getLocation();
            File base = new File(url.toURI());
            File dir = base.isFile() ? base.getParentFile() : base;
            return dir.toPath().resolve("plugins");
        } catch (Exception ignored) {
            return Path.of("plugins");
        }
    }

    /**
     * Ensures the plugins directory exists (creates it if missing).
     */
    public static void ensurePluginsDir(Path dir) {
        try {
            if (dir != null) Files.createDirectories(dir);
        } catch (Exception ignored) {}
    }

    /**
     * Wraps a {@link LegacyProcessor} as a {@link ProcessingPlugin}.
     */
    private static final class LegacyProcessorAdapter implements ProcessingPlugin {
        private final LegacyProcessor legacy;

        private LegacyProcessorAdapter(LegacyProcessor legacy) {
            this.legacy = legacy;
        }

        @Override
        public String getId() {
            return "legacy." + legacy.getClass().getName();
        }

        @Override
        public String getDisplayName() {
            return "[Legacy] " + legacy.name();
        }

        @Override
        public EnumSet<ProcessingStage> supportedStages() {
            return EnumSet.of(ProcessingStage.AFTER_LOAD);
        }

        @Override
        public List<com.library.model.LibraryItem> process(List<com.library.model.LibraryItem> items, ProcessingStage stage) {
            return legacy.process(items);
        }
    }
}

