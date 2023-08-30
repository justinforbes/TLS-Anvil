/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.framework.model.derivationParameter;

import de.rub.nds.anvilcore.model.AnvilTestTemplate;
import de.rub.nds.anvilcore.model.IpmProvider;
import de.rub.nds.anvilcore.model.constraint.ConditionalConstraint;
import de.rub.nds.anvilcore.model.parameter.DerivationParameter;
import de.rub.nds.anvilcore.model.parameter.ParameterIdentifier;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.AlgorithmResolver;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.CipherType;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlstest.framework.anvil.TlsDerivationParameter;
import de.rub.nds.tlstest.framework.model.TlsParameterType;
import de.rub.nds.tlstest.framework.model.constraint.ConstraintHelper;
import de.rwth.swc.coffee4j.model.constraints.ConstraintBuilder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Yields bitmasks to modify the padding of a plaintext. Note that the derivation may generate valid
 * paddings for mac-then-encrypt. These paddings are valid in terms of the padding scheme but are
 * invalid in regard to the position of the MAC.
 */
public class PaddingBitmaskDerivation extends TlsDerivationParameter<Integer> {

    private static final Logger LOGGER = LogManager.getLogger();

    public PaddingBitmaskDerivation() {
        super(TlsParameterType.PADDING_BITMASK, Integer.class);
    }

    public PaddingBitmaskDerivation(Integer selectedValue) {
        this();
        setSelectedValue(selectedValue);
    }

    @Override
    public List<DerivationParameter<Config, Integer>> getParameterValues(
            AnvilTestTemplate anvilTestTemplate) {
        if (ConstraintHelper.isTls13Test(anvilTestTemplate)) {
            throw new RuntimeException(
                    "Padding bitmask is not configured for optional TLS 1.3 record padding");
        }

        Set<CipherSuite> cipherSuiteList = context.getFeatureExtractionResult().getCipherSuites();
        int maxCipherTextByteLen = 0;
        for (CipherSuite cipherSuite : cipherSuiteList) {
            if (AlgorithmResolver.getCipherType(cipherSuite) == CipherType.BLOCK
                    && AlgorithmResolver.getCipher(cipherSuite).getBlocksize()
                            > maxCipherTextByteLen) {
                maxCipherTextByteLen = AlgorithmResolver.getCipher(cipherSuite).getBlocksize();
            }
        }

        List<DerivationParameter<Config, Integer>> parameterValues = new LinkedList<>();
        for (int i = 0; i < maxCipherTextByteLen - 1; i++) {
            parameterValues.add(new PaddingBitmaskDerivation(i));
        }
        return parameterValues;
    }

    @Override
    public void applyToConfig(Config config, AnvilTestTemplate anvilTestTemplate) {}

    @Override
    public List<ConditionalConstraint> getDefaultConditionalConstraints(
            AnvilTestTemplate anvilTestTemplate) {
        List<ConditionalConstraint> condConstraints = new LinkedList<>();

        condConstraints.add(getMustNotExceedPaddingLengthConstraint(anvilTestTemplate, false));
        if (encThenMacDerivationModeled(anvilTestTemplate)) {
            condConstraints.add(getMustNotResultInPlausiblePadding(anvilTestTemplate, false));
        }
        return condConstraints;
    }

    private static boolean encThenMacDerivationModeled(AnvilTestTemplate anvilTestTemplate) {
        return IpmProvider.getParameterIdentifiersForScope(anvilTestTemplate).stream()
                .map(ParameterIdentifier::getParameterType)
                .anyMatch(TlsParameterType.INCLUDE_ENCRYPT_THEN_MAC_EXTENSION::equals);
    }

    public ConditionalConstraint getMustNotExceedPaddingLengthConstraint(
            AnvilTestTemplate scope, boolean enforceEncryptThenMacMode) {
        Set<ParameterIdentifier> requiredDerivations = new HashSet<>();
        requiredDerivations.add(new ParameterIdentifier(TlsParameterType.CIPHER_SUITE));
        requiredDerivations.add(new ParameterIdentifier(TlsParameterType.APP_MSG_LENGHT));

        if (encThenMacDerivationModeled(scope)) {
            requiredDerivations.add(
                    new ParameterIdentifier(TlsParameterType.INCLUDE_ENCRYPT_THEN_MAC_EXTENSION));
            return new ConditionalConstraint(
                    requiredDerivations,
                    ConstraintBuilder.constrain(
                                    getParameterIdentifier().name(),
                                    TlsParameterType.CIPHER_SUITE.name(),
                                    TlsParameterType.APP_MSG_LENGHT.name(),
                                    TlsParameterType.INCLUDE_ENCRYPT_THEN_MAC_EXTENSION.name())
                            .by(
                                    (PaddingBitmaskDerivation paddingBitmaskDerivation,
                                            CipherSuiteDerivation cipherSuiteDerivation,
                                            AppMsgLengthDerivation appMsgLengthDerivation,
                                            IncludeEncryptThenMacExtensionDerivation
                                                    includeEncryptThenMacDerivation) -> {
                                        boolean isEncryptThenMac =
                                                includeEncryptThenMacDerivation.getSelectedValue()
                                                        || enforceEncryptThenMacMode;
                                        return chosenByteIsWithinPadding(
                                                scope,
                                                paddingBitmaskDerivation.getSelectedValue(),
                                                cipherSuiteDerivation.getSelectedValue(),
                                                appMsgLengthDerivation.getSelectedValue(),
                                                isEncryptThenMac);
                                    }));

        } else {

            return new ConditionalConstraint(
                    requiredDerivations,
                    ConstraintBuilder.constrain(
                                    getParameterIdentifier().name(),
                                    TlsParameterType.CIPHER_SUITE.name(),
                                    TlsParameterType.APP_MSG_LENGHT.name())
                            .by(
                                    (PaddingBitmaskDerivation paddingBitmaskDerivation,
                                            CipherSuiteDerivation cipherSuiteDerivation,
                                            AppMsgLengthDerivation appMsgLengthDerivation) -> {
                                        return chosenByteIsWithinPadding(
                                                scope,
                                                paddingBitmaskDerivation.getSelectedValue(),
                                                cipherSuiteDerivation.getSelectedValue(),
                                                appMsgLengthDerivation.getSelectedValue(),
                                                enforceEncryptThenMacMode);
                                    }));
        }
    }

