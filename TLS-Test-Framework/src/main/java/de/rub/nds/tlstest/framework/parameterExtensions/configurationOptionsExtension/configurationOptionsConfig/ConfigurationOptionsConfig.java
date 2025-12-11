/*
 *  TLS-Test-Framework - A framework for modeling TLS tests
 *
 *  Copyright 2020 Ruhr University Bochum and
 *  TÜV Informationstechnik GmbH
 *
 *  Licensed under Apache License 2.0
 *  http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionsConfig;

import de.rub.nds.anvilcore.constants.TestEndpointType;
import de.rub.nds.anvilcore.context.AnvilContext;
import de.rub.nds.anvilcore.context.AnvilContextRegistry;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rub.nds.anvilcore.model.parameter.ParameterScope;
import de.rub.nds.anvilcore.model.parameter.ParameterType;
import de.rub.nds.tlstest.framework.TestContext;
import de.rub.nds.tlstest.framework.TestContextRegistry;
import de.rub.nds.tlstest.framework.anvil.TlsParameterIdentifierProvider;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.CommonBuildParameterScope;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterScope;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigOptionParameterType;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.ConfigurationOptionValue;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.buildManagement.docker.DockerBasedBuildManager;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.buildManagement.docker.DockerFactory;
import de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension.configurationOptionDerivationParameter.ConfigurationOptionDerivationParameter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Contains all configuration data that is contained in the configuration option XML config file.
 * These data are used to specify which TLS library is built and how. Additionally, the set of used
 * ConfigOptionDerivationParameter%s is specified and how to translate them to library specific
 * configurations (e.g. OpenSSL cli parameters).
 *
 * <p>Check out the exampleConfig.xml file in examples/ for usage instructions.
 */
public class ConfigurationOptionsConfig {

    private String tlsLibraryName;
    private String tlsVersionName;
    private DockerBasedBuildManager buildManager;
    private final TestContext testContext;

    private final Map<ParameterIdentifier, ConfigOptionValueTranslation> optionsToTranslation;

    private int configOptionsIpmStrength; // default: strength of main IPM

    private int maxRunningContainers; // default 16
    private int maxSimultaneousBuilds; // default 1

    /**
     * Defines how many containers should be shutdown simultaneously. When measuring coverage the
     * coverage data is collected at the shutdown. Therefore it is much more CPU expensive than a
     * simple shutdown.
     */
    private int maxRunningContainerShutdowns; // default 8

    private String compoundOptionsFile; // default "compoundOptions.cache"

    private String additionalClientCliParameter;
    private String additionalServerCliParameter;

    // Docker Config (not required, but necessary for build managers that work with docker)
    private boolean dockerConfigPresent;

    /** The address the docker host is bound to (e.g. 127.0.0.1, or 0.0.0.0) */
    private String dockerHostBinding;

    /**
     * Thee address to access the host (may differ from dockerHostBinding when using docker with
     * WSL)
     */
    private String dockerHostName;

    private PortRange dockerPortRange;
    private String dockerClientDestinationHostName;
    private static final int DEFAULT_SIMULTANEOUS_BUILDS = 1;
    private static final int DEFAULT_MAX_RUNNING_CONTAINERS = 16;
    private static final int DEFAULT_MAX_RUNNING_SHUTDOWN_CONTAINERS = 8;
    private static final String DEFAULT_COMPOUND_OPTIONS_FILE = "compoundOptions.cache";

    public ConfigurationOptionsConfig(Path configFilePath, TestContext testContext)
            throws FileNotFoundException {
        this.testContext = testContext;
        optionsToTranslation = new HashMap<>();
        parseConfigFile(new FileInputStream(configFilePath.toFile()));
        buildManager = new DockerBasedBuildManager(this, new DockerFactory(this), testContext);
    }

    public ConfigurationOptionsConfig(InputStream inputStream, TestContext testContext) {
        optionsToTranslation = new HashMap<>();
        this.testContext = testContext;
        parseConfigFile(inputStream);
        buildManager = new DockerBasedBuildManager(this, new DockerFactory(this), testContext);
    }

    public String getTlsLibraryName() {
        return tlsLibraryName;
    }

    public String getTlsVersionName() {
        return tlsVersionName;
    }

    public int getConfigOptionsIpmStrength() {
        return configOptionsIpmStrength;
    }

    public boolean isDockerConfigPresent() {
        return dockerConfigPresent;
    }

    public String getDockerHostBinding() {
        return dockerHostBinding;
    }

    public String getDockerHostName() {
        return dockerHostName;
    }

    public PortRange getDockerPortRange() {
        return dockerPortRange;
    }

