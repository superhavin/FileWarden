package view;

import javax.swing.*;
import java.awt.*;

/**
 * Simple about/help dialog.
 *
 * @author Abdulrahman Hassan and Kevin Kamau
 */
public class AboutDialog extends JDialog {
    public AboutDialog(final Frame theOwner) {
        super(theOwner, "About FileWarden", true);
        JTextArea info = new JTextArea(
                "FileWatcher\n" +
                        "Version: 1.0\n" +
                        "Developer: Kevin Kamau and Abdulrahman Hassan\n\n" +
                        "Usage: Choose a directory, set file directory to watch, Start.\n" +
                        "Events will appear in a window; you can Save to DB and run queries.\n"
        );
        info.setEditable(false);
        info.setWrapStyleWord(true);
        info.setLineWrap(true);
        info.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(info, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> setVisible(false));
        JPanel p = new JPanel();
        p.add(ok);
        add(p, BorderLayout.SOUTH);
        setSize(400, 220);
        setLocationRelativeTo(theOwner);
    }
}