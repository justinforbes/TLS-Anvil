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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

public class FeatureConstraintTest {

    @Test
    public void testBasicConstraintCreation() {
        FeatureConstraint constraint = new FeatureConstraint("CIPHER_SUITE", ".*AES.*", "true");

        assertEquals("CIPHER_SUITE", constraint.getParameterIdentifier());
        assertEquals(".*AES.*", constraint.getRegexFilter());
        assertTrue(constraint.appliesForValue("true"));
        assertFalse(constraint.appliesForValue("false"));
    }

    @Test
    public void testRegexMatchingForExclusion() {
        FeatureConstraint constraint =
                new FeatureConstraint("CIPHER_SUITE", ".*AES.*", Set.of("true"));

        // These should return true (meaning they match and should be EXCLUDED)
        assertTrue(constraint.matches("TLS_RSA_WITH_AES_128_CBC_SHA"));
        assertTrue(constraint.matches("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));

        // These should return false (meaning they don't match and should be INCLUDED)
        assertFalse(constraint.matches("TLS_RSA_WITH_3DES_EDE_CBC_SHA"));
        assertFalse(constraint.matches(null));
    }

    @Test
    public void testMultipleApplicableValues() {
        FeatureConstraint constraint =
                new FeatureConstraint("NAMED_GROUP", ".*P256.*", Set.of("OPTION1", "OPTION2"));

        assertTrue(constraint.appliesForValue("OPTION1"));
        assertTrue(constraint.appliesForValue("OPTION2"));
        assertFalse(constraint.appliesForValue("OPTION3"));
    }

    @Test
    public void testInvalidRegex() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new FeatureConstraint("CIPHER_SUITE", "[invalid", "true");
                });
    }

    @Test
    public void testNullValues() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    new FeatureConstraint(null, ".*", "true");
                });

        assertThrows(
                NullPointerException.class,
                () -> {
                    new FeatureConstraint("CIPHER_SUITE", null, "true");
                });

        assertThrows(
                NullPointerException.class,
                () -> {
                    new FeatureConstraint("CIPHER_SUITE", ".*", (Set<String>) null);
                });
    }
}
