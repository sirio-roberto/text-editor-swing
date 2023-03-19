package editor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

    final String PROJECT_RESOURCE = "hidden_directory";

    JTextField searchField;

    private JTextArea textArea;

    private JPanel mainPanel;

    private JFileChooser fileChooser;

    private JCheckBox useRegExCheckbox;

    Matcher matcher;

    int searchIndex;
    int currentArrayIndex;

    private final ArrayList<IndexAndLength> foundIndexesAndLengths = new ArrayList<>();

    public TextEditor() {
        super("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setResizable(false);

        addMenuBar();
        addMainPanel();

        setVisible(true);
    }

    private void addMenuBar() {
        JMenuBar mainMenuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        fileMenu.setFont(fileMenu.getFont().deriveFont(14f));

        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        searchMenu.setFont(searchMenu.getFont().deriveFont(14f));

        JMenuItem openMenu = new JMenuItem("Open");
        openMenu.setName("MenuOpen");
        JMenuItem saveMenu = new JMenuItem("Save");
        saveMenu.setName("MenuSave");
        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.setName("MenuExit");

        JMenuItem startSearchMenu = new JMenuItem("Start search");
        startSearchMenu.setName("MenuStartSearch");
        JMenuItem previousSearchMenu = new JMenuItem("Previous search");
        previousSearchMenu.setName("MenuPreviousMatch");
        JMenuItem nextMatchMenu = new JMenuItem("Next match");
        nextMatchMenu.setName("MenuNextMatch");
        JMenuItem useRegexMenu = new JMenuItem("Use regular expressions");
        useRegexMenu.setName("MenuUseRegExp");

        fileMenu.add(openMenu);
        fileMenu.add(saveMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitMenu);

        searchMenu.add(startSearchMenu);
        searchMenu.add(previousSearchMenu);
        searchMenu.add(nextMatchMenu);
        searchMenu.add(useRegexMenu);

        mainMenuBar.add(fileMenu);
        mainMenuBar.add(searchMenu);
        setJMenuBar(mainMenuBar);

        openMenu.addActionListener(openActionListener);
        saveMenu.addActionListener(saveActionListener);
        exitMenu.addActionListener(l -> System.exit(0));

        startSearchMenu.addActionListener(searchActionListener);
        previousSearchMenu.addActionListener(previousActionListener);
        nextMatchMenu.addActionListener(nextActionListener);
        useRegexMenu.addActionListener(l -> useRegExCheckbox.setSelected(!useRegExCheckbox.isSelected()));
    }

    private void addMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());

        addFileMenu();
        addTextArea();

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addFileMenu() {
        fileChooser = new JFileChooser();
        fileChooser.setName("FileChooser");
        add(fileChooser);

        searchField = new JTextField();
        searchField.setName("SearchField");
        searchField.setPreferredSize(new Dimension(getWidth() - 410, 30));

        JButton saveButton = new JButton(new ImageIcon(PROJECT_RESOURCE + "save.png"));
        saveButton.setName("SaveButton");

        JButton openButton = new JButton(new ImageIcon(PROJECT_RESOURCE + "load.png"));
        openButton.setName("OpenButton");

        JButton searchButton = new JButton(new ImageIcon(PROJECT_RESOURCE + "search.png"));
        searchButton.setName("StartSearchButton");

        JButton previousButton = new JButton(new ImageIcon(PROJECT_RESOURCE + "previous.png"));
        previousButton.setName("PreviousMatchButton");

        JButton nextButton = new JButton(new ImageIcon(PROJECT_RESOURCE + "next.png"));
        nextButton.setName("NextMatchButton");

        useRegExCheckbox = new JCheckBox();
        useRegExCheckbox.setText("Use regex");
        useRegExCheckbox.setName("UseRegExCheckbox");

        JPanel fileMenuPanel = new JPanel();

        fileMenuPanel.add(openButton);
        fileMenuPanel.add(saveButton);
        fileMenuPanel.add(searchField);
        fileMenuPanel.add(searchButton);
        fileMenuPanel.add(previousButton);
        fileMenuPanel.add(nextButton);
        fileMenuPanel.add(useRegExCheckbox);

        mainPanel.add(fileMenuPanel, BorderLayout.NORTH);

        saveButton.addActionListener(saveActionListener);
        openButton.addActionListener(openActionListener);

        searchButton.addActionListener(searchActionListener);
        nextButton.addActionListener(nextActionListener);
        previousButton.addActionListener(previousActionListener);
    }

    private final ActionListener previousActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            currentArrayIndex -= 1;
            if (currentArrayIndex < 0) {
                currentArrayIndex += foundIndexesAndLengths.size();
            }

            highlightFoundText();
        }
    };

    private final ActionListener nextActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            currentArrayIndex += 1;
            if (currentArrayIndex > foundIndexesAndLengths.size() - 1) {
                currentArrayIndex = 0;
            }

            highlightFoundText();
        }
    };

    private void highlightFoundText() {
        if (foundIndexesAndLengths.size() > 0) {
            int startTextIndex = foundIndexesAndLengths.get(currentArrayIndex).getStart();
            int currentTextLength = foundIndexesAndLengths.get(currentArrayIndex).getLength();
            textArea.setCaretPosition(startTextIndex + currentTextLength);
            textArea.select(startTextIndex, startTextIndex + currentTextLength);
            textArea.grabFocus();
        }
    }

    private final ActionListener searchActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            searchIndex = 0;
            foundIndexesAndLengths.clear();
            if (useRegExCheckbox.isSelected()) {
                fillArrayUsingRegex();
            } else {
                fillArray();
            }
            currentArrayIndex = 0;
            highlightFoundText();
        }
    };

    private void fillArray() {
        while (true) {
            searchIndex = textArea.getText().indexOf(searchField.getText(), searchIndex);
            if (searchIndex != -1) {
                IndexAndLength indexAndLength = new IndexAndLength(searchIndex, searchField.getText().length());
                foundIndexesAndLengths.add(indexAndLength);
            } else {
                break;
            }
            searchIndex++;
        }
    }

    private void fillArrayUsingRegex() {
        String searchTerm = searchField.getText();
        if (!searchTerm.isBlank()) {
            Pattern pattern = Pattern.compile(searchTerm);
            matcher = pattern.matcher(textArea.getText());
            while (matcher.find()) {
                String foundText = matcher.group();
                IndexAndLength indexAndLength = new IndexAndLength(matcher.start(), foundText.length());
                foundIndexesAndLengths.add(indexAndLength);
            }
        }
    }

    private final ActionListener saveActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int returnedValue = fileChooser.showSaveDialog(getParent());
            if (returnedValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                saveTextToFile(selectedFile);
            }
        }
    };

    private final ActionListener openActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int returnedValue = fileChooser.showOpenDialog(getParent());
            if (returnedValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                textArea.setText(getTextFromFile(selectedFile));
            }
        }
    };

    private void saveTextToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (char c: textArea.getText().toCharArray()) {
                writer.write(c);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String getTextFromFile(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return new String(fileContent);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return "";
    }

    private void addTextArea() {
        textArea = new JTextArea();
        textArea.setName("TextArea");

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }
}
