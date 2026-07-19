package ie.nci.smartwaste.gui;

import javax.swing.*;

public class SmartWasteGui {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();

            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (
                ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException exception
        ) {
            System.err.println(
                    "Could not apply the system look and feel: "
                            + exception.getMessage()
            );
        }
    }
}