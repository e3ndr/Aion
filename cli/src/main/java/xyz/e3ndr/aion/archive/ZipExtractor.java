package xyz.e3ndr.aion.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import co.casterlabs.rakurai.io.IOUtil;
import xyz.e3ndr.aion.Aion;
import xyz.e3ndr.aion.types.ExtractionPlan;

class ZipExtractor implements Archives.Extractor {

    @Override
    public void extract(File archive, File destDir, ExtractionPlan plan) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archive))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) continue; // Ignore.

                String filename = zipEntry.getName();
                Aion.LOGGER.debug("Found file in zip: %s", filename);

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
                    Aion.LOGGER.debug("    File either was not specified in `keep` or was to be discarded.");
                    continue;
                }

                File newFile = newFileNoSlip(destDir, filename);
                newFile.getParentFile().mkdirs(); // Create the parent directory.

                // Extract the file.
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
                    int read = 0;

                    while ((read = zis.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                Aion.LOGGER.debug("    Wrote file to: %s", newFile);
            }
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
