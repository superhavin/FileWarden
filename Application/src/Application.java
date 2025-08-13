import controller.ChangeDirectoryController;
import controller.SQLController;
import controller.QueryController;
import model.FileDirectoryModel;
import model.FileMonitor;
import view.FileView;

import javax.swing.*;

public class Application {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Model
            FileDirectoryModel directoryModel = new FileDirectoryModel();

            // SQL controller
            SQLController sqlController = new SQLController("filewatcher.db");

            // FileMonitor (not started yet)
            FileMonitor fileMonitor = new FileMonitor(directoryModel);

            // Controllers
            ChangeDirectoryController changeDirController = new ChangeDirectoryController(fileMonitor, directoryModel);
            QueryController queryController = new QueryController(sqlController);

            // View
            FileView mainView = new FileView(directoryModel, fileMonitor, sqlController, changeDirController, queryController);

            mainView.setVisible(true);
        });
    }
}
