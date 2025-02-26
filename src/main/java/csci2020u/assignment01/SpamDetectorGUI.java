package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

<<<<<<< HEAD


=======
>>>>>>> 7abd53f97d140ec880a31ef8d069177fd05e9513
public class SpamDetectorGUI extends JFrame {

    private final JTable resultTable;
    private final JLabel accuracyLabel, precisionLabel, recallLabel, f1ScoreLabel;
    private final SpamDetector detector;
<<<<<<< HEAD
    private final JLabel statusBar;
=======
>>>>>>> 7abd53f97d140ec880a31ef8d069177fd05e9513

    private int truePositives, falsePositives, trueNegatives, falseNegatives;

    public SpamDetectorGUI() {
        setTitle("Spam Detector");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

<<<<<<< HEAD
        JMenuBar menuBar = new JMenuBar(); // Create menu bar
        JMenu helpMenu = new JMenu("Help"); //Create the "Help" menu
        JMenu fileMenu = new JMenu("File"); // Create "File" menu
        JMenuItem exitItem = new JMenuItem("Exit"); // Create the "Exit " menu item
        exitItem.addActionListener(e -> System.exit(0)); // Close the program when clicked
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e-> JOptionPane.showMessageDialog(this,"Spam Detector v1.0"));


        fileMenu.add(exitItem); // Add the "Exit " item to the "File" menu
        helpMenu.add(aboutItem); // Add the "About" item to the "Help" menu
        menuBar.add(fileMenu); // Add "File" menu to the menuBar
        menuBar.add(helpMenu); // Add "Help"  menu to the menuBar
        setJMenuBar(menuBar);// Set th menu bar for the frame

=======
>>>>>>> 7abd53f97d140ec880a31ef8d069177fd05e9513
        detector = new SpamDetector();  // Initialize SpamDetector

        JButton selectDirectoryButton = new JButton("Select Directory");
        selectDirectoryButton.addActionListener(e -> chooseDirectory());
<<<<<<< HEAD
        selectDirectoryButton.setToolTipText("Click to select the directory containing training and testing data");
=======
>>>>>>> 7abd53f97d140ec880a31ef8d069177fd05e9513

        accuracyLabel = new JLabel("Accuracy: N/A");
        precisionLabel = new JLabel("Precision: N/A");
        recallLabel = new JLabel("Recall: N/A");
        f1ScoreLabel = new JLabel("F1 Score: N/A");

        resultTable = new JTable(new DefaultTableModel(new String[]{"Filename", "Predicted", "Actual"}, 0));
        JScrollPane tableScrollPane = new JScrollPane(resultTable);

        JPanel topPanel = new JPanel(new FlowLayout());
<<<<<<< HEAD
        topPanel.setOpaque(false); // Make Panel transparent
        topPanel.add(selectDirectoryButton);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2));
        statsPanel.setOpaque(false); // Make panel transperent
=======
        topPanel.add(selectDirectoryButton);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2));
>>>>>>> 7abd53f97d140ec880a31ef8d069177fd05e9513
        statsPanel.add(accuracyLabel);
        statsPanel.add(precisionLabel);
        statsPanel.add(recallLabel);
        statsPanel.add(f1ScoreLabel);
<<<<<<< HEAD
        JPanel backgroundPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2D = (Graphics2D) g;
                Color col1 = new Color(173,216,204); // Light Blue
                Color col2 = new Color(255,255,255); // White
                GradientPaint gradient = new GradientPaint(0,0,col1,getWidth(),getHeight(),col2);
                g2D.setPaint(gradient);
                g2D.fillRect(0,0,getWidth(), getHeight());



            }

        };
        backgroundPanel.setLayout(new BorderLayout());




        backgroundPanel.add(topPanel, BorderLayout.NORTH);
        backgroundPanel.add(tableScrollPane, BorderLayout.CENTER);
        backgroundPanel.add(statsPanel, BorderLayout.SOUTH);
        // Create status bar
        statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEtchedBorder()); // Add a border for better visibility
        backgroundPanel.add(statusBar, BorderLayout.SOUTH);

        add(backgroundPanel , BorderLayout.CENTER);


