/*
 *  TLS-Test-Framework - A framework for modeling TLS tests
 *
 *  Copyright 2020 Ruhr University Bochum and
 *  TÜV Informationstechnik GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 */

package de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ConfigurationOptionValueTest {

    @Test
    public void testFlagConstructorWithRichestConfigTrue() {
        // Test flag set with richest config = true
        ConfigurationOptionValue value = new ConfigurationOptionValue(true, true);

        assertTrue(value.isFlag());
        assertTrue(value.isOptionSet());
        assertTrue(value.isRichestConfiguration());
        assertEquals("FLAG_SET", value.toString());
    }

    @Test
    public void testFlagConstructorWithRichestConfigFalse() {
        // Test flag set with richest config = false
        ConfigurationOptionValue value = new ConfigurationOptionValue(true, false);

        assertTrue(value.isFlag());
        assertTrue(value.isOptionSet());
        assertFalse(value.isRichestConfiguration());
        assertEquals("FLAG_SET", value.toString());
    }

    @Test
    public void testFlagNotSetWithRichestConfigTrue() {
        // Test flag not set with richest config = true
        ConfigurationOptionValue value = new ConfigurationOptionValue(false, true);

        assertTrue(value.isFlag());
        assertFalse(value.isOptionSet());
        assertTrue(value.isRichestConfiguration());
        assertEquals("FLAG_NOT_SET", value.toString());
    }

    @Test
    public void testFlagNotSetWithRichestConfigFalse() {
        // Test flag not set with richest config = false
        ConfigurationOptionValue value = new ConfigurationOptionValue(false, false);

        assertTrue(value.isFlag());
        assertFalse(value.isOptionSet());
        assertFalse(value.isRichestConfiguration());
        assertEquals("FLAG_NOT_SET", value.toString());
    }

    @Test
    public void testStringConstructorWithRichestConfigTrue() {
        // Test single string value constructor with richest config = true
        ConfigurationOptionValue value = new ConfigurationOptionValue("test-option", true);

        assertFalse(value.isFlag());
        assertTrue(value.isOptionSet());
        assertTrue(value.isRichestConfiguration());
        assertEquals("test-option", value.toString());
    }

    @Test
    public void testStringConstructorWithRichestConfigFalse() {
        // Test single string value constructor with richest config = false
        ConfigurationOptionValue value = new ConfigurationOptionValue("test-option", false);

        assertFalse(value.isFlag());
        assertTrue(value.isOptionSet());
        assertFalse(value.isRichestConfiguration());
        assertEquals("test-option", value.toString());
    }
}
