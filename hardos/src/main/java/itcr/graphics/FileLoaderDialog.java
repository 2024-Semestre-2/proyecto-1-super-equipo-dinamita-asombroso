package itcr.graphics;

import itcr.model.FileInfo;
import itcr.model.Scheduler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FileLoaderDialog extends JDialog {
  private final Scheduler scheduler;
  private final List<JCheckBox> fileCheckboxes;
  private JTextArea previewArea;
  private List<String> selectedFiles;

  public FileLoaderDialog(JFrame parent, Scheduler scheduler) {
    super(parent, "Load Files", true);
    this.scheduler = scheduler;
    this.fileCheckboxes = new ArrayList<>();
    this.selectedFiles = new ArrayList<>();

    setSize(600, 400);
    setLocationRelativeTo(parent);
    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    // File list panel
    JPanel fileListPanel = new JPanel();
    fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));
    JScrollPane fileListScrollPane = new JScrollPane(fileListPanel);

    List<FileInfo> fileList = scheduler.getMemoryManager().getFileList();
    for (FileInfo file : fileList) {
      JCheckBox checkBox = new JCheckBox(file.getFileName());
      checkBox.addActionListener(e -> updateSelectedFiles());
      fileCheckboxes.add(checkBox);

      JPanel filePanel = new JPanel(new BorderLayout());
      filePanel.add(checkBox, BorderLayout.WEST);
      filePanel.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
          showFilePreview(file.getFileName());
        }
      });

      fileListPanel.add(filePanel);
    }

    // Preview panel
    previewArea = new JTextArea();
    previewArea.setEditable(false);
    JScrollPane previewScrollPane = new JScrollPane(previewArea);

    // Split pane
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileListScrollPane, previewScrollPane);
    splitPane.setDividerLocation(300);
    add(splitPane, BorderLayout.CENTER);

    // Buttons panel
    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("OK");
    okButton.addActionListener(e -> {
      updateSelectedFiles();
      dispose();
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonsPanel.add(okButton);
    buttonsPanel.add(cancelButton);
    add(buttonsPanel, BorderLayout.SOUTH);
  }

  private void showFilePreview(String fileName) {
    String content = scheduler.getFileContent(fileName);
    previewArea.setText(content);
  }

  private void updateSelectedFiles() {
    selectedFiles.clear();
    for (JCheckBox checkBox : fileCheckboxes) {
      if (checkBox.isSelected()) {
        selectedFiles.add(checkBox.getText());
      }
    }
  }

  public List<String> getSelectedFiles() {
    return selectedFiles;
  }
}