=======

        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);
    }

    private void chooseDirectory() {
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.setCurrentDirectory(new File("."));

        if (directoryChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            runTrainingAndTesting(directoryChooser.getSelectedFile());
        }
    }

    private void runTrainingAndTesting(File mainDirectory) {
        try {
            File trainHam = new File(mainDirectory, "main/resources/data/train/ham");
            File trainHam2 = new File(mainDirectory, "main/resources/data/train/ham2");
            File trainSpam = new File(mainDirectory, "main/resources/data/train/spam");
            File testHam = new File(mainDirectory, "main/resources/data/test/ham");
            File testSpam = new File(mainDirectory, "main/resources/data/test/spam");

            if ((!trainHam.exists() && !trainHam2.exists()) || !trainSpam.exists() || !testHam.exists() || !testSpam.exists()) {
                JOptionPane.showMessageDialog(this, "Ensure 'train/ham', 'train/ham2', 'train/spam', 'test/ham', and 'test/spam' folders exist.");
                return;
            }

            resetMetrics();

            if (trainHam.exists()) detector.parseTrainingData(trainHam, false);
            if (trainHam2.exists()) detector.parseTrainingData(trainHam2, false);
            detector.parseTrainingData(trainSpam, true);
            detector.calculateProbabilities();
            System.out.println("Total training files processed: " + detector.getNumFilesProcessed());


            DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
            model.setRowCount(0);  // Clear previous results

            int totalFiles = processTestFiles(testSpam, "spam", model) + processTestFiles(testHam, "ham", model);

            calculateAndDisplayMetrics(totalFiles);

            JOptionPane.showMessageDialog(this, "Training and testing completed.\nFiles processed: " + totalFiles);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private int processTestFiles(File testFolder, String actualClass, DefaultTableModel model) throws IOException {
        int filesProcessed = 0;

        for (File file : testFolder.listFiles()) {
            if (!file.isFile()) continue;

            Scanner scanner = new Scanner(file);
            StringBuilder emailContent = new StringBuilder();
            while (scanner.hasNext()) emailContent.append(scanner.next().toLowerCase()).append(" ");
            scanner.close();

            String[] words = emailContent.toString().trim().split("\\W+");
            String predictedClass = detector.classifyEmail(words) > 0.5 ? "spam" : "ham";

            if (predictedClass.equals("spam") && actualClass.equals("spam")) truePositives++;
            else if (predictedClass.equals("spam") && actualClass.equals("ham")) falsePositives++;
            else if (predictedClass.equals("ham") && actualClass.equals("ham")) trueNegatives++;
            else if (predictedClass.equals("ham") && actualClass.equals("spam")) falseNegatives++;

            model.addRow(new Object[]{file.getName(), predictedClass, actualClass});
            filesProcessed++;
        }

        return filesProcessed;
    }

    private void calculateAndDisplayMetrics(int totalFiles) {
        double accuracy = (double) (truePositives + trueNegatives) / totalFiles;
        double precision = (truePositives + falsePositives) > 0 ? (double) truePositives / (truePositives + falsePositives) : 0;
        double recall = (truePositives + falseNegatives) > 0 ? (double) truePositives / (truePositives + falseNegatives) : 0;
        double f1Score = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0;

        accuracyLabel.setText("Accuracy: " + String.format("%.2f", accuracy * 100) + "%");
        precisionLabel.setText("Precision: " + String.format("%.2f", precision * 100) + "%");
        recallLabel.setText("Recall: " + String.format("%.2f", recall * 100) + "%");
        f1ScoreLabel.setText("F1 Score: " + String.format("%.2f", f1Score * 100) + "%");
    }

    private void resetMetrics() {
        truePositives = falsePositives = trueNegatives = falseNegatives = 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpamDetectorGUI().setVisible(true));
>>>>>>> 7abd53f97d140ec880a31ef8d069177fd05e9513
    }

    private void chooseDirectory() {
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.setCurrentDirectory(new File("."));

        if (directoryChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            runTrainingAndTesting(directoryChooser.getSelectedFile());
        }
    }

    private void runTrainingAndTesting(File mainDirectory) {

        try

            {


                File trainHam = new File(mainDirectory, "src/main/resources/data/train/ham");
                File trainHam2 = new File(mainDirectory, "src/main/resources/data/train/ham2");
                File trainSpam = new File(mainDirectory, "src/main/resources/data/train/spam");
                File testHam = new File(mainDirectory, "src/main/resources/data/test/ham");
                File testSpam = new File(mainDirectory, "src/main/resources/data/test/spam");

                if ((!trainHam.exists() && !trainHam2.exists()) || !trainSpam.exists() || !testHam.exists() || !testSpam.exists()) {
                    JOptionPane.showMessageDialog(this, "Ensure 'train/ham', 'train/ham2', 'train/spam', 'test/ham', and 'test/spam' folders exist.");
                    return;
                }

                resetMetrics();

                if (trainHam.exists()) detector.parseTrainingData(trainHam, false);
                if (trainHam2.exists()) detector.parseTrainingData(trainHam2, false);
                detector.parseTrainingData(trainSpam, true);
                detector.calculateProbabilities();
                System.out.println("Total training files processed: " + detector.getNumFilesProcessed());




                DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
                model.setRowCount(0);  // Clear previous results

                int totalFiles = processTestFiles(testSpam, "spam", model) + processTestFiles(testHam, "ham", model);

                calculateAndDisplayMetrics(totalFiles);

                statusBar.setText("Training and testing completed. Files processed:" + totalFiles);

                JOptionPane.showMessageDialog(this, "Training and testing completed.\nFiles processed: " + totalFiles);

            } catch(
            IOException e)

            {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }

        }



    private int processTestFiles(File testFolder, String actualClass, DefaultTableModel model) throws IOException {
        int filesProcessed = 0;

        for (File file : testFolder.listFiles()) {
            if (!file.isFile()) continue;

            Scanner scanner = new Scanner(file);
            StringBuilder emailContent = new StringBuilder();
            while (scanner.hasNext()) emailContent.append(scanner.next().toLowerCase()).append(" ");
            scanner.close();

            String[] words = emailContent.toString().trim().split("\\W+");
            String predictedClass = detector.classifyEmail(words) > 0.5 ? "spam" : "ham";

            if (predictedClass.equals("spam") && actualClass.equals("spam")) truePositives++;
            else if (predictedClass.equals("spam") && actualClass.equals("ham")) falsePositives++;
            else if (predictedClass.equals("ham") && actualClass.equals("ham")) trueNegatives++;
            else if (predictedClass.equals("ham") && actualClass.equals("spam")) falseNegatives++;

            model.addRow(new Object[]{file.getName(), predictedClass, actualClass});
            filesProcessed++;
        }

        return filesProcessed;
    }

    private void calculateAndDisplayMetrics(int totalFiles) {
        double accuracy = (double) (truePositives + trueNegatives) / totalFiles;
        double precision = (truePositives + falsePositives) > 0 ? (double) truePositives / (truePositives + falsePositives) : 0;
        double recall = (truePositives + falseNegatives) > 0 ? (double) truePositives / (truePositives + falseNegatives) : 0;
        double f1Score = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0;

        accuracyLabel.setText("Accuracy: " + String.format("%.2f", accuracy * 100) + "%");
        precisionLabel.setText("Precision: " + String.format("%.2f", precision * 100) + "%");
        recallLabel.setText("Recall: " + String.format("%.2f", recall * 100) + "%");
        f1ScoreLabel.setText("F1 Score: " + String.format("%.2f", f1Score * 100) + "%");
    }

    private void resetMetrics() {
        truePositives = falsePositives = trueNegatives = falseNegatives = 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpamDetectorGUI().setVisible(true));
    }


}