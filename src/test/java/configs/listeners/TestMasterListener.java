package configs.listeners;

import org.testng.ITestContext;
import org.testng.ITestResult;

public interface TestMasterListener {

    default void onSetConfig(ITestContext context) {
    }

    default boolean onRunResolveInterception(ITestResult testResult) {
        return false;
    }

    default void onAfterSuite(ITestContext context) {
    }
}
