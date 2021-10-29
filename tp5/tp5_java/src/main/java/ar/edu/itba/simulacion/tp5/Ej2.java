package ar.edu.itba.simulacion.tp5;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp5.SimulationSettings.MultiRunPedestrianDynamicsConfig;
import lombok.Data;

public final class Ej2 {
    private Ej2() {
        // static
    }

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MultiRunPedestrianDynamicsConfig config = mapper.readValue(new File(args[0]), MultiRunPedestrianDynamicsConfig.class);

        final List<List<Double>> escapeTimes = new ArrayList<>(config.baseConfig.particleGeneration.particleCount);

        for (int j = 0; j < config.baseConfig.particleGeneration.particleCount; j++) {
            escapeTimes.add(new ArrayList<>(config.runCount));
        }

        for(int run = 0; run < config.runCount; run++) {

            config.baseConfig.seed = config.baseConfig.seed + run;

            System.out.println("Particle Generation Seed: " + config.baseConfig.particleGeneration.seed);
            System.out.println("Simulation Seed: " + config.baseConfig.seed);

            final PedestrianDynamicsSimulation simulation = config.toSimulation();
            final double dt = simulation.getDt();
            
            final int[] particlesEscaped = {0};

            simulation.simulate(config.generateInitialState(), (i, locked, escaped, justEscaped) -> {
                final int escapeCount = justEscaped.size();
                if(escapeCount > 0) {
                    for (int index = 0; index < escapeCount; index++) {
                        escapeTimes.get(particlesEscaped[0]).add(dt * i);
                        particlesEscaped[0]++;
                    }
                }
                return true;
            });
        }

        mapper.writeValue(new File("output/ej2.json"), new Ej2Output(escapeTimes));
    }

    @Data
    public static class Ej2Output {
        public final List<List<Double>> timesByParticle;
    }
}
