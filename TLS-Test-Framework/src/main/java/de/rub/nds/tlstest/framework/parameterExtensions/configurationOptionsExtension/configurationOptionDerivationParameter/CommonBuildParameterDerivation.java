package de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter;

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rub.nds.anvilcore.model.parameter.ParameterScope;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlstest.framework.TestContextRegistry;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.CommonBuildParameterScope;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterType;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigurationOptionValue;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.ConfigOptionValueTranslation;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.ConfigurationOptionsConfig;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.FlagTranslation;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.SingleValueOptionTranslation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CommonBuildParameterDerivation extends ConfigurationOptionDerivationParameter {

    public CommonBuildParameterDerivation() {
        super(ConfigOptionParameterType.COMMON_BUILD_FLAG);
    }

    public CommonBuildParameterDerivation(ParameterScope scope) {
        super(ConfigOptionParameterType.COMMON_BUILD_FLAG, scope);
    }

    public CommonBuildParameterDerivation(
            ConfigurationOptionValue selectedValue, ParameterScope scope) {
        this(scope);
        setSelectedValue(selectedValue);
    }

    @Override
    public void applyToConfig(Config config, DerivationScope derivationScope) {
        throw new UnsupportedOperationException("Build flags must never be applied to the config");
    }

    @Override
    public List<DerivationParameter<Config, ConfigurationOptionValue>> getParameterValues(
            DerivationScope derivationScope) {
        ConfigurationOptionsConfig configOptionsConfig =
                TestContextRegistry.byExtensionContext(derivationScope.getExtensionContext())
                        .getConfigurationOptionsExtension()
                        .getDerivationManager()
                        .getConfigurationOptionsConfig();

        List<DerivationParameter<Config, ConfigurationOptionValue>> parameterValues =
                new LinkedList<>();
        ConfigOptionValueTranslation translation =
                configOptionsConfig.getOptionsToTranslationMap().get(getParameterIdentifier());
        if (translation instanceof FlagTranslation) {
            parameterValues.add(
                    new CommonBuildParameterDerivation(
                            new ConfigurationOptionValue(
                                    true, ((FlagTranslation) translation).isRichestConfiguration()),
                            getParameterIdentifier().getParameterScope()));
            parameterValues.add(
                    new CommonBuildParameterDerivation(
                            new ConfigurationOptionValue(
                                    false,
                                    !((FlagTranslation) translation).isRichestConfiguration()),
                            getParameterIdentifier().getParameterScope()));
        } else if (translation instanceof SingleValueOptionTranslation) {
            SingleValueOptionTranslation singleValueOptionTranslation =
                    (SingleValueOptionTranslation) translation;
            singleValueOptionTranslation
                    .getOptions()
                    .forEach(
                            option -> {
                                parameterValues.add(
                                        new CommonBuildParameterDerivation(
                                                new ConfigurationOptionValue(
                                                        option,
                                                        singleValueOptionTranslation
                                                                .isRichestConfiguration(option)),
                                                getParameterIdentifier().getParameterScope()));
                            });
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported translation type + " + translation.getClass());
        }
        return parameterValues;
    }

    @Override
    protected DerivationParameter<Config, ConfigurationOptionValue> generateValue(
            ConfigurationOptionValue selectedValue) {
        return new CommonBuildParameterDerivation(
                selectedValue, getParameterIdentifier().getParameterScope());
    }

    public static List<CommonBuildParameterScope> getCommonDerivationScopes(
            Collection<ParameterIdentifier> parameterIdentifiers) {
        List<CommonBuildParameterScope> commonDerivations = new LinkedList<>();
        for (ParameterIdentifier parameterIdentifier : parameterIdentifiers) {
            if (parameterIdentifier.getParameterType()
                    == ConfigOptionParameterType.COMMON_BUILD_FLAG) {
                commonDerivations.add(
                        (CommonBuildParameterScope) parameterIdentifier.getParameterScope());
            }
        }
        return commonDerivations;
    }

    public static boolean isOptionListed(
            String option, Collection<ParameterIdentifier> parameterIdentifiers) {
        List<CommonBuildParameterScope> scopes = getCommonDerivationScopes(parameterIdentifiers);
        return scopes.stream().anyMatch(scope -> scope.getParameterSpecifier().equals(option));
    }
}
