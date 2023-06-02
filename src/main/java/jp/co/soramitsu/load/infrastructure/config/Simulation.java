package jp.co.soramitsu.load.infrastructure.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
    "system:properties",
    "system:env",
    "classpath:simulation.properties"
})
public interface Simulation extends Config {

    @Key("rampDuration")
    Long rampDuration();

    @Key("stageDuration")
    int stageDuration();

    @Key("intensity")
    Integer intensity();

    @Key("stagesNumber")
    Integer stagesNumber();

    @Key("database.host")
    String databaseHost();

    @Key("database.user")
    String databaseUser();

    @Key("database.password")
    String databasePassword();

    @Key("database.table")
    String databaseTable();

    @Key("bank1.alias")
    String bank1Alias();

    @Key("bank2.alias")
    String bank2Alias();

    @Key("list.banks.aliases")
    String listBanksAliases();

    @Key("soap.username")
    String soapUsername();

    @Key("soap.password")
    String soapPassword();

    @Key("wsdl.path")
    String wsdlPath();

    @Key("bank.csv")
    String bankCsv();
}
