/**
 * TLS-Test-Framework - A framework for modeling TLS tests
 *
 * <p>Copyright 2022 Ruhr University Bochum
 *
 * <p>Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.framework;

import de.rub.nds.anvilcore.execution.TestRunner;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.launcher.TestPlan;

/**
 * Registry for managing multiple TestContext instances identified by unique IDs. This replaces the
 * singleton pattern in TestContext to support concurrent test execution with different
 * configurations.
 */
public class TestContextRegistry {
    private static final ConcurrentHashMap<String, TestContext> CONTEXTS =
            new ConcurrentHashMap<>();

    /**
     * Creates a new TestContext instance using the provided TestPlan ID. This method should only be
     * called by TestPreparator.
     *
     * @param testPlanId the ID from the TestPlan
     * @return the TestContext instance created
     */
    public static TestContext createContext(String testPlanId) {
        TestContext context = new TestContext();
        CONTEXTS.put(TestRunner.CONTEXT_ID_PROPERTY, context);
        return context;
    }

    /**
     * Retrieves a TestContext by its ID.
     *
     * @param contextId the context ID
     * @return the TestContext instance, or null if not found
     */
    public static TestContext getContext(String contextId) {
        return CONTEXTS.get(contextId);
    }

    /**
     * Removes a TestContext from the registry.
     *
     * @param contextId the context ID to remove
     * @return the removed TestContext, or null if not found
     */
    public static TestContext removeContext(String contextId) {
        return CONTEXTS.remove(contextId);
    }

    /** Clears all contexts from the registry. */
    public static void clearAll() {
        CONTEXTS.clear();
    }

    /**
     * Returns the number of active contexts.
     *
     * @return the number of contexts in the registry
     */
    public static int getActiveContextCount() {
        return CONTEXTS.size();
    }

    /**
     * Checks if a context exists for the given ID.
     *
     * @param contextId the context ID to check
     * @return true if context exists, false otherwise
     */
    public static boolean hasContext(String contextId) {
        return CONTEXTS.containsKey(contextId);
    }

    /**
     * Returns any available TestContext from the registry. This is used as a fallback when no
     * specific context ID is available.
     *
     * @return a TestContext instance if any exists, or null if registry is empty
     */
    public static TestContext getAnyContext() {
        return CONTEXTS.values().stream().findFirst().orElse(null);
    }

    /**
     * Retrieves a TestContext by extracting the context ID from the JUnit TestPlan. This is a
     * convenience method for JUnit TestExecutionListener methods that need to access their specific
     * context.
     *
     * @param testPlan the JUnit TestPlan
     * @return the TestContext instance for the context ID found in the test plan, or null if not
     *     found
     */
    public static TestContext byTestPlan(TestPlan testPlan) {
        String contextId =
                testPlan.getConfigurationParameters()
                        .get(TestRunner.CONTEXT_ID_PROPERTY)
                        .orElse(null);
        if (contextId == null) {
            return null;
        }
        return getContext(contextId);
    }

    /**
     * Retrieves a TestContext by extracting the context ID from the JUnit ExtensionContext. This is
     * a convenience method for JUnit tests that need to access their specific context.
     *
     * @param extensionContext the JUnit ExtensionContext
     * @return the TestContext instance for the context ID found in the extension context, or null
     *     if not found
     */
    public static TestContext byExtensionContext(ExtensionContext extensionContext) {
        String contextId = getContextIdFromExtensionContext(extensionContext);
        if (contextId == null) {
            return null;
        }
        return getContext(contextId);
    }

    /**
     * Extracts the context ID from the JUnit ExtensionContext configuration parameters.
     *
     * @param extensionContext the JUnit ExtensionContext
     * @return the context ID, or null if not found
     */
    private static String getContextIdFromExtensionContext(ExtensionContext extensionContext) {
        ExtensionContext current = extensionContext;
        while (current != null) {
            String contextId =
                    current.getConfigurationParameter(TestRunner.CONTEXT_ID_PROPERTY).orElse(null);
            if (contextId != null) {
                return contextId;
            }
            current = current.getParent().orElse(null);
        }
        return null;
    }
}
