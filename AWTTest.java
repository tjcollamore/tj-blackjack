
import javax.swing.*;
import java.awt.*;

public class AWTTest {
    public static void main(String[] args) {
        // Set system properties for VNC
        System.setProperty("java.awt.headless", "false");
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AWT/Swing VNC Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            JLabel label = new JLabel("If you see this, AWT/Swing is working in VNC!");
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            frame.add(label);
            frame.setSize(400, 200);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("AWT/Swing window created successfully!");
        });
    }
}
