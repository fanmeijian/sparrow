package cn.sparrowmini.bpm.server.util;


import cn.sparrowmini.bpm.server.process.GlobalVariableMap;
import cn.sparrowmini.bpm.server.process.model.GlobalVariable;
import org.drools.compiler.compiler.AnalysisResult;
import org.drools.compiler.compiler.DescrBuildError;
import org.drools.compiler.kie.builder.impl.AbstractKieProject;
import org.drools.compiler.lang.descr.ActionDescr;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.drools.compiler.shade.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.drools.model.DeclarationSource;
import org.drools.core.rule.Declaration;
import org.drools.model.DomainClassMetadata;
import org.drools.model.Window;
import org.drools.modelcompiler.builder.generator.Consequence;
import org.drools.mvel.MVELDialectRuntimeData;
import org.drools.mvel.builder.MVELAnalysisResult;
import org.drools.mvel.builder.MVELDialect;
import org.drools.mvel.expr.MVELCompilationUnit;
import org.jbpm.process.builder.ProcessBuildContext;
import org.jbpm.process.builder.dialect.mvel.MVELActionBuilder;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.impl.MVELAction;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.mvel2.ParserContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyMVELActionBuilder extends MVELActionBuilder {
    @Override
    public void buildAction(final PackageBuildContext context,
                            final DroolsAction action,
                            final ActionDescr actionDescr,
                            final ContextResolver contextResolver,
                            final MVELDialect dialect,
                            final MVELAnalysisResult analysis,
                            final String text,
                            Map<String, Class<?>> variables) throws Exception {

        variables.put("global", GlobalVariableMap.class);
        variables.put("mvelUtils", MvelUtils.class);
        Set<String> variableNames = analysis.getNotBoundedIdentifiers();
        if (contextResolver != null) {
            for (String variableName: variableNames) {
                if ( analysis.getMvelVariables().keySet().contains( variableName )
                        ||  variableName.equals( "kcontext" )
                        || variableName.equals( "context" ) || variableName.equals( "mvelUtils" ) || variableName.equals( "global" ) ) {
                    continue;
                }
                VariableScope variableScope
                        = (VariableScope) contextResolver.resolveContext(VariableScope.VARIABLE_SCOPE, variableName);
                if (variableScope == null) {
                    context.getErrors().add(
                            new DescrBuildError(context.getParentDescr(),
                                    actionDescr,
                                    null,
                                    "Could not find variable '" + variableName + "' "
                                            + "for action '" + actionDescr.getText() + "'" ) );
                } else {
                    variables.put(variableName,
                            context.getDialect().getTypeResolver().resolveType(
                                    variableScope.findVariable(variableName).getType().getStringType()));
                }
            }
        }



        MVELCompilationUnit unit = dialect.getMVELCompilationUnit( text,
                analysis,
                null,
                null,
                variables,
                context,
                "context",
                org.kie.api.runtime.process.ProcessContext.class,
                false,
                MVELCompilationUnit.Scope.EXPRESSION);
        MVELAction expr = new MVELAction( unit, context.getDialect().getId() );
        action.setMetaData("Action",  expr );

        MVELDialectRuntimeData data
                = (MVELDialectRuntimeData) context.getPkg().getDialectRuntimeRegistry().getDialectData( dialect.getId() );
        data.addCompileable( action, expr );

        expr.compile( data );

        collectTypes("MVELDialect", analysis, (ProcessBuildContext) context);
    }




    @Override
    public void build(final PackageBuildContext context,
                      final DroolsAction action,
                      final ActionDescr actionDescr,
                      final ContextResolver contextResolver) {

        String text = processMacros( actionDescr.getText() );
        Map<String, Class<?>> variables = new HashMap<String,Class<?>>();
        // 注入 mvelUtils
//        variables.put("mvelUtils", cn.sparrowmini.bpm.server.util.MvelUtils.class);




        try {

            MVELDialect dialect = (MVELDialect) context.getDialect("mvel");
            MVELAnalysisResult analysis = getAnalysis(context, actionDescr, dialect, text, variables);
            if (analysis == null) {
                // not possible to get the analysis results
                return;
            }


//
//            System.out.println(analysis);
            this.buildAction(context,
                    action,
                    actionDescr,
                    contextResolver,
                    dialect,
                    analysis,
                    text,
                    variables);
        } catch ( final Exception e ) {
            context.getErrors().add( new DescrBuildError( context.getParentDescr(),
                    actionDescr,
                    null,
                    "Unable to build expression for action '" + actionDescr.getText() + "' :" + e ) );
        }
    }

}
