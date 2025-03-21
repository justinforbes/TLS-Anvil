/*
 *  TLS-Test-Framework - A framework for modeling TLS tests
 *
 *  Copyright 2020 Ruhr University Bochum and
 *  TÜV Informationstechnik GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 */

package de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter;

import com.fasterxml.jackson.annotation.JsonValue;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rub.nds.anvilcore.model.parameter.ParameterScope;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterScope;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterType;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigurationOptionValue;
import java.util.List;

public abstract class ConfigurationOptionDerivationParameter
        extends DerivationParameter<Config, ConfigurationOptionValue> {

    // We use Config.class throughout these parameters allthough they are applied when building the
    // containers and do not affect the config.
    public ConfigurationOptionDerivationParameter(ConfigOptionParameterType type) {
        super(ConfigurationOptionValue.class, Config.class, new ParameterIdentifier(type, ConfigOptionParameterScope.DEFAULT));
    }

    public ConfigurationOptionDerivationParameter(
            ConfigOptionParameterType type, ParameterScope scope) {
        super(ConfigurationOptionValue.class, Config.class, new ParameterIdentifier(type, scope));
    }

    /**
     * Returns the value that results int the most feature-rich library build. It is required to
     * create an upper boundary for prefiltering test cases using the MethodCondition annotation.
     *
     * <p>If the option does not touch any features at all 'ConfigurationOptionValue(false)' can be
     * returned.
     *
     * @return the option value resulting int the most feature-rich library build
     */
    public ConfigurationOptionValue getMaxFeatureValue() {
        List<DerivationParameter<Config, ConfigurationOptionValue>> parameterValues =
                getParameterValues(null);
        return parameterValues.stream()
                .filter(parameter -> parameter.getSelectedValue().isRichestConfiguration())
                .findFirst()
                .orElse(parameterValues.get(0))
                .getSelectedValue();
    }

    /**
     * Returns the implicit value that is chosen if a configuration option is not used. Must be
     * overridden for non flag values.
     *
     * @return the default value
     */
    public ConfigurationOptionValue getDefaultValue() {
        // Default (Override for non-flag values)
        return new ConfigurationOptionValue(false, true);
    }

    public ConfigurationOptionDerivationParameter getDefaultValueParameter() {
        return (ConfigurationOptionDerivationParameter) generateValue(getDefaultValue());
    }

    public ConfigurationOptionDerivationParameter getMaxFeatureValueParameter() {
        return (ConfigurationOptionDerivationParameter) generateValue(getMaxFeatureValue());
    }

    @JsonValue
    public String jsonValue() {
        return getSelectedValue().toString();
    }
}
