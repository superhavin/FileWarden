package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model storing in-memory list of FileRecord entries and notifying listeners on updates.
 */
public class FileDirectoryModel {
    public static final String PROP_RECORDS = "records";

    private final List<FileRecord> records;
    private final PropertyChangeSupport pcs;

    public FileDirectoryModel() {
        this.records = new ArrayList<>();
        this.pcs = new PropertyChangeSupport(this);
    }

    public synchronized void addRecord(FileRecord r) {
        List<FileRecord> old = new ArrayList<>(records);
        records.add(r);
        pcs.firePropertyChange(PROP_RECORDS, old, Collections.unmodifiableList(new ArrayList<>(records)));
    }

    public synchronized void clearRecords() {
        List<FileRecord> old = new ArrayList<>(records);
        records.clear();
        pcs.firePropertyChange(PROP_RECORDS, old, Collections.unmodifiableList(new ArrayList<>(records)));
    }

    public synchronized List<FileRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}