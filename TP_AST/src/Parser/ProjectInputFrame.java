package Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ProjectInputFrame extends JFrame {
    private JTextField pathField;
    private JButton parseButton;

    public ProjectInputFrame() {
        setTitle("Project Path Input");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Enter Java Project Path:");
        pathField = new JTextField(ParserConfig.PROJECT_SOURCE_PATH);
        panel.add(label, BorderLayout.NORTH);
        panel.add(pathField, BorderLayout.CENTER);

        parseButton = new JButton("Parse Project");
        parseButton.addActionListener(new ParseActionListener());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(parseButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pathField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        parseButton.setFont(new Font("SansSerif", Font.PLAIN, 14));

        add(panel);
        setVisible(true);
    }

    private class ParseActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String path = pathField.getText();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(ProjectInputFrame.this, "Please enter a valid path.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                ParserLogic parserLogic = new ParserLogic(path, ParserConfig.JRE_PATH);
                parserLogic.parseProject();
                dispose();
                new MetricsDashboardFrame(parserLogic);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ProjectInputFrame.this, "Error parsing project: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}