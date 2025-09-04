package de.rub.nds.tlstest.suite.util;

import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.scanner.core.probe.result.TestResults;
import de.rub.nds.tlsscanner.core.constants.TlsAnalyzedProperty;
import de.rub.nds.tlstest.framework.ClientFeatureExtractionResult;
import de.rub.nds.tlstest.framework.TestContext;
import de.rub.nds.tlstest.framework.TestContextRegistry;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Collection of common feature requirements for DTLS tests. */
public class DtlsTestConditions {

    public ConditionEvaluationResult isServerTestOrClientSendsAppData(
            ExtensionContext extensionContext) {
        TestContext context = TestContextRegistry.byExtensionContext(extensionContext);
        if (!context.getConfig().isUseDTLS()) {
            // accept if TLS is used so we can use this method for limited DTLS tests
            return ConditionEvaluationResult.enabled("Target can be evaluated");
        }
        if (context.getConfig().getTestEndpointMode() == TestEndpointType.SERVER
                || ((ClientFeatureExtractionResult) context.getFeatureExtractionResult())
                                .getResult(TlsAnalyzedProperty.SENDS_APPLICATION_MESSAGE)
                        == TestResults.TRUE) {
            return ConditionEvaluationResult.enabled("Target can be evaluated");
        }
        return ConditionEvaluationResult.disabled(
                "Target is a client and does not send application data after the handshake. Unable to evaluate if client detects the manipulated final message flight.");
    }

    public ConditionEvaluationResult serverSendsHelloVerifyRequest(
            ExtensionContext extensionContext) {
        TestContext context = TestContextRegistry.byExtensionContext(extensionContext);
        if (context.getConfig().getTestEndpointMode() == TestEndpointType.SERVER
                && context.getFeatureExtractionResult()
                                .getResult(TlsAnalyzedProperty.SUPPORTS_DTLS_COOKIE_EXCHANGE)
                        != TestResults.TRUE) {
            return ConditionEvaluationResult.enabled(
                    "Target does not send HelloVerifyRequest messages");
        }
        return ConditionEvaluationResult.enabled("Target can be evaluated");
    }
}
