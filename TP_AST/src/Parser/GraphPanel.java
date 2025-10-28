package Parser;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.*;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

public class GraphPanel extends JPanel {
    private final Map<String, List<String>> methodCallGraph;
    private Map<String, List<String>> filteredGraph;
    private Set<String> allMethods;
    private Map<String, Integer> callCounts; // Track call frequency
    private Map<String, Color> nodeColors;
    private Map<String, Point> nodePositions;
    private double zoomFactor = 1.0;
    private int transX = 0, transY = 0;
    private Point lastDragPoint;
    private String highlightedNode = null;

    public GraphPanel(Map<String, List<String>> methodCallGraph) {
        this.methodCallGraph = methodCallGraph;
        this.filteredGraph = new HashMap<>(methodCallGraph);
        this.callCounts = calculateCallCounts();
        System.out.println("Initial callCounts: " + callCounts);
        initializeGraphData();

        addMouseWheelListener(this::handleZoom);
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint != null) {
                    transX += e.getX() - lastDragPoint.x;
                    transY += e.getY() - lastDragPoint.y;
                    repaint();
                }
                lastDragPoint = e.getPoint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                lastDragPoint = null;
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                highlightedNode = findNodeAtPoint(e.getPoint());
                repaint();
            }
        });
    }

    private Map<String, Integer> calculateCallCounts() {
        Map<String, Integer> counts = new HashMap<>();
        Set<String> visited = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : methodCallGraph.entrySet()) {
            String caller = entry.getKey();
            counts.putIfAbsent(caller, 0);
            visited.add(caller);
            for (String callee : entry.getValue()) {
                if (!callee.startsWith("[unresolved].") && !visited.contains(callee)) {
                    counts.put(callee, counts.getOrDefault(callee, 0) + 1);
                    System.out.println("Counting call: " + caller + " -> " + callee + ", Count: " + counts.get(callee));
                    countRecursiveCalls(callee, counts, visited);
                }
            }
        }
        return counts;
    }

    private void countRecursiveCalls(String method, Map<String, Integer> counts, Set<String> visited) {
        if (methodCallGraph.containsKey(method) && !visited.contains(method)) {
            visited.add(method);
            for (String callee : methodCallGraph.get(method)) {
                if (!callee.startsWith("[unresolved].")) {
                    counts.put(callee, counts.getOrDefault(callee, 0) + 1);
                    System.out.println("Recursive call: " + method + " -> " + callee + ", Count: " + counts.get(callee));
                    countRecursiveCalls(callee, counts, visited);
                }
            }
        }
    }

    private void initializeGraphData() {
        allMethods = new HashSet<>();
        nodeColors = new HashMap<>();
        nodePositions = new HashMap<>();

        for (String method : filteredGraph.keySet()) {
            allMethods.add(method);
            allMethods.addAll(filteredGraph.get(method));
        }

        for (String method : allMethods) {
            nodeColors.put(method, getNodeColor(method));
            System.out.println("Method: " + method + ", Color: " + nodeColors.get(method) + ", Call Count: " + callCounts.getOrDefault(method, 0));
        }

        layoutNodes();
    }

    private Color getNodeColor(String method) {
        int callCount = callCounts.getOrDefault(method, 0);
        if (callCount == 0) {
            return Color.LIGHT_GRAY; // Not called
        } else if (callCount <= 2) {
            return new Color(0, 150, 255); // Blue for 1-2 calls
        } else if (callCount <= 5) {
            return new Color(255, 165, 0); // Orange for 3-5 calls
        } else if (callCount <= 10) {
            return new Color(255, 69, 0); // Red for 6-10 calls
        }
        return Color.LIGHT_GRAY; // Default (should not occur with current data)
    }

    private void layoutNodes() {
        int gridSize = (int) Math.ceil(Math.sqrt(allMethods.size()));
        int x = 50, y = 50, xSpacing = 200, ySpacing = 80;
        int index = 0;
        for (String method : allMethods) {
            if (method.startsWith("[unresolved].")) continue;
            nodePositions.put(method, new Point(x + (index % gridSize) * xSpacing, y + (index / gridSize) * ySpacing));
            index++;
        }
    }

    public void applyFilters(String classFilter, String packageFilter) {
        filteredGraph = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        // Initialize with all methods as potential roots
        for (String method : methodCallGraph.keySet()) {
            int lastDot = method.lastIndexOf(".");
            String packageName = lastDot != -1 && method.lastIndexOf(".", lastDot - 1) != -1 
                ? method.substring(0, method.lastIndexOf(".", lastDot - 1)) : "[default]";
            if (packageFilter.isEmpty() || packageName.equals(packageFilter)) {
                queue.offer(method);
                visited.add(method);
            }
        }

        // BFS without depth limit
        while (!queue.isEmpty()) {
            String current = queue.poll();
            filteredGraph.put(current, methodCallGraph.get(current));
            for (String callee : methodCallGraph.get(current)) {
                if (!visited.contains(callee) && !callee.startsWith("[unresolved].")) {
                    int lastDot = callee.lastIndexOf(".");
                    String packageName = lastDot != -1 && callee.lastIndexOf(".", lastDot - 1) != -1 
                        ? callee.substring(0, callee.lastIndexOf(".", lastDot - 1)) : "[default]";
                    if (packageFilter.isEmpty() || packageName.equals(packageFilter)) {
                        queue.offer(callee);
                        visited.add(callee);
                    }
                }
            }
        }

        initializeGraphData();
        repaint();
    }

    private void handleZoom(MouseWheelEvent e) {
        if (e.getPreciseWheelRotation() < 0) zoomFactor += 0.1;
        else zoomFactor -= 0.1;
        zoomFactor = Math.max(0.5, zoomFactor);
        repaint();
    }

    private String findNodeAtPoint(Point p) {
        for (String method : nodePositions.keySet()) {
            Point pos = nodePositions.get(method);
            String label = method.substring(method.lastIndexOf(".", method.lastIndexOf(".") - 1) + 1);
            FontMetrics fm = getFontMetrics(getFont());
            int width = Math.max(150, fm.stringWidth(label) + 20); // Minimum 150, +20 for padding
            Rectangle rect = new Rectangle(pos.x, pos.y, width, 40);
            if (rect.contains(p)) return method;
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(transX, transY);
        g2d.scale(zoomFactor, zoomFactor);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.DARK_GRAY);
        for (Map.Entry<String, List<String>> entry : filteredGraph.entrySet()) {
            String caller = entry.getKey();
            Point callerPos = nodePositions.get(caller);
            if (callerPos == null) continue;

            for (String callee : entry.getValue()) {
                if (callee.startsWith("[unresolved].")) continue;
                Point calleePos = nodePositions.get(callee);
                if (calleePos == null) continue;

                drawArrow(g2d, callerPos.x + 75, callerPos.y + 20, calleePos.x + 75, calleePos.y + 20);
            }
        }

        for (String method : nodePositions.keySet()) {
            Point pos = nodePositions.get(method);
            Color color = nodeColors.get(method);
            g2d.setColor(color);
            String label = method.substring(method.lastIndexOf(".", method.lastIndexOf(".") - 1) + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int width = Math.max(150, fm.stringWidth(label) + 20); // Minimum 150, +20 for padding
            g2d.fillRect(pos.x, pos.y, width, 40);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(pos.x, pos.y, width, 40);
            if (method.equals(highlightedNode)) {
                g2d.setColor(Color.YELLOW); // Highlight on hover
                g2d.drawRect(pos.x - 2, pos.y - 2, width + 4, 44);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawString(label, pos.x + 10, pos.y + 25);
            System.out.println("Drawing " + method + " with color " + color + " at " + pos + ", width: " + width);
        }
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
        int arrowSize = 10;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int x3 = x2 - (int) (arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = y2 - (int) (arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = x2 - (int) (arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = y2 - (int) (arrowSize * Math.sin(angle + Math.PI / 6));
        g2d.drawLine(x2, y2, x3, y3);
        g2d.drawLine(x2, y2, x4, y4);
    }
}