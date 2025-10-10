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
import java.util.List;
import org.w3c.dom.Element;

/**
 * A ConfigOptionValueTranslation that is used for flags. It contains data a String for a set and
 * unset flag.
 */
public class FlagTranslation extends ConfigOptionValueTranslation {
    private String dataIfSet;
    private String dataIfNotSet;

    private boolean isRichestConfiguration;
    private List<FeatureConstraint> constraints;

    public FlagTranslation(Element xmlElement) {
        super(xmlElement);
        this.constraints = new ArrayList<>();
    }

    @Override
    protected void setFromXmlElement(Element xmlElement) {
        dataIfSet = xmlElement.getElementsByTagName("true").item(0).getTextContent();
        dataIfNotSet = xmlElement.getElementsByTagName("false").item(0).getTextContent();
        // check if maxValue element is added to true element
        if (xmlElement.getElementsByTagName("true").item(0).hasAttributes()
                && xmlElement
                                .getElementsByTagName("true")
                                .item(0)
                                .getAttributes()
                                .getNamedItem("maxValue")
                        != null) {
            isRichestConfiguration = true;
        }
    }

    public String getDataIfSet() {
        return dataIfSet;
    }

    public String getDataIfNotSet() {
        return dataIfNotSet;
    }

    public boolean isRichestConfiguration() {
        return isRichestConfiguration;
    }

    public List<FeatureConstraint> getConstraints() {
        return new ArrayList<>(constraints);
    }

    public void addConstraint(FeatureConstraint constraint) {
        this.constraints.add(constraint);
    }

    @Override
    public List<FeatureConstraint> getConstraintsForValue(Object configValue) {
        if (!(configValue instanceof Boolean)) {
            return new ArrayList<>();
        }

        boolean flagValue = (Boolean) configValue;
        String valueString = String.valueOf(flagValue);

        return constraints.stream()
                .filter(constraint -> constraint.appliesForValue(valueString))
                .toList();
    }
}
