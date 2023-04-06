import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TagExtractorGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JTextArea textArea;
    private JButton extractButton;
    private JButton saveButton;
    private File selectedFile;
    private File stopWordsFile;
    private Map<String, Integer> tagMap;

    public TagExtractorGUI() {
        super("Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        JPanel buttonPanel = new JPanel();
        extractButton = new JButton("Extract Tags");
        extractButton.addActionListener(this);
        buttonPanel.add(extractButton);
        saveButton = new JButton("Save Tags");
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.NORTH);
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == extractButton) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
            fileChooser.setFileFilter(textFilter);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                tagMap = extractTags(selectedFile);
                displayTags(selectedFile.getName(), tagMap);
            }
        } else if (event.getSource() == saveButton) {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "No file selected to extract tags from.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
                fileChooser.setFileFilter(textFilter);
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File outputFile = fileChooser.getSelectedFile();
                    if (!outputFile.getName().endsWith(".txt")) {
                        outputFile = new File(outputFile.getAbsolutePath() + ".txt");
                    }
                    saveTags(outputFile, tagMap);
                    JOptionPane.showMessageDialog(this, "Tags saved to file: " + outputFile.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private Map<String, Integer> extractTags(File inputFile) {
        Map<String, Integer> tagMap = new HashMap<>();
        Set<String> stopWordsSet = loadStopWords(stopWordsFile);
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
                for (String word : words) {
                    if (!stopWordsSet.contains(word)) {
                        if (tagMap.containsKey(word)) {
                            tagMap.put(word, tagMap.get(word) +1);
                        } else {
                            tagMap.put(word, 1);
                        }
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading input file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return tagMap;
    }
    private Set<String> loadStopWords(File stopWordsFile) {
        Set<String> stopWordsSet = new HashSet<>();
        if (stopWordsFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(stopWordsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stopWordsSet.add(line.trim().toLowerCase());
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading stop words file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return stopWordsSet;
    }

    private void displayTags(String fileName, Map<String, Integer> tagMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tags extracted from file: ").append(fileName).append("\n\n");
        for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        textArea.setText(sb.toString());
    }

    private void saveTags(File outputFile, Map<String, Integer> tagMap) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error writing output file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        TagExtractorGUI gui = new TagExtractorGUI();
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
        fileChooser.setFileFilter(textFilter);
        int result = fileChooser.showOpenDialog(gui);
        if (result == JFileChooser.APPROVE_OPTION) {
            gui.selectedFile = fileChooser.getSelectedFile();
            fileChooser.setDialogTitle("Select Stop Words File");
            result = fileChooser.showOpenDialog(gui);
            if (result == JFileChooser.APPROVE_OPTION) {
                gui.stopWordsFile = fileChooser.getSelectedFile();
            }
            gui.tagMap = gui.extractTags(gui.selectedFile);
            gui.displayTags(gui.selectedFile.getName(), gui.tagMap);
        }
    }}

