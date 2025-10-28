package Parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ParserLogic {
    private final String projectSourcePath;
    private final String jrePath;
    private int totalClasses = 0;
    private int totalLines = 0;
    private int totalMethods = 0;
    private int totalAttributes = 0;
    private Set<String> packages = new HashSet<>();
    private Map<String, Integer> classMethodCounts = new HashMap<>();
    private Map<String, Integer> classAttributeCounts = new HashMap<>();
    private Map<String, List<MetricsCollector.MethodInfo>> classMethodLineCounts = new HashMap<>();
    private int[] maxParameterCount = {0};
    private List<MetricsCollector.MethodInfo> maxParameterMethods = new ArrayList<>();
    private Map<String, List<String>> methodCallGraph = new HashMap<>();
    private StringBuilder methodInfo = new StringBuilder();
    private StringBuilder variableInfo = new StringBuilder();
    private StringBuilder invocationInfo = new StringBuilder();
    private static Map<String, List<String>> staticMethodCallGraph;

    public ParserLogic(String projectSourcePath, String jrePath) {
        this.projectSourcePath = projectSourcePath;
        this.jrePath = jrePath;
    }

    public void parseProject() throws IOException {
        File folder = new File(projectSourcePath);
        ArrayList<File> javaFiles = FileProcessor.listJavaFilesForFolder(folder);

        for (File fileEntry : javaFiles) {
            String content = FileUtils.readFileToString(fileEntry, "UTF-8");
            totalLines += FileProcessor.countLines(content);

            CompilationUnit parse = ASTParserUtil.parse(content.toCharArray(), projectSourcePath, jrePath);

            String packageName = ASTParserUtil.extractPackageName(parse);
            packages.add(packageName);

            totalClasses += MetricsCollector.countClassInfo(parse, fileEntry.getName());
            totalMethods += MetricsCollector.countMethodInfo(parse);
            totalAttributes += MetricsCollector.countAttributeInfo(parse);

            MetricsCollector.collectClassMetrics(parse, packageName, classMethodCounts, classAttributeCounts, 
                    classMethodLineCounts, maxParameterMethods, maxParameterCount, methodCallGraph);

            methodInfo.append(ASTPrinter.printMethodInfo(parse));
            variableInfo.append(ASTPrinter.printVariableInfo(parse));
            invocationInfo.append(ASTPrinter.printMethodInvocationInfo(parse));
        }
        staticMethodCallGraph = new HashMap<>(methodCallGraph);
    }

    public int getTotalClasses() { return totalClasses; }
    public int getTotalLines() { return totalLines; }
    public int getTotalMethods() { return totalMethods; }
    public int getTotalAttributes() { return totalAttributes; }
    public Set<String> getPackages() { return packages; }
    public Map<String, Integer> getClassMethodCounts() { return classMethodCounts; }
    public Map<String, Integer> getClassAttributeCounts() { return classAttributeCounts; }
    public Map<String, List<MetricsCollector.MethodInfo>> getClassMethodLineCounts() { return classMethodLineCounts; }
    public int getMaxParameterCount() { return maxParameterCount[0]; }
    public List<MetricsCollector.MethodInfo> getMaxParameterMethods() { return maxParameterMethods; }
    public Map<String, List<String>> getMethodCallGraph() { return methodCallGraph; }
    public String getMethodInfo() { return methodInfo.toString(); }
    public String getVariableInfo() { return variableInfo.toString(); }
    public String getInvocationInfo() { return invocationInfo.toString(); }

    public static Map<String, List<String>> getMethodCallGraphStatic() {
        return staticMethodCallGraph;
    }
}