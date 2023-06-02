package jp.co.soramitsu.load.base;

import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.OpenInjectionStep;
import jp.co.soramitsu.load.infrastructure.config.SimulationConfig;

import static io.gatling.javaapi.core.CoreDsl.*;

public class LoadProfiles {
    public static ClosedInjectionStep getMaxPerformanceClosedProfile() {
        return incrementConcurrentUsers(SimulationConfig.simulation.intensity())
                .times(SimulationConfig.simulation.stagesNumber())
                .eachLevelLasting(SimulationConfig.simulation.stageDuration())
                .separatedByRampsLasting(SimulationConfig.simulation.rampDuration())
                .startingFrom(0);
    }

    public static OpenInjectionStep[] getStabilityOpenProfile() {
        return new OpenInjectionStep[]{
                rampUsersPerSec(0).to(SimulationConfig.simulation.intensity()).during(SimulationConfig.simulation.rampDuration()),
                constantUsersPerSec(SimulationConfig.simulation.intensity()).during(SimulationConfig.simulation.stageDuration())
        };
    }

    public static ClosedInjectionStep[] getStabilityClosedProfile() {
        return new ClosedInjectionStep[]{
                rampConcurrentUsers(0).to(SimulationConfig.simulation.intensity()).during(SimulationConfig.simulation.rampDuration()),
                constantConcurrentUsers(SimulationConfig.simulation.intensity()).during(SimulationConfig.simulation.stageDuration())
        };
    }
}
