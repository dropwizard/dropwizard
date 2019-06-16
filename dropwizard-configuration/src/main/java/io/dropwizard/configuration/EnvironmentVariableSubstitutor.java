package io.dropwizard.configuration;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;
import org.apache.commons.text.matcher.StringMatcher;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom {@link StringSubstitutor} using environment variables as lookup source.
 */
public class EnvironmentVariableSubstitutor extends StringSubstitutor {
    private final boolean strict;

    public EnvironmentVariableSubstitutor() {
        this(true, false);
    }

    public EnvironmentVariableSubstitutor(boolean strict) {
        this(strict, false);
    }

    /**
     * @param strict                  {@code true} if looking up undefined environment variables should throw a
     *                                {@link UndefinedEnvironmentVariableException}, {@code false} otherwise.
     * @param substitutionInVariables a flag whether substitution is done in variable names.
     * @see org.apache.commons.text.StringSubstitutor#setEnableSubstitutionInVariables(boolean)
     */
    public EnvironmentVariableSubstitutor(boolean strict, boolean substitutionInVariables) {
        super(new EnvironmentVariableLookup());
        this.strict = strict;
        this.setEnableSubstitutionInVariables(substitutionInVariables);
    }

    @Override
    protected boolean substitute(TextStringBuilder buf, int offset, int length) {
        return substitute(buf, offset, length, null) > 0;
    }

    /**
     * Checks if the specified variable is already in the stack (list) of variables.
     *
     * @param varName
     *            the variable name to check
     * @param priorVariables
     *            the list of prior variables
     */
    private void checkCyclicSubstitution(final String varName, final List<String> priorVariables) {
        if (!priorVariables.contains(varName)) {
            return;
        }
        final TextStringBuilder buf = new TextStringBuilder(256);
        buf.append("Infinite loop in property interpolation of ");
        buf.append(priorVariables.remove(0));
        buf.append(": ");
        buf.appendWithSeparators(priorVariables, "->");
        throw new IllegalStateException(buf.toString());
    }

    /**
     * Implementation is from commons-text with a modification to optionally reject variables resolved to null.
     * https://github.com/apache/commons-text/blob/commons-text-1.6/src/main/java/org/apache/commons/text/StringSubstitutor.java#L1248-L1381
     *
     * This implementation will no longer be necessary once commons-text 1.7 is released and allows
     * an exception to be thrown on undefined variables
     */
    private int substitute(TextStringBuilder buf, int offset, int length, @Nullable List<String> priorVariables) {
        final StringMatcher pfxMatcher = getVariablePrefixMatcher();
        final StringMatcher suffMatcher = getVariableSuffixMatcher();
        final char escape = getEscapeChar();
        final StringMatcher valueDelimMatcher = getValueDelimiterMatcher();
        final boolean substitutionInVariablesEnabled = isEnableSubstitutionInVariables();
        final boolean substitutionInValuesDisabled = isDisableSubstitutionInValues();

        final boolean top = priorVariables == null;
        boolean altered = false;
        int lengthChange = 0;
        char[] chars = buf.toCharArray();
        int bufEnd = offset + length;
        int pos = offset;
        while (pos < bufEnd) {
            final int startMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd);
            if (startMatchLen == 0) {
                pos++;
            } else {
                // found variable start marker
                if (pos > offset && chars[pos - 1] == escape) {
                    // escaped
                    if (isPreserveEscapes()) {
                        pos++;
                        continue;
                    }
                    buf.deleteCharAt(pos - 1);
                    chars = buf.toCharArray(); // in case buffer was altered
                    lengthChange--;
                    altered = true;
                    bufEnd--;
                } else {
                    // find suffix
                    final int startPos = pos;
                    pos += startMatchLen;
                    int endMatchLen = 0;
                    int nestedVarCount = 0;
                    while (pos < bufEnd) {
                        if (substitutionInVariablesEnabled && pfxMatcher.isMatch(chars, pos, offset, bufEnd) != 0) {
                            // found a nested variable start
                            endMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd);
                            nestedVarCount++;
                            pos += endMatchLen;
                            continue;
                        }

                        endMatchLen = suffMatcher.isMatch(chars, pos, offset, bufEnd);
                        if (endMatchLen == 0) {
                            pos++;
                        } else {
                            // found variable end marker
                            if (nestedVarCount == 0) {
                                String varNameExpr = new String(chars, startPos + startMatchLen,
                                    pos - startPos - startMatchLen);
                                if (substitutionInVariablesEnabled) {
                                    final TextStringBuilder bufName = new TextStringBuilder(varNameExpr);
                                    substitute(bufName, 0, bufName.length());
                                    varNameExpr = bufName.toString();
                                }
                                pos += endMatchLen;
                                final int endPos = pos;

                                String varName = varNameExpr;
                                String varDefaultValue = null;

                                if (valueDelimMatcher != null) {
                                    final char[] varNameExprChars = varNameExpr.toCharArray();
                                    int valueDelimiterMatchLen = 0;
                                    for (int i = 0; i < varNameExprChars.length; i++) {
                                        // if there's any nested variable when nested variable substitution disabled,
                                        // then stop resolving name and default value.
                                        if (!substitutionInVariablesEnabled && pfxMatcher.isMatch(varNameExprChars, i,
                                            i, varNameExprChars.length) != 0) {
                                            break;
                                        }
                                        if (valueDelimMatcher.isMatch(varNameExprChars, i, 0,
                                            varNameExprChars.length) != 0) {
                                            valueDelimiterMatchLen = valueDelimMatcher.isMatch(varNameExprChars, i, 0,
                                                varNameExprChars.length);
                                            varName = varNameExpr.substring(0, i);
                                            varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                                            break;
                                        }
                                    }
                                }

                                // on the first call initialize priorVariables
                                if (priorVariables == null) {
                                    priorVariables = new ArrayList<>();
                                    priorVariables.add(new String(chars, offset, length + lengthChange));
                                }

                                // handle cyclic substitution
                                checkCyclicSubstitution(varName, priorVariables);
                                priorVariables.add(varName);

                                // resolve the variable
                                String varValue = resolveVariable(varName, buf, startPos, endPos);
                                if (varValue == null) {
                                    varValue = varDefaultValue;
                                }

                                if (varValue != null) {
                                    final int varLen = varValue.length();
                                    buf.replace(startPos, endPos, varValue);
                                    altered = true;
                                    int change = 0;
                                    if (!substitutionInValuesDisabled) { // recursive replace
                                        change = substitute(buf, startPos, varLen, priorVariables);
                                    }
                                    change = change + varLen - (endPos - startPos);
                                    pos += change;
                                    bufEnd += change;
                                    lengthChange += change;
                                    chars = buf.toCharArray(); // in case buffer was altered
                                } else if (strict) {
                                    throw new UndefinedEnvironmentVariableException("The environment variable '" + varName
                                        + "' is not defined; could not substitute the expression '${"
                                        + varName + "}'.");
                                }

                                // remove variable from the cyclic stack
                                priorVariables.remove(priorVariables.size() - 1);
                                break;
                            }
                            nestedVarCount--;
                            pos += endMatchLen;
                        }
                    }
                }
            }
        }
        if (top) {
            return altered ? 1 : 0;
        }
        return lengthChange;
    }
}
