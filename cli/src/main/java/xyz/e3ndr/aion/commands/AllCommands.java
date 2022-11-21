package xyz.e3ndr.aion.commands;

import lombok.AllArgsConstructor;

/**
 * This entire class is essentially a mapping of the commands to easy-to-use
 * methods. All command classes are annotated with {@link AllArgsConstructor} to
 * generate the constructors, just make sure that this class stays up-to-date
 * with the new method signatures (fix any errors).
 */
public class AllCommands {

    /* ---- Install ---- */

    public static void install(boolean isDryRun, boolean autoYes, boolean reinstall, String... packagesToInstall) {
        new CommandInstall(isDryRun, autoYes, reinstall, packagesToInstall)
            .run();
    }

    /* ---- List ---- */

    public static void list() {
        new CommandList()
            .run();
    }

    /* ---- Path ---- */

    public static void path_list() {
        new CommandPath()
            .run();
    }

    public static void path_what(String... packagesToCheck) {
        new CommandPath.CommandPathWhat(packagesToCheck)
            .run();
    }

    public static void path_update(boolean easy, String packageToUse, String... commandsToUpdate) {
        new CommandPath.CommandPathUpdate(easy, packageToUse, commandsToUpdate)
            .run();
    }

    public static void path_rebuild() {
        new CommandPath.CommandPathRebuild()
            .run();
    }

    /* ---- Remove ---- */

    public static void remove(boolean isDryRun, boolean autoYes, String... packagesToRemove) {
        new CommandRemove(isDryRun, autoYes, packagesToRemove)
            .run();
    }

    /* ---- Run ---- */

    public static void run(boolean autoInstall, String[] packagesToUse, String... command) {
        new CommandRun(autoInstall, packagesToUse, command)
            .run();
    }

    /* ---- Sources ---- */

    public static void sources_list() {
        new CommandSources()
            .run();
    }

    public static void sources_add(String... sourcesToAdd) {
        new CommandSources.CommandSourcesAdd(sourcesToAdd)
            .run();
    }

    public static void sources_remove(String... sourcesToRemove) {
        new CommandSources.CommandSourcesRemove(sourcesToRemove)
            .run();
    }

    public static void sources_refresh() {
        new CommandSources.CommandSourcesRefresh()
            .run();
    }

    /* ---- Update ---- */

    public static void update(boolean isDryRun, boolean autoYes) {
        new CommandUpdate(isDryRun, autoYes)
            .run();
    }

}
