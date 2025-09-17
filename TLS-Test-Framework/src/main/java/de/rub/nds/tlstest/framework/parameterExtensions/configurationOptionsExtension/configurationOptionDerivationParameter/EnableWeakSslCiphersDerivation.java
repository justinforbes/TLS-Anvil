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

import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterType;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigurationOptionValue;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.ConfigurationOptionsConfig;
import java.util.LinkedList;
import java.util.List;

public class EnableWeakSslCiphersDerivation extends ConfigurationOptionDerivationParameter {
    public EnableWeakSslCiphersDerivation() {
        super(ConfigOptionParameterType.ENABLE_WEAK_SSL_CIPHERS);
    }

    public EnableWeakSslCiphersDerivation(ConfigurationOptionValue selectedValue) {
        this();
        setSelectedValue(selectedValue);
    }

    @Override
    public void applyToConfig(Config config, DerivationScope derivationScope) {}

    @Override
    public List<DerivationParameter<Config, ConfigurationOptionValue>> getParameterValues(
            DerivationScope derivationScope) {
        List<DerivationParameter<Config, ConfigurationOptionValue>> parameterValues =
                new LinkedList<>();
        parameterValues.add(
                new EnableWeakSslCiphersDerivation(new ConfigurationOptionValue(true, true)));
        parameterValues.add(
                new EnableWeakSslCiphersDerivation(new ConfigurationOptionValue(false, false)));

        return parameterValues;
    }

    @Override
    protected DerivationParameter<Config, ConfigurationOptionValue> generateValue(
            ConfigurationOptionValue selectedValue) {
        return new EnableWeakSslCiphersDerivation(selectedValue);
    }

    @Override
    public List<DerivationParameter<Config, ConfigurationOptionValue>> getParameterValuesForConfig(
            ConfigurationOptionsConfig configOptionsConfig) {
        return getParameterValues(null);
    }
}
