package xyz.e3ndr.aion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WindowsExeAlias {

    public static void create(String program) {
        File windowsExecutableAliasFile = new File(Aion.PATH_DIR, program + ".exe");

        try {
            Files.copy(
                new File(Aion.BASE_DIR, "windows-exe-alias.exe").toPath(),
                windowsExecutableAliasFile.toPath()
            );
        } catch (IOException ignored) {} // Silently fail.
    }

}
