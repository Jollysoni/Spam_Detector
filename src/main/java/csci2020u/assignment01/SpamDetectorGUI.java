package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class SpamDetectorGUI extends Component {
    private final SpamDetector detector;
    private final JFrame frame;
    private final JTable table;
    private final JLabel status;
    private int truePositives=0;
    private int falsePositives=0;
    private int falseNegatives=0;
    private int trueNegatives=0;
    private File testDirectory;

    // Icons to indicate correct and incorrect classifications
    private final ImageIcon Tick = resizeIcon(new ImageIcon(getClass().getResource("/icons/greentick.png")), 16, 16);
    private final ImageIcon Cross = resizeIcon(new ImageIcon(getClass().getResource("/icons/crossicon.png")), 16, 16);

    public SpamDetectorGUI() {
        detector = new SpamDetector();
        frame = new JFrame("Spam Detector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);


        // Setting up menu bar
        JMenuBar menu = new JMenuBar();
        JMenu help = new JMenu("Help");
        JMenu filem = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this, "Spam Detector v1.0"));

        // Main panel setup
        filem.add(exit);
        help.add(about);
        menu.add(filem);
        menu.add(help);
        frame.setJMenuBar(menu);

        JPanel panel = new JPanel(new BorderLayout());
        JButton selectDirButton = new JButton("Select Training Directory");
        selectDirButton.addActionListener(e -> selectDirectory());

        // Table to display classification results
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
        JPanel statusP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        status = new JLabel("Ready");
        statusP.add(status);

        panel.add(selectDirButton, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(statusP, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        // Adding click event to display email content when row is clicked
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    String fileName = (String) table.getValueAt(row, 0);
                    String actualClass = (String) table.getValueAt(row, 1);
                    openEmailContent(fileName, actualClass);
                }
            }
        });
    }
    // Method to resize an icon to fit the UI
    private ImageIcon resizeIcon(ImageIcon icon , int width, int height ){
        Image pic = icon.getImage();
        Image resizedpic = pic.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedpic);
    }
    // Custom renderer to display images in the table
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
    // Method to open and display email content in a popup
    private void openEmailContent(String fileName, String actualClass) {
        if (testDirectory == null) {
            JOptionPane.showMessageDialog(frame, "Please select a directory first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File emailFile = new File(testDirectory, actualClass + "/" + fileName);
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(emailFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            content.append("Error reading file.");
        }

        JTextArea textArea = new JTextArea(content.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(frame, scrollPane, "Email Content: " + fileName, JOptionPane.INFORMATION_MESSAGE);
    }
    // Method to allow user to select training and testing directory
    private void selectDirectory() {
        JFileChooser chooser = new JFileChooser((File) null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File mainDir = chooser.getSelectedFile();
            detector.train(new File(mainDir, "train"));
            testDirectory = new File(mainDir, "test");
            testSpamDetection(testDirectory);
        }
    }
    // Method to test spam detector on test dataset
    private void testSpamDetection(File testDir) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        truePositives=0;
        falsePositives=0;
        falseNegatives=0;
        trueNegatives=0;

        for (File category : new File[]{new File(testDir, "spam"), new File(testDir, "ham")}) {
            String actualClass = category.getName();
            for (File file : category.listFiles()) {
                double prob = detector.classify(file);
                String predictedClass = prob >= 0.6 ? "Spam" : (prob <= 0.4 ? "Ham" : "Uncertain");
                boolean isRight = predictedClass.equalsIgnoreCase(actualClass);
                ImageIcon icon = isRight ? Tick : Cross;
                model.addRow(new Object[]{file.getName(), actualClass, predictedClass, String.format("%.5f", prob), icon});

                // Update confusion matrix counts
                if (predictedClass.equals("Spam") && actualClass.equals("spam")) {
                    truePositives++;
                } else if (predictedClass.equals("Spam") && actualClass.equals("ham")) {
                    falsePositives++;
                } else if (predictedClass.equals("Ham") && actualClass.equals("spam")) {
                    falseNegatives++;
                } else if (predictedClass.equals("Ham") && actualClass.equals("ham")) {
                    trueNegatives++;
                }
            }
        }

        // Safe calculations to prevent division by zero
        double precision = (truePositives + falsePositives) > 0 ? (double) truePositives / (truePositives + falsePositives) : 0;
        double recall = (truePositives + falseNegatives) > 0 ? (double) truePositives / (truePositives + falseNegatives) : 0;
        double f1Score = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0;
        double accuracy = (truePositives + trueNegatives + falsePositives + falseNegatives) > 0 ?
                (double) (truePositives + trueNegatives) / (truePositives + trueNegatives + falsePositives + falseNegatives)
                : 0;

        // Update status label with the calculated values
        status.setText(String.format("Status: Accuracy = %.2f%%, Precision = %.2f%%, Recall = %.2f%%, F1 Score = %.2f%%",
                accuracy * 100, precision * 100, recall * 100, f1Score * 100));
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpamDetectorGUI::new);
    }
}