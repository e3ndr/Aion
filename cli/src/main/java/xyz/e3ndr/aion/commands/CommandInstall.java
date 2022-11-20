package xyz.e3ndr.aion.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Bootstrap;

@Command(name = "install", description = "Installs the specified list of packages")
public class CommandInstall implements Callable<Integer> {

    @Parameters(arity = "1..*", description = "The list of packages to install", paramLabel = "PACKAGE[:VERSION]")
    private String[] interim_packagesToInstall;

    @Override
    public Integer call() throws Exception {
        Map<String, String> packagesToInstall = new HashMap<>();

        for (String interim : this.interim_packagesToInstall) {
            String[] parts = interim.split(":");

            if (parts.length > 2) {
                throw new IllegalArgumentException("Package declaration must be in the form of PACKAGE[:VERSION], got: " + interim);
            }

            String name = parts[0].toLowerCase();
            String version = "LATEST";

            if (parts.length == 2) {
                version = parts[1].toUpperCase();
            }

            packagesToInstall.put(name, version);
        }

        Bootstrap.LOGGER.info("Are you sure you wish to install the following packages? (Y/n)");
        for (Map.Entry<String, String> entry : packagesToInstall.entrySet()) {
            Bootstrap.LOGGER.info("    %s:%s", entry.getKey(), entry.getValue());
        }

        return -1;
    }

}
