package ar.edu.itba.simulacion.tp5;

import static ar.edu.itba.simulacion.tp5.SimulationSettings.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

public final class Caudal {
    private Caudal() {
        // static
    }

    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MultiRunPedestrianDynamicsConfig config = mapper.readValue(new File(args[0]),
                MultiRunPedestrianDynamicsConfig.class);

        final List<List<Integer>> runs = new ArrayList<>(config.runCount);
        double dt = 0;

        for (int run = 0; run < config.runCount; run++) {

            config.baseConfig.seed = config.baseConfig.seed + run;

            System.out.println("Particle Generation Seed: " + config.baseConfig.particleGeneration.seed);
            System.out.println("Simulation Seed: " + config.baseConfig.seed);

            final PedestrianDynamicsSimulation simulation = config.toSimulation();

            dt = simulation.getDt();

            final List<Integer> escapesAccum = new LinkedList<>();

            simulation.simulate(config.generateInitialState(), (i, locked, escaped, justEscaped) -> {
                
                escapesAccum.add(justEscaped.size());

                return true;
            });

            runs.add(escapesAccum);
        }

        mapper.writeValue(new File("output/caudal.json"), new Ej2BOutput(dt, runs));
    }

    @Data
    public static class Ej2BOutput {
        public final double dt;
        public final List<List<Integer>> escapesByRun;
    }
}
