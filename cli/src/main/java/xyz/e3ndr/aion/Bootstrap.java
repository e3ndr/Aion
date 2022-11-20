package xyz.e3ndr.aion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.aion.commands.CommandInstall;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Bootstrap {
    public static final FastLogger LOGGER = new FastLogger("Aion");

    private static final File CONFIG_FILE = new File("config.json");
    private static final BaseCommand BASE = new BaseCommand();

    private static @Getter Config config = new Config();

    public static void main(String[] args) throws InterruptedException {
        FastLoggingFramework.setLogHandler(new LogHandler());

        if (args.length == 0) {
            args = new String[] {
                    "--help"
            };
        }

        if (CONFIG_FILE.exists()) {
            try {
                String content = new String(
                    Files.readAllBytes(CONFIG_FILE.toPath()),
                    StandardCharsets.UTF_8
                );
                config = Rson.DEFAULT.fromJson(content, Config.class);
            } catch (IOException e) {
                LOGGER.fatal("Unable to parse config file:\n%s", e);
                System.exit(1);
            }
        }

        new CommandLine(BASE)
            .setExecutionStrategy((parseResult) -> {
                BASE.setup(); // Intercept execution, do setup, then execute.
                config.save();
                return new CommandLine.RunLast().execute(parseResult);
            })
            .execute(args);
    }

    @Getter
    @JsonClass(exposeAll = true)
    public static class Config {

        public void save() {
            try {
                Files.write(
                    CONFIG_FILE.toPath(),
                    Rson.DEFAULT.toJsonString(this).getBytes(StandardCharsets.UTF_8)
                );
                LOGGER.debug("Updated config.");
            } catch (IOException e) {
                LOGGER.severe("Unable to save config, changes/settings will NOT persist.\n%s", e.getMessage());
            }
        }

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
