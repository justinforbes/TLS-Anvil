package de.rub.nds.tlstest.framework.testClasses;


import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlstest.framework.annotations.KeyExchange;
import de.rub.nds.tlstest.framework.annotations.TlsVersion;
import de.rub.nds.tlstest.framework.constants.KeyExchangeType;
import org.junit.jupiter.api.Tag;

@TlsVersion(supported = ProtocolVersion.TLS13)
@KeyExchange(supported = KeyExchangeType.ALL13)
@Tag("tls13")
public class Tls13Test extends TlsBaseTest {
    @Override
    public Config getConfig() {
        return context.getConfig().createTls13Config();
    }
}
