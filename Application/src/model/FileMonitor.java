package model;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Watches a directory using WatchService. Filters by extensions set via setExtensionsToWatch.
 * Emits FileRecord entries into FileDirectoryModel.
 */
public class FileMonitor {
    private final FileDirectoryModel model;
    private WatchService watchService;
    private Thread watchThread;
    private Path currentDir;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Set<String> extensionsToWatch = Collections.synchronizedSet(new HashSet<>()); // store in lower-case without dot
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public FileMonitor(FileDirectoryModel model) {
        this.model = model;
        // default extensions
        setExtensionsToWatch(Arrays.asList("txt", "log", "csv"));
    }

    public void setExtensionsToWatch(Collection<String> exts) {
        extensionsToWatch.clear();
        for (String s : exts) {
            if (s == null) continue;
            s = s.trim().toLowerCase();
            if (s.startsWith(".")) s = s.substring(1);
            if (!s.isEmpty()) extensionsToWatch.add(s);
        }
        pcs.firePropertyChange("extensions", null, Collections.unmodifiableSet(extensionsToWatch));
    }

    public Set<String> getExtensionsToWatch() {
        return Collections.unmodifiableSet(extensionsToWatch);
    }

    public void start(Path dir) throws IOException {
        if (running.get()) throw new IllegalStateException("Already running");
        if (dir == null) throw new IllegalArgumentException("dir cannot be null");
        this.currentDir = dir;
        watchService = FileSystems.getDefault().newWatchService();
        dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        running.set(true);
        watchThread = new Thread(this::processEvents, "FileMonitorThread");
        watchThread.setDaemon(true);
        watchThread.start();
        pcs.firePropertyChange("running", false, true);
    }

    public void stop() {
        running.set(false);
        try {
            if (watchService != null) watchService.close();
        } catch (IOException ignored) {}
        if (watchThread != null) {
            try { watchThread.join(1000); } catch (InterruptedException ignored) {}
        }
        pcs.firePropertyChange("running", true, false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public Path getCurrentDir() {
        return currentDir;
    }

    private void processEvents() {
        while (running.get()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }
            for (WatchEvent<?> ev : key.pollEvents()) {
                WatchEvent.Kind<?> kind = ev.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) continue;
                WatchEvent<Path> we = (WatchEvent<Path>) ev;
                Path filename = we.context();
                Path fullPath = currentDir.resolve(filename);
                String name = filename.toString();
                String ext = "";
                int i = name.lastIndexOf('.');
                if (i > -1 && i < name.length()-1) {
                    ext = name.substring(i+1).toLowerCase();
                }
                boolean matched = extensionsToWatch.isEmpty() || extensionsToWatch.contains(ext);
                if (!matched) continue;
                String evt = "UNKNOWN";
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) evt = "CREATED";
                if (kind == StandardWatchEventKinds.ENTRY_DELETE) evt = "DELETED";
                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) evt = "MODIFIED";
                FileRecord fr = new FileRecord(name, ext, fullPath.toAbsolutePath().toString(), evt, LocalDateTime.now());
                model.addRecord(fr);
            }
            boolean valid = key.reset();
            if (!valid) break;
        }
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}