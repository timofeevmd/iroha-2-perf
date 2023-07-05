package jp.co.soramitsu.load.base;

import io.gatling.javaapi.core.ScenarioBuilder;
import jp.co.soramitsu.load.base.scenarious.FindAllDomain;
import jp.co.soramitsu.load.base.scenarious.RegisterDomain;

public class ScenarioSelector {


    public static ScenarioBuilder getScenario() {
        //return FindAllDomain.Companion.apply();
        return RegisterDomain.Companion.apply();
        /*switch (findByValue(System.getProperty("scenario"))) {
            case FINDALLDOMAIN:
                return FindAllDomain.apply();
                return null;
        }*/

    }
}
