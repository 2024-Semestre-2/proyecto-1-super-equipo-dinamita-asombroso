package itcr.controllers;

import itcr.graphics.OSSystem;
import itcr.models.MemoryManager;

public class OSSystemController {
  private OSSystem osSystem;
  private MemoryManager memoryManager;

  public OSSystemController(OSSystem osSystem) {
    this.osSystem = osSystem;
  }

  public void switchToLoginScreen() {
    osSystem.showLoginScreen();
  }

  public void switchToDesktopScreen() {
    osSystem.showDesktopScreen();
  }

  public MemoryManager getMemoryManager() {
    return memoryManager;
  }
}