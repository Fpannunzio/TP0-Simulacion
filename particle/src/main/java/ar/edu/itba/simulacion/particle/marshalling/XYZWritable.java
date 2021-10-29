package ar.edu.itba.simulacion.particle.marshalling;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;

public interface XYZWritable {

    /** Line separator to use. OS dependant */
    String LINE_SEPARATOR = System.lineSeparator();

    String FIELD_SEPARATOR = " ";

    static void newLine(final Writer writer) throws IOException {
        writer.write(LINE_SEPARATOR);
    }

    static void xyzWrite(final Writer writer, final XYZWritable writable) throws RuntimeException {
        try {
            writable.xyzWrite(writer);
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void xyzWrite(final Writer writer, final Collection<? extends XYZWritable> writable) throws RuntimeException {
        try {
            writeCollection(writer, writable);
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    static void xyzWrite(final Writer writer, final Collection<? extends XYZWritable> ...writables) throws RuntimeException {
        final long totalSize = Arrays.stream(writables).mapToInt(Collection::size).sum();

        try {
            writer.write(String.valueOf(totalSize));
            XYZWritable.newLine(writer);
            XYZWritable.newLine(writer);

            for(final Collection<? extends XYZWritable> writable : writables) {
                for(final XYZWritable elem : writable) {
                    elem.xyzWrite(writer);
                }
            }
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void writeCollection(final Writer writer, final Collection<? extends XYZWritable> collection) throws IOException {
        writer.write(String.valueOf(collection.size()));
        XYZWritable.newLine(writer);
        XYZWritable.newLine(writer);

        for(final XYZWritable elem : collection) {
            elem.xyzWrite(writer);
        }
    }

    static void exportToFile(final String filePath, final XYZWritable writable) {
        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writable.xyzWrite(writer);
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void exportToFile(final String filePath, final Collection<? extends XYZWritable> writables) {
        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for(final XYZWritable writable : writables) {
                writable.xyzWrite(writer);
            }
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    void xyzWrite(final Writer writer) throws IOException;
}
