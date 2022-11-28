package xyz.e3ndr.aion;

import co.casterlabs.commons.platform.Arch;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.aion.commands.CommandInstall;
import xyz.e3ndr.aion.commands.CommandList;
import xyz.e3ndr.aion.commands.CommandPath;
import xyz.e3ndr.aion.commands.CommandRemove;
import xyz.e3ndr.aion.commands.CommandRun;
import xyz.e3ndr.aion.commands.CommandSources;
import xyz.e3ndr.aion.commands.CommandUpdate;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Bootstrap {
    private static final BaseCommand BASE = new BaseCommand();

    public static void main(String[] args) throws InterruptedException {
        FastLoggingFramework.setLogHandler(new LogHandler());

        // If there are no args then output the help command.
        if (args.length == 0) {
            args = new String[] {
                    "--help"
            };
        }

        new CommandLine(BASE)
            .setExecutionStrategy((parseResult) -> {
                BASE.initCLIOptions(); // Intercept execution, do setup, then execute.
                Aion.setup(BASE.arch, BASE.distribution);

                return new CommandLine.RunLast().execute(parseResult);
            })
            .execute(args);
    }

    // @formatter:off
    @Getter
    @JsonClass(exposeAll = true)
    @Command(
        name = "aion",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        subcommands = {
            CommandInstall.class,
            CommandList.class,
            CommandPath.class,
            CommandRemove.class,
            CommandRun.class,
            CommandSources.class,
            CommandUpdate.class
        }
    )
    // @formatter:on
    public static class BaseCommand implements Runnable {

        @Option(names = {
                "-vb",
                "--verbosity"
        }, description = "Sets the verbosity of logging, either ALL, FATAL, SEVERE, WARNING, INFO, DEBUG, or TRACE.")
        private LogLevel verbosity = LogLevel.INFO;

        @Option(names = {
                "-nc",
                "--no-color"
        }, description = "Disables colored console output.")
        private boolean noColor = false;

        @Option(names = {
                "--override-architecture"
        }, description = "Overrides the current system cpu architecture, Not recommended.")
        private Arch arch;

        @Option(names = {
                "--override-distribution"
        }, description = "Overrides the current system distribution, Not recommended.")
        private OSDistribution distribution;

        @Override
        public void run() {} // Never executed.

        private void initCLIOptions() {
            FastLoggingFramework.setColorEnabled(!this.noColor);

            if (this.arch == null) {
                this.arch = Platform.arch;
            } else {
                Aion.LOGGER.warn("System CPU architecture overriden to '%s', this may cause problems.", this.arch);
            }

            if (this.distribution == null) {
                this.distribution = Platform.osDistribution;
            } else {
                Aion.LOGGER.warn("System distribution overriden to '%s', this may cause problems.", this.distribution);
            }

            Aion.LOGGER.setCurrentLevel(this.verbosity);
        }

    }

}
