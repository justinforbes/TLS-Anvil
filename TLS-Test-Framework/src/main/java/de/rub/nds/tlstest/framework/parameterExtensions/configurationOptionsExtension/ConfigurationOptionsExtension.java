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

import de.rub.nds.tlstest.framework.FeatureExtractionResult;
import de.rub.nds.tlstest.framework.TestContext;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.ConfigurationOptionsConfig;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This parameter extension is used to add ConfigOptionsDerivationType DerivationParameter%s to the
 * IPM. The DerivationParameters were selected by a config file; Its path must be passed using a
 * String.
 */
public class ConfigurationOptionsExtension {

    private static final Logger LOGGER = LogManager.getLogger();

    private ConfigurationOptionsConfig config;
    private final TestContext testContext;
    private final ConfigurationOptionsDerivationManager derivationManager =
            new ConfigurationOptionsDerivationManager();

    public ConfigurationOptionsExtension(TestContext testContex) {
        this.testContext = testContex;
        testContext.setConfigurationOptionsExtension(this);
    }

    public void load(Object initData) {
        if (!(initData instanceof String)) {
            throw new IllegalArgumentException(
                    "The ConfigurationOptionsExtension requires a String for initialization data.");
        }
        String configPathString = (String) initData;
        Path configPath = Paths.get(configPathString);
        if (Files.notExists(configPath)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Illegal path was passed. No file at '%s' can be found.",
                            configPath.toAbsolutePath()));
        }
        try {
            config = new ConfigurationOptionsConfig(configPath, testContext);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    String.format(
                            "The passed configuration options config file '%s' could not be found.",
                            configPath));
        }
        LOGGER.info(
                "Configured library: {} with version {}",
                config.getTlsLibraryName(),
                config.getTlsVersionName());
        LOGGER.info(
                "Testing with configuration options: {}",
                config.getEnabledConfigOptionDerivations());

        derivationManager.initializeConfigOptionsConfig(config);
        config.getBuildManager().init();
        derivationManager.preBuildAndValidateAndFilterSetups();

        FeatureExtractionResult maxFeatureExtractionResult =
                config.getBuildManager().getMaximalFeatureExtractionResult();
        logContainerFeatures("richest configuration", maxFeatureExtractionResult);
        testContext.setFeatureExtractionResult(maxFeatureExtractionResult);
    }

    public void shutdown() {
        config.getBuildManager().onShutdown();
    }

    public ConfigurationOptionsConfig getConfig() {
        return config;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    public ConfigurationOptionsDerivationManager getDerivationManager() {
        return derivationManager;
    }

    public static void logContainerFeatures(
            String identifier, FeatureExtractionResult featureExtractionResult) {
        LOGGER.info(
                "Container ({}) offers {} versions, {} cipher suites, {} named groups, {} TLS 1.3 groups",
                identifier,
                featureExtractionResult.getSupportedVersions().size(),
                featureExtractionResult.getSupportedCipherSuites().size(),
                featureExtractionResult.getSupportedNamedGroups().size(),
                featureExtractionResult.getSupportedTls13Groups().size());
    }
}
