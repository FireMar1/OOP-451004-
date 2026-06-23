package com.library.ui;

import com.library.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A dialog that builds its form fields dynamically based on the item type.
 *
 * Base types are registered in the static initialiser below.
 * Plugin types are registered by the plugin itself, but plugin discovery and loading
 * is handled by {@code PluginManager}. This keeps UI code independent from how plugins
 * are loaded (classpath vs external JAR).
 *
 * No if-else / switch anywhere: pure data-driven dispatch.
 */
public class ItemFormDialog extends JDialog {

    // ── Functional interfaces ────────────────────────────────────────────────

    @FunctionalInterface
    public interface FormPopulator {
        void populate(LibraryItem item, Map<String, JTextField> fields);
    }

    @FunctionalInterface
    public interface FormApplier {
        void apply(LibraryItem item, Map<String, JTextField> fields) throws NumberFormatException;
    }

    @FunctionalInterface
    public interface ItemFactory {
        LibraryItem create();
    }

    public record FormDefinition(
            String[]       fieldNames,
            FormPopulator  populator,
            FormApplier    applier,
            ItemFactory    factory
    ) {}

    // ── Central form registry (type name → definition) ───────────────────────

    private static final Map<String, FormDefinition> FORM_REGISTRY = new LinkedHashMap<>();

    /**
     * Public API used by plugins (and the static initialiser below) to register a form.
     * Thread-safe enough for startup-time registration.
     */
    public static void registerForm(String typeName,
                                    String[] fieldNames,
                                    FormPopulator populator,
                                    FormApplier   applier,
                                    ItemFactory   factory) {
        FORM_REGISTRY.put(typeName, new FormDefinition(fieldNames, populator, applier, factory));
    }

    // ── Static initialiser — register built-in types ─────────────────────────

