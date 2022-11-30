package xyz.e3ndr.aion.archive;

import java.net.URI;

import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;

public class Archives {

    public static @Nullable Format probeFormat(String location) {
        String pathname = URI.create(location)
            .getPath()
            .toLowerCase();

        for (Format f : Format.values()) {
            if (pathname.endsWith(f.extension)) {
                return f;
            }
        }

        return null;
    }

    @AllArgsConstructor
    public static enum Format {
        // @formatter:off
        TAR_GZ(".tar.gz"),
        TAR_XZ(".tar.xz"),
        TAR   (".tar"),
        _7ZIP (".7z"),
        ZIP   (".zip"),
        ;
        // @formatter:on

        public final String extension;

    }

}
