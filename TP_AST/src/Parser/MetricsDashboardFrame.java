// src/Parser/MetricsDashboardFrame.java
package Parser;

import TP2.DendrogramFrame;
import TP2.ModuleIdentifier;
import TP2.SpoonAnalyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class MetricsDashboardFrame extends JFrame {
    private final ParserLogic parserLogic;
    private JPanel metricsPanel;

    public MetricsDashboardFrame(ParserLogic parserLogic) {
        this.parserLogic = parserLogic;
        setTitle("Metrics Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // === DASHBOARD PANEL ===
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        metricsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        metricsPanel.setBackground(new Color(30, 30, 40));
        populateMetricsPanel();
        JScrollPane metricsScrollPane = new JScrollPane(metricsPanel);
        dashboardPanel.add(metricsScrollPane, BorderLayout.CENTER);

        // === FILTER PANEL ===
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        filterPanel.setBackground(new Color(40, 40, 50));
        JLabel filterLabel = new JLabel("Filter by Package:");
        filterLabel.setForeground(Color.WHITE);
        JComboBox<String> packageCombo = new JComboBox<>(parserLogic.getPackages().toArray(new String[0]));
        packageCombo.setBackground(new Color(50, 50, 60));
        packageCombo.setForeground(Color.WHITE);
        packageCombo.setPreferredSize(new Dimension(200, 25));

        JButton applyFilter = new JButton("Apply Filter");
        applyFilter.setBackground(new Color(0, 120, 215));
        applyFilter.setForeground(Color.WHITE);
        applyFilter.addActionListener(new FilterActionListener(packageCombo));
        filterPanel.add(filterLabel);
        filterPanel.add(packageCombo);
        filterPanel.add(applyFilter);
        dashboardPanel.add(filterPanel, BorderLayout.NORTH);

        // === BUTTON PANEL ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(30, 30, 40));

        // Show Call Graph
        JButton showGraphButton = new JButton("Show Call Graph");
        showGraphButton.setBackground(new Color(0, 120, 215));
        showGraphButton.setForeground(Color.WHITE);
        showGraphButton.addActionListener(new ShowGraphActionListener());
        buttonPanel.add(showGraphButton);

     // Show Modules (JDT) → ALL CLASSES
        JButton modulesBtn = new JButton("Show All Modules (JDT)");
        modulesBtn.setBackground(new Color(0, 150, 100));
        modulesBtn.setForeground(Color.WHITE);
        modulesBtn.addActionListener(e -> {
            var calls = toCalls(parserLogic.getMethodCallGraph());
            var matrix = buildCouplingMatrix(calls);
            var modules = ModuleIdentifier.identifyModules(matrix, 0.02);
            StringBuilder sb = new StringBuilder("=== ALL MODULES (JDT) ===\n");
            for (int i = 0; i < modules.size(); i++) {
                sb.append("Module ").append(i + 1).append(": ")
                  .append(modules.get(i)).append("\n");
            }
            JOptionPane.showMessageDialog(
                null,
                new JScrollPane(new JTextArea(sb.toString())),
                "All Identified Modules",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        buttonPanel.add(modulesBtn);

        // Spoon Analysis (Full HAC + Dendrogram)
        JButton spoonBtn = new JButton("Spoon Analysis (HAC)");
        spoonBtn.setBackground(new Color(200, 0, 100));
        spoonBtn.setForeground(Color.WHITE);
        spoonBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> SpoonAnalyzer.main(new String[]{}));
        });
        buttonPanel.add(spoonBtn);

        // Show Dendrogram (JDT)
        JButton dendroBtn = new JButton("Show Dendrogram (JDT)");
        dendroBtn.setBackground(new Color(120, 0, 200));
        dendroBtn.setForeground(Color.WHITE);
        dendroBtn.setFont(new Font("Arial", Font.BOLD, 12));
        dendroBtn.addActionListener(e -> {
            var calls = toCalls(parserLogic.getMethodCallGraph());
            var matrix = buildCouplingMatrix(calls);
            var root = ModuleIdentifier.buildDendrogram(matrix, 0.02);
            new DendrogramFrame(root);
        });
        buttonPanel.add(dendroBtn);

        dashboardPanel.add(buttonPanel, BorderLayout.SOUTH);

        // === LOG PANEL ===
        JPanel logPanel = new JPanel(new BorderLayout());
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(40, 40, 50));
        logArea.setForeground(Color.WHITE);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logArea.setText(
            parserLogic.getMethodInfo() + "\n" +
            parserLogic.getVariableInfo() + "\n" +
            parserLogic.getInvocationInfo()
        );

        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("AST Logs", logPanel);

        add(tabbedPane);
        setVisible(true);
    }

    // === METRICS ===
    private void populateMetricsPanel() {
        metricsPanel.removeAll();
        Map<String, Integer> filteredMethodCounts = new HashMap<>();
        Map<String, Integer> filteredAttributeCounts = new HashMap<>();
        int totalClasses = 0, totalLines = 0, totalMethods = 0, totalAttributes = 0;

        Set<String> packages = parserLogic.getPackages();
        for (Map.Entry<String, Integer> entry : parserLogic.getClassMethodCounts().entrySet()) {
            String className = entry.getKey();
            String packageName = className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "[default]";
            if (packages.contains(packageName)) {
                filteredMethodCounts.put(className, entry.getValue());
                filteredAttributeCounts.put(className, parserLogic.getClassAttributeCounts().getOrDefault(className, 0));
                totalClasses++;
                totalMethods += entry.getValue();
                totalAttributes += parserLogic.getClassAttributeCounts().getOrDefault(className, 0);
            }
        }
        totalLines = parserLogic.getTotalLines();

        addMetricCard("Total Classes", String.valueOf(totalClasses));
        addMetricCard("Total Application Lines", String.valueOf(totalLines));
        addMetricCard("Total Methods", String.valueOf(totalMethods));
        addMetricCard("Total Packages", String.valueOf(packages.size()));
        addMetricCard("Avg Methods/Class", String.format("%.2f", totalClasses > 0 ? (double) totalMethods / totalClasses : 0.0));
        addMetricCard("Avg Lines/Method", String.format("%.2f", totalMethods > 0 ? (double) totalLines / totalMethods : 0.0));
        addMetricCard("Avg Attributes/Class", String.format("%.2f", totalClasses > 0 ? (double) totalAttributes / totalClasses : 0.0));

        metricsPanel.revalidate();
        metricsPanel.repaint();
    }

    private void addMetricCard(String label, String value) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 70), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(new Color(50, 50, 60));
        card.setPreferredSize(new Dimension(450, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        JLabel titleLabel = new JLabel(label + ":");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        card.add(titleLabel, gbc);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(new Color(0, 150, 255));
        gbc.gridy = 1; gbc.gridwidth = 1;
        card.add(valueLabel, gbc);

        metricsPanel.add(card);
    }

    // === FILTER LISTENER ===
    private class FilterActionListener implements ActionListener {
        private final JComboBox<String> packageCombo;

        FilterActionListener(JComboBox<String> packageCombo) {
            this.packageCombo = packageCombo;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedPackage = (String) packageCombo.getSelectedItem();
            metricsPanel.removeAll();
            Map<String, Integer> filteredMethodCounts = new HashMap<>();
            Map<String, Integer> filteredAttributeCounts = new HashMap<>();
            int totalClasses = 0, totalLines = 0, totalMethods = 0, totalAttributes = 0;

            for (Map.Entry<String, Integer> entry : parserLogic.getClassMethodCounts().entrySet()) {
                String className = entry.getKey();
                String packageName = className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "[default]";
                if (selectedPackage == null || selectedPackage.equals("[default]") || packageName.equals(selectedPackage)) {
                    filteredMethodCounts.put(className, entry.getValue());
                    filteredAttributeCounts.put(className, parserLogic.getClassAttributeCounts().getOrDefault(className, 0));
                    totalClasses++;
                    totalMethods += entry.getValue();
                    totalAttributes += parserLogic.getClassAttributeCounts().getOrDefault(className, 0);
                }
            }
            totalLines = parserLogic.getTotalLines();

            addMetricCard("Total Classes", String.valueOf(totalClasses));
            addMetricCard("Total Lines", String.valueOf(totalLines));
            addMetricCard("Total Methods", String.valueOf(totalMethods));
            addMetricCard("Total Packages", String.valueOf(selectedPackage != null ? 1 : parserLogic.getPackages().size()));
            addMetricCard("Avg Methods/Class", String.format("%.2f", totalClasses > 0 ? (double) totalMethods / totalClasses : 0.0));
            addMetricCard("Avg Lines/Method", String.format("%.2f", totalMethods > 0 ? (double) totalLines / totalMethods : 0.0));
            addMetricCard("Avg Attributes/Class", String.format("%.2f", totalClasses > 0 ? (double) totalAttributes / totalClasses : 0.0));

            metricsPanel.revalidate();
            metricsPanel.repaint();
        }
    }

    private class ShowGraphActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new CallGraphFrame(parserLogic.getMethodCallGraph()).setVisible(true);
        }
    }

    // === IN-PLACE COUPLING LOGIC (NO COUPLINGGRAPH) ===
 // In buildCouplingMatrix() and toCalls()
 // === IN-PLACE COUPLING LOGIC ===
    private List<Call> toCalls(Map<String, List<String>> graph) {
        List<Call> calls = new ArrayList<>();
        for (var e : graph.entrySet()) {
            String caller = extractClass(e.getKey());
            if (caller == null || isExternal(caller)) continue;
            for (String callee : e.getValue()) {
                String calleeClass = extractClass(callee);
                if (calleeClass == null || isExternal(calleeClass)) continue;
                calls.add(new Call(caller, calleeClass));
            }
        }
        return calls;
    }

    private String extractClass(String methodFQN) {
        if (methodFQN == null || methodFQN.isEmpty()) return null;
        int lastDot = methodFQN.lastIndexOf('.');
        if (lastDot <= 0) return null;
        int secondLastDot = methodFQN.lastIndexOf('.', lastDot - 1);
        if (secondLastDot <= 0) return null;
        return methodFQN.substring(0, secondLastDot);
    }

    private boolean isExternal(String className) {
        return className.startsWith("java.") ||
               className.startsWith("javax.") ||
               className.startsWith("sun.") ||
               className.startsWith("com.sun.") ||
               className.startsWith("org.") ||
               className.contains("$"); // inner classes
    }

    private Map<String, Map<String, Double>> buildCouplingMatrix(List<Call> calls) {
        Set<String> classes = new HashSet<>();
        Map<String, Map<String, Integer>> count = new HashMap<>();

        for (Call call : calls) {
            String c1 = call.caller();
            String c2 = call.callee();
            if (c1.equals(c2)) continue;

            classes.add(c1);
            classes.add(c2);
            count.computeIfAbsent(c1, k -> new HashMap<>()).merge(c2, 1, Integer::sum);
        }

        Map<String, Map<String, Double>> matrix = new HashMap<>();
        for (String c1 : classes) {
            Map<String, Double> row = new HashMap<>();
            int out = count.getOrDefault(c1, Map.of()).values().stream().mapToInt(Integer::intValue).sum();
            for (String c2 : classes) {
                if (c1.equals(c2)) continue;
                int in = count.getOrDefault(c2, Map.of()).getOrDefault(c1, 0);
                int links = count.getOrDefault(c1, Map.of()).getOrDefault(c2, 0) + in;
                row.put(c2, links > 0 ? (double) links / (out + in + 1) : 0.0);
            }
            matrix.put(c1, row);
        }
        return matrix;
    }

    // Local Call record (no external dependency)
    record Call(String caller, String callee) {}
}