package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class SpamDetectorGUI {
    // Instance variables for the Spam Detector, GUI Frame, Table, and Accuracy Label
    private final SpamDetector detector;
    private final JFrame frame;
    private final JTable table;
    private final JLabel accuracyLabel;

    // Constructor to initialize the GUI
    public SpamDetectorGUI() {
        detector = new SpamDetector();
        frame = new JFrame("Spam Detector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Create a panel with a BorderLayout
        JPanel panel = new JPanel(new BorderLayout());

        // Button to select the directory for training data
        JButton selectDirButton = new JButton("Select Training Directory");
        selectDirButton.addActionListener(e -> selectDirectory());

        // Create a table to display file classification results
        table = new JTable(new DefaultTableModel(new String[]{"File Name", "Actual Class", "Predicted Class", "Spam Probability"}, 0));
        JScrollPane scrollPane = new JScrollPane(table);

        // Label to display accuracy
        accuracyLabel = new JLabel("Accuracy: ");

        // Add components to the panel
        panel.add(selectDirButton, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(accuracyLabel, BorderLayout.SOUTH);

        // Add panel to the frame and make it visible
        frame.add(panel);
        frame.setVisible(true);
    }

    // Method to select the directory containing training and testing data
    private void selectDirectory() {
        JFileChooser chooser = new JFileChooser((File) null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File mainDir = chooser.getSelectedFile();
            detector.train(new File(mainDir, "train")); // Train the spam detector
            testSpamDetection(new File(mainDir, "test")); // Test the model on test data
        }
    }

    // Method to test the spam detector on test data
    private void testSpamDetection(File testDir) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        int correct = 0, total = 0;

        // Iterate over both spam and ham categories in the test dataset
        for (File category : new File[]{new File(testDir, "spam"), new File(testDir, "ham")}) {
            String actualClass = category.getName();

            // Iterate through each file in the category
            for (File file : category.listFiles()) {
                double prob = detector.classify(file);
                String predictedClass;

                // Define thresholds for classification
                if (prob >= 0.6) {
                    predictedClass = "Spam";
                } else if (prob <= 0.4) {
                    predictedClass = "Ham";
                } else {
                    predictedClass = "Uncertain";
                }

                // Add results to the table
                model.addRow(new Object[]{file.getName(), actualClass, predictedClass, String.format("%.5f", prob)});

                // Count correct predictions
                if ((predictedClass.equals("Spam") && actualClass.equals("spam")) ||
                        (predictedClass.equals("Ham") && actualClass.equals("ham"))) {
                    correct++;
                }
                total++;
            }
        }

        // Update accuracy label
        accuracyLabel.setText("Accuracy: " + String.format("%.5f", (double) correct / total));
    }

    // Main method to run the GUI application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpamDetectorGUI::new);
    }
}
