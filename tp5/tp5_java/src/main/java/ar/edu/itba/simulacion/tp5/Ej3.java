package ar.edu.itba.simulacion.tp5;

import static ar.edu.itba.simulacion.tp5.SimulationSettings.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

public final class Ej3 {
    private Ej3() {
        // static
    }

    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final MultiConfigRunPedestrianDynamicsConfig config = mapper.readValue(new File(args[0]),
            MultiConfigRunPedestrianDynamicsConfig.class);

        final int totalConfig = config.configurations.size();
        final List<Round> rounds = new ArrayList<>(totalConfig);

        for (int j = 0; j < totalConfig; j++) {
            
            final double exitLength = config.configurations.get(j).getDoorDistance();
            final int particleCount = config.configurations.get(j).getParticleCount();

            config.baseConfig.exitLength = exitLength;
            config.baseConfig.particleGeneration.particleCount = particleCount;
            
            final List<List<Integer>> escapesByRun = new ArrayList<>(config.runCount);
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

                escapesByRun.add(escapesAccum);
            }
            rounds.add(new Round(dt, new DistanceParticle(exitLength, particleCount), escapesByRun));
        }
        mapper.writeValue(new File("output/ej3.json"), new Ej3Output(rounds));
    }

    @Data
    public static class Ej3Output {
        public final List<Round> rounds;
    }

    @Data
    public static class Round {
        public final double dt;
        public final DistanceParticle distanceParticle;

        public final List<List<Integer>> escapesByRun;
    }

    
}
