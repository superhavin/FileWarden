package model;

import view.FileView;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * The model of the Application.
 * <p>
 * Manages the application's monitored directories and their associated monitors.
 * Provides property change notifications to the FileView so the view
 * can update when directories are added, removed, or when application state changes.
 * <p>
 * Implements a singleton pattern so that only one instance of this model exists.
 *
 * @author Abdulrahman Hassan and Kevin Kamau
 */
public class FileDirectoryModel {
    /**
     * PropertyChangeSupport for notifying the view of changes.
     */
    private final PropertyChangeSupport viewChanges = new PropertyChangeSupport(this);

    /**
     * The default directory in cases of resetting the application.
     */
    private static final String HOME_DIRECTORY = returnDefaultDirectory();

    /**
     * Mutable stack-like collection that stores the current file directories being monitored.
     */
    private final Deque<String> myFileDirectories;

    /**
     * The active monitors for the chosen directories, mapped by directory path.
     */
    private final Map<String, FileMonitor> myMonitors;

    /**
     * Boolean indicating the current active status of the application.
     */
    private boolean isActive = false;

    /**
     * The singleton instance of this model.
     */
    private static FileDirectoryModel myInstance = null;

    /**
     * Constructs a FileDirectoryModel with empty directory and monitor collections,
     * then resets the application state.
     */
    public FileDirectoryModel() {
        myMonitors = new HashMap<>();
        myFileDirectories = new ArrayDeque<>();
        resetApp();
    }

    /**
     * Returns the current singleton instance of the FileDirectoryModel,
     * creating it if necessary.
     *
     * @return the singleton instance of the FileDirectoryModel
     */
    public static FileDirectoryModel getInstance() {
        if (myInstance == null) {
            myInstance = new FileDirectoryModel();
        }
        return myInstance;
    }

    /**
     * Starts the application by setting it to active and notifying the view.
     */
    public void startApp() {
        boolean oldActive = isActive;
        isActive = true;
        viewChanges.firePropertyChange(FileView.ACTIVE, oldActive, isActive);
    }

    /**
     * Resets the application.
     * <p>
     * Clears all file directories and fires a change to the view to reset its properties.
     * This also resets windows and their associated monitors.
     */
    public void resetApp() {
        if (!myFileDirectories.isEmpty()) {
            clearFileDirectory();
        }

        boolean oldActive = isActive;
        isActive = false;
        viewChanges.firePropertyChange(FileView.ACTIVE, oldActive, isActive);
    }

    /**
     * Helper method to add a new directory to the directory collection.
     * Notifies the view with the new directory.
     *
     * @param theDirectory the new directory
     */
    private void addDirectory(final String theDirectory) {
        String lastDirectory = myFileDirectories.isEmpty() ? HOME_DIRECTORY : myFileDirectories.getLast();
        myFileDirectories.add(theDirectory);

        final String NEW_DIRECTORY = myFileDirectories.getLast();
        viewChanges.firePropertyChange(FileView.CHANGE_DIRECTORY, lastDirectory, NEW_DIRECTORY);
    }

    /**
     * Helper method to clear the directory collection.
     * Notifies the view with the default home directory.
     */
    private void clearFileDirectory() {
        final String OLD_DIRECTORY = myFileDirectories.peekLast();
        myFileDirectories.clear();
        viewChanges.firePropertyChange(FileView.CHANGE_DIRECTORY, OLD_DIRECTORY, HOME_DIRECTORY);
    }

    /**
     * Starts monitoring a directory and associates it with a view window.
     *
     * @param theDirectory the directory being monitored
     * @param theWindows   the directory's window property change listener
     */
    public void startMonitoring(final String theDirectory, final PropertyChangeListener theWindows) {
        if (!myFileDirectories.contains(theDirectory)) {
            addDirectory(theDirectory);

            FileMonitor theMonitor = new FileMonitor(theDirectory);
            theMonitor.addPropertyChangeListener(theWindows);
            myMonitors.put(theDirectory, theMonitor);
        }
    }

    /**
     * Stops monitoring the given directory, removes its monitor, and notifies the view.
     *
     * @param theDirectory the directory to stop monitoring
     */
    public void stopMonitoring(String theDirectory) {
        FileMonitor theMonitor = myMonitors.remove(theDirectory);
        myFileDirectories.remove(theDirectory);
        if (theMonitor != null) {
            theMonitor.stopMonitoring();
            viewChanges.firePropertyChange("stopDirectory", theDirectory, null);
        }
    }

    /**
     * Returns whether the application is currently active.
     *
     * @return true if the application is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the string representing the system's home directory.
     *
     * @return the home directory path
     */
    public static String getHomeDirectory() {
        return HOME_DIRECTORY;
    }

    /**
     * Checks if the given directory is currently contained in the directory collection.
     *
     * @param theDirectory the directory to check
     * @return true if the directory is being monitored, false otherwise
     */
    public boolean containsDirectory(final String theDirectory) {
        return myFileDirectories.contains(theDirectory);
    }

    /**
     * Returns the default home directory of the system depending on the operating system.
     *
     * @return the default home directory path
     */
    private static String returnDefaultDirectory() {
        try {
            String aHomeDirectory = System.getProperty("user.home");
            if (aHomeDirectory != null && !aHomeDirectory.isBlank()) {
                return aHomeDirectory;
            }

            String aOs = System.getProperty("os.name").toLowerCase();
            if (aOs.contains("win")) {
                return "C:\\";
            } else if (aOs.contains("mac")) {
                return "/Users";
            } else if (aOs.contains("nix") || aOs.contains("nux")) {
                return "/home";
            } else {
                return "/";
            }
        } catch (Exception theException) {
            return "/";
        }
    }

    /**
     * Adds a property change listener for this model.
     *
     * @param theListener the property change listener to add
     */
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        viewChanges.addPropertyChangeListener(theListener);
    }

    /**
     * Removes a property change listener from this model.
     *
     * @param theListener the property change listener to remove
     */
    public void removePropertyChangeListener(final PropertyChangeListener theListener) {
        viewChanges.removePropertyChangeListener(theListener);
    }
}

