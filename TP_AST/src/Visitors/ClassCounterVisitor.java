package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;

public class ClassCounterVisitor extends ASTVisitor {
    
    private List<String> classNames = new ArrayList<>();
    
    public boolean visit(TypeDeclaration node) {
        if (node.getParent() instanceof CompilationUnit) {
            classNames.add(node.getName().getIdentifier());
        }
        return true; 
    }
    
    public boolean visit(EnumDeclaration node) {
        if (node.getParent() instanceof CompilationUnit) {
            classNames.add(node.getName().getIdentifier());
        }
        return true;
    }
    

    public List<String> getClassNames() {
        return classNames;
    }

    public int getClassCount() {
        return classNames.size();
    }
}