    private int getResultingPaddingSize(
            boolean isEncryptThenMac,
            int applicationMessageContentLength,
            CipherSuite cipherSuite,
            ProtocolVersion targetVersion) {
        int blockSize = AlgorithmResolver.getCipher(cipherSuite).getBlocksize();
        int macSize = AlgorithmResolver.getMacAlgorithm(targetVersion, cipherSuite).getSize();
        if (isEncryptThenMac) {
            return blockSize - (applicationMessageContentLength % blockSize);
        } else {
            return blockSize - ((applicationMessageContentLength + macSize) % blockSize);
        }
    }

    public ConditionalConstraint getMustNotResultInPlausiblePadding(
            AnvilTestTemplate scope, boolean enforceEncryptThenMacMode) {
        Set<ParameterIdentifier> requiredDerivations = new HashSet<>();
        requiredDerivations.add(new ParameterIdentifier(TlsParameterType.CIPHER_SUITE));
        requiredDerivations.add(new ParameterIdentifier(TlsParameterType.APP_MSG_LENGHT));
        requiredDerivations.add(
                new ParameterIdentifier(TlsParameterType.INCLUDE_ENCRYPT_THEN_MAC_EXTENSION));

        return new ConditionalConstraint(
                requiredDerivations,
                ConstraintBuilder.constrain(
                                getParameterIdentifier().name(),
                                TlsParameterType.CIPHER_SUITE.name(),
                                TlsParameterType.APP_MSG_LENGHT.name(),
                                TlsParameterType.INCLUDE_ENCRYPT_THEN_MAC_EXTENSION.name(),
                                getParameterIdentifier()
                                        + "."
                                        + TlsParameterType.BIT_POSITION.name())
                        .by(
                                (PaddingBitmaskDerivation paddingBitmaskDerivation,
                                        CipherSuiteDerivation cipherSuiteDerivation,
                                        AppMsgLengthDerivation appMsgLengthDerivation,
                                        IncludeEncryptThenMacExtensionDerivation
                                                includeEncryptThenMacDerivation,
                                        BitPositionDerivation bitPositionDerivation) -> {
                                    boolean isEncryptThenMac =
                                            includeEncryptThenMacDerivation.getSelectedValue()
                                                    || enforceEncryptThenMacMode;

                                    if (isEncryptThenMac) {
                                        return resultsInPlausiblePadding(
                                                scope,
                                                paddingBitmaskDerivation,
                                                cipherSuiteDerivation,
                                                appMsgLengthDerivation,
                                                bitPositionDerivation);
                                    }
                                    // without enc-then-mac, a padding error or misread MAC is
                                    // guaranteed - we include coincidentally valid values here
                                    // as this reduces the complexity of the IPM and should not
                                    // result in false positives
                                    return true;
                                }));
    }

    private boolean resultsInPlausiblePadding(
            AnvilTestTemplate scope,
            PaddingBitmaskDerivation paddingBitmaskDerivation,
            CipherSuiteDerivation cipherSuiteDerivation,
            AppMsgLengthDerivation appMsgLengthDerivation,
            BitPositionDerivation bitPositionDerivation) {
        int selectedBitmaskBytePosition = paddingBitmaskDerivation.getSelectedValue();
        CipherSuite selectedCipherSuite = cipherSuiteDerivation.getSelectedValue();
        int selectedAppMsgLength = appMsgLengthDerivation.getSelectedValue();
        int selectedBitPosition = bitPositionDerivation.getSelectedValue();

        int resultingPaddingSize =
                getResultingPaddingSize(
                        true,
                        selectedAppMsgLength,
                        selectedCipherSuite,
                        ConstraintHelper.getTargetVersion(scope));
        if ((selectedBitmaskBytePosition + 1) == resultingPaddingSize
                && (1 << selectedBitPosition) == (resultingPaddingSize - 1)) {
            // padding appears to be only the lengthfield byte
            return false;
        } else if (resultingPaddingSize == 1
                && selectedBitmaskBytePosition == 0
                && (resultingPaddingSize ^ (1 << selectedBitPosition))
                        == AppMsgLengthDerivation.getAsciiLetter()
                && selectedAppMsgLength >= AppMsgLengthDerivation.getAsciiLetter()) {
            // only one byte of padding (lengthfield) gets modified in a way
            // that it matches the ASCII contents of the AppMsg data
            return false;
        }
        return true;
    }

    private boolean chosenByteIsWithinPadding(
            AnvilTestTemplate scope,
            int selectedPaddingBitmaskBytePosition,
            CipherSuite selectedCipherSuite,
            int selectedAppMsgLength,
            boolean isEncryptThenMac) {
        int resultingPaddingSize =
                getResultingPaddingSize(
                        isEncryptThenMac,
                        selectedAppMsgLength,
                        selectedCipherSuite,
                        ConstraintHelper.getTargetVersion(scope));
        return resultingPaddingSize > selectedPaddingBitmaskBytePosition;
    }

    @Override
    protected TlsDerivationParameter<Integer> generateValue(Integer selectedValue) {
        return new PaddingBitmaskDerivation(selectedValue);
    }
}
