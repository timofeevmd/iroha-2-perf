package jp.co.soramitsu.load.simulation;

import io.gatling.javaapi.core.Simulation;
import jp.co.soramitsu.load.base.*;
import jp.co.soramitsu.load.infrastructure.config.SimulationConfig;

import static jp.co.soramitsu.load.base.LoadProfiles.getStabilityClosedProfile;
import static jp.co.soramitsu.load.base.Protocols.httpProtocol;


public class ClosedStabilitySimulation extends Simulation {

    {
        setUp(
            ScenarioSelector.getScenario().injectClosed(getStabilityClosedProfile()).protocols(httpProtocol)).maxDuration(
            (SimulationConfig.simulation.rampDuration() + SimulationConfig.simulation.stageDuration()) * SimulationConfig.simulation.stagesNumber());
    }
}
