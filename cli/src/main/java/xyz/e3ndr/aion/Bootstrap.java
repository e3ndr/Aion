package xyz.e3ndr.aion;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.IExecutionStrategy;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import xyz.e3ndr.aion.commands.CommandInstall;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Bootstrap {
    public static final FastLogger LOGGER = new FastLogger("Aion");

    private static @Getter Config config = new Config();

    public static void main(String[] args) throws InterruptedException {
        FastLoggingFramework.setLogHandler(new LogHandler());

        if (args.length == 0) {
            args = new String[] {
                    "--help"
            };
        }

        int exitCode = new CommandLine(config)
            .setExecutionStrategy(new IExecutionStrategy() {
                @Override
                public int execute(ParseResult parseResult) throws ExecutionException, ParameterException {
                    config.setup(); // Intercept execution, do setup, then execute.
                    return new CommandLine.RunLast().execute(parseResult);
                }
            })
            .execute(args);

//        if (exitCode != 0) {
//            Thread.sleep(100); // Hack to allow FLF to flush.
//            System.exit(1);
//        }
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
        }
    )
    // @formatter:on
    public static class Config implements Runnable {

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

            LOGGER.debug("Using config:\n%s", Rson.DEFAULT.toJson(this));
        }

    }

}
