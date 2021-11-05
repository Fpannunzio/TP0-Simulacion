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
        double dt = 0;
        for (int r = 0; r < totalConfig; r++) {
            
            final double exitLength = config.configurations.get(r).getDoorDistance();
            final int particleCount = config.configurations.get(r).getParticleCount();

            config.baseConfig.exitLength = exitLength;
            config.baseConfig.particleGeneration.particleCount = particleCount;
            
            final List<List<Double>> escapesByRun = new ArrayList<>(config.runCount);
            

            for (int j = 0; j < config.baseConfig.particleGeneration.particleCount; j++) {
                escapesByRun.add(new ArrayList<>(config.runCount));
            }

            for (int run = 0; run < config.runCount; run++) {

                config.baseConfig.seed = config.baseConfig.seed + run;

                System.out.println("Particle Generation Seed: " + config.baseConfig.particleGeneration.seed);
                System.out.println("Simulation Seed: " + config.baseConfig.seed);

                final PedestrianDynamicsSimulation simulation = config.toSimulation();
                dt = simulation.getDt();
                final double auxDt = dt;
                final int[] particlesEscaped = {0};

                simulation.simulate(config.generateInitialState(), (i, locked, escaped, justEscaped) -> {
                    
                    final int escapeCount = justEscaped.size();
                    if(escapeCount > 0) {
                    for (int index = 0; index < escapeCount; index++) {
                        escapesByRun.get(particlesEscaped[0]).add(auxDt * i);
                        particlesEscaped[0]++;
                    }
                }
                return true;
                });
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

        public final List<List<Double>> escapesByRun;
    }

    
}
