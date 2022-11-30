package xyz.e3ndr.aion.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import co.casterlabs.rakurai.io.IOUtil;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.types.ExtractionPlan;

public class ApacheArchiveExtractor {

    public static void extract(Archives.Format format, File archiveFile, File destDir, ExtractionPlan plan) throws FileNotFoundException, IOException {
        destDir.mkdirs();

        switch (format) {

            // These are not seekable and thus use a stream implementation.

            case TAR_GZ:
                try (
                    FileInputStream fin = new FileInputStream(archiveFile);
                    GzipCompressorInputStream gzin = new GzipCompressorInputStream(fin);
                    TarArchiveInputStream ain = new TarArchiveInputStream(gzin)) {
                    ArchiveEntry entry = null;
                    while ((entry = ain.getNextEntry()) != null) {
                        File newFile = shouldExtract(destDir, plan, entry);
                        if (newFile == null) continue;

                        extract(newFile, ain);
                    }
                }
                return;

            case TAR_XZ:
                try (
                    FileInputStream fin = new FileInputStream(archiveFile);
                    XZCompressorInputStream xzin = new XZCompressorInputStream(fin);
                    TarArchiveInputStream ain = new TarArchiveInputStream(xzin)) {
                    ArchiveEntry entry = null;
                    while ((entry = ain.getNextEntry()) != null) {
                        File newFile = shouldExtract(destDir, plan, entry);
                        if (newFile == null) continue;

                        extract(newFile, ain);
                    }
                }
                return;

            case TAR:
                try (
                    FileInputStream fin = new FileInputStream(archiveFile);
                    TarArchiveInputStream ain = new TarArchiveInputStream(fin)) {
                    ArchiveEntry entry = null;
                    while ((entry = ain.getNextEntry()) != null) {
                        File newFile = shouldExtract(destDir, plan, entry);
                        if (newFile == null) continue;

                        extract(newFile, ain);
                    }
                }
                return;

            // These use their own seekable files.

            case _7ZIP:
                try (SevenZFile archive = new SevenZFile(archiveFile)) {
                    for (SevenZArchiveEntry entry : archive.getEntries()) {
                        File newFile = shouldExtract(destDir, plan, entry);
                        if (newFile == null) continue;

                        try (InputStream in = archive.getInputStream(entry)) {
                            extract(newFile, in);
                        }
                    }
                }
                return;

            case ZIP:
                try (ZipFile archive = new ZipFile(archiveFile)) {
                    for (ZipArchiveEntry entry : Collections.list(archive.getEntries())) {
                        File newFile = shouldExtract(destDir, plan, entry);
                        if (newFile == null) continue;

                        try (InputStream in = archive.getInputStream(entry)) {
                            extract(newFile, in);
                        }
                    }
                }
                return;
        }
    }

    /**
     * @return null, if you should NOT extract.
     */
    private static File shouldExtract(File destDir, ExtractionPlan plan, ArchiveEntry zipEntry) throws FileNotFoundException, IOException {
        // We ignore directories.
        if (zipEntry.isDirectory()) {
            return null;
        }

        String filename = zipEntry.getName();
        Aion.LOGGER.debug("Found file in archive: %s", filename);

        // Check that the file is in the base directory.
        if (!filename.startsWith(plan.getBase())) {
            Aion.LOGGER.debug("    File was outside of the target base directory, ignoring.");
            return null;
        }

        // Remove the leading base directory.
        filename = filename.substring(plan.getBase().length());
        Aion.LOGGER.debug("    New filename: %s", filename);

        // Check that the file is allowed.
        if (!plan.allowFile(filename)) {
            Aion.LOGGER.debug("    File was either not specified in `keep` or was to be discarded.");
            return null;
        }

        File newFile = newFileNoSlip(destDir, filename);
        newFile.getParentFile().mkdirs(); // Create the parent directory.

        return newFile;
    }

    private static void extract(File newFile, InputStream in) throws FileNotFoundException, IOException {
        Aion.LOGGER.debug("    Extracting file: %s", newFile);

        // Extract the file.
        try (FileOutputStream out = new FileOutputStream(newFile)) {
            byte[] buffer = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
            int read = 0;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        Aion.LOGGER.debug("    Wrote file to: %s", newFile);
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
