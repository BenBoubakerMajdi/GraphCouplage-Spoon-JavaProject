package TP2;

import javax.swing.*;
import java.awt.*;

public class DendrogramFrame extends JFrame {
    public DendrogramFrame(ModuleIdentifier.ClusterNode root) {
        setTitle("Coupling Dendrogram (HAC)");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new JScrollPane(new DendrogramPanel(root)));

        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(BorderFactory.createTitledBorder("Legend"));
        legend.setBackground(new Color(245, 245, 255));
        addLegendEntry(legend, "Horizontal lines = merge points");
        addLegendEntry(legend, "Red numbers = coupling strength");
        addLegendEntry(legend, "Leaf nodes = class names");
        add(legend, BorderLayout.EAST);

        setVisible(true);
    }

    private void addLegendEntry(JPanel p, String text) {
        JLabel l = new JLabel("• " + text);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        p.add(l);
    }
}