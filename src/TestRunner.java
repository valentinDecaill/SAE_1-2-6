import org.junit.platform.launcher  .Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class TestRunner {
    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectPackage("merelle.model"), selectPackage("merelle.control"))
            .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request, listener);
        TestExecutionSummary summary = listener.getSummary();
        long total = summary.getTestsFoundCount();
        long passed = summary.getTestsSucceededCount();
        long failed = summary.getTestsFailedCount();
        System.out.println("Tests found: " + total);
        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            System.out.println("FAILED: " + failure.getTestIdentifier().getDisplayName());
            failure.getException().printStackTrace();
        }
        if (failed > 0) {
            System.exit(1);
        }
    }
}
