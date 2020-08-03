package de.rub.nds.tlstest.suite.tests.client.tls12.rfc4492;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.ECPointFormat;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ECPointFormatExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.EllipticCurvesExtensionMessage;
import de.rub.nds.tlstest.framework.annotations.ClientTest;
import de.rub.nds.tlstest.framework.annotations.KeyExchange;
import de.rub.nds.tlstest.framework.annotations.RFC;
import de.rub.nds.tlstest.framework.annotations.TlsTest;
import de.rub.nds.tlstest.framework.constants.AssertMsgs;
import de.rub.nds.tlstest.framework.constants.KeyExchangeType;
import de.rub.nds.tlstest.framework.testClasses.Tls12Test;

import java.util.Set;

import static org.junit.Assert.*;


@RFC(number = 4492, section = "4. TLS Extensions for ECC")
@ClientTest
public class TLSExtensionForECC extends Tls12Test {

    @TlsTest(description = "The client MUST NOT include these extensions in the ClientHello " +
            "message if it does not propose any ECC cipher suites.")
    @KeyExchange(supported = {KeyExchangeType.DH, KeyExchangeType.RSA})
    public void bothECExtensions_WithoutECCCipher() {
        ClientHelloMessage msg = context.getReceivedClientHelloMessage();
        assertNotNull(AssertMsgs.ClientHelloNotReceived, msg);
        Set<CipherSuite> suites = context.getConfig().getSiteReport().getCipherSuites();
        suites.removeIf(cs -> !KeyExchangeType.ECDH.compatibleWithCiphersuite(cs));

        if (suites.size() == 0) {
            ECPointFormatExtensionMessage poinfmtExt = msg.getExtension(ECPointFormatExtensionMessage.class);
            EllipticCurvesExtensionMessage ecExt = msg.getExtension(EllipticCurvesExtensionMessage.class);
            assertNull("ECPointFormatExtension should be null", poinfmtExt);
            assertNull("EllipticCurveExtension should be null", ecExt);
        }
    }


    @TlsTest(description = "If the Supported Point Formats Extension is indeed sent, " +
            "it MUST contain the value 0 (uncompressed) " +
            "as one of the items in the list of point formats.")
    @KeyExchange(supported = KeyExchangeType.ECDH)
    public void invalidPointFormat() {
        ClientHelloMessage msg = context.getReceivedClientHelloMessage();
        assertNotNull(AssertMsgs.ClientHelloNotReceived, msg);
        ECPointFormatExtensionMessage poinfmtExt = msg.getExtension(ECPointFormatExtensionMessage.class);
        assertNotNull("No ECPointFormatExtension in ClientHello", poinfmtExt);

        boolean contains_zero = false;
        for (byte b : poinfmtExt.getPointFormats().getValue()) {
            if (b == ECPointFormat.UNCOMPRESSED.getValue()) {
                contains_zero = true;
                break;
            }
        }
        assertTrue("ECPointFormatExtension does not contain uncompressed format", contains_zero);
    }

}
