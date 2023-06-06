package jp.co.soramitsu.load.base.scenarious;

import static io.gatling.javaapi.core.CoreDsl.scenario;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import java.util.ArrayList;
import java.util.List;
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Domain;

public class FindAllDomain {

    public static final ScenarioBuilder scn = scenario("find all domain")
        .exec(session -> {
                List domains = new ArrayList<jp.co.soramitsu.iroha2.generated.datamodel.domain.Domain>();
                domains.add(Constant.Companion.findAllDomainObj());
                Session newSession = session.set("DOMAINS", domains);
                return newSession;
            }
        );

    public static ScenarioBuilder apply() {
        return scn;
    }
}
