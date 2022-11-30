package xyz.e3ndr.aion.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import co.casterlabs.rakurai.io.IOUtil;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.types.ExtractionPlan;

public class ApacheArchiveExtractor {

    public static void extract(Archives.Format format, File archive, File destDir, ExtractionPlan plan) throws FileNotFoundException, IOException {
        switch (format) {
        }
    }

    private static void extract(File destDir, ExtractionPlan plan, ArchiveInputStream ais) throws FileNotFoundException, IOException {
        ArchiveEntry zipEntry;

        while ((zipEntry = ais.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) continue; // Ignore.

            String filename = zipEntry.getName();
            Aion.LOGGER.debug("Found file in archive: %s", filename);

            // Check that the file is in the base directory.
            if (!filename.startsWith(plan.getBase())) {
                Aion.LOGGER.debug("    File was outside of the target base directory, ignoring.");
                continue;
            }

            // Remove the leading base directory.
            filename = filename.substring(plan.getBase().length());
            Aion.LOGGER.debug("    New filename: %s", filename);

            // Check that the file is allowed.
            if (!plan.allowFile(filename)) {
                Aion.LOGGER.debug("    File was either not specified in `keep` or was to be discarded.");
                continue;
            }

            File newFile = newFileNoSlip(destDir, filename);
            newFile.getParentFile().mkdirs(); // Create the parent directory.

            // Extract the file.
            try (FileOutputStream out = new FileOutputStream(newFile)) {
                byte[] buffer = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
                int read = 0;

                while ((read = ais.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            Aion.LOGGER.debug("    Wrote file to: %s", newFile);
        }
    }

    private static File newFileNoSlip(File destDir, String filename) throws IOException {
        File destFile = new File(destDir, filename);

        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            String message = "    File was outside of the destination directory. (ZipSlip)";
            Aion.LOGGER.fatal(message);
            throw new IOException(message);
        }

        return destFile;
    }

}
