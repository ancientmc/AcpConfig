package com.ancientmc.acpconfig.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses through a .match file to get the matched intermediary for a given obfuscated entry.
 */
public class MatchParser {

    /**
     * Gets the intermediary of the class.
     * @param newEntry The obfuscated entry from the new version.
     * @param match The match file.
     * @param prefix The prefix for the type (e.g. "c" used for classes)
     * @param tabIndex The tab index used when splitting the line.
     * @return The old name, if it exists.
     */
    public static String getOld(String newEntry, File match, String prefix, int tabIndex) {
        List<String> lines = getPrefixedLines(match, prefix);
        String name = "";
        for (String line : lines) {
            if (line.contains(newEntry)) {
                name = getFilteredName(line.split("\t")[tabIndex]);
            }
        }
        return name;
    }

    /**
     * Gets the pure, filtered entry name, removing the extra stuff Matcher adds.
     * @param name The entry name.
     * @return The filtered entry name.
     */
    public static String getFilteredName(String name) {
        return switch (name.charAt(0)) {
            case 'L' -> // class
                    name.substring(1, name.indexOf(';') - 1); // L<class_name>; -> <class_name>
            case 'f' -> // field
                    name.substring(0, name.indexOf(';') - 1); // <field_name>;; -> <field_name>
            case 'm' -> // method
                    name.substring(0, name.indexOf('(') - 1); // <method_name>descriptor -> <method_name>
            default -> name; // anything else
        };
    }

    /**
     * Filters through the match file and returns a filtered line list based on the given prefix.
     * @param match The match file.
     * @param prefix The prefix at the beginning of the line that indicates what type the entry is.
     *               c -> classes, \tf -> fields, \tm -> methods
     * @return The lines in the match file that have the prefix.
     */
    public static List<String> getPrefixedLines(File match, String prefix) {
        try {
            return Files.readAllLines(match.toPath()).stream()
                    .filter(l -> l.startsWith(prefix + " ")) // add space to remove any uncertainty about entry types.
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
