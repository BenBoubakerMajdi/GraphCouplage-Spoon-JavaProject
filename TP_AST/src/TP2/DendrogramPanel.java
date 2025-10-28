// src/TP2/DendrogramPanel.java
package TP2;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DendrogramPanel extends JPanel {
    private final ModuleIdentifier.ClusterNode root;
    private final Map<String, Integer> leafX = new HashMap<>();
    private final Map<ModuleIdentifier.ClusterNode, Integer> nodeY = new HashMap<>();
    private final List<String> orderedLeaves = new ArrayList<>();
    private int maxDepth = 0;

    public DendrogramPanel(ModuleIdentifier.ClusterNode root) {
        this.root = root;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1200, 700));
        layoutTree();
    }

    private void layoutTree() {
        if (root == null) return;

        // FORCE ALL CLASSES FROM MATRIX INTO orderedLeaves
        Set<String> allClasses = new HashSet<>();
        collectAllClasses(root, allClasses);
        orderedLeaves.clear();
        orderedLeaves.addAll(allClasses);
        Collections.sort(orderedLeaves); // Optional: sort for consistency

        int leafCount = orderedLeaves.size();
        if (leafCount == 0) return;

        int spacing = Math.max(60, 1000 / Math.max(1, leafCount - 1));
        int x = 100;
        for (String leaf : orderedLeaves) {
            leafX.put(leaf, x);
            x += spacing;
        }

        maxDepth = calculateDepth(root);
        int heightPerLevel = 500 / Math.max(1, maxDepth);
        assignY(root, 600 - heightPerLevel, heightPerLevel);
    }

    private void collectAllClasses(ModuleIdentifier.ClusterNode node, Set<String> set) {
        if (node == null) return;
        if (node.isLeaf()) {
            set.addAll(node.classes());
        } else {
            collectAllClasses(node.left(), set);
            collectAllClasses(node.right(), set);
        }
    }

    private int calculateDepth(ModuleIdentifier.ClusterNode node) {
        if (node == null || node.isLeaf()) return 0;
        return 1 + Math.max(calculateDepth(node.left()), calculateDepth(node.right()));
    }

    private int assignY(ModuleIdentifier.ClusterNode node, int currentY, int levelHeight) {
        if (node == null) return currentY;

        if (node.isLeaf()) {
            nodeY.put(node, currentY);
            return currentY;
        }

        int leftY = assignY(node.left(), currentY - levelHeight, levelHeight);
        int rightY = assignY(node.right(), currentY - levelHeight, levelHeight);
        int midY = (leftY + rightY) / 2;
        nodeY.put(node, midY);
        return midY;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));

        if (root != null) drawNode(g2d, root);
    }

    private void drawNode(Graphics2D g2d, ModuleIdentifier.ClusterNode node) {
        if (node == null) return;

        if (node.isLeaf()) {
            // DRAW ONLY IF CLASS IS IN leafX
            for (String className : node.classes()) {
                Integer xObj = leafX.get(className);
                if (xObj == null) continue; // Skip if not in layout

                int x = xObj;
                int y = nodeY.getOrDefault(node, 650);
                g2d.setColor(Color.BLACK);
                g2d.drawLine(x, y, x, 650); // vertical to base
                g2d.drawString(shortName(className), x - 30, 670);
            }
            return;
        }

        List<String> leftLeaves  = node.left().leaves();
        List<String> rightLeaves = node.right().leaves();
        if (leftLeaves.isEmpty() || rightLeaves.isEmpty()) return;

        String leftmost  = leftLeaves.get(0);
        String rightmost = rightLeaves.get(rightLeaves.size() - 1);

        Integer x1Obj = leafX.get(leftmost);
        Integer x2Obj = leafX.get(rightmost);
        if (x1Obj == null || x2Obj == null) return;

        int x1 = x1Obj;
        int x2 = x2Obj;
        int midX = (x1 + x2) / 2;
        int y    = nodeY.getOrDefault(node, 650);

        drawNode(g2d, node.left());
        drawNode(g2d, node.right());

        Integer leftY  = nodeY.get(node.left());
        Integer rightY = nodeY.get(node.right());
        if (leftY == null || rightY == null) return;

        // HORIZONTAL MERGE LINE
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(x1, y, x2, y);
        g2d.drawLine(x1, leftY,  x1, y);
        g2d.drawLine(x2, rightY, x2, y);

        g2d.drawLine(midX, y, midX, 650);

        // COUPLING VALUE
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.format("%.3f", node.coupling()), midX + 8, y - 8);
        g2d.setColor(Color.BLACK);
    }

    private String shortName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot == -1 ? fqn : fqn.substring(dot + 1);
    }
}