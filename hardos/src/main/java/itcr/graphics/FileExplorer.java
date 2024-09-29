package itcr.graphics;

import itcr.controllers.FileExplorerController;
import itcr.controllers.NotepadController;
import itcr.model.FileInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * FileExplorer class represents a custom floating window that simulates a file
 * explorer.
 * It displays a list of files and allows interaction with them.
 */
public class FileExplorer extends FloatingWindow<FileExplorerController> {
  private JPanel filePanel;
  private JFrame parent;
  private static final int BUTTONS_PER_ROW = 5;

  /**
   * Constructor for FileExplorer.
   * Initializes the file explorer window.
   *
   * @param parent     the parent frame
   * @param controller the controller to handle file operations
   */
  public FileExplorer(JFrame parent, FileExplorerController controller) {
    super(parent, "Explorador de Archivos", controller);
    setSize(800, 600);
    setResizable(false);
    setLocationRelativeTo(parent);
    controller.setFileExplorer(this);
  }

  /**
   * Initializes the components of the file explorer.
   */
  @Override
  protected void initComponents() {
    filePanel = new JPanel(new GridBagLayout());
    JScrollPane scrollPane = new JScrollPane(filePanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    add(scrollPane, BorderLayout.CENTER);

    updateFileList();

    filePanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showContextMenu(e);
        }
      }
    });
  }

  /**
   * Updates the file list displayed in the file explorer.
   */
  public void updateFileList() {
    filePanel.removeAll();
    List<FileInfo> files = controller.getFileList();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.NORTHWEST;

    for (int i = 0; i < files.size(); i++) {
      FileInfo file = files.get(i);
      JButton fileButton = createFileButton(file);

      gbc.gridx = i % BUTTONS_PER_ROW;
      gbc.gridy = i / BUTTONS_PER_ROW;

      filePanel.add(fileButton, gbc);
    }

    // Add a filler component to push everything up
    gbc.gridx = 0;
    gbc.gridy = (files.size() / BUTTONS_PER_ROW) + 1;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    filePanel.add(Box.createGlue(), gbc);

    filePanel.revalidate();
    filePanel.repaint();
  }

  /**
   * Creates a button representing a file.
   *
   * @param file the file information
   * @return the created JButton
   */
  private JButton createFileButton(FileInfo file) {
    JButton button = new JButton();
    button.setPreferredSize(new Dimension(120, 120));
    button.setLayout(new BorderLayout());

    // Load and scale the image
    ImageIcon icon = getScaledIcon("xp_filefolder_icon.png", 64, 64);
    JLabel iconLabel = new JLabel(icon);
    button.add(iconLabel, BorderLayout.CENTER);

    JLabel infoLabel = new JLabel(
        "<html><center>" + file.getFileName() + "<br>(" + file.getSize() + " bytes)</center></html>");
    infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
    button.add(infoLabel, BorderLayout.SOUTH);

    button.setContentAreaFilled(false);
    button.setBorderPainted(false);

    button.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showContextMenu(e, file.getFileName());
        }
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          String fileContent = controller.getFileContent(file.getFileName());
          openNotepad(file.getFileName(), fileContent);
        }
      }
    });

    return button;
  }

  /**
   * Opens a notepad window to edit the file content.
   *
   * @param fileName the name of the file
   * @param content  the content of the file
   */
  private void openNotepad(String fileName, String content) {
    NotepadController notepadController = new NotepadController(controller.getMemoryManager(), this::updateFileList);
    Notepad notepad = new Notepad(parent, notepadController, fileName, content);
    notepad.setVisible(true);
  }

  /**
   * Loads and scales an image icon.
   *
   * @param path   the path to the image file
   * @param width  the desired width
   * @param height the desired height
   * @return the scaled ImageIcon
   */
  private ImageIcon getScaledIcon(String path, int width, int height) {
    try {
      BufferedImage img = ImageIO.read(new File(path));
      Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
      return new ImageIcon(scaledImage);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Shows a context menu when right-clicking on the file panel.
   *
   * @param e the mouse event
   */
  private void showContextMenu(MouseEvent e) {
    JPopupMenu contextMenu = new JPopupMenu();
    JMenuItem newFileItem = new JMenuItem("New File");
    newFileItem.addActionListener(event -> openNotepad(null, null));
    contextMenu.add(newFileItem);
    contextMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  /**
   * Shows a context menu when right-clicking on a file button.
   *
   * @param e        the mouse event
   * @param fileName the name of the file
   */
  private void showContextMenu(MouseEvent e, String fileName) {
    JPopupMenu contextMenu = new JPopupMenu();
    JMenuItem deleteItem = new JMenuItem("Delete File");
    deleteItem.addActionListener(event -> controller.deleteFile(fileName));
    contextMenu.add(deleteItem);
    contextMenu.show(e.getComponent(), e.getX(), e.getY());
  }
}