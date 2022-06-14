package pl.edu.icm.pdyn2.administration;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.isolation.IsolationService;
import pl.edu.icm.trurl.ecs.EntitySystem;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

/**
 * Builds a System instance that uses EligibleForTestsSelector
 * to run testingService#maybeTestAgent
 */
public class TestingSystemBuilder {

    private final EligibleForTestsSelector eligibleForTestsSelector;
    private final TestingService testingService;
    private final IsolationService isolationService;

    @WithFactory
    public TestingSystemBuilder(EligibleForTestsSelector eligibleForTestsSelector,
                                TestingService testingService,
                                IsolationService isolationService) {
        this.eligibleForTestsSelector = eligibleForTestsSelector;
        this.testingService = testingService;
        this.isolationService = isolationService;
    }

    public EntitySystem buildTestingSystem() {
        return select(eligibleForTestsSelector)
                .forEach(e -> {
                    isolationService.maybeIsolateAgent(e);
                    testingService.maybeTestAgent(e);
                });
    }
}
