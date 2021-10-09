package ar.edu.itba.simulacion.tp4.marsMission;

import static ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.marshalling.XYZWritable;

public final class XYZAnimation {
    private XYZAnimation() {
        // static
    }

    public static void main(String[] args) throws IOException {
         
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        final MarsMissionSimulation simulation = config.toSimulation();

        try(final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {

            simulation.simulate(10000, (i, spaceship, earth, mars, sun) -> {
                // Imprimimos estado
                XYZWritable.xyzWrite(writer, List.of(spaceship, earth, mars, sun));

                if(i % 1000 == 0) {
                    // Informamos que la simulacion avanza
                    System.out.println("Total states processed so far: " + i);
                }
            });
        }
    }
}
