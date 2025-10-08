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
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rub.nds.anvilcore.model.parameter.ParameterScope;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlstest.framework.TestContext;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.CommonBuildParameterScope;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterScope;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterType;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigurationOptionValue;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.ConfigurationOptionsConfig;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.FeatureConstraint;
import de.rwth.swc.coffee4j.model.constraints.ConstraintBuilder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class ConfigurationOptionDerivationParameter
        extends DerivationParameter<Config, ConfigurationOptionValue> {

    // We use Config.class throughout these parameters allthough they are applied when building the
    // containers and do not affect the config.
    public ConfigurationOptionDerivationParameter(ConfigOptionParameterType type) {
        super(
                ConfigurationOptionValue.class,
                Config.class,
                new ParameterIdentifier(type, ConfigOptionParameterScope.DEFAULT));
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
    public ConfigurationOptionValue getMaxFeatureValue(
            ConfigurationOptionsConfig configOptionsConfig) {
        List<DerivationParameter<Config, ConfigurationOptionValue>> parameterValues =
                getParameterValuesForConfig(configOptionsConfig);
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

    public abstract List<DerivationParameter<Config, ConfigurationOptionValue>>
            getParameterValuesForConfig(ConfigurationOptionsConfig configOptionsConfig);

    public ConfigurationOptionDerivationParameter getDefaultValueParameter() {
        return (ConfigurationOptionDerivationParameter) generateValue(getDefaultValue());
    }

    public ConfigurationOptionDerivationParameter getMaxFeatureValueParameter(
            ConfigurationOptionsConfig configOptionsConfig) {
        return (ConfigurationOptionDerivationParameter)
                generateValue(getMaxFeatureValue(configOptionsConfig));
    }

    @JsonValue
    public String jsonValue() {
        return getParameterIdentifier().name() + "=" + getSelectedValue().toString();
    }

    public String toString() {
        return jsonValue();
    }

    public List<ConditionalConstraint> getRegexFilterConstraints(
            ConfigurationOptionsConfig configOptionsConfig, TestContext testContext) {
        List<ConditionalConstraint> constraints = new LinkedList<>();

        if (this.getParameterIdentifier().getParameterType()
                == ConfigOptionParameterType.COMMON_BUILD_FLAG) {
            List<FeatureConstraint> buildflagConstraints =
                    configOptionsConfig.getConstraintsForConfigOption(
                            this.getParameterIdentifier());
            buildflagConstraints =
                    buildflagConstraints.stream()
                            .filter(
                                    constraint ->
                                            constraint
                                                    .getParameterIdentifier()
                                                    .contains(
                                                            CommonBuildParameterScope
                                                                    .SCOPE_IDENTIFIER))
                            .toList();

            for (FeatureConstraint newConstraint : buildflagConstraints) {
                ParameterIdentifier targetIdentifier =
                        new ParameterIdentifier(
                                ConfigOptionParameterType.COMMON_BUILD_FLAG,
                                configOptionsConfig.getCommonScopeForType(
                                        newConstraint.getParameterIdentifier()));
                constraints.add(
                        getRegexFlagConstraint(
                                targetIdentifier, newConstraint, configOptionsConfig));
            }
        }

        return constraints;
    }

    private ConditionalConstraint getRegexFlagConstraint(
            ParameterIdentifier targetParameter,
            FeatureConstraint constraint,
            ConfigurationOptionsConfig configOptionsConfig) {
        Set<ParameterIdentifier> requiredDerivations = new HashSet<>();
        requiredDerivations.add(targetParameter);
        requiredDerivations.add(this.getParameterIdentifier());

        return new ConditionalConstraint(
                requiredDerivations,
                ConstraintBuilder.constrain(
                                this.getParameterIdentifier().name(), targetParameter.name())
                        .by(
                                (ConfigurationOptionDerivationParameter restrictionDefining,
                                        ConfigurationOptionDerivationParameter targetParam) -> {

                                    // For all options of a setup, check if constraint applies to
                                    // value of option and check if regex matches
                                    String valueString =
                                            restrictionDefining.getSelectedValue().toString();
                                    if (restrictionDefining.getSelectedValue().isFlag()) {
                                        valueString =
                                                configOptionsConfig.translateOptionValue(
                                                        restrictionDefining);
                                    }
                                    if (constraint.appliesForValue(valueString)) {
                                        String regex = constraint.getRegexFilter();
                                        String targetValue =
                                                targetParam.getSelectedValue().toString();
                                        if (targetParam.getSelectedValue().isFlag()) {
                                            targetValue =
                                                    configOptionsConfig.translateOptionValue(
                                                            targetParam);
                                        }
                                        if (regex != null && targetValue.matches(regex)) {
                                            return false;
                                        }
                                    }
                                    return true;
                                }));
    }
}
