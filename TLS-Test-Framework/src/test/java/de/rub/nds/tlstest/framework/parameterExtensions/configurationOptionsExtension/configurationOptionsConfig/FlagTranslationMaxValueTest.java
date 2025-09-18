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

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FlagTranslationMaxValueTest {

    @Test
    public void testMaxValueOnTrueElement() throws Exception {
        String xml =
                """
            <valueTranslation type="Flag">
                <true maxValue="true">enable-feature</true>
                <false>disable-feature</false>
            </valueTranslation>
            """;

        FlagTranslation translation = createFlagTranslation(xml);

        assertEquals("enable-feature", translation.getDataIfSet());
        assertEquals("disable-feature", translation.getDataIfNotSet());
        assertTrue(
                translation.isRichestConfiguration(),
                "When maxValue is on <true>, isRichestConfiguration should be true");
    }

    @Test
    public void testMaxValueOnFalseElement() throws Exception {
        // This is the critical test case from openssl-3_5_0.xml
        String xml =
                """
            <valueTranslation type="Flag">
                <true>no-sse2</true>
                <false maxValue="true"></false>
            </valueTranslation>
            """;

        FlagTranslation translation = createFlagTranslation(xml);

        assertEquals("no-sse2", translation.getDataIfSet());
        assertEquals("", translation.getDataIfNotSet());

        // Currently this will be false because the code only checks <true> element
        // This documents the current limitation
        assertFalse(
                translation.isRichestConfiguration(),
                "Current implementation only detects maxValue on <true> element, not on <false>");
    }

    @Test
    public void testNoMaxValueAttribute() throws Exception {
        String xml =
                """
            <valueTranslation type="Flag">
                <true>enable-feature</true>
                <false>disable-feature</false>
            </valueTranslation>
            """;

        FlagTranslation translation = createFlagTranslation(xml);

        assertEquals("enable-feature", translation.getDataIfSet());
        assertEquals("disable-feature", translation.getDataIfNotSet());
        assertFalse(
                translation.isRichestConfiguration(),
                "When no maxValue attribute is present, isRichestConfiguration should be false");
    }

    @Test
    public void testMaxValueFalseOnTrueElement() throws Exception {
        String xml =
                """
            <valueTranslation type="Flag">
                <true maxValue="false">enable-feature</true>
                <false>disable-feature</false>
            </valueTranslation>
            """;

        FlagTranslation translation = createFlagTranslation(xml);

        // Note: The current implementation doesn't check the value of maxValue attribute,
        // only its presence. This test documents this behavior.
        assertTrue(
                translation.isRichestConfiguration(),
                "Current implementation only checks presence of maxValue, not its value");
    }

    private FlagTranslation createFlagTranslation(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
        Element element = doc.getDocumentElement();
        return new FlagTranslation(element);
    }
}
