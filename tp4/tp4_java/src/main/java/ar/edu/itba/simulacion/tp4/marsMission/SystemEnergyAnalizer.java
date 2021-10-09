package ar.edu.itba.simulacion.tp4.marsMission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.marsMission.SimulationSettings.MarsMissionConfig;

public class SystemEnergyAnalizer {

    public static final int MAX_ITERATIONS = 10_000_000;
    public static final int TOTAL_DT_TRIED = 5;
    public static final int DT_FACTOR_MULTIPLIER = 10;
    public static final int OUTPUT_SAMPLE_RATE = MAX_ITERATIONS / 10_000;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MarsMissionConfig config = mapper.readValue(new File(args[0]), MarsMissionConfig.class);

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(config.outputFile))) {

            for (int dtCount = 0; dtCount < TOTAL_DT_TRIED; dtCount++) {
                final MarsMissionSimulation simulation = config.toSimulation();

                simulation.simulate((i, spaceship, earth, mars, sun) -> {
                    // Imprimimos estado
                    if (i % OUTPUT_SAMPLE_RATE == 0) {
                        try {
                            writer.write(config.dt + "," + i + "," + simulation.getSystemEnergy());
                            writer.newLine();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                    if (i % (MAX_ITERATIONS / 10) == 0) {
                        // Informamos que la simulacion avanza
                        System.out.println("Total states processed so far: " + i);
                    }

                    return i <= MAX_ITERATIONS;
                });
                config.dt *= DT_FACTOR_MULTIPLIER;
            }
        }
    }
}