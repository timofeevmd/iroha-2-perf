package jp.co.soramitsu.load.simulation;

import io.gatling.javaapi.core.Simulation;
import jp.co.soramitsu.load.base.*;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static jp.co.soramitsu.load.base.Protocols.httpProtocol;

public class DebugSimulation extends Simulation {

    {
        setUp(
            ScenarioSelector.getScenario().injectOpen(atOnceUsers(10))).protocols(httpProtocol);
    }
}
