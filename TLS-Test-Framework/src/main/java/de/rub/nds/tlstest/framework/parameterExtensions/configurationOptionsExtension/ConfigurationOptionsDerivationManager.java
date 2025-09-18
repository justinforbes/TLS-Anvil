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

import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlstest.framework.FeatureExtractionResult;
import de.rub.nds.tlstest.framework.TestContext;
import de.rub.nds.tlstest.framework.execution.TestPreparator;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.buildManagement.docker.DockerBasedBuildManager;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.buildManagement.docker.DockerTestContainer;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter.ConfigurationOptionDerivationParameter;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig.ConfigurationOptionsConfig;
import de.rwth.swc.coffee4j.engine.constraint.HardConstraintCheckerFactory;
import de.rwth.swc.coffee4j.engine.generator.TestInputGroup;
import de.rwth.swc.coffee4j.engine.generator.ipog.Ipog;
import de.rwth.swc.coffee4j.engine.report.Report;
import de.rwth.swc.coffee4j.engine.report.ReportLevel;
import de.rwth.swc.coffee4j.engine.report.Reporter;
import de.rwth.swc.coffee4j.model.Combination;
import de.rwth.swc.coffee4j.model.InputParameterModel;
import de.rwth.swc.coffee4j.model.Parameter;
import de.rwth.swc.coffee4j.model.Value;
import de.rwth.swc.coffee4j.model.converter.IndexBasedModelConverter;
import de.rwth.swc.coffee4j.model.converter.ModelConverter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The DerivationCategoryManager responsible for the ConfigOptionsDerivationType. It also contains
 * the configured ConfigurationOptionsConfig and knows the required
 * ConfigurationOptionsBuildManager.
 */
public class ConfigurationOptionsDerivationManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private ConfigurationOptionsConfig config;
    private List<List<ConfigurationOptionDerivationParameter>> compoundSetupList;

    private Map<List<ConfigurationOptionDerivationParameter>, FeatureExtractionResult>
            compoundFeatureExtractionResult;

    private ExecutorService buildExecutor;

    public ConfigurationOptionsDerivationManager() {
        config = null;
        compoundSetupList = null;
        compoundFeatureExtractionResult = null;
    }

    public List<ParameterIdentifier> getAllActivatedCOTypes() {
        return new LinkedList<>(config.getEnabledConfigOptionDerivations());
    }

    public void initializeConfigOptionsConfig(ConfigurationOptionsConfig optionsConfig) {
        config = optionsConfig;
        initCompoundParameterSetup();
    }

    public ConfigurationOptionsConfig getConfigurationOptionsConfig() {
        return config;
    }

    public DockerBasedBuildManager getConfigurationOptionsBuildManager() {
        if (config == null) {
            throw new IllegalStateException(
                    "No ConfigurationOptionsConfig was set so far. Register it before calling this method.");
        }
        return config.getBuildManager();
    }

    public Map<List<ConfigurationOptionDerivationParameter>, FeatureExtractionResult>
            getCompoundFeatureExtractionResult() {
        return compoundFeatureExtractionResult;
    }

    public List<FeatureExtractionResult> getAllCompondSiteReports() {
        return new ArrayList<FeatureExtractionResult>(compoundFeatureExtractionResult.values());
    }

    public static class LoggerReporter implements Reporter {
        @Override
        public void report(ReportLevel level, Report report) {
            LOGGER.warn("Generation Reporter ({}): {}", level.toString(), report);
        }

        @Override
        public void report(ReportLevel level, Supplier<Report> reportSupplier) {
            LOGGER.warn("Generation Reporter ({}): {}", level.toString(), reportSupplier.get());
        }
    }

    private void initCompoundParameterSetup() {
        compoundSetupList = new LinkedList<>();

        // Try to load from cache file first
        Path cacheFile = Paths.get(config.getCompoundOptionsFile());
        if (Files.exists(cacheFile)) {
            if (loadCompoundSetupFromCache(cacheFile)) {
                LOGGER.info(
                        "Loaded {} configuration option combinations from cache file: {}.",
                        compoundSetupList.size(),
                        config.getCompoundOptionsFile());
                return;
            } else {
                LOGGER.warn("Failed to load cache file, generating new compound parameter setup.");
            }
        }

        int strength = config.getConfigOptionsIpmStrength();

        // -- Create the IPM of coffee4j
        InputParameterModel.Builder builder =
                InputParameterModel.inputParameterModel("configuration-options-ipm");
        builder.strength(strength);
        for (ParameterIdentifier coIdentifier : config.getEnabledConfigOptionDerivations()) {
            ConfigurationOptionDerivationParameter coDerivationParameter =
                    (ConfigurationOptionDerivationParameter) coIdentifier.getInstance();
            // DerivationScopes are bound to test templates but this selection happens idependently
            // of any test template so we use null
            List<DerivationParameter<Config, ConfigurationOptionValue>> derivationParameterValues =
                    coDerivationParameter.getParameterValuesForConfig(config);
            // - Add values
            List<Value> values = new LinkedList<>();
            for (int idx = 0; idx < derivationParameterValues.size(); idx++) {
                values.add(new Value(idx, derivationParameterValues.get(idx)));
            }
            builder.parameter(new Parameter(coIdentifier.name(), values));
            // - Add constraints
            List<ConditionalConstraint> constraints =
                    coDerivationParameter.getRegexFilterConstraints(
                            config, config.getTestContext());
            for (ConditionalConstraint condConstraint : constraints) {
                boolean allRequiredParametersAvailable =
                        condConstraint.getRequiredParameters().stream()
                                .allMatch(
                                        reqParameter ->
                                                (reqParameter.getParameterType()
                                                                instanceof
                                                                ConfigOptionParameterType)
                                                        && config.getEnabledConfigOptionDerivations()
                                                                .contains(reqParameter));

                if (allRequiredParametersAvailable) {
                    builder.exclusionConstraint(condConstraint.getConstraint());
                }
            }
        }
        InputParameterModel ipm = builder.build();
        // -- Convert the IPM to a model the IPOG algorithm can use.
        final ModelConverter converter = new IndexBasedModelConverter(ipm);
        // -- Create the combinations for combinatorial testing in the converted model.
        Ipog ipog = new Ipog(new HardConstraintCheckerFactory());
        Set<Supplier<TestInputGroup>> suppliers =
                ipog.generate(converter.getConvertedModel(), new LoggerReporter());

        TestInputGroup testInputGroup = null;
        for (Supplier<TestInputGroup> s : suppliers) {
            TestInputGroup group = s.get();
            if (group.getIdentifier() == "Positive IpogAlgorithm Tests") {
                testInputGroup = group;
                break;
            }
        }
        if (testInputGroup == null) {
            throw new RuntimeException("Configuration option combination could not be created.");
        }

        // -- Convert the computed combinations back to the model of the IPM and collect the
        // derivation parameter combinations
        for (int[] testInput : testInputGroup.getTestInputs()) {
            Combination convertedCombination = converter.convertCombination(testInput);
            List<ConfigurationOptionDerivationParameter> parameterCombinationList =
                    new LinkedList<>();
            for (Value value : convertedCombination.getParameterValueMap().values()) {
                if (!(value.get() instanceof ConfigurationOptionDerivationParameter)) {
                    throw new RuntimeException(
                            "Value is no configuration option derivation parameter. This should never happen...");
                }
                ConfigurationOptionDerivationParameter codParameter =
                        (ConfigurationOptionDerivationParameter) value.get();
                parameterCombinationList.add(codParameter);
            }
            // Sort after type for consistent order (not necessary)
            parameterCombinationList.sort(
                    Comparator.comparing(
                            e -> e.getParameterIdentifier().getParameterType().toString()));
            compoundSetupList.add(Collections.unmodifiableList(parameterCombinationList));
        }

        compoundSetupList = Collections.unmodifiableList(compoundSetupList);

        LOGGER.info("Compiled {} configuration option combinations.", compoundSetupList.size());

        // Save to cache file
        saveCompoundSetupToCache(cacheFile);
    }

    public void preBuildAndValidateAndFilterSetups() {

        LOGGER.info("== Precompute config options builds ==");
        int buildFailedSetupCount = 0;

        List<List<ConfigurationOptionDerivationParameter>> successfulSetups = new LinkedList<>();
        HashMap<
                        List<ConfigurationOptionDerivationParameter>,
                        Future<Callable<FeatureExtractionResult>>>
                compoundSetupToFuture =
                        new HashMap<
                                List<ConfigurationOptionDerivationParameter>,
                                Future<Callable<FeatureExtractionResult>>>();
        compoundFeatureExtractionResult =
                new HashMap<
                        List<ConfigurationOptionDerivationParameter>, FeatureExtractionResult>();
        buildExecutor = Executors.newFixedThreadPool(config.getMaxSimultaneousBuilds());

        // Create all builds (with multiple threads if configured)
        for (List<ConfigurationOptionDerivationParameter> setup : compoundSetupList) {
            Set<ConfigurationOptionDerivationParameter> setupSet = new HashSet<>(setup);
            Config conf = Config.createEmptyConfig();
            Future<Callable<FeatureExtractionResult>> testSiteReportCallableFuture =
                    getFeatureExtractionFuture(conf, config.getTestContext(), setupSet);
            compoundSetupToFuture.put(setup, testSiteReportCallableFuture);
        }

        // Wait until all builds are finished
        for (Map.Entry<
                        List<ConfigurationOptionDerivationParameter>,
                        Future<Callable<FeatureExtractionResult>>>
                setupToFuture : compoundSetupToFuture.entrySet()) {
            while (!setupToFuture.getValue().isDone()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                }
            }
        }

        LOGGER.info("== Check builds and precompute site reports ==");

        // Create all site reports and check if the builds were successful
        for (Map.Entry<
                        List<ConfigurationOptionDerivationParameter>,
                        Future<Callable<FeatureExtractionResult>>>
                setupToFuture : compoundSetupToFuture.entrySet()) {
            try {
                FeatureExtractionResult featureExtractionResult =
                        setupToFuture.getValue().get().call();
                ConfigurationOptionsExtension.logContainerFeatures(
                        setupToFuture.getKey().toString(), featureExtractionResult);
                compoundFeatureExtractionResult.put(
                        setupToFuture.getKey(), featureExtractionResult);
                successfulSetups.add(setupToFuture.getKey());
            } catch (Exception e) {
                LOGGER.error(
                        "Exception occurred while pre-building container for setup with options {}. Exception: ",
                        setupToFuture.getKey(),
                        e);
                buildFailedSetupCount += 1;
            }
        }

        compoundSetupList = successfulSetups;
        if (buildFailedSetupCount > 0) {
            LOGGER.warn(
                    "{} builds failed. Continuing with reduced setup (see below). Due to the reduced option set the "
                            + "configured test strength cannot be guaranteed. Consider stopping and reconfiguring the tests or adding"
                            + "constraints to prevent invalid combinations. "
                            + "Reduced options set: {}",
                    buildFailedSetupCount,
                    compoundSetupList);
        }
    }

    private Future<Callable<FeatureExtractionResult>> getFeatureExtractionFuture(
            Config conf,
            TestContext context,
            Set<ConfigurationOptionDerivationParameter> setupSet) {
        return buildExecutor.submit(
                () -> {
                    DockerBasedBuildManager coBuildManager = getConfigurationOptionsBuildManager();
                    String containerTag =
                            coBuildManager.preparePeerConnection(conf, context, setupSet);
                    DockerTestContainer testContainer =
                            coBuildManager.getDockerTagToContainerInfoMap().get(containerTag);

                    // Create a callable that checks cache first, then extracts features if needed
                    return new Callable<FeatureExtractionResult>() {
                        @Override
                        public FeatureExtractionResult call() throws Exception {
                            String cacheFileName =
                                    "co_features_"
                                            + containerTag.replace(":", "_").replace("/", "_");
                            boolean ignoreCache =
                                    context.getConfig().getAnvilTestConfig().isIgnoreCache();

                            // Try to load from cache first
                            FeatureExtractionResult cachedResult =
                                    TestPreparator.loadFromCache(cacheFileName, ignoreCache);

                            if (cachedResult != null) {
                                LOGGER.info(
                                        "Loaded feature extraction result from cache for container tag '{}'",
                                        containerTag);
                                testContainer.setFeatureExtractionResult(cachedResult);
                                return cachedResult;
                            }

                            FeatureExtractionResult result =
                                    testContainer.getFeatureExtractionResult();

                            // Save to cache for future use
                            TestPreparator.saveToCache(result, cacheFileName);
                            LOGGER.info(
                                    "Saved feature extraction result to cache for container tag '{}'",
                                    containerTag);

                            return result;
                        }
                    };
                });
    }

    private boolean loadCompoundSetupFromCache(Path cacheFile) {
        List<List<ConfigurationOptionDerivationParameter>> loadedSetups = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<ConfigurationOptionDerivationParameter> parameterCombinationList =
                        new LinkedList<>();
                String[] cachedEntries = line.split(",");

                // Parse each cached entry (format: "parameterIdentifier=value")
                for (String cachedEntry : cachedEntries) {
                    String[] parts = cachedEntry.trim().split("=", 2);
                    if (parts.length != 2) {
                        LOGGER.warn(
                                "Was unable to process cached compund setup entry {}. Expected identifier=value syntax.",
                                cachedEntry);
                        continue; // Skip malformed entries
                    }

                    String parameterIdentifierStr = parts[0];
                    String valueStr = parts[1];

                    // Find the matching parameter identifier
                    ParameterIdentifier matchingIdentifier = null;
                    for (ParameterIdentifier coIdentifier :
                            config.getEnabledConfigOptionDerivations()) {
                        if (coIdentifier.toString().equals(parameterIdentifierStr)) {
                            matchingIdentifier = coIdentifier;
                            break;
                        }
                    }

                    if (matchingIdentifier != null) {
                        ConfigurationOptionDerivationParameter coDerivationParameter =
                                (ConfigurationOptionDerivationParameter)
                                        matchingIdentifier.getInstance();
                        List<DerivationParameter<Config, ConfigurationOptionValue>>
                                derivationParameterValues =
                                        coDerivationParameter.getParameterValuesForConfig(config);

                        // Find matching parameter based on selected value string
                        for (DerivationParameter<Config, ConfigurationOptionValue> paramValue :
                                derivationParameterValues) {
                            if (paramValue.getSelectedValue().toString().equals(valueStr)) {
                                parameterCombinationList.add(
                                        (ConfigurationOptionDerivationParameter) paramValue);
                                break;
                            }
                        }
                    }
                }
                loadedSetups.add(parameterCombinationList);
            }
            compoundSetupList = loadedSetups;
            return !compoundSetupList.isEmpty();
        } catch (IOException e) {
            LOGGER.error("Error loading compound setup from cache file: {}", cacheFile, e);
            return false;
        }
    }

    private void saveCompoundSetupToCache(Path cacheFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile.toFile()))) {
            for (List<ConfigurationOptionDerivationParameter> setup : compoundSetupList) {
                List<String> selectedValues = new ArrayList<>();
                for (ConfigurationOptionDerivationParameter param : setup) {
                    selectedValues.add(
                            param.getParameterIdentifier()
                                    + "="
                                    + param.getSelectedValue().toString());
                }
                writer.write(String.join(",", selectedValues));
                writer.newLine();
            }
            LOGGER.info(
                    "Saved {} configuration option combinations to cache file: {}.",
                    compoundSetupList.size(),
                    config.getCompoundOptionsFile());
        } catch (IOException e) {
            LOGGER.error("Error saving compound setup to cache file: {}", cacheFile, e);
        }
    }

    public List<List<ConfigurationOptionDerivationParameter>> getCompoundSetupList() {
        return compoundSetupList;
    }

    public void setCompoundSetupList(
            List<List<ConfigurationOptionDerivationParameter>> compoundSetupList) {
        this.compoundSetupList = compoundSetupList;
    }
}
