package TP2;

import Parser.ParserConfig;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class SpoonAnalyzer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String src = ParserConfig.PROJECT_SOURCE_PATH;
            File srcDir = new File(src);

            if (!srcDir.exists() || !srcDir.isDirectory()) {
                JOptionPane.showMessageDialog(null,
                        "Source path not found: " + src,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Launcher launcher = new Launcher();
            launcher.getEnvironment().setNoClasspath(true);
            launcher.getEnvironment().setSourceClasspath(new String[0]);
            launcher.getEnvironment().setComplianceLevel(17);
            launcher.getEnvironment().setAutoImports(true);
            launcher.getEnvironment().setCommentEnabled(true);

            try {
                Files.walk(srcDir.toPath())
                     .filter(p -> p.toString().endsWith(".java"))
                     .forEach(p -> {
                         try {
                             String content = Files.readString(p);
                             String rel = srcDir.toPath().relativize(p).toString()
                                               .replace("\\", "/");
                             launcher.addInputResource(
                                     new spoon.support.compiler.VirtualFile(content, rel));
                         } catch (Exception e) {
                             System.err.println("Failed to read: " + p);
                         }
                     });
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to scan files: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try { launcher.buildModel(); }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Spoon failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            CtModel model = launcher.getModel();
            if (model.getAllTypes().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No classes parsed from: " + src,
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ----- extract method calls -----
            List<Call> calls = new ArrayList<>();
            for (CtType<?> type : model.getAllTypes()) {
                for (CtMethod<?> m : type.getMethods()) {
                    String caller = type.getQualifiedName() + "." + m.getSimpleName();
                    for (CtInvocation<?> inv : m.getElements(
                            new TypeFilter<>(CtInvocation.class))) {
                        var exec = inv.getExecutable();
                        if (exec != null && exec.getDeclaringType() != null) {
                            String callee = exec.getDeclaringType().getQualifiedName()
                                            + "." + exec.getSimpleName();
                            calls.add(new Call(caller, callee));
                        }
                    }
                }
            }

            // ----- build coupling matrix (in-place) -----
            Map<String, Map<String, Double>> matrix = buildCouplingMatrix(calls);

            // ----- HAC + dendrogram -----
            ModuleIdentifier.ClusterNode root = ModuleIdentifier.buildDendrogram(matrix, 0.02);
            new DendrogramFrame(root);

            // ----- show modules -----
            List<Set<String>> modules = ModuleIdentifier.identifyModules(matrix, 0.02);
            StringBuilder sb = new StringBuilder("=== SPOON MODULES (HAC) ===\n");
            for (int i = 0; i < modules.size(); i++) {
                sb.append("Module ").append(i + 1).append(": ")
                  .append(modules.get(i)).append("\n");
            }
            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new Font("Monospaced", Font.PLAIN, 14));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(null,
                    new JScrollPane(ta),
                    "Spoon Modules",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private static Map<String, Map<String, Double>> buildCouplingMatrix(List<Call> calls) {
        Set<String> classes = new HashSet<>();
        Map<String, Map<String, Integer>> count = new HashMap<>();

        for (Call c : calls) {
            String c1 = c.caller().substring(0, c.caller().lastIndexOf('.'));
            String c2 = c.callee().substring(0, c.callee().lastIndexOf('.'));
            if (c1.equals(c2)) continue;

            classes.add(c1); classes.add(c2);
            count.computeIfAbsent(c1, k -> new HashMap<>()).merge(c2, 1, Integer::sum);
        }

        Map<String, Map<String, Double>> matrix = new HashMap<>();
        for (String c1 : classes) {
            Map<String, Double> row = new HashMap<>();
            int out = count.getOrDefault(c1, Map.of())
                           .values().stream().mapToInt(Integer::intValue).sum();
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
}