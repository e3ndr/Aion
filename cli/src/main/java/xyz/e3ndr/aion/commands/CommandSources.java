package xyz.e3ndr.aion.commands;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import xyz.e3ndr.aion.Bootstrap;
import xyz.e3ndr.aion.SourceResolver;
import xyz.e3ndr.aion.commands.CommandSources.CommandSourcesAdd;
import xyz.e3ndr.aion.commands.CommandSources.CommandSourcesRefresh;
import xyz.e3ndr.aion.commands.CommandSources.CommandSourcesRemove;
import xyz.e3ndr.aion.configuration.Sources;
import xyz.e3ndr.aion.types.AionSourceList;

@Command(name = "sources", description = "Lists all configured sources", subcommands = {
        CommandSourcesAdd.class,
        CommandSourcesRemove.class,
        CommandSourcesRefresh.class
})
public class CommandSources implements Runnable {

    @Override
    public void run() {
        if (Bootstrap.getSourceCache().isEmpty()) {
            Bootstrap.LOGGER.info("No sources configured.");
            return;
        }

        Bootstrap.LOGGER.info("Sources:");
        for (AionSourceList sourcelist : Bootstrap.getSourceCache()) {
            Bootstrap.LOGGER.info("    %s (%s)", sourcelist.getName(), sourcelist.getUrl());
        }
    }

    public static Integer refresh() throws Exception {
        Bootstrap.LOGGER.info("Refreshing source cache, this may take some time.");

        List<AionSourceList> sourcelists = Bootstrap
            .getConfig()
            .getSources()
            .parallelStream()
            .map((url) -> {
                try {
                    return SourceResolver.resolve(url);
                } catch (IOException e) {
                    Bootstrap.LOGGER.fatal("An error occurred whilst grabbing sourcelist:\n%s", e);
                    try {
                        Thread.sleep(500); // Try to allow FLF to flush.
                    } catch (InterruptedException e1) {}
                    System.exit(1);
                    return null;
                }
            })
            .collect(Collectors.toList());

        Sources.save(sourcelists);

        return 0;
    }

    @Command(name = "add", description = "Adds the specified sources")
    public static class CommandSourcesAdd implements Runnable {

        @Parameters(arity = "1..*", description = "The list of sources to add", paramLabel = "[URL]")
        private String[] sourcesToAdd;

        @SneakyThrows
        @Override
        public void run() {
            List<String> sources = Bootstrap.getConfig().getSources();
            int count = 0;

            for (String toAdd : this.sourcesToAdd) {
                if (sources.contains(toAdd)) {
                    Bootstrap.LOGGER.warn("%s is already in the source list, ignoring.", toAdd);
                    continue;
                }

                count++;
                sources.add(toAdd);
                Bootstrap.LOGGER.info("Added %s.", toAdd);
            }

            if (count == 0) {
                Bootstrap.LOGGER.info("No sources added.");
                return;
            } else {
                Bootstrap.getConfig().save();
                Bootstrap.LOGGER.info(""); // Newline.
                refresh();
            }
        }

    }

    @Command(name = "remove", description = "Removes the specified sources")
    public static class CommandSourcesRemove implements Runnable {

        @Parameters(/*arity = "1..*", */description = "The list of sources to remove", paramLabel = "[URL]")
        private String[] sourcesToRemove;

        @SneakyThrows
        @Override
        public void run() {
            List<String> sources = Bootstrap.getConfig().getSources();
            int count = 0;

            for (String toRemove : this.sourcesToRemove) {
                boolean removed = sources.remove(toRemove);

                if (removed) {
                    count++;
                    Bootstrap.LOGGER.info("Removed %s.", toRemove);
                } else {
                    Bootstrap.LOGGER.warn("%s is not in the source list, ignoring.", toRemove);
                }
            }

            if (count == 0) {
                Bootstrap.LOGGER.info("No sources removed.");
                return;
            } else {
                Bootstrap.getConfig().save();
                Bootstrap.LOGGER.info(""); // Newline.
                refresh();
            }
        }

    }

    @Command(name = "refresh", description = "Refreshes the local sourcelist cache")
    public static class CommandSourcesRefresh implements Runnable {

        @SneakyThrows
        @Override
        public void run() {
            refresh();
        }

    }

}
