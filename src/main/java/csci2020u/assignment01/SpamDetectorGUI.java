package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class SpamDetectorGUI extends JFrame {

    private final JTable resultTable;
    private final JLabel accuracyLabel, precisionLabel, recallLabel, f1ScoreLabel;
    private final SpamDetector detector;
    private final JLabel statusBar;

    private int truePositives, falsePositives, trueNegatives, falseNegatives;

    public SpamDetectorGUI() {
        setTitle("Spam Detector");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Help");
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Spam Detector v1.0"));

        fileMenu.add(exitItem);
        helpMenu.add(aboutItem);
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        detector = new SpamDetector();  // Initialize SpamDetector

        // Create the "Select Directory" button
        JButton selectDirectoryButton = new JButton("Select Directory");
        selectDirectoryButton.addActionListener(e -> chooseDirectory());
        selectDirectoryButton.setToolTipText("Click to select the directory containing training and testing data");

        // Create labels for metrics
        accuracyLabel = new JLabel("Accuracy: N/A");
        precisionLabel = new JLabel("Precision: N/A");
        recallLabel = new JLabel("Recall: N/A");
        f1ScoreLabel = new JLabel("F1 Score: N/A");

        // Create the table and scroll pane
        resultTable = new JTable(new DefaultTableModel(new String[]{"Filename", "Predicted", "Actual", "Correct"}, 0));
        JScrollPane tableScrollPane = new JScrollPane(resultTable);

        resultTable.getColumn("Correct").setCellRenderer(new CorrectColumnRenderer());

        // Create the top panel with the button
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setOpaque(false); // Make the panel transparent
        topPanel.add(selectDirectoryButton);

        // Create the stats panel with labels
        JPanel statsPanel = new JPanel(new GridLayout(2, 2));
        statsPanel.setOpaque(false); // Make the panel transparent
        statsPanel.add(accuracyLabel);
        statsPanel.add(precisionLabel);
        statsPanel.add(recallLabel);
        statsPanel.add(f1ScoreLabel);

        // Create the background panel with gradient
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2D = (Graphics2D) g;
                Color col1 = new Color(173, 216, 204); // Light blue
                Color col2 = new Color(255, 255, 255); // White
                GradientPaint gradient = new GradientPaint(0, 0, col1, getWidth(), getHeight(), col2);
                g2D.setPaint(gradient);
                g2D.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());


        JPanel statsAndStatusPanel = new JPanel(new BorderLayout());
        statsAndStatusPanel.setOpaque(false); // Make the panel transparent

        // Add the stats panel to the first row
        statsAndStatusPanel.add(statsPanel, BorderLayout.NORTH);

        // Add the status bar to the second row
        statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEtchedBorder()); // Add a border for better visibility
        statusBar.setPreferredSize(new Dimension(getWidth(), 20)); // Set preferred height
        statsAndStatusPanel.add(statusBar, BorderLayout.SOUTH);


        // Add components to the background panel
        backgroundPanel.add(topPanel, BorderLayout.NORTH);
        backgroundPanel.add(tableScrollPane, BorderLayout.CENTER);
        backgroundPanel.add(statsAndStatusPanel, BorderLayout.SOUTH);


        // Add the background panel to the frame
        add(backgroundPanel, BorderLayout.CENTER);
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
            File trainHam = new File(mainDirectory, "train/ham");
            File trainHam2 = new File(mainDirectory, "train/ham2");
            File trainSpam = new File(mainDirectory, "train/spam");
            File testHam = new File(mainDirectory, "test/ham");
            File testSpam = new File(mainDirectory, "test/spam");

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

            statusBar.setText("Training and testing completed. Files processed: " + totalFiles);
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

            String correctnessSymbol = predictedClass.equals(actualClass) ? "✓" : "✗";
            model.addRow(new Object[]{file.getName(), predictedClass, actualClass, correctnessSymbol});
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

    private static class CorrectColumnRenderer extends DefaultTableCellRenderer{
        private final Icon tickIcon;
        private final Icon crossIcon;

        public CorrectColumnRenderer(){
            ImageIcon tick = new ImageIcon("src/main/resources/icons/greentick.png");
            ImageIcon cross = new ImageIcon("src/main/resources/icons/crossicon.png");

            int iconWidth =16;
            int iconHeight=16;

            tickIcon = new ImageIcon(tick.getImage().getScaledInstance(iconWidth,iconHeight,Image.SCALE_SMOOTH));
            crossIcon = new ImageIcon(cross.getImage().getScaledInstance(iconWidth,iconHeight,Image.SCALE_SMOOTH));

        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row , column);

            if (value!=null){
                String correctnessSymbol = value.toString();
                if(correctnessSymbol.equals("✓")){
                    label.setIcon(tickIcon);
                    label.setText("");
                } else if (correctnessSymbol.equals("✗")) {
                    label.setIcon(crossIcon);
                    label.setText("");// Clear text
                    
                }else{
                    label.setIcon(null);
                    label.setText("");
                }
            }else{
                label.setIcon(null);
                label.setText("");
            }
            label.setHorizontalAlignment(JLabel.CENTER);

            return label;
        }
    }


}



