/**
 * TLS-Testsuite - A testsuite for the TLS protocol
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.suite.tests.client.tls12.rfc5246;

import de.rub.nds.anvilcore.annotation.*;
import de.rub.nds.anvilcore.coffee4j.model.ModelFromScope;
import de.rub.nds.anvilcore.model.DerivationScope;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterScope;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.modifiablevariable.util.Modifiable;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.*;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTraceConfigurationUtil;
import de.rub.nds.tlsattacker.core.workflow.action.ReceiveAction;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import de.rub.nds.tlstest.framework.ClientFeatureExtractionResult;
import de.rub.nds.tlstest.framework.Validator;
import de.rub.nds.tlstest.framework.annotations.KeyExchange;
import de.rub.nds.tlstest.framework.constants.KeyExchangeType;
import de.rub.nds.tlstest.framework.execution.WorkflowRunner;
import de.rub.nds.tlstest.framework.model.TlsParameterType;
import de.rub.nds.tlstest.framework.model.derivationParameter.CertificateDerivation;
import de.rub.nds.tlstest.framework.model.derivationParameter.CipherSuiteDerivation;
import de.rub.nds.tlstest.framework.model.derivationParameter.NamedGroupDerivation;
import de.rub.nds.tlstest.framework.model.derivationParameter.SigAndHashDerivation;
import de.rub.nds.tlstest.framework.model.derivationParameter.helper.CertificateConfigChainValue;
import de.rub.nds.tlstest.framework.testClasses.Tls12Test;
import de.rub.nds.tlstest.framework.utils.X509CertificateChainProvider;
import de.rub.nds.x509attacker.config.X509CertificateConfig;
import de.rub.nds.x509attacker.constants.X509PublicKeyType;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Tag;

@ClientTest
@Tag("SKE")
public class ServerKeyExchange extends Tls12Test {

    @AnvilTest(id = "5246-zqCFt52rqY")
    @ModelFromScope(modelType = "CERTIFICATE")
    @KeyExchange(
            supported = {KeyExchangeType.ALL12},
            requiresServerKeyExchMsg = true)
    @IncludeParameter("SIGNATURE_BITMASK")
    public void invalidServerKeyExchangeSignature(AnvilTestCase testCase, WorkflowRunner runner) {
        Config c = getPreparedConfig(runner);
        byte[] bitmask = parameterCombination.buildBitmask();

        WorkflowTrace workflowTrace =
                runner.generateWorkflowTraceUntilReceivingMessage(
                        WorkflowTraceType.HANDSHAKE, HandshakeMessageType.CLIENT_KEY_EXCHANGE);
        ServerKeyExchangeMessage serverKeyExchangeMsg =
                (ServerKeyExchangeMessage)
                        WorkflowTraceConfigurationUtil.getFirstStaticConfiguredSendMessage(
                                workflowTrace, HandshakeMessageType.SERVER_KEY_EXCHANGE);
        serverKeyExchangeMsg.setSignature(Modifiable.xor(bitmask, 0));

        workflowTrace.addTlsAction(new ReceiveAction(new AlertMessage()));

        State state = runner.execute(workflowTrace, c);

        if (serverKeyExchangeMsg.getSignatureLength().getValue() < bitmask.length) {
            // we can't determine the ECDSA signature length beforehand
            // as trailing zeros may be stripped - the manipulation won't be
            // applied in these cases which results in false positives
            testCase.addAdditionalResultInfo("Bitmask exceeded signature length");
            return;
        }
        Validator.receivedFatalAlert(state, testCase);
    }

    public boolean isStaticEcdhCipherSuite(CipherSuite cipherSuite) {
        return cipherSuite.getKeyExchangeAlgorithm().isKeyExchangeEcdh()
                && !cipherSuite.isEphemeral();
    }

    public List<DerivationParameter> getEcdhCertsForUnproposedGroups(DerivationScope scope) {
        List<DerivationParameter> parameterValues = new LinkedList<>();
        CertificateDerivation certificateDerivation = new CertificateDerivation();
        List<DerivationParameter<Config, CertificateConfigChainValue>> certChains =
                certificateDerivation.getApplicableCertificateConfigs(context, scope, true);
        for (DerivationParameter<Config, CertificateConfigChainValue> certChain : certChains) {
            X509CertificateConfig leafConfig =
                    certChain.getSelectedValue().get(X509CertificateChainProvider.LEAF_CERT_INDEX);
            if (leafConfig.getPublicKeyType() == X509PublicKeyType.ECDH_ECDSA
                    && !context.getFeatureExtractionResult()
                            .getNamedGroups()
                            .contains(
                                    NamedGroup.convertFromX509NamedCurve(
                                            leafConfig.getDefaultSubjectNamedCurve()))) {
                parameterValues.add(certChain);
            }
        }
        return parameterValues;
    }

    public List<DerivationParameter> getUnproposedNamedGroups(DerivationScope scope) {
        List<DerivationParameter> parameterValues = new LinkedList<>();
        NamedGroup.getImplemented().stream()
                .filter(group -> group.isEcGroup())
                .filter(
                        curve ->
                                !context.getFeatureExtractionResult()
                                        .getNamedGroups()
                                        .contains(curve))
                .forEach(
                        unofferedCurve ->
                                parameterValues.add(new NamedGroupDerivation(unofferedCurve)));
        return parameterValues;
    }

    public List<DerivationParameter> getUnproposedSignatureAndHashAlgorithms(
            DerivationScope scope) {
        List<DerivationParameter> unsupportedAlgorithms = new LinkedList<>();
        ClientFeatureExtractionResult extractionResult =
                (ClientFeatureExtractionResult) context.getFeatureExtractionResult();
        SignatureAndHashAlgorithm.getImplemented().stream()
                .filter(
                        algorithm ->
                                !extractionResult
                                        .getAdvertisedSignatureAndHashAlgorithms()
                                        .contains(algorithm))
                .filter(algorithm -> algorithm.getSignatureAlgorithm() != null)
                .forEach(
                        algorithm ->
                                unsupportedAlgorithms.add(new SigAndHashDerivation(algorithm)));
        return unsupportedAlgorithms;
    }

    public List<DerivationParameter<Config, CertificateConfigChainValue>>
            getCertsIncludingUnsupportedPkGroups(DerivationScope scope) {
        CertificateDerivation certDerivation =
                (CertificateDerivation)
                        TlsParameterType.CERTIFICATE.getInstance(ParameterScope.NO_SCOPE);
        List<DerivationParameter<Config, CertificateConfigChainValue>> parameterList =
                certDerivation.getApplicableCertificateConfigs(context, scope, true);
        return parameterList;
    }

    @AnvilTest(id = "5246-wPU1BxUpeu")
    @ModelFromScope(modelType = "CERTIFICATE")
    @KeyExchange(
            supported = {KeyExchangeType.ECDH},
            requiresServerKeyExchMsg = true)
    @ExplicitValues(
            affectedIdentifiers = {"NAMED_GROUP", "CERTIFICATE"},
            methods = {"getUnproposedNamedGroups", "getCertsIncludingUnsupportedPkGroups"})
    public void acceptsUnproposedNamedGroup(AnvilTestCase testCase, WorkflowRunner runner) {
        Config c = getPreparedConfig(runner);

        WorkflowTrace workflowTrace =
                runner.generateWorkflowTraceUntilReceivingMessage(
                        WorkflowTraceType.HANDSHAKE, HandshakeMessageType.CLIENT_KEY_EXCHANGE);
        workflowTrace.addTlsAction(new ReceiveAction(new AlertMessage()));

        State state = runner.execute(workflowTrace, c);
        Validator.receivedFatalAlert(state, testCase);
    }

    @AnvilTest(id = "5246-cNKtuNg3Lc")
    @ModelFromScope(modelType = "GENERIC")
    @IncludeParameter("CERTIFICATE")
    @ExcludeParameter("NAMED_GROUP")
    @ExplicitValues(
            affectedIdentifiers = "CERTIFICATE",
            methods = "getEcdhCertsForUnproposedGroups")
    @DynamicValueConstraints(
            affectedIdentifiers = "CIPHER_SUITE",
            methods = "isStaticEcdhCipherSuite")
    public void acceptsUnproposedNamedGroupStatic(AnvilTestCase testCase, WorkflowRunner runner) {
        Config c = getPreparedConfig(runner);

        WorkflowTrace workflowTrace =
                runner.generateWorkflowTraceUntilReceivingMessage(
                        WorkflowTraceType.HANDSHAKE, HandshakeMessageType.CLIENT_KEY_EXCHANGE);
        workflowTrace.addTlsAction(new ReceiveAction(new AlertMessage()));

        State state = runner.execute(workflowTrace, c);
        Validator.receivedFatalAlert(state, testCase);
    }

    @AnvilTest(id = "5246-KAA9dJJg3h")
    @ModelFromScope(modelType = "CERTIFICATE")
    @KeyExchange(
            supported = {KeyExchangeType.ALL12},
            requiresServerKeyExchMsg = true)
    public void acceptsMissingSignature(AnvilTestCase testCase, WorkflowRunner runner) {
        Config c = getPreparedConfig(runner);

        WorkflowTrace workflowTrace =
                runner.generateWorkflowTraceUntilReceivingMessage(
                        WorkflowTraceType.HANDSHAKE, HandshakeMessageType.CLIENT_KEY_EXCHANGE);
        workflowTrace.addTlsAction(new ReceiveAction(new AlertMessage()));

        ServerKeyExchangeMessage serverKeyExchange =
                (ServerKeyExchangeMessage)
                        WorkflowTraceConfigurationUtil.getFirstStaticConfiguredSendMessage(
                                workflowTrace, HandshakeMessageType.SERVER_KEY_EXCHANGE);
        serverKeyExchange.setSignature(Modifiable.explicit(new byte[0]));

        State state = runner.execute(workflowTrace, c);
        Validator.receivedFatalAlert(state, testCase);
    }

    public boolean isNotAnonCipherSuite(CipherSuite cipherSuite) {
        return !cipherSuite.isAnon();
    }

    @AnvilTest(id = "5246-xTN7vXv2VU")
    @ModelFromScope(modelType = "CERTIFICATE")
    @ExcludeParameter("SIG_HASH_ALGORIHTM")
    @KeyExchange(
            supported = {KeyExchangeType.ALL12},
            requiresServerKeyExchMsg = true)
    @DynamicValueConstraints(affectedIdentifiers = "CIPHER_SUITE", methods = "isNotAnonCipherSuite")
    public void acceptsAnonSignatureForNonAnonymousCipherSuite(
            AnvilTestCase testCase, WorkflowRunner runner) {
        Config c = getPreparedConfig(runner);

        WorkflowTrace workflowTrace =
                runner.generateWorkflowTraceUntilReceivingMessage(
                        WorkflowTraceType.HANDSHAKE, HandshakeMessageType.CLIENT_KEY_EXCHANGE);
        workflowTrace.addTlsAction(new ReceiveAction(new AlertMessage()));

        CipherSuite selectedCipherSuite =
                parameterCombination.getParameter(CipherSuiteDerivation.class).getSelectedValue();
        DigestAlgorithm digest =
                AlgorithmResolver.getDigestAlgorithm(ProtocolVersion.TLS12, selectedCipherSuite);
        String digestName = "NONE";
        if (digest != null) {
            digestName = digest.name();
        }
        SignatureAndHashAlgorithm matchingAnon =
                SignatureAndHashAlgorithm.valueOf("ANONYMOUS_" + digestName);
        ServerKeyExchangeMessage serverKeyExchange =
                (ServerKeyExchangeMessage)
                        WorkflowTraceConfigurationUtil.getFirstStaticConfiguredSendMessage(
                                workflowTrace, HandshakeMessageType.SERVER_KEY_EXCHANGE);
        serverKeyExchange.setSignatureAndHashAlgorithm(
                Modifiable.explicit(matchingAnon.getByteValue()));
        serverKeyExchange.setSignature(Modifiable.explicit(new byte[0]));

        State state = runner.execute(workflowTrace, c);
        Validator.receivedFatalAlert(state, testCase);
    }

    @AnvilTest(id = "5246-1Bsg5xe2cv")
    @ModelFromScope(modelType = "CERTIFICATE")
    @KeyExchange(
            supported = {KeyExchangeType.ALL12},
            requiresServerKeyExchMsg = true)
    @ExplicitValues(
            affectedIdentifiers = "SIG_HASH_ALGORIHTM",
            methods = "getUnproposedSignatureAndHashAlgorithms")
    public void acceptsUnproposedSignatureAndHash(AnvilTestCase testCase, WorkflowRunner runner) {
        Config c = getPreparedConfig(runner);

        WorkflowTrace workflowTrace =
                runner.generateWorkflowTraceUntilReceivingMessage(
                        WorkflowTraceType.HANDSHAKE, HandshakeMessageType.CLIENT_KEY_EXCHANGE);
        workflowTrace.addTlsAction(new ReceiveAction(new AlertMessage()));

        State state = runner.execute(workflowTrace, c);
        Validator.receivedFatalAlert(state, testCase);
    }
}
