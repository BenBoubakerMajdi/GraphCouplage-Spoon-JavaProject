// src/TP2/ModuleIdentifier.java
package TP2;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleIdentifier {

    public record ClusterNode(Set<String> classes, ClusterNode left, ClusterNode right, double coupling) {
        boolean isLeaf() { return left == null && right == null; }
        List<String> leaves() {
            if (isLeaf()) return List.of(classes.iterator().next());
            List<String> res = new ArrayList<>();
            res.addAll(left.leaves());
            res.addAll(right.leaves());
            return res;
        }
    }

    // BUILD FULL DENDROGRAM (NO PRUNING)
    public static ClusterNode buildDendrogram(Map<String, Map<String, Double>> matrix, double CP) {
        List<ClusterNode> clusters = matrix.keySet().stream()
                .map(c -> new ClusterNode(Set.of(c), null, null, 0.0))
                .collect(Collectors.toList());

        // MERGE UNTIL ONLY 1 CLUSTER (full tree)
        while (clusters.size() > 1) {
            double best = -1; int iBest = -1, jBest = -1;
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double coup = averageCoupling(clusters.get(i), clusters.get(j), matrix);
                    if (coup > best) {
                        best = coup;
                        iBest = i;
                        jBest = j;
                    }
                }
            }
            if (iBest == -1) break;

            Set<String> merged = new HashSet<>(clusters.get(iBest).classes);
            merged.addAll(clusters.get(jBest).classes);
            ClusterNode newNode = new ClusterNode(merged, clusters.get(iBest), clusters.get(jBest), best);

            clusters.set(iBest, newNode);
            clusters.remove(jBest);
        }

        return clusters.isEmpty() ? null : clusters.get(0);
    }

    private static double averageCoupling(ClusterNode a, ClusterNode b, Map<String, Map<String, Double>> m) {
        double sum = 0;
        int count = 0;
        for (String x : a.classes) {
            for (String y : b.classes) {
                sum += m.getOrDefault(x, Map.of()).getOrDefault(y, 0.0);
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    // RETURN ALL LEAF CLUSTERS (ONE CLASS PER MODULE)
    public static List<Set<String>> identifyModules(Map<String, Map<String, Double>> matrix, double CP) {
        ClusterNode root = buildDendrogram(matrix, CP);
        if (root == null) return List.of();

        List<Set<String>> modules = new ArrayList<>();
        collectProjectClasses(root, modules);
        return modules;
    }

    private static void collectProjectClasses(ClusterNode node, List<Set<String>> modules) {
        if (node.isLeaf()) {
            modules.add(node.classes);
        } else {
            collectProjectClasses(node.left, modules);
            collectProjectClasses(node.right, modules);
        }
    }

    private static List<Set<String>> collectLeaves(ClusterNode node) {
        List<Set<String>> modules = new ArrayList<>();
        if (node.isLeaf()) {
            modules.add(node.classes);
        } else {
            modules.addAll(collectLeaves(node.left));
            modules.addAll(collectLeaves(node.right));
        }
        return modules;
    }
}