package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.*;

public class SpamDetectorGUI extends Component {
    // Instance variables for the Spam Detector, GUI Frame, Table, and Accuracy Label
    private final SpamDetector detector;
    private final JFrame frame;
    private final JTable table;
    private final JLabel status;
    private int truePositives=0;
    private int falsePositives=0;
    private int falseNegatives=0;
    private int trueNegatives=0;

    private final ImageIcon Tick = resizeIcon(new ImageIcon(getClass().getResource("/icons/greentick.png")), 16, 16);
    private final ImageIcon Cross = resizeIcon(new ImageIcon(getClass().getResource("/icons/crossicon.png")), 16, 16);


    // Constructor to initialize the GUI
    public SpamDetectorGUI() {
        detector = new SpamDetector();
        frame = new JFrame("Spam Detector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JMenuBar menu = new JMenuBar();
        JMenu help = new JMenu("Help");
        JMenu filem = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this, "Spam Detector v1.0"));

        filem.add(exit);
        help.add(about);
        menu.add(filem);
        menu.add(help);
        frame.setJMenuBar(menu);

        // Create a panel with a BorderLayout
        JPanel panel = new JPanel(new BorderLayout());

        // Button to select the directory for training data
        JButton selectDirButton = new JButton("Select Training Directory");
        selectDirButton.addActionListener(e -> selectDirectory());

        // Create a table to display file classification results
        table = new JTable(new DefaultTableModel(new String[]{"File Name", "Actual Class", "Predicted Class", "Spam Probability","Correct"}, 0)) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int col){
                if(col== 4){
                    return new ImageRenderer();
                }
                return super.getCellRenderer(row, col);

            }
        };


        JScrollPane scrollPane = new JScrollPane(table);

        // Label to display accuracy
        JPanel statusP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        status = new JLabel("Ready");
        statusP.add(status);

        // Add components to the panel
        panel.add(selectDirButton, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(statusP, BorderLayout.SOUTH);

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

        
        truePositives=0;
        falsePositives=0;
        falseNegatives=0;
        trueNegatives=0;

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

                boolean isRight = predictedClass.equalsIgnoreCase(actualClass);
                ImageIcon icon = isRight ? Tick : Cross;

                // Add results to the table
                model.addRow(new Object[]{file.getName(), actualClass, predictedClass, String.format("%.5f", prob), icon});

                // Count correct predictions
                if(predictedClass.equals("Spam") && actualClass.equals("spam")){
                    truePositives++;
                    
                } else if (predictedClass.equals("Spam") && actualClass.equals("ham")) {
                    falsePositives++;
                    
                }else if (predictedClass.equals("Ham") && actualClass.equals("spam")){
                    falseNegatives++;
                } else if (predictedClass.equals("Ham") && actualClass.equals("ham")) {
                    trueNegatives++;
                }

            }
        }

        double precision = (double) truePositives/(truePositives + falsePositives);
        double recall =(double) truePositives/(truePositives + falseNegatives);
        double f1Score = 2 * (precision * recall)/ (precision + recall);
        double accuracy = (double) (truePositives + trueNegatives)/ (truePositives+ trueNegatives + falsePositives+ falseNegatives);

        // Update accuracy label
        status.setText(String.format("Status: Accuracy = %.2f%%, Precision = %.2f%%, Recall = %.2f%%, F1 Score = %.2f%%", accuracy * 100, precision * 100, recall * 100, f1Score * 100));

    }
    private ImageIcon resizeIcon(ImageIcon icon , int width, int height ){
        Image pic = icon.getImage();
        Image resizedpic = pic.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        return new ImageIcon(resizedpic);
    }

    private class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col){
            if(value instanceof ImageIcon) {
                setIcon((ImageIcon) value);
                setText("");
            }else{
                setIcon(null);
                setText( value != null ? value.toString() : "");

            }
            return this;

        }
    }

    // Main method to run the GUI application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpamDetectorGUI::new);
    }
}
