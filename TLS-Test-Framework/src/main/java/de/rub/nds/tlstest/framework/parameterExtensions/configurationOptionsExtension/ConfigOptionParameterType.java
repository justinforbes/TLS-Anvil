/*
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * Copyright 2020 Ruhr University Bochum and
 * TÜV Informationstechnik GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension;

import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterScope;
import de.rub.nds.anvilcore.model.parameter.ParameterType;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter.CommonBuildParameterDerivation;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter.ConfigurationOptionCompoundDerivation;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter.EnableWeakSslCiphersDerivation;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter.SeedingMethodDerivation;
import java.lang.reflect.InvocationTargetException;

/**
 * All these types represent configuration options. Configuration options are library options that
 * are configured at compile time and are therefore NOT configured and negotiated during the TLS
 * handshake. Note that not all of these options are supported by every TLS-library.
 *
 * <p>To implement new options (e.g. the option ExampleOption) the following steps need to be
 * applied: 1) Add ExampleOption to the ConfigOptionDerivationType enum below 2) Add a new class
 * 'ExampleOptionDerivation' in the package
 * de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension. Implement the
 * required functions like the other classes. 3) Add the new class to the factory method
 * 'ConfigurationOptionsDerivationManager.getDerivationParameterInstance(...)' 4) To use the new
 * option in your test, make sure to add it to your config options config file (together with the
 * respective translation) 5) If required: Add constraints regarding your new option to the required
 * tests in your testsuite.
 */
public enum ConfigOptionParameterType implements ParameterType {
    CONFIG_OPTION_COMPOUND_PARAMETER(ConfigurationOptionCompoundDerivation.class),

    COMMON_BUILD_FLAG(CommonBuildParameterDerivation.class),
    SEEDING_METHOD(SeedingMethodDerivation.class),
    ENABLE_WEAK_SSL_CIPHERS(EnableWeakSslCiphersDerivation.class);

    ConfigOptionParameterType(Class<? extends DerivationParameter> derivationClass) {
        this.derivationClass = derivationClass;
    }

    private Class<? extends DerivationParameter> derivationClass;

    @Override
    public DerivationParameter getInstance(ParameterScope scope) {
        try {
            if (scope.getUniqueScopeIdentifier()
                    .startsWith(CommonBuildParameterScope.SCOPE_IDENTIFIER)) {
                return new CommonBuildParameterDerivation(scope);
            } else {
                return derivationClass.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
