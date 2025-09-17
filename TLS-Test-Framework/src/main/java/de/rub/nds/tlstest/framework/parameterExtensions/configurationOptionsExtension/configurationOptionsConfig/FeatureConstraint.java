/*
 *  TLS-Test-Framework - A framework for modeling TLS tests
 *
 *  Copyright 2020 Ruhr University Bochum and
 *  TÜV Informationstechnik GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a feature constraint that applies regex-based exclusion filtering to specific
 * parameter types based on configuration option values. Each constraint defines: 1. Which parameter
 * identifier it applies to (e.g., CIPHER_SUITE, NAMED_GROUP) 2. The regex pattern to use for
 * exclusion - parameter values that MATCH the pattern are EXCLUDED 3. For which configuration
 * option values the constraint should be applied
 */
public class FeatureConstraint {
    private final String parameterIdentifier;
    private final String regexFilter;
    private final Set<String> applicableValues;
    private final Pattern compiledPattern;

    /**
     * Creates a new FeatureConstraint
     *
     * @param parameterIdentifier The target parameter identifier (e.g., "CIPHER_SUITE")
     * @param regexFilter The regex pattern to use for exclusion (matching values will be excluded)
     * @param applicableValues Set of configuration values for which this constraint applies
     * @throws IllegalArgumentException if the regex pattern is invalid
     */
    public FeatureConstraint(
            String parameterIdentifier, String regexFilter, Set<String> applicableValues) {
        this.parameterIdentifier =
                Objects.requireNonNull(parameterIdentifier, "Parameter identifier cannot be null");
        this.regexFilter = Objects.requireNonNull(regexFilter, "Regex filter cannot be null");
        this.applicableValues =
                new HashSet<>(
                        Objects.requireNonNull(
                                applicableValues, "Applicable values cannot be null"));

        try {
            this.compiledPattern = Pattern.compile(regexFilter);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + regexFilter, e);
        }
    }

    /** Creates a FeatureConstraint for a single applicable value */
    public FeatureConstraint(
            String parameterIdentifier, String regexFilter, String applicableValue) {
        this(parameterIdentifier, regexFilter, Set.of(applicableValue));
    }

    /**
     * @return The parameter identifier this constraint applies to
     */
    public String getParameterIdentifier() {
        return parameterIdentifier;
    }

    /**
     * @return The regex filter pattern as a string (for exclusion)
     */
    public String getRegexFilter() {
        return regexFilter;
    }

    /**
     * @return The compiled regex pattern for efficient matching
     */
    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    /**
     * @return Set of configuration values for which this constraint applies
     */
    public Set<String> getApplicableValues() {
        return new HashSet<>(applicableValues);
    }

    /**
     * Checks if this constraint applies for the given configuration value
     *
     * @param configValue The configuration value to check
     * @return true if the constraint applies for this value
     */
    public boolean appliesForValue(String configValue) {
        return applicableValues.contains(configValue);
    }

    /**
     * Tests if the given parameter value matches this constraint's regex pattern. Note: This is
     * used for exclusion - if this returns true, the parameter should be excluded.
     *
     * @param parameterValue The parameter value to test
     * @return true if the value matches the regex pattern (and should be excluded)
     */
    public boolean matches(String parameterValue) {
        if (parameterValue == null) {
            return false;
        }
        return compiledPattern.matcher(parameterValue).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureConstraint that = (FeatureConstraint) o;
        return Objects.equals(parameterIdentifier, that.parameterIdentifier)
                && Objects.equals(regexFilter, that.regexFilter)
                && Objects.equals(applicableValues, that.applicableValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterIdentifier, regexFilter, applicableValues);
    }

    @Override
    public String toString() {
        return "FeatureConstraint{"
                + "parameterIdentifier='"
                + parameterIdentifier
                + '\''
                + ", regexFilter='"
                + regexFilter
                + '\''
                + ", applicableValues="
                + applicableValues
                + '}';
    }
}