    static {

        // ── Book ──────────────────────────────────────────────────────────────
        registerForm("Book",
                new String[]{"ID", "Title", "Year", "Description", "Author", "ISBN", "Pages", "Genre"},
                (item, f) -> {
                    Book b = (Book) item;
                    f.get("ID").setText(b.getId());
                    f.get("Title").setText(b.getTitle());
                    f.get("Year").setText(String.valueOf(b.getYear()));
                    f.get("Description").setText(b.getDescription());
                    f.get("Author").setText(b.getAuthor());
                    f.get("ISBN").setText(b.getIsbn());
                    f.get("Pages").setText(String.valueOf(b.getPageCount()));
                    f.get("Genre").setText(b.getGenre());
                },
                (item, f) -> {
                    Book b = (Book) item;
                    b.setId(f.get("ID").getText());
                    b.setTitle(f.get("Title").getText());
                    b.setYear(Integer.parseInt(f.get("Year").getText()));
                    b.setDescription(f.get("Description").getText());
                    b.setAuthor(f.get("Author").getText());
                    b.setIsbn(f.get("ISBN").getText());
                    b.setPageCount(Integer.parseInt(f.get("Pages").getText()));
                    b.setGenre(f.get("Genre").getText());
                },
                () -> new Book(UUID.randomUUID().toString().substring(0, 8),
                        "New Book", 2024, "", "Author", "000-0", 0, "Fiction")
        );

        // ── Magazine ──────────────────────────────────────────────────────────
        registerForm("Magazine",
                new String[]{"ID", "Title", "Year", "Description", "Publisher", "Issue Number", "Category"},
                (item, f) -> {
                    Magazine m = (Magazine) item;
                    f.get("ID").setText(m.getId());
                    f.get("Title").setText(m.getTitle());
                    f.get("Year").setText(String.valueOf(m.getYear()));
                    f.get("Description").setText(m.getDescription());
                    f.get("Publisher").setText(m.getPublisher());
                    f.get("Issue Number").setText(String.valueOf(m.getIssueNumber()));
                    f.get("Category").setText(m.getCategory());
                },
                (item, f) -> {
                    Magazine m = (Magazine) item;
                    m.setId(f.get("ID").getText());
                    m.setTitle(f.get("Title").getText());
                    m.setYear(Integer.parseInt(f.get("Year").getText()));
                    m.setDescription(f.get("Description").getText());
                    m.setPublisher(f.get("Publisher").getText());
                    m.setIssueNumber(Integer.parseInt(f.get("Issue Number").getText()));
                    m.setCategory(f.get("Category").getText());
                },
                () -> new Magazine(UUID.randomUUID().toString().substring(0, 8),
                        "New Magazine", 2024, "", "Publisher", 1, "General")
        );

        // ── Newspaper ─────────────────────────────────────────────────────────
        registerForm("Newspaper",
                new String[]{"ID", "Title", "Year", "Description", "Publisher", "Frequency", "Region"},
                (item, f) -> {
                    Newspaper n = (Newspaper) item;
                    f.get("ID").setText(n.getId());
                    f.get("Title").setText(n.getTitle());
                    f.get("Year").setText(String.valueOf(n.getYear()));
                    f.get("Description").setText(n.getDescription());
                    f.get("Publisher").setText(n.getPublisher());
                    f.get("Frequency").setText(n.getFrequency());
                    f.get("Region").setText(n.getRegion());
                },
                (item, f) -> {
                    Newspaper n = (Newspaper) item;
                    n.setId(f.get("ID").getText());
                    n.setTitle(f.get("Title").getText());
                    n.setYear(Integer.parseInt(f.get("Year").getText()));
                    n.setDescription(f.get("Description").getText());
                    n.setPublisher(f.get("Publisher").getText());
                    n.setFrequency(f.get("Frequency").getText());
                    n.setRegion(f.get("Region").getText());
                },
                () -> new Newspaper(UUID.randomUUID().toString().substring(0, 8),
                        "New Newspaper", 2024, "", "Publisher", "Daily", "Region")
        );

        // ── ScientificArticle ─────────────────────────────────────────────────
        registerForm("ScientificArticle",
                new String[]{"ID", "Title", "Year", "Description", "Author", "Journal", "DOI", "Field"},
                (item, f) -> {
                    ScientificArticle a = (ScientificArticle) item;
                    f.get("ID").setText(a.getId());
                    f.get("Title").setText(a.getTitle());
                    f.get("Year").setText(String.valueOf(a.getYear()));
                    f.get("Description").setText(a.getDescription());
                    f.get("Author").setText(a.getAuthor());
                    f.get("Journal").setText(a.getJournal());
                    f.get("DOI").setText(a.getDoi());
                    f.get("Field").setText(a.getField());
                },
                (item, f) -> {
                    ScientificArticle a = (ScientificArticle) item;
                    a.setId(f.get("ID").getText());
                    a.setTitle(f.get("Title").getText());
                    a.setYear(Integer.parseInt(f.get("Year").getText()));
                    a.setDescription(f.get("Description").getText());
                    a.setAuthor(f.get("Author").getText());
                    a.setJournal(f.get("Journal").getText());
                    a.setDoi(f.get("DOI").getText());
                    a.setField(f.get("Field").getText());
                },
                () -> new ScientificArticle(UUID.randomUUID().toString().substring(0, 8),
                        "New Article", 2024, "", "Author", "Journal", "10.0000/x", "Science")
        );

        // ── Textbook ──────────────────────────────────────────────────────────
        registerForm("Textbook",
                new String[]{"ID", "Title", "Year", "Description", "Author", "ISBN", "Pages", "Genre", "Subject", "Grade Level"},
                (item, f) -> {
                    Textbook t = (Textbook) item;
                    f.get("ID").setText(t.getId());
                    f.get("Title").setText(t.getTitle());
                    f.get("Year").setText(String.valueOf(t.getYear()));
                    f.get("Description").setText(t.getDescription());
                    f.get("Author").setText(t.getAuthor());
                    f.get("ISBN").setText(t.getIsbn());
                    f.get("Pages").setText(String.valueOf(t.getPageCount()));
                    f.get("Genre").setText(t.getGenre());
                    f.get("Subject").setText(t.getSubject());
                    f.get("Grade Level").setText(String.valueOf(t.getGradeLevel()));
                },
                (item, f) -> {
                    Textbook t = (Textbook) item;
                    t.setId(f.get("ID").getText());
                    t.setTitle(f.get("Title").getText());
                    t.setYear(Integer.parseInt(f.get("Year").getText()));
                    t.setDescription(f.get("Description").getText());
                    t.setAuthor(f.get("Author").getText());
                    t.setIsbn(f.get("ISBN").getText());
                    t.setPageCount(Integer.parseInt(f.get("Pages").getText()));
                    t.setGenre(f.get("Genre").getText());
                    t.setSubject(f.get("Subject").getText());
                    t.setGradeLevel(Integer.parseInt(f.get("Grade Level").getText()));
                },
                () -> new Textbook(UUID.randomUUID().toString().substring(0, 8),
                        "New Textbook", 2024, "", "Author", "000-0", 0, "Education", "Mathematics", 1)
        );

        // ── AudioBook ─────────────────────────────────────────────────────────
        registerForm("AudioBook",
                new String[]{"ID", "Title", "Year", "Description", "Author", "Narrator", "Duration (min)", "Format"},
                (item, f) -> {
                    AudioBook ab = (AudioBook) item;
                    f.get("ID").setText(ab.getId());
                    f.get("Title").setText(ab.getTitle());
                    f.get("Year").setText(String.valueOf(ab.getYear()));
                    f.get("Description").setText(ab.getDescription());
                    f.get("Author").setText(ab.getAuthor());
                    f.get("Narrator").setText(ab.getNarrator());
                    f.get("Duration (min)").setText(String.valueOf(ab.getDurationMinutes()));
                    f.get("Format").setText(ab.getFormat());
                },
                (item, f) -> {
                    AudioBook ab = (AudioBook) item;
                    ab.setId(f.get("ID").getText());
                    ab.setTitle(f.get("Title").getText());
                    ab.setYear(Integer.parseInt(f.get("Year").getText()));
                    ab.setDescription(f.get("Description").getText());
                    ab.setAuthor(f.get("Author").getText());
                    ab.setNarrator(f.get("Narrator").getText());
                    ab.setDurationMinutes(Integer.parseInt(f.get("Duration (min)").getText()));
                    ab.setFormat(f.get("Format").getText());
                },
                () -> new AudioBook(UUID.randomUUID().toString().substring(0, 8),
                        "New AudioBook", 2024, "", "Author", "Narrator", 60, "MP3")
        );

    }

