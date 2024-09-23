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

public class FileExplorer extends FloatingWindow<FileExplorerController> {
  private JPanel filePanel;
  private JFrame parent;

  public FileExplorer(JFrame parent, FileExplorerController controller) {
    super(parent, "Explorador de Archivos", controller);
    setSize(800, 600);
    setResizable(false);
    setLocationRelativeTo(parent);
    controller.setFileExplorer(this);
  }

  @Override
  protected void initComponents() {
    filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
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

  public void updateFileList() {
    filePanel.removeAll();
    List<FileInfo> files = controller.getFileList();

    for (FileInfo file : files) {
      JButton fileButton = createFileButton(file);
      filePanel.add(fileButton);
    }

    filePanel.revalidate();
    filePanel.repaint();
  }

  private JButton createFileButton(FileInfo file) {
    JButton button = new JButton();
    button.setPreferredSize(new Dimension(120, 120));
    button.setLayout(new BorderLayout());

    // Cargar y escalar la imagen
    ImageIcon icon = getScaledIcon("xp_filefolder_icon.png", 64, 64);
    JLabel iconLabel = new JLabel(icon);
    button.add(iconLabel, BorderLayout.CENTER);

    JLabel infoLabel = new JLabel(file.getFileName() + " (" + file.getSize() + " bytes)");
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

  private void openNotepad(String fileName, String content) {
    NotepadController notepadController = new NotepadController(controller.getMemoryManager(), this::updateFileList);
    Notepad notepad = new Notepad(parent, notepadController,  fileName, content);
    notepad.setVisible(true);
  }

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

  private void showContextMenu(MouseEvent e) {
    JPopupMenu contextMenu = new JPopupMenu();
    JMenuItem newFileItem = new JMenuItem("New File");
    newFileItem.addActionListener(event -> openNotepad(null, null));
    contextMenu.add(newFileItem);
    contextMenu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void showContextMenu(MouseEvent e, String fileName) {
    JPopupMenu contextMenu = new JPopupMenu();
    JMenuItem deleteItem = new JMenuItem("Delete File");
    deleteItem.addActionListener(event -> controller.deleteFile(fileName));
    contextMenu.add(deleteItem);
    contextMenu.show(e.getComponent(), e.getX(), e.getY());
  }
}
