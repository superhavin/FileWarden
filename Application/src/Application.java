import view.FileView;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.UIManager;

/**
 * Exists to instantiate the view and manages IO
 */
public class Application {
    // Name-constants to define the various dimensions
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 600;


    public static void main(String[] args) {
        try{
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize theme.");
        }

        FileView.createAndShowGUI(WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}