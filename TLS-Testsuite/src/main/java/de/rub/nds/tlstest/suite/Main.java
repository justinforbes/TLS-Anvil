package de.rub.nds.tlstest.suite;


import com.beust.jcommander.ParameterException;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlstest.framework.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean finished = false;

    public static void main(String[] args) {

        TestContext testContext = new TestContext();
        testContext.getConfig().setSupportedVersions(Arrays.asList(ProtocolVersion.TLS12, ProtocolVersion.TLS13));

        new Thread(() -> {
           while (!finished) {
               LOGGER.debug("RAM: {}/{}",(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000, Runtime.getRuntime().totalMemory()/1000000);
               try {
                   Thread.sleep(2000);
               } catch (Exception ignored){}
           }
        }).start();

        try {
            testContext.getConfig().parse(args);

            testContext.getTestRunner().runTests(Main.class);
        }
        catch (ParameterException E) {
            LOGGER.error("Could not parse provided parameters", E);
            LOGGER.error(String.join(" ", args));
            System.exit(2);
        } catch (Exception e) {
            LOGGER.error("Something went wrong", e);
            System.exit(1);
        }

        finished = true;
    }
}
