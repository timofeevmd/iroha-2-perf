package jp.co.soramitsu.load;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;

public class  Engine {

    public static void main(String[] args) {
        //Database.initBanks(); //Creation tables for banks from csv file
        //Database.initUsersTable("bank_users_env"); //Creation table for users - new env
        GatlingPropertiesBuilder props = new GatlingPropertiesBuilder()
            .resourcesDirectory(IDEPathHelper.mavenResourcesDirectory.toString())
            .resultsDirectory(IDEPathHelper.resultsDirectory.toString())
            .binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString())
            .simulationClass("jp.co.soramitsu.load.simulation.DebugSimulation");
        Gatling.fromMap(props.build());
    }
}
