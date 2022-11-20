package xyz.e3ndr.aion;

import java.util.List;

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
import xyz.e3ndr.aion.configuration.Config;
import xyz.e3ndr.aion.configuration.Installed;
import xyz.e3ndr.aion.configuration.Sources;
import xyz.e3ndr.aion.types.AionPackage;
import xyz.e3ndr.aion.types.AionSourceList;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Bootstrap {
    public static final FastLogger LOGGER = new FastLogger("Aion");

    private static final BaseCommand BASE = new BaseCommand();

    private static @Getter Config config;
    private static @Getter List<AionSourceList> sourceCache;
    private static @Getter List<AionPackage.Version> installCache;

    public static void main(String[] args) throws InterruptedException {
        FastLoggingFramework.setLogHandler(new LogHandler());

        if (args.length == 0) {
            args = new String[] {
                    "--help"
            };
        }

        new CommandLine(BASE)
            .setExecutionStrategy((parseResult) -> {
                BASE.setup(); // Intercept execution, do setup, then execute.

                config = Config.load();
                sourceCache = Sources.load();
                installCache = Installed.load();

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
            CommandSources.class
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

        @Override
        public void run() {} // Never executed.

        private void setup() {
            FastLoggingFramework.setColorEnabled(!this.noColor);
            LOGGER.setCurrentLevel(this.verbosity);
        }

    }

}
