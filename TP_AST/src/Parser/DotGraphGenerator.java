package Parser;

import java.util.List;
import java.util.Map;

public class DotGraphGenerator {
    public static void generateDotGraph(Map<String, List<String>> methodCallGraph, String outputFile) {
        new CallGraphFrame(methodCallGraph).setVisible(true);
    }
}