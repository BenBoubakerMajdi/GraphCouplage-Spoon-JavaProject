// src/TP2/CouplingGraph.java
package TP2;

import java.util.*;
import java.util.stream.Collectors;

public class CouplingGraph {

    public record Call(String caller, String callee) {}

    // Convert JDT call graph (Map<callerMethod, List<calleeMethod>>) to Call list
    public static List<Call> toCalls(Map<String, List<String>> graph) {
        List<Call> calls = new ArrayList<>();
        for (var e : graph.entrySet()) {
            String caller = e.getKey();
            for (String callee : e.getValue()) {
                calls.add(new Call(caller, callee));
            }
        }
        return calls;
    }

    // Build coupling matrix from method calls
    public static Map<String, Map<String, Double>> buildCouplingMatrix(List<Call> calls) {
        Set<String> classes = new HashSet<>();
        Map<String, Map<String, Integer>> count = new HashMap<>();

        for (Call call : calls) {
            String c1 = call.caller().substring(0, call.caller().lastIndexOf('.'));
            String c2 = call.callee().substring(0, call.callee().lastIndexOf('.'));
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

    // Optional: Print matrix
    public static void printMatrix(Map<String, Map<String, Double>> matrix) {
        List<String> sorted = new ArrayList<>(matrix.keySet());
        Collections.sort(sorted);
        System.out.printf("%15s", "");
        for (String c : sorted) System.out.printf("%15s", shortName(c));
        System.out.println();
        for (String c1 : sorted) {
            System.out.printf("%15s", shortName(c1));
            for (String c2 : sorted) {
                if (c1.equals(c2)) {
                    System.out.printf("%15s", "-");
                } else {
                    System.out.printf("%15.3f", matrix.get(c1).getOrDefault(c2, 0.0));
                }
            }
            System.out.println();
        }
    }

    private static String shortName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot == -1 ? fqn : fqn.substring(dot + 1);
    }
}