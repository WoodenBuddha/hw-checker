package com.webdorphin.bot.homeworkchecker.util;

public final class FileUtils {
    private FileUtils(){}

    public static String removeExtension(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
    }

}
