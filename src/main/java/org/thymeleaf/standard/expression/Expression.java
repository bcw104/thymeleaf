/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2012, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.standard.expression;

import java.io.Serializable;

import org.thymeleaf.Configuration;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.util.Validate;



/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 1.1
 *
 */
public abstract class Expression implements IStandardExpression, Serializable {

    private static final long serialVersionUID = 1608378943284014151L;

    
    public static final char PARSING_PLACEHOLDER_CHAR = '\u00A7';
    
    public static final char NESTING_START_CHAR = '(';
    public static final char NESTING_END_CHAR = ')';
    
    
    
    protected Expression() {
        super();
    }
    
    
    public abstract String getStringRepresentation();
    
    
    @Override
    public String toString() {
        return getStringRepresentation();
    }

    
    
    static Expression parse(final String input) {

        Validate.notNull(input, "Input cannot be null");

        /*
         * PHASE 01: Decomposition (including unnesting parenthesis)
         */
        final ExpressionParsingState decomposition =
                ExpressionParsingUtil.decompose(input, ExpressionParsingDecompositionConfig.DECOMPOSE_ALL_AND_UNNEST);
        if (decomposition == null) {
            return null;
        }

        /*
         * PHASE 02: Composition
         */
        final ExpressionParsingState result = ExpressionParsingUtil.compose(decomposition);
        if (result == null || !result.hasExpressionAt(0)) {
            return null;
        }

        return result.get(0).getExpression();

    }
    

    
    

    
    
    
    
    static Object execute(final Configuration configuration, final IProcessingContext processingContext, 
            final Expression expression, final IStandardVariableExpressionEvaluator expressionEvaluator,
            final StandardExpressionExecutionContext expContext) {
        
        if (expression instanceof SimpleExpression) {
            return SimpleExpression.executeSimple(
                    configuration, processingContext, (SimpleExpression)expression, expressionEvaluator, expContext);
        }
        if (expression instanceof ComplexExpression) {
            return ComplexExpression.executeComplex(
                    configuration, processingContext, (ComplexExpression)expression, expContext);
        }

        throw new TemplateProcessingException("Unrecognized expression: " + expression.getClass().getName());
        
    }





    public Object execute(final Configuration configuration, final IProcessingContext processingContext) {
        return execute(configuration, processingContext, StandardExpressionExecutionContext.NORMAL);
    }


    public Object execute(final Configuration configuration, final IProcessingContext processingContext,
                        final StandardExpressionExecutionContext expContext) {

        Validate.notNull(configuration, "Configuration cannot be null");
        Validate.notNull(processingContext, "Processing context cannot be null");

        final IStandardVariableExpressionEvaluator variableExpressionEvaluator =
                StandardExpressions.getVariableExpressionEvaluator(configuration);

        final Object result = execute(configuration, processingContext, this, variableExpressionEvaluator, expContext);
        return LiteralValue.unwrap(result);

    }


    
}