    public String getDockerClientDestinationHostName() {
        return dockerClientDestinationHostName;
    }

    public Map<ParameterIdentifier, ConfigOptionValueTranslation> getOptionsToTranslationMap() {
        return new HashMap<>(optionsToTranslation);
    }

    public Set<ParameterIdentifier> getEnabledConfigOptionDerivations() {
        return new HashSet<>(optionsToTranslation.keySet());
    }

    public int getMaxRunningContainers() {
        return maxRunningContainers;
    }

    public int getMaxSimultaneousBuilds() {
        return maxSimultaneousBuilds;
    }

    public int getMaxRunningContainerShutdowns() {
        return maxRunningContainerShutdowns;
    }

    public String getCompoundOptionsFile() {
        return compoundOptionsFile;
    }

    public String getAdditionalClientCliParameter() {
        return additionalClientCliParameter;
    }

    public void setAdditionalClientCliParameter(String additionalClientCliParameter) {
        this.additionalClientCliParameter = additionalClientCliParameter;
    }

    public String getAdditionalServerCliParameter() {
        return additionalServerCliParameter;
    }

    public void setAdditionalServerCliParameter(String additionalServerCliParameter) {
        this.additionalServerCliParameter = additionalServerCliParameter;
    }

    private void parseConfigFile(InputStream inputStream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            doc.getDocumentElement().normalize();

            Element rootElement = doc.getDocumentElement();

            parseAndConfigureTLSLibraryName(rootElement);
            parseAndConfigureTLSVersionName(rootElement);
            parseAndConfigureConfigOptionsIpmStrength(rootElement);

            parseAndConfigureDockerConfig(rootElement);

            parseAndConfigureMaxRunningContainers(rootElement);
            parseAndConfigureMaxSimultaneousBuilds(rootElement);
            parseAndConfigureMaxRunningContainerShutdowns(rootElement);
            parseAndConfigureCompoundOptionsFile(rootElement);
            parseAndConfigureAdditionalClientCliParameter(rootElement);
            parseAndConfigureAdditionalServerCliParameter(rootElement);
            parseAndConfigureOptionsToTest(rootElement);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException("Parsing failure.");
        }
    }

    private void parseAndConfigureTLSLibraryName(Element rootElement) {
        tlsLibraryName =
                Objects.requireNonNull(
                                XmlParseUtils.findElement(rootElement, "tlsLibraryName", true))
                        .getTextContent();
    }

    private void parseAndConfigureTLSVersionName(Element rootElement) {
        tlsVersionName =
                Objects.requireNonNull(
                                XmlParseUtils.findElement(rootElement, "tlsVersionName", true))
                        .getTextContent();
    }

    private void parseAndConfigureConfigOptionsIpmStrength(Element rootElement) {
        Element configOptionsIpmStrengthElement =
                XmlParseUtils.findElement(rootElement, "configOptionsIpmStrength", false);
        if (configOptionsIpmStrengthElement != null) {
            configOptionsIpmStrength =
                    Integer.parseInt(configOptionsIpmStrengthElement.getTextContent());
        } else {
            configOptionsIpmStrength =
                    testContext.getConfig().getAnvilTestConfig().getStrength(); // default
        }
    }

    private void parseAndConfigureMaxSimultaneousBuilds(Element rootElement) {
        Element maxSimultaneousBuildsElement =
                XmlParseUtils.findElement(rootElement, "maxSimultaneousBuilds", false);
        if (maxSimultaneousBuildsElement != null) {
            maxSimultaneousBuilds = Integer.parseInt(maxSimultaneousBuildsElement.getTextContent());
        } else {
            maxSimultaneousBuilds = DEFAULT_SIMULTANEOUS_BUILDS;
        }
    }

    private void parseAndConfigureMaxRunningContainers(Element rootElement) {
        Element maxRunningContainersElement =
                XmlParseUtils.findElement(rootElement, "maxRunningContainers", false);
        if (maxRunningContainersElement != null) {
            maxRunningContainers = Integer.parseInt(maxRunningContainersElement.getTextContent());
        } else {
            maxRunningContainers = DEFAULT_MAX_RUNNING_CONTAINERS;
        }
    }

    // Must be called after parseAndConfigureMaxRunningContainers
    private void parseAndConfigureMaxRunningContainerShutdowns(Element rootElement) {
        Element maxRunningContainersElement =
                XmlParseUtils.findElement(rootElement, "maxRunningContainerShutdowns", false);
        if (maxRunningContainersElement != null) {
            maxRunningContainerShutdowns =
                    Integer.parseInt(maxRunningContainersElement.getTextContent());
        } else {
            maxRunningContainerShutdowns = DEFAULT_MAX_RUNNING_SHUTDOWN_CONTAINERS;
        }
    }

    private void parseAndConfigureCompoundOptionsFile(Element rootElement) {
        Element compoundOptionsElement =
                XmlParseUtils.findElement(rootElement, "compoundOptions", false);
        if (compoundOptionsElement != null) {
            compoundOptionsFile = compoundOptionsElement.getTextContent();
        } else {
            compoundOptionsFile = DEFAULT_COMPOUND_OPTIONS_FILE;
        }
    }

    private void parseAndConfigureAdditionalClientCliParameter(Element rootElement) {
        Element additionalClientCliParameterElement =
                XmlParseUtils.findElement(rootElement, "additionalClientCliParameter", false);
        if (additionalClientCliParameterElement != null) {
            additionalClientCliParameter = additionalClientCliParameterElement.getTextContent();
        }
    }

    private void parseAndConfigureAdditionalServerCliParameter(Element rootElement) {
        Element additionalServerCliParameterElement =
                XmlParseUtils.findElement(rootElement, "additionalServerCliParameter", false);
        if (additionalServerCliParameterElement != null) {
            additionalServerCliParameter = additionalServerCliParameterElement.getTextContent();
        }
    }

    private void parseAndConfigureDockerConfig(Element rootElement) {
        NodeList dockerConfigList = rootElement.getElementsByTagName("dockerConfig");
        if (dockerConfigList.getLength() > 0) {
            Element dockerConfigElement = (Element) dockerConfigList.item(0);
            dockerHostName =
                    Objects.requireNonNull(
                                    XmlParseUtils.findElement(
                                            dockerConfigElement, "dockerHostName", true))
                            .getTextContent();
            dockerHostBinding =
                    Objects.requireNonNull(
                                    XmlParseUtils.findElement(
                                            dockerConfigElement, "dockerHostBinding", true))
                            .getTextContent();
            dockerPortRange =
                    PortRange.fromString(
                            Objects.requireNonNull(
                                            XmlParseUtils.findElement(
                                                    dockerConfigElement, "portRange", true))
                                    .getTextContent());
            // Docker client dest is required for client tests
            Element dockerClientDestElement =
                    XmlParseUtils.findElement(
                            dockerConfigElement,
                            "dockerClientDestinationHost",
                            (testContext.getConfig().getTestEndpointMode()
                                    == TestEndpointType.CLIENT));
            if (dockerClientDestElement != null) {
                dockerClientDestinationHostName = dockerClientDestElement.getTextContent();
            }
            dockerConfigPresent = true;
        } else {
            dockerConfigPresent = false;
        }
    }

    private void parseAndConfigureOptionsToTest(Element rootElement) {
        // Parse options-translation list
        Element optionsToTest = XmlParseUtils.findElement(rootElement, "optionsToTest", true);
        assert optionsToTest != null;
        NodeList list = optionsToTest.getElementsByTagName("optionEntry");

        for (int optionEntryIdx = 0; optionEntryIdx < list.getLength(); optionEntryIdx++) {

            Node optionEntryNode = list.item(optionEntryIdx);

            if (optionEntryNode.getNodeType() == Node.ELEMENT_NODE) {
                Element optionEntry = (Element) optionEntryNode;

                // Disable the option if enabled is set to false. Options are enabled by default
                // (i.e. if the element is not given)
                Element enabledElement = XmlParseUtils.findElement(optionEntry, "enabled", false);
                if (enabledElement != null) {
                    boolean optionEnabled = Boolean.parseBoolean(enabledElement.getTextContent());
                    if (!optionEnabled) {
                        continue;
                    }
                }

                // Parse derivation type
                String type =
                        Objects.requireNonNull(
                                        XmlParseUtils.findElement(
                                                optionEntry, "derivationType", true))
                                .getTextContent();

                // Parse value translation
                ConfigOptionValueTranslation translation =
                        getTranslationFromElement(
                                Objects.requireNonNull(
                                        XmlParseUtils.findElement(
                                                optionEntry, "valueTranslation", true)));

                // Parse feature constraints (if present)
                parseFeatureConstraints(optionEntry, translation);

                ParameterScope scopeToUse = null;
                ParameterType typeToUse = null;
                if (type.startsWith(CommonBuildParameterScope.SCOPE_IDENTIFIER + ":")) {
                    typeToUse = ConfigOptionParameterType.COMMON_BUILD_FLAG;
                    scopeToUse = getCommonScopeForType(type);
                } else {
                    scopeToUse = ConfigOptionParameterScope.DEFAULT;
                    typeToUse = derivationTypeFromString(type);
                }

                if (scopeToUse == null || typeToUse == null) {
                    throw new IllegalArgumentException(
                            "Failed to resolve config option derivation type '"
                                    + type
                                    + "' to parameter with options.");
                }

                optionsToTranslation.put(
                        new ParameterIdentifier(typeToUse, scopeToUse), translation);
            }
        }
    }

    public ParameterScope getCommonScopeForType(String commonOptionEntry) {
        String optionName = commonOptionEntry.split(":")[1];
        return new CommonBuildParameterScope(optionName);
    }

    private ConfigOptionParameterType derivationTypeFromString(String str)
            throws IllegalArgumentException {
        AnvilContext anvilContext =
                AnvilContextRegistry.getContext(TestContextRegistry.getContextId(testContext));
        List<ParameterIdentifier> configOptionIdentifiers =
                TlsParameterIdentifierProvider.getAllParameterIdentifiers(anvilContext).stream()
                        .filter(
                                identifier ->
                                        identifier.getParameterScope()
                                                == ConfigOptionParameterScope.DEFAULT)
                        .collect(Collectors.toList());
        for (ParameterIdentifier identifier : configOptionIdentifiers) {
            if (str.equals(identifier.name())) {
                return (ConfigOptionParameterType) identifier.getParameterType();
            }
        }
        throw new IllegalArgumentException("Unsupported derivation type '" + str + "'");
    }

    private ConfigOptionValueTranslation getTranslationFromElement(Element translationElement)
            throws IllegalArgumentException {
        String type = translationElement.getAttribute("type");
        switch (type) {
            case "Flag":
                return new FlagTranslation(translationElement);
            case "SingleValueOption":
                return new SingleValueOptionTranslation(translationElement);
            default:
                throw new IllegalArgumentException("Unsupported translation type '" + type + "'");
        }
    }

    public DockerBasedBuildManager getBuildManager() {
        return buildManager;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    /**
     * Returns all constraints defined for a specific configuration option.
     *
     * @param configOption The configuration option parameter identifier
     * @return List of all constraints for this option, or empty list if none exist
     */
    public List<FeatureConstraint> getConstraintsForConfigOption(ParameterIdentifier configOption) {
        ConfigOptionValueTranslation translation = optionsToTranslation.get(configOption);
        if (translation == null) {
            return new ArrayList<>();
        }

        if (translation instanceof FlagTranslation) {
            return ((FlagTranslation) translation).getConstraints();
        } else if (translation instanceof SingleValueOptionTranslation) {
            return ((SingleValueOptionTranslation) translation).getConstraints();
        }

        return new ArrayList<>();
    }

    public List<FeatureConstraint> getConstraintForConfigOptionTargetPair(
            ParameterIdentifier configOption, ParameterIdentifier constrainedParameter) {
        List<FeatureConstraint> configOptionConstraints =
                getConstraintsForConfigOption(configOption);
        AnvilContext anvilContext =
                AnvilContextRegistry.getContext(TestContextRegistry.getContextId(testContext));
        return configOptionConstraints.stream()
                .filter(
                        constraint ->
                                !constraint
                                                .getParameterIdentifier()
                                                .contains(
                                                        CommonBuildParameterScope.SCOPE_IDENTIFIER)
                                        && ParameterIdentifier.fromName(
                                                        constraint.getParameterIdentifier(),
                                                        anvilContext)
                                                .equals(constrainedParameter))
                .collect(Collectors.toList());
    }

    /** Parses feature constraints from an optionEntry element and adds them to the translation. */
    private void parseFeatureConstraints(
            Element optionEntry, ConfigOptionValueTranslation translation) {
        Element featureConstraintsElement =
                XmlParseUtils.findElement(optionEntry, "featureConstraints", false);
        if (featureConstraintsElement == null) {
            return; // No constraints defined
        }

        NodeList constraintList = featureConstraintsElement.getElementsByTagName("constraint");
        for (int i = 0; i < constraintList.getLength(); i++) {
            Node constraintNode = constraintList.item(i);
            if (constraintNode.getNodeType() == Node.ELEMENT_NODE) {
                Element constraintElement = (Element) constraintNode;
                FeatureConstraint constraint = parseIndividualConstraint(constraintElement);
                addConstraintToTranslation(translation, constraint);
            }
        }
    }

    /** Parses an individual constraint element. */
    private FeatureConstraint parseIndividualConstraint(Element constraintElement) {
        // Parse parameter identifier
        String parameterIdentifier =
                Objects.requireNonNull(
                                XmlParseUtils.findElement(
                                        constraintElement, "parameterIdentifier", true))
                        .getTextContent();

        // Parse regex filter
        String regexFilter =
                Objects.requireNonNull(
                                XmlParseUtils.findElement(constraintElement, "regexFilter", true))
                        .getTextContent();

        // Parse applicable values
        Set<String> applicableValues = parseApplicableValues(constraintElement);

        return new FeatureConstraint(parameterIdentifier, regexFilter, applicableValues);
    }

    /**
     * Parses the applyFor section to determine which configuration values this constraint applies
     * to.
     */
    private Set<String> parseApplicableValues(Element constraintElement) {
        Set<String> applicableValues = new HashSet<>();

        Element applyForElement =
                Objects.requireNonNull(
                        XmlParseUtils.findElement(constraintElement, "applyFor", true));

        // Check for flag values
        Element flagValueElement = XmlParseUtils.findElement(applyForElement, "flagValue", false);
        if (flagValueElement != null) {
            applicableValues.add(flagValueElement.getTextContent());
        }

        // Check for option values
        NodeList optionValueList = applyForElement.getElementsByTagName("optionValue");
        for (int i = 0; i < optionValueList.getLength(); i++) {
            Node optionValueNode = optionValueList.item(i);
            if (optionValueNode.getNodeType() == Node.ELEMENT_NODE) {
                Element optionValueElement = (Element) optionValueNode;
                applicableValues.add(optionValueElement.getTextContent());
            }
        }

        if (applicableValues.isEmpty()) {
            throw new IllegalArgumentException(
                    "Constraint applyFor section must contain at least one flagValue or optionValue");
        }

        return applicableValues;
    }

    /** Adds a constraint to the appropriate translation object. */
    private void addConstraintToTranslation(
            ConfigOptionValueTranslation translation, FeatureConstraint constraint) {
        if (translation instanceof FlagTranslation) {
            ((FlagTranslation) translation).addConstraint(constraint);
        } else if (translation instanceof SingleValueOptionTranslation) {
            ((SingleValueOptionTranslation) translation).addConstraint(constraint);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported translation type for constraints: " + translation.getClass());
        }
    }

    /**
     * Translates a given configuration option to a tls library specific string.
     *
     * @param optionParameter - the configuration option to translate (including its set value)
     * @return the translated string
     */
    public String translateOptionValue(ConfigurationOptionDerivationParameter optionParameter) {
        ConfigurationOptionValue value = optionParameter.getSelectedValue();
        if (value == null) {
            throw new IllegalArgumentException(
                    "Passed option parameter has no selected value yet.");
        }
        ParameterIdentifier parameterIdentifier = optionParameter.getParameterIdentifier();
        if (!(parameterIdentifier.getParameterType() instanceof ConfigOptionParameterType)) {
            throw new IllegalArgumentException(
                    "Passed derivation parameter is not of type ConfigOptionDerivationType.");
        }

        if (!optionsToTranslation.containsKey(parameterIdentifier)) {
            throw new IllegalStateException(
                    "The ConfigurationOptionsConfig's translation map does not contain the passed type");
        }

        ConfigOptionValueTranslation translation = optionsToTranslation.get(parameterIdentifier);

        if (translation instanceof FlagTranslation) {
            FlagTranslation flagTranslation = (FlagTranslation) translation;
            if (!value.isFlag()) {
                throw new IllegalStateException(
                        "The ConfigurationOptionsConfig's translation is a flag, but the ConfigurationOptionValue isn't. Value can't be translated.");
            }

            if (value.isOptionSet()) {
                return flagTranslation.getDataIfSet();
            } else {
                return flagTranslation.getDataIfNotSet();
            }
        } else if (translation instanceof SingleValueOptionTranslation) {
            SingleValueOptionTranslation singleValueTranslation =
                    (SingleValueOptionTranslation) translation;
            if (value.isFlag()) {
                throw new IllegalStateException(
                        "The ConfigurationOptionsConfig's translation has a value, but the ConfigurationOptionValue is a flag. Value can't be translated.");
            }
            List<String> optionValues = value.getOptionValues();
            if (optionValues.size() != 1) {
                throw new IllegalStateException(
                        "The ConfigurationOptionsConfig's translation has a single value, but the ConfigurationOptionValue is not a single value. Value can't be translated.");
            }
            String optionValue = optionValues.get(0);

            String translatedName = singleValueTranslation.getIdentifier();
            String translatedValue = singleValueTranslation.getValueTranslation(optionValue);

            return String.format("%s=%s", translatedName, translatedValue);
        } else {
            throw new UnsupportedOperationException(
                    String.format(
                            "The DockerBasedBuildManager does not support translations '%s'.",
                            translation.getClass()));
        }
    }
}
