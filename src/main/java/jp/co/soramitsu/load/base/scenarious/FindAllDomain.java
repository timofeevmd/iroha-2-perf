package jp.co.soramitsu.load.base.scenarious;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;

import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.scenario;

public class FindAllDomain {

    public static ScenarioBuilder apply() {
        return scn;
    }

    public static final ScenarioBuilder scn = scenario("find all domain")
            //send request
            .exec(session -> { // session - current user session state
                List<kotlinx.coroutines.Job>domains = new ArrayList<>();
                domains.add(Constant.Companion.findAllDomainObj());
                Session newSession = session.set("DOMAINS", domains);
                return newSession;
                }
            );
}
