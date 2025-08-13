package controller;

import model.FileDirectoryModel;
import model.FileMonitor;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;

/**
 * Handles directory choice and starting/stopping monitoring.
 */
public class ChangeDirectoryController {
    private final FileMonitor monitor;
    private final FileDirectoryModel model;

    public ChangeDirectoryController(FileMonitor monitor, FileDirectoryModel model) {
        this.monitor = monitor;
        this.model = model;
    }

    public Path chooseDirectory(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(parent);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            return f.toPath();
        }
        return null;
    }

    public void startMonitoring(Path dir) throws Exception {
        if (dir == null) throw new IllegalArgumentException("Directory cannot be null");
        monitor.start(dir);
    }

    public void stopMonitoring() {
        monitor.stop();
    }
}