/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.framework.model.derivationParameter;

import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.NamedGroup;
import de.rub.nds.tlstest.framework.anvil.TlsDerivationParameter;
import de.rub.nds.tlstest.framework.anvil.TlsParameterIdentifierProvider;
import de.rub.nds.tlstest.framework.constants.KeyExchangeType;
import de.rub.nds.tlstest.framework.model.TlsParameterType;
import de.rwth.swc.coffee4j.model.constraints.ConstraintBuilder;
import java.util.*;
import java.util.stream.Collectors;

public class NamedGroupDerivation extends TlsDerivationParameter<NamedGroup> {

    public NamedGroupDerivation() {
        super(TlsParameterType.NAMED_GROUP, NamedGroup.class);
    }

    public NamedGroupDerivation(NamedGroup group) {
        this();
        setSelectedValue(group);
    }

    @Override
    public List<DerivationParameter<Config, NamedGroup>> getParameterValues(
            DerivationScope derivationScope) {

        List<DerivationParameter<Config, NamedGroup>> parameterValues = new LinkedList<>();
        List<NamedGroup> groupList = context.getFeatureExtractionResult().getTls13Groups();
        if (!TlsParameterIdentifierProvider.isTls13Test(derivationScope)
                || TlsParameterIdentifierProvider.getKeyExchangeRequirements(derivationScope)
                        .supports(KeyExchangeType.ECDH)) {
            groupList = context.getFeatureExtractionResult().getNamedGroups();
            parameterValues.add(new NamedGroupDerivation(null));
        } else if (TlsParameterIdentifierProvider.isTls13Test(derivationScope)
                && context.getConfig().getTestEndpointMode() == TestEndpointType.CLIENT) {
            groupList = context.getFeatureExtractionResult().getTls13Groups();
        }
        groupList =
                groupList.stream()
                        .filter(group -> NamedGroup.getImplemented().contains(group))
                        .collect(Collectors.toList());
        groupList.forEach(group -> parameterValues.add(new NamedGroupDerivation(group)));

        return parameterValues;
    }

    @Override
    public void applyToConfig(Config config, DerivationScope derivationScope) {
        if (getSelectedValue() != null) {
            if (context.getConfig().getTestEndpointMode() == TestEndpointType.SERVER) {
                config.setDefaultClientNamedGroups(getSelectedValue());
                config.setDefaultClientKeyShareNamedGroups(getSelectedValue());
            } else {
                config.setDefaultServerNamedGroups(getSelectedValue());
            }
            config.setDefaultSelectedNamedGroup(getSelectedValue());
        } else {
            config.setAddEllipticCurveExtension(false);
            config.setAddECPointFormatExtension(false);
        }
    }

    @Override
    public void postProcessConfig(Config config, DerivationScope derivationScope) {
        if (getSelectedValue() != null
                && context.getConfig().getTestEndpointMode() == TestEndpointType.SERVER) {

            Set<NamedGroup> groups = new HashSet<NamedGroup>();
            NamedGroup selectedGroup = getSelectedValue();
            groups.add(selectedGroup);

            // TODO: Still required?
            /*
            ServerFeatureExtractionResult extractionResult =
                    (ServerFeatureExtractionResult) context.getFeatureExtractionResult();
            NamedGroupWitness witness =
                    extractionResult.getNamedGroupWitnesses().get(selectedGroup);
            if (witness != null) {
                if (config.getDefaultSelectedCipherSuite().isEphemeral()) {
                    groups.add(witness.getEcdsaPkGroupEphemeral());
                    groups.add(witness.getEcdsaSigGroupEphemeral());
                } else {
                    groups.add(witness.getEcdsaSigGroupStatic());
                }
            }
             */
            groups.remove(null);
            config.setDefaultClientNamedGroups(new LinkedList<>(groups));
        }
    }

    @Override
    public List<ConditionalConstraint> getDefaultConditionalConstraints(DerivationScope scope) {
        List<ConditionalConstraint> condConstraints = new LinkedList<>();
        if (!TlsParameterIdentifierProvider.isTls13Test(scope)) {
            condConstraints.add(getCipherSuiteConstraint());
        }

        return condConstraints;
    }

    private ConditionalConstraint getCipherSuiteConstraint() {
        Set<ParameterIdentifier> requiredDerivations = new HashSet<>();
        requiredDerivations.add(new ParameterIdentifier(TlsParameterType.CIPHER_SUITE));
        return new ConditionalConstraint(
                requiredDerivations,
                ConstraintBuilder.constrain(
                                getParameterIdentifier().name(),
                                TlsParameterType.CIPHER_SUITE.name())
                        .by(
                                (NamedGroupDerivation namedGroupDerivation,
                                        CipherSuiteDerivation cipherSuiteDerivation) -> {
                                    NamedGroup selectedNamedGroup =
                                            namedGroupDerivation.getSelectedValue();
                                    CipherSuite selectedCipherSuite =
                                            cipherSuiteDerivation.getSelectedValue();

                                    if (selectedNamedGroup == null) {
                                        // don't send named groups extension
                                        // only if using non-ephemeral or non-ECDH cipher suites
                                        return !selectedCipherSuite
                                                .getKeyExchangeAlgorithm()
                                                .isKeyExchangeEcdhe();
                                    } else if (selectedNamedGroup.isDhGroup()) {
                                        // only use FFDH named groups for DHE cipher suites
                                        return selectedCipherSuite
                                                .getKeyExchangeAlgorithm()
                                                .isKeyExchangeDhe();
                                    } else {
                                        // all other named groups are meant
                                        // for ECDHE cipher suites
                                        return selectedCipherSuite
                                                .getKeyExchangeAlgorithm()
                                                .isKeyExchangeEcdhe();
                                    }
                                }));
    }

    @Override
    protected TlsDerivationParameter<NamedGroup> generateValue(NamedGroup selectedValue) {
        return new NamedGroupDerivation(selectedValue);
    }
}
