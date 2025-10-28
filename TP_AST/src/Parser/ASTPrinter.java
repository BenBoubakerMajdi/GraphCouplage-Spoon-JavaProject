package Parser;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import Visitors.MethodDeclarationVisitor;
import Visitors.MethodInvocationVisitor;
import Visitors.VariableDeclarationFragmentVisitor;

// Class to generate string representations of AST details for GUI display
public class ASTPrinter {
    // Generates a string of method names and return types from a CompilationUnit
    public static String printMethodInfo(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        StringBuilder sb = new StringBuilder();
        for (MethodDeclaration method : visitor.getMethods()) {
            sb.append("Method name: ").append(method.getName())
              .append(" Return type: ").append(method.getReturnType2()).append("\n");
        }
        return sb.toString();
    }

    // Generates a string of variable names and initializers within methods
    public static String printVariableInfo(CompilationUnit parse) {
        MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
        parse.accept(visitor1);
        StringBuilder sb = new StringBuilder();
        for (MethodDeclaration method : visitor1.getMethods()) {
            VariableDeclarationFragmentVisitor visitor2 = new VariableDeclarationFragmentVisitor();
            method.accept(visitor2);
            for (VariableDeclarationFragment variableDeclarationFragment : visitor2.getVariables()) {
                sb.append("variable name: ").append(variableDeclarationFragment.getName())
                  .append(" variable Initializer: ").append(variableDeclarationFragment.getInitializer()).append("\n");
            }
        }
        return sb.toString();
    }

    // Generates a string of method invocations within methods
    public static String printMethodInvocationInfo(CompilationUnit parse) {
        MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
        parse.accept(visitor1);
        StringBuilder sb = new StringBuilder();
        for (MethodDeclaration method : visitor1.getMethods()) {
            MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
            method.accept(visitor2);
            for (MethodInvocation methodInvocation : visitor2.getMethods()) {
                sb.append("method ").append(method.getName())
                  .append(" invoc method ").append(methodInvocation.getName()).append("\n");
            }
        }
        return sb.toString();
    }
}