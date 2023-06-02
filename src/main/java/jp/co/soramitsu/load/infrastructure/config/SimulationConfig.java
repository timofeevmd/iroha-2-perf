package jp.co.soramitsu.load.infrastructure.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.aeonbits.owner.ConfigFactory;

@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class SimulationConfig {
    public static final Simulation simulation = ConfigFactory.create(Simulation.class);
}
