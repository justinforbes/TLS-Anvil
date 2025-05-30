/**
 * TLS-Testsuite - A testsuite for the TLS protocol
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.suite.tests.both.lengthfield.extensions;

import de.rub.nds.anvilcore.annotation.AnvilTest;
import de.rub.nds.anvilcore.annotation.ServerTest;
import de.rub.nds.anvilcore.coffee4j.model.ModelFromScope;
import de.rub.nds.anvilcore.teststate.AnvilTestCase;
import de.rub.nds.modifiablevariable.util.Modifiable;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.protocol.message.extension.KeyShareExtensionMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlstest.framework.annotations.KeyExchange;
import de.rub.nds.tlstest.framework.annotations.TlsVersion;
import de.rub.nds.tlstest.framework.constants.KeyExchangeType;
import de.rub.nds.tlstest.framework.execution.WorkflowRunner;
import de.rub.nds.tlstest.framework.testClasses.TlsLengthfieldTest;
import org.junit.jupiter.api.Tag;

@Tag("tls13")
@TlsVersion(supported = ProtocolVersion.TLS13)
@KeyExchange(supported = KeyExchangeType.ALL13)
public class KeyShareExtension extends TlsLengthfieldTest {

    @AnvilTest(id = "XLF-YEP4C4ruSR")
    @ModelFromScope(modelType = "LENGTHFIELD")
    public void keyShareExtensionLength(AnvilTestCase testCase, WorkflowRunner runner) {
        Config config = context.getConfig().createTls13Config();
        genericExtensionLengthTest(runner, testCase, config, KeyShareExtensionMessage.class);
    }

    @ServerTest
    @AnvilTest(id = "XLF-kVXshRHqZy")
    @ModelFromScope(modelType = "LENGTHFIELD")
    public void keyShareEntryListLength(AnvilTestCase testCase, WorkflowRunner runner) {
        WorkflowTrace workflowTrace = setupLengthFieldTestTls13(runner);
        KeyShareExtensionMessage keyShareExtension =
                getTargetedExtension(KeyShareExtensionMessage.class, workflowTrace);
        keyShareExtension.setKeyShareListLength(Modifiable.sub(1));
        State state = runner.execute(workflowTrace, runner.getPreparedConfig());
        validateLengthTest(state, testCase);
    }
}
