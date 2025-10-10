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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Translation for an option with a single value, e.g. 'fruit=apple'. In this example 'fruit' is the
 * identifier and 'apple' a possible value. All values that can appear must be covered.
 */
public class SingleValueOptionTranslation extends ConfigOptionValueTranslation {
    private String identifier;
    private final Map<String, String> valueTranslationMap;
    private String richestConfigurationLabel = "";
    private List<FeatureConstraint> constraints;

    public SingleValueOptionTranslation(Element xmlElement) {
        valueTranslationMap = new HashMap<>();
        this.constraints = new ArrayList<>();
        this.setFromXmlElement(xmlElement);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getValueTranslation(String key) {
        if (!valueTranslationMap.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Cannot get translation for key '"
                            + key
                            + "'. It was not configured in the config file. Found: "
                            + valueTranslationMap.keySet().stream()
                                    .collect(Collectors.joining(",")));
        }
        return valueTranslationMap.get(key);
    }

    public List<String> getOptions() {
        return valueTranslationMap.keySet().stream().collect(Collectors.toList());
    }

    @Override
    protected void setFromXmlElement(Element xmlElement) {
        try {

            this.identifier =
                    Objects.requireNonNull(
                                    XmlParseUtils.findElement(xmlElement, "identifier", true))
                            .getTextContent();
            NodeList valueElementList = xmlElement.getElementsByTagName("value");

            for (int optionEntryIdx = 0;
                    optionEntryIdx < valueElementList.getLength();
                    optionEntryIdx++) {
                Node valueNode = valueElementList.item(optionEntryIdx);
                if (valueNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element valueElement = (Element) valueNode;
                    boolean isRichest =
                            valueNode.hasAttributes()
                                    && valueNode.getAttributes().getNamedItem("maxValue") != null;
                    addValueTranslationByElement(valueElement, isRichest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    "Error while parsing OptionWithSingleValueTranslation.");
        }
    }

    private void addValueTranslationByElement(Element valueElement, boolean isRichest) {
        String key = valueElement.getAttribute("key");
        if (key.equals("")) {
            throw new IllegalArgumentException(
                    String.format(
                            "In OptionWithSingleValue translation with identifier '%s': <value> element does not contain required attribute 'key'.",
                            identifier));
        }
        if (valueTranslationMap.containsKey(key)) {
            throw new IllegalArgumentException(
                    String.format(
                            "In OptionWithSingleValue translation with identifier '%s': Key '%s' is defined multiple times.",
                            identifier, key));
        }
        String value = valueElement.getTextContent();
        if (isRichest) {
            richestConfigurationLabel = key;
        }
        valueTranslationMap.put(key, value);
    }

    public boolean isRichestConfiguration(String key) {
        return key.equals(richestConfigurationLabel);
    }

    public List<FeatureConstraint> getConstraints() {
        return new ArrayList<>(constraints);
    }

    public void addConstraint(FeatureConstraint constraint) {
        this.constraints.add(constraint);
    }

    @Override
    public List<FeatureConstraint> getConstraintsForValue(Object configValue) {
        if (!(configValue instanceof String)) {
            return new ArrayList<>();
        }

        String valueString = (String) configValue;

        return constraints.stream()
                .filter(constraint -> constraint.appliesForValue(valueString))
                .toList();
    }
}
