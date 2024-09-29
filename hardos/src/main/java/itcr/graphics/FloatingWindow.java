package itcr.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * FloatingWindow is an abstract class that represents a custom JDialog window.
 * It provides a template for creating floating windows with a specified
 * controller.
 *
 * @param <T> the type of the controller
 */
public abstract class FloatingWindow<T> extends JDialog {
  protected T controller;

  /**
   * Constructor for FloatingWindow.
   * Initializes the dialog with the given parent frame, title, and controller.
   *
   * @param parent     the parent frame
   * @param title      the title of the dialog
   * @param controller the controller to manage the dialog's operations
   */
  public FloatingWindow(JFrame parent, String title, T controller) {
    super(parent, title, true);
    this.controller = controller;
    setResizable(false);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());
    initComponents();
    pack();
    setLocationRelativeTo(parent);
  }

  /**
   * Abstract method to initialize the components of the dialog.
   * Subclasses must implement this method to set up their specific components.
   */
  protected abstract void initComponents();
}