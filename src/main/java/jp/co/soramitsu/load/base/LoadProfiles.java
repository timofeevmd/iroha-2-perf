package jp.co.soramitsu.load.base;

import io.gatling.javaapi.core.ClosedInjectionStep;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.incrementConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.rampConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import io.gatling.javaapi.core.OpenInjectionStep;
import jp.co.soramitsu.load.infrastructure.config.SimulationConfig;

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
