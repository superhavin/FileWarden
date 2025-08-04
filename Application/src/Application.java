import controller.ChangeDirectoryController;
import model.FileMonitor;
import view.FileView;

import javax.swing.*;

import java.sql.SQLOutput;

import static view.FileView.createAndShowGUI;

/**
 * Exists to instantiate the view and manages IO
 */
public class Application {
    // Name-constants to define the various dimensions
    public static final int WINDOW_WIDTH = 650;
    public static final int WINDOW_HEIGHT = 950;


    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread to create and show this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI(WINDOW_WIDTH, WINDOW_HEIGHT);
            }
        });

    }
}