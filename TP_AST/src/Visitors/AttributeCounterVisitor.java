package Visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;

public class AttributeCounterVisitor extends ASTVisitor {
    private int attributeCount = 0;

    @Override
    public boolean visit(FieldDeclaration node) {
        attributeCount++;
        return super.visit(node);
    }

    public int getAttributeCount() {
        return attributeCount;
    }
}