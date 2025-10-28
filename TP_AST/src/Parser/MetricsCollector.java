package Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import Visitors.AttributeCounterVisitor;
import Visitors.ClassCounterVisitor;
import Visitors.MethodDeclarationVisitor;
import Visitors.MethodInvocationVisitor;

public class MetricsCollector {
    public static class MethodInfo {
        String className;
        String methodName;
        int lineCount;
        int parameterCount;

        MethodInfo(String className, String methodName, int lineCount, int parameterCount) {
            this.className = className;
            this.methodName = methodName;
            this.lineCount = lineCount;
            this.parameterCount = parameterCount;
        }
    }

    public static int countClassInfo(CompilationUnit parse, String fileName) {
        ClassCounterVisitor visitor = new ClassCounterVisitor();
        parse.accept(visitor);
        return visitor.getClassCount();
    }

    public static int countMethodInfo(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        return visitor.getMethods().size();
    }

    public static int countAttributeInfo(CompilationUnit parse) {
        AttributeCounterVisitor visitor = new AttributeCounterVisitor();
        parse.accept(visitor);
        return visitor.getAttributeCount();
    }

    public static void collectClassMetrics(CompilationUnit parse, String packageName, 
            Map<String, Integer> classMethodCounts, Map<String, Integer> classAttributeCounts, 
            Map<String, List<MethodInfo>> classMethodLineCounts, 
            List<MethodInfo> maxParameterMethods, int[] maxParameterCount,
            Map<String, List<String>> methodCallGraph) {
        class ClassMetricsVisitor extends ClassCounterVisitor {
            private final CompilationUnit compilationUnit;
            private final String packageName;
            private final Map<String, Integer> methodCounts;
            private final Map<String, Integer> attributeCounts;
            private final Map<String, List<MethodInfo>> methodLineCounts;
            private final List<MethodInfo> maxParameterMethods;
            private final int[] maxParameterCount;
            private final Map<String, List<String>> methodCallGraph;

            ClassMetricsVisitor(CompilationUnit compilationUnit, String packageName, 
                    Map<String, Integer> methodCounts, Map<String, Integer> attributeCounts, 
                    Map<String, List<MethodInfo>> methodLineCounts, 
                    List<MethodInfo> maxParameterMethods, int[] maxParameterCount,
                    Map<String, List<String>> methodCallGraph) {
                this.compilationUnit = compilationUnit;
                this.packageName = packageName;
                this.methodCounts = methodCounts;
                this.attributeCounts = attributeCounts;
                this.methodLineCounts = methodLineCounts;
                this.maxParameterMethods = maxParameterMethods;
                this.maxParameterCount = maxParameterCount;
                this.methodCallGraph = methodCallGraph;
            }

            @Override
            public boolean visit(TypeDeclaration node) {
                String className = node.getName().getFullyQualifiedName();
                String fullClassName = packageName.equals("[default]") ? className : packageName + "." + className;

                MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
                node.accept(methodVisitor);
                int methodCount = methodVisitor.getMethods().size();
                methodCounts.put(fullClassName, methodCount);

                List<MethodInfo> methodInfos = new ArrayList<>();
                for (MethodDeclaration method : methodVisitor.getMethods()) {
                    String methodName = method.getName().toString();
                    String fullMethodName = fullClassName + "." + methodName;

                    int startLine = compilationUnit.getLineNumber(method.getStartPosition());
                    int endLine = compilationUnit.getLineNumber(method.getStartPosition() + method.getLength());
                    int lineCount = endLine - startLine + 1;

                    int paramCount = method.parameters().size();
                    if (paramCount > maxParameterCount[0]) {
                        maxParameterCount[0] = paramCount;
                        maxParameterMethods.clear();
                        maxParameterMethods.add(new MethodInfo(fullClassName, methodName, lineCount, paramCount));
                    } else if (paramCount == maxParameterCount[0]) {
                        maxParameterMethods.add(new MethodInfo(fullClassName, methodName, lineCount, paramCount));
                    }

                    methodInfos.add(new MethodInfo(fullClassName, methodName, lineCount, paramCount));

                    MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
                    method.accept(invocationVisitor);
                    List<String> calledMethods = new ArrayList<>();
                    for (MethodInvocation invocation : invocationVisitor.getMethods()) {
                        String calledMethodName = invocation.getName().toString();
                        System.out.println("Found invocation: " + calledMethodName + " in " + fullMethodName);
                        if (invocation.resolveMethodBinding() != null && invocation.resolveMethodBinding().getDeclaringClass() != null) {
                            String declaringClass = invocation.resolveMethodBinding().getDeclaringClass().getQualifiedName();
                            if (!declaringClass.startsWith("java.")) {
                                String calledFullName = declaringClass + "." + calledMethodName;
                                calledMethods.add(calledFullName);
                                System.out.println("Resolved call: " + fullMethodName + " -> " + calledFullName);
                            }
                        } else {
                            calledMethods.add("[unresolved]." + calledMethodName);
                            System.out.println("Unresolved call: " + fullMethodName + " -> [unresolved]." + calledMethodName);
                        }
                    }
                    methodCallGraph.put(fullMethodName, calledMethods);
                    System.out.println("Updated methodCallGraph for " + fullMethodName + ": " + calledMethods);
                }
                methodLineCounts.put(fullClassName, methodInfos);

                AttributeCounterVisitor attributeVisitor = new AttributeCounterVisitor();
                node.accept(attributeVisitor);
                attributeCounts.put(fullClassName, attributeVisitor.getAttributeCount());

                super.visit(node);
                return true;
            }
        }

        ClassMetricsVisitor visitor = new ClassMetricsVisitor(parse, packageName, 
                classMethodCounts, classAttributeCounts, classMethodLineCounts, 
                maxParameterMethods, maxParameterCount, methodCallGraph);
        parse.accept(visitor);
    }
}