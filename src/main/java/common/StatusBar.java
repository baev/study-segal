package common;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    JLabel label = new JLabel(" ");

    public StatusBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        add(label, BorderLayout.WEST);
    }

    public void setText(String text) {
        label.setText(text);
    }
}