package Parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

public class ASTParserUtil {
    public static CompilationUnit parse(char[] classSource, String projectSourcePath, String jrePath) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(JavaCore.getOptions());
        parser.setUnitName("");
        String[] sources = { projectSourcePath };
        String[] classpath = { jrePath };
        parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
        parser.setSource(classSource);
        return (CompilationUnit) parser.createAST(null);
    }

    public static String extractPackageName(CompilationUnit parse) {
        PackageDeclaration packageDecl = parse.getPackage();
        return packageDecl != null ? packageDecl.getName().getFullyQualifiedName() : "[default]";
    }
}