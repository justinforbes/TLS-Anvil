package de.rub.nds.tlstest.framework.parameterExtensions.configurationOptionsExtension;

import de.rub.nds.anvilcore.model.parameter.ParameterScope;

public class CommonBuildParameterScope extends ParameterScope {

    public static final String SCOPE_IDENTIFIER = "CommonBuildParameter";

    private final String fullUniqueIdentifier;
    private final String parameterSpecifier;

    public CommonBuildParameterScope(String parameterSpecifier) {
        this.parameterSpecifier = parameterSpecifier;
        fullUniqueIdentifier = SCOPE_IDENTIFIER + "(" + parameterSpecifier + ")";
    }

    @Override
    public String getUniqueScopeIdentifier() {
        return fullUniqueIdentifier;
    }

    public String getParameterSpecifier() {
        return parameterSpecifier;
    }
}
