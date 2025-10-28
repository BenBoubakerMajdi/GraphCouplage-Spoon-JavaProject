package Parser;

// Imports for collections and stream operations
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import Parser.MetricsCollector.MethodInfo;

public class MetricsDisplay {
    // Generates a formatted string of metrics 1-13 for GUI display
    public static String displayMetrics(int totalApplicationClasses, int totalApplicationLines, 
            int totalApplicationMethods, int totalApplicationAttributes, 
            Set<String> applicationPackages, Map<String, Integer> classMethodCounts, 
            Map<String, Integer> classAttributeCounts, Map<String, List<MethodInfo>> classMethodLineCounts, 
            List<MethodInfo> maxParameterMethods, int[] maxParameterCount, int methodThreshold) {
        // Calculate number of classes for top 10% (minimum 1)
        int topPercentCount = Math.max(1, (int) Math.ceil(totalApplicationClasses * 0.1));

        // Get top 10% classes by method count
        List<Map.Entry<String, Integer>> topClassesByMethods = classMethodCounts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sort descending
            .limit(topPercentCount) // Limit to top 10%
            .collect(Collectors.toList());

        // Get top 10% classes by attribute count
        List<Map.Entry<String, Integer>> topClassesByAttributes = classAttributeCounts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sort descending
            .limit(topPercentCount) // Limit to top 10%
            .collect(Collectors.toList());

        // Find classes in top 10% for both methods and attributes
        Set<String> topMethodClasses = topClassesByMethods.stream()
            .map(Map.Entry::getKey) // Extract class names
            .collect(Collectors.toSet());
        Set<String> topAttributeClasses = topClassesByAttributes.stream()
            .map(Map.Entry::getKey) // Extract class names
            .collect(Collectors.toSet());
        Set<String> commonClasses = new HashSet<>(topMethodClasses);
        commonClasses.retainAll(topAttributeClasses); // Intersection of both sets

        // Get classes with more methods than threshold
        List<Map.Entry<String, Integer>> classesWithManyMethods = classMethodCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > methodThreshold) // Filter by threshold
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sort descending
            .collect(Collectors.toList());

        // Get top 10% methods by line count per class
        Map<String, List<MethodInfo>> topMethodsByLines = new HashMap<>();
        for (Map.Entry<String, List<MethodInfo>> entry : classMethodLineCounts.entrySet()) {
            String className = entry.getKey();
            List<MethodInfo> methods = entry.getValue();
            int methodCount = methods.size();
            int topMethodCount = Math.max(1, (int) Math.ceil(methodCount * 0.1)); // Top 10% methods
            List<MethodInfo> topMethods = methods.stream()
                .sorted((m1, m2) -> Integer.compare(m2.lineCount, m1.lineCount)) // Sort by line count
                .limit(topMethodCount) // Limit to top 10%
                .collect(Collectors.toList());
            topMethodsByLines.put(className, topMethods);
        }

        // Build formatted string for metrics 1-13
        StringBuilder sb = new StringBuilder();
        sb.append("\n=======================================================\n");
        sb.append("1) Nombre total de classes : ").append(totalApplicationClasses).append("\n");
        sb.append("2) Nombre total de lignes de code : ").append(totalApplicationLines).append("\n");
        sb.append("3) Nombre total de méthodes : ").append(totalApplicationMethods).append("\n");
        sb.append("4) Nombre total de packages : ").append(applicationPackages.size()).append("\n");
        sb.append(String.format("5) Nombre moyen de méthodes par classe : %.2f%n", 
                totalApplicationClasses > 0 ? (double) totalApplicationMethods / totalApplicationClasses : 0.0));
        sb.append(String.format("6) Nombre moyen de lignes de code par méthode : %.2f%n", 
                totalApplicationMethods > 0 ? (double) totalApplicationLines / totalApplicationMethods : 0.0));
        sb.append(String.format("7) Nombre moyen d'attributs par classe : %.2f%n", 
                totalApplicationClasses > 0 ? (double) totalApplicationAttributes / totalApplicationClasses : 0.0));
        sb.append("8) Les 10% des classes avec le plus grand nombre de méthodes (").append(topPercentCount).append(" classes) :\n");
        for (Map.Entry<String, Integer> entry : topClassesByMethods) {
            sb.append("   - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" méthodes\n");
        }
        sb.append("9) Les 10% des classes avec le plus grand nombre d'attributs (").append(topPercentCount).append(" classes) :\n");
        for (Map.Entry<String, Integer> entry : topClassesByAttributes) {
            sb.append("   - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" attributs\n");
        }
        sb.append("10) Classes dans les deux catégories (top 10% méthodes et attributs) :\n");
        if (commonClasses.isEmpty()) {
            sb.append("   - Aucune classe n'est dans les deux top 10%.\n");
        } else {
            for (String className : commonClasses) {
                int methodCount = classMethodCounts.getOrDefault(className, 0);
                int attributeCount = classAttributeCounts.getOrDefault(className, 0);
                sb.append("   - ").append(className).append(": ").append(methodCount).append(" méthodes, ").append(attributeCount).append(" attributs\n");
            }
        }
        sb.append("11) Classes avec plus de ").append(methodThreshold).append(" méthodes :\n");
        if (classesWithManyMethods.isEmpty()) {
            sb.append("   - Aucune classe n'a plus de ").append(methodThreshold).append(" méthodes.\n");
        } else {
            for (Map.Entry<String, Integer> entry : classesWithManyMethods) {
                sb.append("   - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" méthodes\n");
            }
        }
        sb.append("12) Les 10% des méthodes avec le plus grand nombre de lignes de code par classe :\n");
        if (topMethodsByLines.isEmpty()) {
            sb.append("   - Aucune méthode trouvée.\n");
        } else {
            for (Map.Entry<String, List<MethodInfo>> entry : topMethodsByLines.entrySet()) {
                String className = entry.getKey();
                List<MethodInfo> methods = entry.getValue();
                if (!methods.isEmpty()) {
                    sb.append("   - Classe: ").append(className).append("\n");
                    for (MethodInfo method : methods) {
                        sb.append("      - Méthode: ").append(method.methodName).append(", ").append(method.lineCount).append(" lignes\n");
                    }
                }
            }
        }
        sb.append("13) Nombre maximal de paramètres parmi toutes les méthodes :\n");
        if (maxParameterMethods.isEmpty()) {
            sb.append("   - Aucune méthode trouvée.\n");
        } else {
            sb.append("   - Maximum: ").append(maxParameterCount[0]).append(" paramètres\n");
            sb.append("   - Méthode(s) avec le maximum de paramètres :\n");
            for (MethodInfo method : maxParameterMethods) {
                sb.append("      - ").append(method.className).append(".").append(method.methodName).append(": ").append(method.parameterCount).append(" paramètres\n");
            }
        }
        sb.append("=======================================================\n");

        return sb.toString();
    }
}