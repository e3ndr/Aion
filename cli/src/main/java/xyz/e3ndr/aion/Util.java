package xyz.e3ndr.aion;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Util {

    public static void recursivelyDeleteDirectory(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                recursivelyDeleteDirectory(sub);
            }
        }
        file.delete();
    }

    public static void recursivelyMoveDirectoryContents(File sourceDir, File destDir) {
        for (String filename : sourceDir.list()) {
            File sourceFile = new File(sourceDir, filename);
            File destFile = new File(destDir, filename);
            sourceFile.renameTo(destFile);
        }
    }

    @SafeVarargs
    public static <T> List<T> concat(List<T>... lists) {
        List<T> list = new LinkedList<>();

        for (List<T> l : lists) {
            list.addAll(l);
        }

        return list;
    }

}
