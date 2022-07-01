package pl.maciejwalkowiak.drools;

import org.kie.api.runtime.KieSession;
import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be injected instead of {@link KieSession} in test classes to improve readability in case if only basic methods are used
 *
 * @author Maciej Walkowiak
 */
public class DroolsSessionImpl implements DroolsSession {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsSessionImpl.class);

    private KieSession kieSession;

    public DroolsSessionImpl(KieSession statefulSession) {
        this.kieSession = statefulSession;
    }

    @Override
    public void fire(String ruleName) {
        LOG.debug("Firing rule: {}", ruleName);

        this.kieSession.fireAllRules(new RuleNameEqualsAgendaFilter(ruleName));
    }

    @Override
    public void fireAllRules() {
        this.kieSession.fireAllRules();
    }

    @Override
    public void insert(Object object) {
        this.kieSession.insert(object);
    }

    @Override
    public KieSession getKieSession() {
        return kieSession;
    }
}
