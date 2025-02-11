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
    public static String getOldClass(String newEntry, File match, String prefix, int tabIndex) {
        List<String> lines = getPrefixedLines(match, prefix);
        for (String line : lines) {
            if (line.contains(newEntry)) {
                return getFilteredName(line.split("\t")[tabIndex]);
            }
        }
        return null;
    }

    /**
     * Matcher doesn't append class names to the beginnnings fields and methods,
     * This means we have to do some extra work to ensure we get the right deobfuscated inners. This method parses the
     * match file starting at the inner's parent, and keeps parsing until it matches with the type.
     */
    public static String getOldInner(String newEntry, String parent, String desc, File match, String prefix, int tabIndex) {
        // Get the class line for our inner type.
        boolean exists = getPrefixedLines(match, "c").stream().anyMatch(l -> l.contains("L" + parent + ";"));
        if (exists) {
            String classLine = getPrefixedLines(match, "c").stream().filter(l -> l.contains("L" + parent + ";")).findAny().get();
            try {
                // Parse through all lines of the match file, *starting* at our class line, and *ending* before the next class set.
                List<String> allLines = Files.readAllLines(match.toPath());
                List<String> lines = allLines.subList(allLines.indexOf(classLine), getNextClassIndex(allLines, classLine));

                for (String line : lines) {

                    // Stop parsing when we reach our type in the given pattern, AKA the unique name-desc combo.
                    // For fields, the name and desc are separated by two semicolons (e.g. "a;;F"). Methods are not separated. (e.g. "a()V")
                    String pattern = (prefix.equals("\tf")) ? (newEntry + ";;" + desc) : newEntry + desc;
                    if (line.contains(pattern)) {
                        return getFilteredName(line.split("\t")[tabIndex]);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static int getNextClassIndex(List<String> lines, String classLine) {
        for (int i = lines.indexOf(classLine) + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("c\tL")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the pure, filtered entry name, removing the extra stuff Matcher adds.
     * @param name The entry name.
     * @return The filtered entry name.
     */
    public static String getFilteredName(String name) {
        return switch (name.charAt(0)) {
            case 'L' -> // class
                    name.substring(1, name.indexOf(';')); // L<class_name>; -> <class_name>
            case 'f' -> // field
                    name.substring(0, 7); // <field_name>;; -> <field_name>
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
                    .filter(l -> l.startsWith(prefix + "\t")) // add space to remove any uncertainty about entry types.
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
