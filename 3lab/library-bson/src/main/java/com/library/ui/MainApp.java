package com.library.ui;

import com.library.model.LibraryItem;
import com.library.plugin.PluginManager;
import com.library.plugin.ProcessingPlugin;
import com.library.plugin.ProcessingStage;
import com.library.serialization.BsonSerializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Main application window.
 * Provides: add / edit / delete items, serialize / deserialize to BSON file.
 */
public class MainApp extends JFrame {

    private final DefaultListModel<LibraryItem> listModel = new DefaultListModel<>();
    private final JList<LibraryItem>            itemList  = new JList<>(listModel);
    private final JTextArea                     detailArea = new JTextArea(6, 40);
    private final JLabel                        statusLabel = new JLabel(" Ready");
    private File                                currentFile = null;

    private final PluginManager pluginManager = new PluginManager();
    private final Path pluginsDir = PluginManager.defaultPluginsDir();

    /**
     * Enabled processor plugin ids for each stage.
     * Keeping settings in-memory is enough for the lab requirements.
     */
    private final Set<String> enabledBeforeSave = new TreeSet<>();
    private final Set<String> enabledAfterLoad  = new TreeSet<>();

    private JMenu settingsMenu;

    public MainApp() {
        super("Library Manager — BSON Serialization");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(780, 520));
        PluginManager.ensurePluginsDir(pluginsDir);
        reloadPlugins();
        buildUI();
        loadSampleData();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildUI() {
        // ── List panel ──
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LibraryItem li) {
                    setText(li.getDisplayLabel());
                    setFont(getFont().deriveFont(13f));
                }
                return this;
            }
        });
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });
        itemList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JScrollPane listScroll = new JScrollPane(itemList);
        listScroll.setPreferredSize(new Dimension(320, 0));
        listScroll.setBorder(BorderFactory.createTitledBorder("Library Items"));

        // ── Detail panel ──
        detailArea.setEditable(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Details"));

        // ── Button panel ──
        JButton addBtn    = button("Add",       e -> addItem());
        JButton editBtn   = button("Edit",      e -> editSelected());
        JButton deleteBtn = button("Delete",    e -> deleteSelected());
        JButton saveBtn   = button("Save BSON", e -> saveBson());
        JButton loadBtn   = button("Load BSON", e -> loadBson());

        JPanel btnPanel = new JPanel(new GridLayout(5, 1, 0, 6));
        btnPanel.setBorder(new EmptyBorder(0, 8, 0, 0));
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(new JSeparator());
        btnPanel.add(saveBtn);
        btnPanel.add(loadBtn);

        // ── Status bar ──
        statusLabel.setBorder(new EmptyBorder(2, 8, 2, 8));
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // ── Layout ──
        JPanel center = new JPanel(new BorderLayout(8, 0));
        center.setBorder(new EmptyBorder(10, 10, 10, 10));
        center.add(listScroll,  BorderLayout.WEST);
        center.add(detailScroll, BorderLayout.CENTER);
        center.add(btnPanel,    BorderLayout.EAST);

        getContentPane().add(center,    BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // ── Menu bar ──
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItem("Save BSON",    e -> saveBson()));
        fileMenu.add(menuItem("Load BSON",    e -> loadBson()));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Exit",         e -> System.exit(0)));
        JMenu itemMenu = new JMenu("Item");
        itemMenu.add(menuItem("Add",    e -> addItem()));
        itemMenu.add(menuItem("Edit",   e -> editSelected()));
        itemMenu.add(menuItem("Delete", e -> deleteSelected()));

        JMenu pluginsMenu = new JMenu("Plugins");
        pluginsMenu.add(menuItem("Reload from plugins folder", e -> {
            reloadPlugins();
            rebuildSettingsMenu();
            status("Plugins reloaded from: " + pluginsDir.toAbsolutePath());
        }));
        pluginsMenu.add(menuItem("Load plugin JAR…", e -> {
            loadPluginJarViaUi();
            rebuildSettingsMenu();
        }));

        settingsMenu = new JMenu("Settings");
        rebuildSettingsMenu();

        menuBar.add(fileMenu);
        menuBar.add(itemMenu);
        menuBar.add(pluginsMenu);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void addItem() {
        String[] types = ItemFormDialog.getTypeNames();
        String chosen = (String) JOptionPane.showInputDialog(
                this, "Select item type:", "Add Item",
                JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
        if (chosen == null) return;

        LibraryItem newItem = ItemFormDialog.createDefault(chosen);
        ItemFormDialog dlg = new ItemFormDialog(this, newItem);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            listModel.addElement(dlg.getItem());
            itemList.setSelectedValue(dlg.getItem(), true);
            status("Added: " + dlg.getItem().getDisplayLabel());
        }
    }

    private void editSelected() {
        LibraryItem item = itemList.getSelectedValue();
        if (item == null) { warn("Select an item first."); return; }

        ItemFormDialog dlg = new ItemFormDialog(this, item);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            // Force list repaint (model already holds the same object, now mutated)
            int idx = itemList.getSelectedIndex();
            listModel.set(idx, item);
            showDetails();
            status("Edited: " + item.getDisplayLabel());
        }
    }

    private void deleteSelected() {
        int idx = itemList.getSelectedIndex();
        if (idx < 0) { warn("Select an item first."); return; }
        LibraryItem item = listModel.get(idx);
        int res = JOptionPane.showConfirmDialog(this,
                "Delete \"" + item.getTitle() + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            listModel.remove(idx);
            detailArea.setText("");
            status("Deleted: " + item.getDisplayLabel());
        }
    }

    private void saveBson() {
        File file = chooseBsonFile(true);
        if (file == null) return;
        try {
            List<LibraryItem> items = new ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) items.add(listModel.get(i));

            // Apply processing plugins before saving (lab #5 requirement).
            items = applyProcessing(items, ProcessingStage.BEFORE_SAVE);
            BsonSerializer.serialize(items, file);
            currentFile = file;
            status("Saved " + items.size() + " items → " + file.getName());
        } catch (Exception ex) {
            error("Save failed: " + ex.getMessage());
        }
    }

    private void loadBson() {
        File file = chooseBsonFile(false);
        if (file == null) return;
        try {
            List<LibraryItem> items = BsonSerializer.deserialize(file);

            // Apply processing plugins after loading (lab #5 requirement).
            items = applyProcessing(items, ProcessingStage.AFTER_LOAD);
            listModel.clear();
            items.forEach(listModel::addElement);
            currentFile = file;
            detailArea.setText("");
            status("Loaded " + items.size() + " items from " + file.getName());
        } catch (Exception ex) {
            error("Load failed: " + ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showDetails() {
        LibraryItem item = itemList.getSelectedValue();
        if (item == null) { detailArea.setText(""); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Type        : ").append(item.getTypeName()).append("\n");
        sb.append("ID          : ").append(item.getId()).append("\n");
        sb.append("Title       : ").append(item.getTitle()).append("\n");
        sb.append("Year        : ").append(item.getYear()).append("\n");
        sb.append("Description : ").append(item.getDescription()).append("\n");
        sb.append("\nBSON preview:\n").append(item.toBson().toJson());
        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }

    private File chooseBsonFile(boolean save) {
        JFileChooser fc = new JFileChooser(currentFile != null ? currentFile.getParentFile() : null);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("BSON files (*.bson)", "bson"));
        int res = save ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return null;
        File f = fc.getSelectedFile();
        if (save && !f.getName().endsWith(".bson")) f = new File(f.getAbsolutePath() + ".bson");
        return f;
    }

    private void loadSampleData() {
        listModel.addElement(new com.library.model.Book(
                "b001", "Clean Code", 2008,
                "A handbook of agile software craftsmanship.",
                "Robert C. Martin", "978-0132350884", 431, "Programming"));
        listModel.addElement(new com.library.model.Magazine(
                "m001", "National Geographic", 2023,
                "Nature and science monthly.",
                "Nat Geo Partners", 542, "Science"));
        listModel.addElement(new com.library.model.Textbook(
                "t001", "Calculus: Early Transcendentals", 2020,
                "Standard university calculus textbook.",
                "James Stewart", "978-1285741550", 960, "Education",
                "Mathematics", 11));
        listModel.addElement(new com.library.model.ScientificArticle(
                "a001", "Attention Is All You Need", 2017,
                "Introduced the Transformer architecture.",
                "Vaswani et al.", "NeurIPS", "10.48550/arXiv.1706.03762", "AI"));
        listModel.addElement(new com.library.model.Newspaper(
                "n001", "The Guardian", 2024,
                "British daily newspaper.",
                "Guardian Media Group", "Daily", "United Kingdom"));
        listModel.addElement(new com.library.model.AudioBook(
                "ab001", "Sapiens", 2015,
                "A brief history of humankind.",
                "Yuval Noah Harari", "Derek Perkins", 915, "MP3"));
    }

    private void status(String msg) { statusLabel.setText(" " + msg); }
    private void warn(String msg)   { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void error(String msg)  { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }

    private JButton button(String label, ActionListener al) {
        JButton b = new JButton(label); b.addActionListener(al); return b;
    }
    private JMenuItem menuItem(String label, ActionListener al) {
        JMenuItem mi = new JMenuItem(label); mi.addActionListener(al); return mi;
    }


    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(MainApp::new);
    }

    // ── Plugins & settings ────────────────────────────────────────────────────

    /**
     * Reloads plugins and registers their contributions in registries and UI forms.
     */
    private void reloadPlugins() {
        pluginManager.reload(pluginsDir);
    }

    /**
     * Rebuilds Settings menu based on loaded processing plugins.
     * The menu is dynamic and depends on currently loaded plugins (lab #5 requirement).
     */
    private void rebuildSettingsMenu() {
        if (settingsMenu == null) return;
        settingsMenu.removeAll();

        Map<String, ProcessingPlugin> procs = pluginManager.getProcessingPluginsById();
        if (procs.isEmpty()) {
            JMenuItem empty = new JMenuItem("No processing plugins loaded");
            empty.setEnabled(false);
            settingsMenu.add(empty);
            settingsMenu.revalidate();
            settingsMenu.repaint();
            return;
        }

        JMenu beforeSave = new JMenu("Before save");
        JMenu afterLoad  = new JMenu("After load");

        for (ProcessingPlugin p : procs.values()) {
            EnumSet<ProcessingStage> stages = p.supportedStages();

            if (stages.contains(ProcessingStage.BEFORE_SAVE)) {
                JCheckBoxMenuItem cb = new JCheckBoxMenuItem(p.getDisplayName(), enabledBeforeSave.contains(p.getId()));
                cb.addActionListener(e -> toggle(enabledBeforeSave, p.getId(), cb.isSelected()));
                beforeSave.add(cb);
            }

            if (stages.contains(ProcessingStage.AFTER_LOAD)) {
                JCheckBoxMenuItem cb = new JCheckBoxMenuItem(p.getDisplayName(), enabledAfterLoad.contains(p.getId()));
                cb.addActionListener(e -> toggle(enabledAfterLoad, p.getId(), cb.isSelected()));
                afterLoad.add(cb);
            }
        }

        settingsMenu.add(beforeSave);
        settingsMenu.add(afterLoad);
        settingsMenu.revalidate();
        settingsMenu.repaint();
    }

    private static void toggle(Set<String> set, String id, boolean enabled) {
        if (enabled) set.add(id);
        else set.remove(id);
    }

    /**
     * Applies enabled processing plugins for a given stage in a stable order.
     */
    private List<LibraryItem> applyProcessing(List<LibraryItem> items, ProcessingStage stage) {
        Map<String, ProcessingPlugin> procs = pluginManager.getProcessingPluginsById();
        Set<String> enabled = (stage == ProcessingStage.BEFORE_SAVE) ? enabledBeforeSave : enabledAfterLoad;

        List<LibraryItem> out = items;
        for (String id : enabled) {
            ProcessingPlugin p = procs.get(id);
            if (p == null) continue; // plugin no longer loaded
            if (!p.supportedStages().contains(stage)) continue;
            out = p.process(out, stage);
        }
        return out;
    }

    /**
     * Lets the user pick a single plugin JAR and copies it into the plugins folder,
     * then reloads plugins.
     */
    private void loadPluginJarViaUi() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JAR plugins (*.jar)", "jar"));
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        try {
            File src = fc.getSelectedFile();
            Path dst = pluginsDir.resolve(src.getName());
            java.nio.file.Files.copy(src.toPath(), dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            reloadPlugins();
            status("Loaded plugin: " + src.getName());
        } catch (Exception ex) {
            error("Plugin load failed: " + ex.getMessage());
        }
    }
}
