
public class JavaFXLauncher {
    public static void main(String[] args) {
        // Set JavaFX system properties for VNC compatibility
        System.setProperty("java.awt.headless", "false");
        System.setProperty("prism.order", "sw");
        System.setProperty("glass.platform", "gtk");
        System.setProperty("javafx.platform", "desktop");
        System.setProperty("monocle.platform", "Headless");
        
        // Launch the actual JavaFX application
        Main.main(args);
    }
}
