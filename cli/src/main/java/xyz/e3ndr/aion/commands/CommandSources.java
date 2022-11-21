package xyz.e3ndr.aion.commands;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.Resolver;
import xyz.e3ndr.aion.commands.CommandSources.CommandSourcesAdd;
import xyz.e3ndr.aion.commands.CommandSources.CommandSourcesRefresh;
import xyz.e3ndr.aion.commands.CommandSources.CommandSourcesRemove;
import xyz.e3ndr.aion.configuration.Sources;
import xyz.e3ndr.aion.types.AionSourceList;

//@NoArgsConstructor
@AllArgsConstructor
// @formatter:off
@Command(
    name = "sources", 
    description = "Lists all configured sources, run `aion sources --help` to see subcommands.", 
    mixinStandardHelpOptions = true, 
    subcommands = {
        CommandSourcesAdd.class,
        CommandSourcesRemove.class,
        CommandSourcesRefresh.class
    }
)
//@formatter:on
public class CommandSources implements Runnable {

    @Override
    public void run() {
        if (Aion.sourceCache().isEmpty()) {
            Aion.LOGGER.info("No sources configured.");
            return;
        }

        Aion.LOGGER.info("Sources:");
        for (AionSourceList sourcelist : Aion.sourceCache()) {
            Aion.LOGGER.info("    %s (%s)", sourcelist.getName(), sourcelist.getUrl());
        }
    }

//    @NoArgsConstructor
    @AllArgsConstructor
    @Command(name = "add", description = "Adds the specified sources.")
    public static class CommandSourcesAdd implements Runnable {

        @Parameters(arity = "1..*", description = "The list of sources to add.", paramLabel = "[URL]")
        private String[] sourcesToAdd;

        @SneakyThrows
        @Override
        public void run() {
            List<String> sources = Aion.config().getSources();
            int count = 0;

            for (String toAdd : this.sourcesToAdd) {
                if (sources.contains(toAdd)) {
                    Aion.LOGGER.warn("%s is already in the source list, ignoring.", toAdd);
                    continue;
                }

                count++;
                sources.add(toAdd);
                Aion.LOGGER.info("Added %s.", toAdd);
            }

            if (count == 0) {
                Aion.LOGGER.info("No sources added.");
                return;
            } else {
                Aion.config().save();
                Aion.LOGGER.info(""); // Newline.
                AionCommands.sources_refresh();
            }
        }

    }

//    @NoArgsConstructor
    @AllArgsConstructor
    @Command(name = "remove", description = "Removes the specified sources.")
    public static class CommandSourcesRemove implements Runnable {

        @Parameters(/*arity = "1..*", */description = "The list of sources to remove.", paramLabel = "[URL]")
        private String[] sourcesToRemove;

        @SneakyThrows
        @Override
        public void run() {
            List<String> sources = Aion.config().getSources();
            int count = 0;

            for (String toRemove : this.sourcesToRemove) {
                boolean removed = sources.remove(toRemove);

                if (removed) {
                    count++;
                    Aion.LOGGER.info("Removed %s.", toRemove);
                } else {
                    Aion.LOGGER.warn("%s is not in the source list, ignoring.", toRemove);
                }
            }

            if (count == 0) {
                Aion.LOGGER.info("No sources removed.");
                return;
            } else {
                Aion.config().save();
                Aion.LOGGER.info(""); // Newline.
                AionCommands.sources_refresh();
            }
        }

    }

//    @NoArgsConstructor
    @AllArgsConstructor
    @Command(name = "refresh", description = "Refreshes the local sourcelist cache.")
    public static class CommandSourcesRefresh implements Runnable {

        @SneakyThrows
        @Override
        public void run() {
            Aion.LOGGER.info("Refreshing source cache, this may take some time.");

            List<AionSourceList> sourcelists = Aion
                .config()
                .getSources()
                .parallelStream()
                .map((url) -> {
                    try {
                        return Resolver.resolve(url);
                    } catch (IOException e) {
                        Aion.LOGGER.fatal("An error occurred whilst grabbing sourcelist:\n%s", e);
                        try {
                            Thread.sleep(500); // Try to allow FLF to flush.
                        } catch (InterruptedException e1) {}
                        System.exit(1);
                        return null;
                    }
                })
                .collect(Collectors.toList());

            Sources.save(sourcelists);
        }

    }

}
