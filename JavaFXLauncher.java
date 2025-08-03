
import javafx.application.Application;

public class JavaFXLauncher {
    public static void main(String[] args) {
        // Set comprehensive JavaFX system properties for VNC compatibility
        System.setProperty("java.awt.headless", "false");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "true");
        System.setProperty("glass.platform", "gtk");
        System.setProperty("javafx.platform", "desktop");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("embed", "monocle");
        System.setProperty("monocle.screen.width", "1024");
        System.setProperty("monocle.screen.height", "768");
        System.setProperty("prism.allowhidpi", "false");
        System.setProperty("prism.vsync", "false");
        
        // Ensure DISPLAY is set
        if (System.getenv("DISPLAY") == null) {
            System.setProperty("DISPLAY", ":0");
        }
        
        try {
            // Initialize AWT to ensure display system is ready
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            System.out.println("Graphics environment initialized: " + ge.getClass().getName());
        } catch (Exception e) {
            System.out.println("Warning: Could not initialize graphics environment: " + e.getMessage());
        }
        
        System.out.println("Starting JavaFX application with VNC compatibility settings...");
        
        // Launch the JavaFX application
        Application.launch(Main.class, args);
    }
}
