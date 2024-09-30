package itcr.graphics;

import itcr.model.FileInfo;
import itcr.model.Scheduler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileLoaderDialog class represents a dialog for loading files.
 * It allows users to select files and preview their content.
 */
public class FileLoaderDialog extends JDialog {
  private final Scheduler scheduler;
  private final List<JCheckBox> fileCheckboxes;
  private JTextArea previewArea;
  private List<String> selectedFiles;

  /**
   * Constructor for FileLoaderDialog.
   * Initializes the dialog with the given parent frame and scheduler.
   *
   * @param parent    the parent frame
   * @param scheduler the scheduler to manage file operations
   */
  public FileLoaderDialog(JFrame parent, Scheduler scheduler) {
    super(parent, "Load Files", true);
    this.scheduler = scheduler;
    this.fileCheckboxes = new ArrayList<>();
    this.selectedFiles = new ArrayList<>();

    setSize(600, 400);
    setLocationRelativeTo(parent);
    initComponents();
  }

  /**
   * Initializes the components of the dialog.
   */
  private void initComponents() {
    setLayout(new BorderLayout());

    // File list panel
    JPanel fileListPanel = new JPanel();
    fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));
    JScrollPane fileListScrollPane = new JScrollPane(fileListPanel);

    List<FileInfo> fileList = scheduler.memoryManager.getFileList();
    for (FileInfo file : fileList) {
      JCheckBox checkBox = new JCheckBox(file.getFileName());
      checkBox.addActionListener(e -> updateSelectedFiles());
      fileCheckboxes.add(checkBox);

      JPanel filePanel = new JPanel(new BorderLayout());
      filePanel.add(checkBox, BorderLayout.WEST);
      filePanel.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
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

    // "Select All" button
    JButton selectAllButton = new JButton("Select All");
    selectAllButton.addActionListener(e -> toggleSelectAllFiles());

    JButton okButton = new JButton("OK");
    okButton.addActionListener(e -> {
      updateSelectedFiles();
      dispose();
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonsPanel.add(selectAllButton);
    buttonsPanel.add(okButton);
    buttonsPanel.add(cancelButton);
    add(buttonsPanel, BorderLayout.SOUTH);
  }

  /**
   * Displays the preview of the selected file.
   *
   * @param fileName the name of the file to preview
   */
  private void showFilePreview(String fileName) {
    String content = scheduler.memoryManager.getFile(fileName);
    previewArea.setText(content);
  }

  /**
   * Updates the list of selected files based on the checkboxes.
   */
  private void updateSelectedFiles() {
    selectedFiles.clear();
    for (JCheckBox checkBox : fileCheckboxes) {
      if (checkBox.isSelected()) {
        selectedFiles.add(checkBox.getText());
      }
    }
  }

  /**
   * Toggles the selection of all files.
   */
  private void toggleSelectAllFiles() {
    boolean selectAll = fileCheckboxes.stream().anyMatch(cb -> !cb.isSelected());
    for (JCheckBox checkBox : fileCheckboxes) {
      checkBox.setSelected(selectAll);
    }
    updateSelectedFiles();
  }

  /**
   * Returns the list of selected files.
   *
   * @return the list of selected files
   */
  public List<String> getSelectedFiles() {
    return selectedFiles;
  }
}