// src/TP2/CouplingGraphFrame.java
package TP2;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CouplingGraphFrame extends JFrame {
    private final Map<String, Map<String, Double>> matrix;
    private final List<String> classes;

    public CouplingGraphFrame(Map<String, Map<String, Double>> matrix) {
        this.matrix = matrix;
        this.classes = new ArrayList<>(matrix.keySet());
        Collections.sort(classes);

        setTitle("Coupling Graph (Interactive)");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new GraphPanel());
        setVisible(true);
    }

    private class GraphPanel extends JPanel {
        private final int NODE_RADIUS = 30;
        private final int MARGIN = 80;

        public GraphPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 2 * MARGIN;
            int h = getHeight() - 2 * MARGIN;
            int n = classes.size();
            if (n == 0) return;

            Map<String, Point> pos = new HashMap<>();
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                int x = MARGIN + (int) (w / 2 + (w / 2 - 60) * Math.cos(angle));
                int y = MARGIN + (int) (h / 2 + (h / 2 - 60) * Math.sin(angle));
                pos.put(classes.get(i), new Point(x, y));
            }

            // Draw edges
            g2d.setStroke(new BasicStroke(1.5f));
            for (String c1 : classes) {
                for (String c2 : classes) {
                    if (c1.compareTo(c2) >= 0) continue;
                    double coup = matrix.get(c1).getOrDefault(c2, 0.0);
                    if (coup < 0.01) continue;

                    Point p1 = pos.get(c1);
                    Point p2 = pos.get(c2);
                    int alpha = (int) (255 * Math.min(coup * 3, 1.0));
                    g2d.setColor(new Color(0, 100, 200, alpha));
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

                    // Label
                    if (coup > 0.1) {
                        int mx = (p1.x + p2.x) / 2;
                        int my = (p1.y + p2.y) / 2;
                        g2d.setColor(Color.RED);
                        g2d.setFont(new Font("Arial", Font.BOLD, 10));
                        g2d.drawString(String.format("%.2f", coup), mx + 5, my - 5);
                    }
                }
            }

            // Draw nodes
            for (String c : classes) {
                Point p = pos.get(c);
                g2d.setColor(new Color(0, 120, 215));
                g2d.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String name = shortName(c);
                FontMetrics fm = g2d.getFontMetrics();
                int sw = fm.stringWidth(name);
                g2d.drawString(name, p.x - sw / 2, p.y + 5);
            }

            // Legend
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Edge opacity = coupling strength", 20, getHeight() - 30);
            g2d.drawString("Red labels = >0.1", 20, getHeight() - 15);
        }

        private String shortName(String fqn) {
            int dot = fqn.lastIndexOf('.');
            return dot == -1 ? fqn : fqn.substring(dot + 1);
        }
    }
}