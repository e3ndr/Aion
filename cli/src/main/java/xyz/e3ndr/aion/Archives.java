package xyz.e3ndr.aion;

import java.io.File;
import java.net.URI;

import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;
import xyz.e3ndr.aion.types.AionPackage;

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
        TAR_GZ(".zip", null),
        ZIP(".tar.gz", null);

        public final String extension;
        public final Extractor extractor;

    }

    public static interface Extractor {
        public void extract(File archive, File destDir, AionPackage.Version.ExtractionPlan plan);
    }

}
