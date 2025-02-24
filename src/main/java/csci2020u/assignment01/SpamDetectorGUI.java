package csci2020u.assignment01;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class SpamDetectorGUI {
    public static void main(String[] args) {
        // TODO: Put your Java Swing components here and you will use SpamDetector.java here
        //Make JFrame
        JFrame frame = new JFrame("Spam Detector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER));
        frame.setSize(500, 500);


        // Create a button to trigger folder selection
        JButton selectFolderButton = new JButton("Select A Folder");
        selectFolderButton.setBounds(50, 50, 120, 50);


        // Add an action listener to handle the directory selection and training
        selectFolderButton.addActionListener(e -> {
            // Create a JFileChooser to ask the user to choose a directory
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.setCurrentDirectory(new File("."));

            // Show the file chooser dialog
            int returnValue = directoryChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                // Get the selected directory
                File mainDirectory = directoryChooser.getSelectedFile();
                System.out.println("Selected folder: " + mainDirectory.getAbsolutePath());

                try {
                    SpamDetector spamDetector = new SpamDetector();
                    spamDetector.parseTrainingData(new File(mainDirectory, "train/ham"), false);
                    spamDetector.parseTrainingData(new File(mainDirectory, "train/spam"), true);
                    spamDetector.parseTrainingData(new File(mainDirectory, "test/ham"), false);
                    spamDetector.parseTrainingData(new File(mainDirectory, "test/spam"), true);
                    spamDetector.calculateProbabilities();
                    System.out.println("Training complete. Probabilities calculated.");



                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });
        frame.add(selectFolderButton);

        frame.setVisible(true);
    }
}