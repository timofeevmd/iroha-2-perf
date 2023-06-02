package jp.co.soramitsu.load.base;

import io.gatling.javaapi.core.ScenarioBuilder;
import jp.co.soramitsu.load.base.scenarious.FindAllDomain;

public class ScenarioSelector {

    public static ScenarioBuilder getScenario() {
        return FindAllDomain.apply();
        /*switch (findByValue(System.getProperty("scenario"))) {
            case FINDALLDOMAIN:
                return FindAllDomain.apply();
                return null;
        }*/

    }
}