    // ── Static helpers ────────────────────────────────────────────────────────

    public static String[] getTypeNames() {
        return FORM_REGISTRY.keySet().toArray(new String[0]);
    }

    public static LibraryItem createDefault(String typeName) {
        FormDefinition def = FORM_REGISTRY.get(typeName);
        if (def == null) throw new IllegalArgumentException("No form registered for type: " + typeName);
        return def.factory().create();
    }

    // ── Instance ──────────────────────────────────────────────────────────────

    private final Map<String, JTextField> fields = new LinkedHashMap<>();
    private LibraryItem item;
    private boolean confirmed = false;

    /** Open dialog for editing an existing item. */
    public ItemFormDialog(Frame parent, LibraryItem item) {
        super(parent, "Edit: " + item.getTypeName(), true);
        this.item = item;
        FormDefinition def = FORM_REGISTRY.get(item.getTypeName());
        if (def == null) throw new IllegalArgumentException("No form for type: " + item.getTypeName());
        buildUI(def);
        def.populator().populate(item, fields);
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI(FormDefinition def) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(12, 12, 4, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < def.fieldNames().length; i++) {
            String name = def.fieldNames()[i];
            JTextField tf = new JTextField(24);
            fields.put(name, tf);

            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            formPanel.add(new JLabel(name + ":"), gc);
            gc.gridx = 1; gc.weightx = 1;
            formPanel.add(tf, gc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok     = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        btnPanel.add(ok);
        btnPanel.add(cancel);

        ok.addActionListener(e -> {
            try {
                def.applier().apply(item, fields);
                confirmed = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for numeric fields.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dispose());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(formPanel, BorderLayout.CENTER);
        getContentPane().add(btnPanel,  BorderLayout.SOUTH);
    }

    public boolean isConfirmed() { return confirmed; }
    public LibraryItem getItem() { return item; }
}