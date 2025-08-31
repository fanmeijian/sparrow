import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.*;

import java.util.*;

public class JavaCodeCompletion {

    public static void main(String[] args) {
        String source =
                "public class Test {\n" +
                        "  public void testMethod() {\n" +
                        "    System.\n" +  // <-- 模拟输入到“System.”
                        "  }\n" +
                        "}";

        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        // 设置编译环境 (模拟 classpath, bootclasspath)
        parser.setEnvironment(
                new String[] { "/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar" }, // classpath（必须有 java.lang.* 等类）
                new String[] { "." },              // sourcepath
                null,
                true
        );

        parser.setUnitName("Test.java");
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        parser.setCompilerOptions(options);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        for (IProblem problem : cu.getProblems()) {
            System.out.println("Problem: " + problem.getMessage());
        }

        // 通过 AST 和 Binding 分析当前位置类型，再返回推荐成员（模拟补全）
        cu.accept(new ASTVisitor() {
            public boolean visit(MethodInvocation node) {
                IMethodBinding binding = node.resolveMethodBinding();
                System.out.println("Method call: " + node + " => " + binding);
                return true;
            }

            public boolean visit(QualifiedName name) {
                IBinding binding = name.resolveBinding();
                System.out.println("Qualified name: " + name + " => " + binding);
                return true;
            }
        });
    }
}