package view;

import javax.swing.*;
import java.awt.*;

/**
 * Simple about/help dialog.
 */
public class AboutDialog extends JDialog {
    public AboutDialog(Frame owner) {
        super(owner, "About FileWatcher", true);
        JTextArea info = new JTextArea(
                "FileWatcher\n" +
                        "Version: 1.0\n" +
                        "Developer: Abdulrahman Hassan and Kevin Kamau\n\n" +
                        "Usage: Choose a directory, set file extensions to watch, Start.\n" +
                        "Events will appear in the table; you can Save to DB and run queries.\n"
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
        setLocationRelativeTo(owner);
    }
}