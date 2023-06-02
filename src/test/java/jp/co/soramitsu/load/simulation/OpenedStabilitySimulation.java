package jp.co.soramitsu.load.simulation;

import io.gatling.javaapi.core.Simulation;
import jp.co.soramitsu.load.base.*;
import jp.co.soramitsu.load.infrastructure.config.SimulationConfig;

import static jp.co.soramitsu.load.base.LoadProfiles.getStabilityOpenProfile;
import static jp.co.soramitsu.load.base.Protocols.httpProtocol;


public class OpenedStabilitySimulation extends Simulation {

    {
        setUp(
            ScenarioSelector.getScenario()
                            .injectOpen(getStabilityOpenProfile())
                            .protocols(httpProtocol))
            .maxDuration((SimulationConfig.simulation.rampDuration() + SimulationConfig.simulation.stageDuration()) * SimulationConfig.simulation.stagesNumber());
    }

}
