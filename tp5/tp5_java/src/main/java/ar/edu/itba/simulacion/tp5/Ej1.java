package ar.edu.itba.simulacion.tp5;

import static ar.edu.itba.simulacion.tp5.SimulationSettings.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

public final class Ej1 {
    private Ej1() {
        // static
    }

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MultiRunPedestrianDynamicsConfig config = mapper.readValue(new File(args[0]), MultiRunPedestrianDynamicsConfig.class);

        System.out.println("Particle Generation Seed: " + config.baseConfig.particleGeneration.seed);
        System.out.println("Simulation Seed: " + config.baseConfig.seed);

        final PedestrianDynamicsSimulation simulation = config.toSimulation();

        final int maxIterations = config.baseConfig.iterations;
        final double dt = simulation.getDt();
        final List<List<EscapesByTime>> runs = new ArrayList<>(config.runCount);

        for(int run = 0; run < config.runCount; run++) {
            final List<EscapesByTime> escapesAccum = new LinkedList<>();
            final long[] totalEscapesPtr = {0};

            simulation.simulate(config.generateInitialState(), (i, locked, escaped, justEscaped) -> {
                final int escapeCount = justEscaped.size();
                if(escapeCount > 0) {
                    totalEscapesPtr[0] += escapeCount;
                    escapesAccum.add(new EscapesByTime(dt * i, totalEscapesPtr[0]));
                }

                return i < maxIterations;
            });

            runs.add(escapesAccum);
        }

        mapper.writeValue(new File("output/ej1.json"), new Ej1Output(dt, runs));
    }

    @Data
    public static class Ej1Output {
        public final double dt;
        public final List<List<EscapesByTime>> escapesByRun;
    }
}
