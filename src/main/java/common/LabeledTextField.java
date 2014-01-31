package common;

import javax.swing.*;
import java.awt.*;

public class LabeledTextField extends JPanel {
    private JLabel label;
    private JTextField field;

    public LabeledTextField(String labelText, String defaultText) {
        label = new JLabel(labelText);
        field = new JTextField(defaultText);

        setLayout(new BorderLayout());
        add(label, BorderLayout.NORTH);
        add(field, BorderLayout.CENTER);
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String text) {
        field.setText(text);
    }
}
