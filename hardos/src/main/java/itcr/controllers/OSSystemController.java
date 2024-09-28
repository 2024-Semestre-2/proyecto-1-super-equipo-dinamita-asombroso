package itcr.controllers;

import itcr.graphics.OSSystem;
import itcr.model.MemoryManager;

/**
 * The OSSystemController class manages the transitions between different screens
 * in the operating system, such as the login screen and the desktop screen.
 */
public class OSSystemController {
  private final OSSystem osSystem;
  private MemoryManager memoryManager;

  /**
   * Constructs an OSSystemController with the specified OSSystem.
   *
   * @param osSystem the OSSystem instance
   */
  public OSSystemController(OSSystem osSystem) {
    this.osSystem = osSystem;
  }

  /**
   * Switches the display to the login screen.
   */
  public void switchToLoginScreen() {
    osSystem.showLoginScreen();
  }

  /**
   * Switches the display to the desktop screen.
   */
  public void switchToDesktopScreen() {
    osSystem.showDesktopScreen();
  }

  /**
   * Retrieves the MemoryManager instance.
   *
   * @return the MemoryManager instance
   */
  public MemoryManager getMemoryManager() {
    return memoryManager;
  }

  /**
   * Sets the MemoryManager instance.
   *
   * @param memoryManager the MemoryManager instance to set
   */
  public void setMemoryManager(MemoryManager memoryManager) {
    this.memoryManager = memoryManager;
  }
}