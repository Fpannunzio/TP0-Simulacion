package ar.edu.itba.simulacion.particle.marshalling;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public interface XYZWritable {

    /** Line separator to use. OS dependant */
    String LINE_SEPARATOR = System.lineSeparator();

    String FIELD_SEPARATOR = " ";

    static void newLine(final Writer writer) throws IOException {
        writer.write(LINE_SEPARATOR);
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
