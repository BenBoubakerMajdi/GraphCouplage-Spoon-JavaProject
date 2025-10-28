package Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class CallGraphFrame extends JFrame {
    private final Map<String, List<String>> methodCallGraph;
    private GraphPanel graphPanel;

    public CallGraphFrame(Map<String, List<String>> methodCallGraph) {
        this.methodCallGraph = methodCallGraph;
        setTitle("Call Graph Visualization");
        setSize(1200, 700); // Increased width for legend
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        graphPanel = new GraphPanel(methodCallGraph);
        JScrollPane scrollPane = new JScrollPane(graphPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        filterPanel.setBackground(new Color(40, 40, 50)); // Darker filter panel
        JLabel packageLabel = new JLabel("Filter by Package:");
        packageLabel.setForeground(Color.WHITE);
        Set<String> packages = extractPackages();
        JComboBox<String> packageCombo = new JComboBox<>(packages.toArray(new String[0]));
        packageCombo.setBackground(new Color(50, 50, 60));
        packageCombo.setForeground(Color.WHITE);
        packageCombo.setPreferredSize(new Dimension(200, 25));
        JButton applyFilter = new JButton("Apply Filter");
        applyFilter.setBackground(new Color(0, 120, 215)); // Professional blue
        applyFilter.setForeground(Color.WHITE);
        applyFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedPackage = (String) packageCombo.getSelectedItem();
                graphPanel.applyFilters("", selectedPackage != null ? selectedPackage : "");
            }
        });
        filterPanel.add(packageLabel);
        filterPanel.add(packageCombo);
        filterPanel.add(applyFilter);
        add(filterPanel, BorderLayout.NORTH);

        // Legend Panel
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        legendPanel.setBackground(new Color(30, 30, 40)); // Match dashboard background
        legendPanel.setPreferredSize(new Dimension(200, 0)); // Fixed width for legend
        addLegendEntry(legendPanel, Color.LIGHT_GRAY, "Not Called (0)");
        addLegendEntry(legendPanel, new Color(0, 150, 255), "Low Calls (1-2)");
        addLegendEntry(legendPanel, new Color(255, 165, 0), "Medium Calls (3-5)");
        addLegendEntry(legendPanel, new Color(255, 69, 0), "High Calls (6-10)");
        add(legendPanel, BorderLayout.EAST);

        setVisible(true);
    }

    private Set<String> extractPackages() {
        Set<String> packages = new HashSet<>();
        for (String method : methodCallGraph.keySet()) {
            int lastDot = method.lastIndexOf(".");
            if (lastDot != -1) {
                String packagePath = method.substring(0, lastDot);
                packages.add(packagePath.contains(".") ? packagePath.substring(0, packagePath.lastIndexOf(".")) : "[default]");
            } else {
                packages.add("[default]");
            }
        }
        return packages;
    }

    private void addLegendEntry(JPanel panel, Color color, String label) {
        JPanel entry = new JPanel(new FlowLayout(FlowLayout.LEFT));
        entry.setBackground(new Color(30, 30, 40));
        JLabel colorBox = new JLabel("    ");
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        JLabel text = new JLabel(label);
        text.setForeground(Color.WHITE);
        entry.add(colorBox);
        entry.add(text);
        panel.add(entry);
    }
}