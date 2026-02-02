package com.ancientmc.acpgen.tasks.mapping;

import com.ancientmc.acpgen.util.Util;
import net.neoforged.srgutils.IMappingFile;
import net.neoforged.srgutils.IMappingFile.INode;
import net.neoforged.srgutils.IMappingFile.IClass;
import net.neoforged.srgutils.IMappingFile.IField;
import net.neoforged.srgutils.IMappingFile.IMethod;
import net.neoforged.srgutils.IMappingFile.IParameter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Converts an Enigma-generated TSRGv2 file into a directory of four CSVs. */
public abstract class Tsrg2Csv extends DefaultTask {

    @TaskAction
    public void exec() throws IOException {
        File tsrg = Util.getFile(getTsrg());
        File csvDirectory = Util.getFile(getCsvDirectory());
        IMappingFile mapping = IMappingFile.load(tsrg);

        Collection<? extends IClass> classes = mapping.getClasses();
        Csv<? extends IClass> classCsv = getCsv(classes, csvDirectory, "classes");
        Csv<? extends IField> fieldCsv = getFields(classes, csvDirectory);
        Csv<? extends IMethod> methodCsv = getMethods(classes, csvDirectory);
        Csv<? extends IParameter> paramCsv = getParams(classes, csvDirectory);

        List<Csv<? extends INode>> csvs = List.of(classCsv, fieldCsv, methodCsv, paramCsv);
        write(csvs);
    }

    public static void write(List<Csv<? extends INode>> csvs) throws IOException {
        for (Csv<? extends INode> csv : csvs) {
            try (BufferedWriter writer = Files.newBufferedWriter(csv.file.toPath())) {
                writer.write("srg,cnf\n");

                for (CsvEntry entry : csv.getEntries()) {
                    writer.write(entry.toString());
                    writer.flush();
                }
            }
        }
    }

    public Csv<IField> getFields(Collection<? extends IClass> classes, File csvDirectory) {
        Collection<IField> fieldMappings = new ArrayList<>();
        classes.forEach(cls -> fieldMappings.addAll(cls.getFields()));
        return getCsv(fieldMappings, csvDirectory, "fields");
    }

    public Csv<IMethod> getMethods(Collection<? extends IClass> classes, File csvDirectory) {
        Collection<IMethod> methodMappings = new ArrayList<>();
        classes.forEach(cls -> methodMappings.addAll(cls.getMethods()));
        return getCsv(methodMappings, csvDirectory, "methods");
    }

    public Csv<IParameter> getParams(Collection<? extends IClass> classes, File csvDirectory) {
        Collection<IMethod> methodMappings = new ArrayList<>();
        Collection<IParameter> paramMappings = new ArrayList<>();
        classes.forEach(cls -> methodMappings.addAll(cls.getMethods()));
        methodMappings.forEach(m -> paramMappings.addAll(m.getParameters()));
        return getCsv(paramMappings, csvDirectory, "params");
    }

    public <T extends INode> Csv<T> getCsv(Collection<T> mappings, File csvDirectory, String name) {
        File file = new File(csvDirectory, name + ".csv");
        return new Csv<>(mappings, file);
    }

    /** CSV file object. **/
    public static class Csv<T extends INode> {
        public Collection<T> data; // data
        public File file; // file path

        public Csv(Collection<T> data, File file) {
            this.data = data;
            this.file = file;
        }

        public List<CsvEntry> getEntries() {
            List<CsvEntry> entries = new ArrayList<>();
            data.forEach(e -> entries.add(new CsvEntry(e.getOriginal(), e.getMapped())));
            return entries.stream().sorted(new IntermediateComparator())
                    .filter(e -> e.intermediate.contains("_"))
                    .distinct().toList(); // remove duplicates, remove init methods, and sort.
        }
    }

    public static class IntermediateComparator implements Comparator<CsvEntry> {
        @Override
        public int compare(CsvEntry o1, CsvEntry o2) {
            return o1.intermediate.compareTo(o2.intermediate);
        }
    }

    /** CSV line entry. **/
    public static class CsvEntry {
        public String intermediate;
        public String cuneiform;

        public CsvEntry(String intermediate, String cuneiform) {
            this.intermediate = intermediate;
            this.cuneiform = cuneiform;
        }

        public String toString() {
            return intermediate + "," + cuneiform + "\n";
        }
    }

    @InputFile
    public abstract RegularFileProperty getTsrg();

    @OutputDirectory
    public abstract DirectoryProperty getCsvDirectory();
}
