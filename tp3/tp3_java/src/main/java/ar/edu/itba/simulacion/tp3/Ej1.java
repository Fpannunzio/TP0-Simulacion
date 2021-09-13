package ar.edu.itba.simulacion.tp3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.tp3.BrownianParticleSystem.Collision;
import ar.edu.itba.simulacion.tp3.BrownianParticleSystem.SimulationState;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class Ej1 {
    
    public static void main( String[] args ) throws IOException {
        
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final Ej1Config config = mapper.readValue(new File(args[0]), Ej1Config.class);
        
        final List<RoundSummary> summary = new ArrayList<>(config.getParticleCounts().length);
        
        for(final int particleCount : config.getParticleCounts()) {
            
            System.out.printf("Working with %d particles.\n", particleCount);

            config.particleGen.get(1).particleCount = particleCount;

            final RoundSummary round = new RoundSummary(new ArrayList<>(config.getRounds()), particleCount);

            for (int i = 0; i < config.getRounds(); i++) {
                
                final List<Particle2D> initialState =  ParticleGeneration.particleGenerator(config.getParticleGen());
                final BrownianParticleSystem brownianSystem = new BrownianParticleSystem(config.spaceWidth, initialState);
                
                brownianSystem.calculateUntilBigParticleCollision(config.maxIterations, null);

                round.collisionTimes.add(
                    brownianSystem.getStates()
                        .stream()
                        .map(SimulationState::getCollision)
                        .filter(Objects::nonNull)
                        .map(Collision::getDTime)
                        .collect(Collectors.toList())
                );
            }

            summary.add(round);
        }

        mapper.writeValue(new File(config.outputFile), summary);
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej1Config {
        public List<ParticleGeneration.ParticleGenerationConfig>    particleGen;
        public int[]                                                particleCounts;
        public int                                                  rounds;
        public int                                                  maxIterations;
        public double                                               spaceWidth;
        public String                                               outputFile;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class RoundSummary {
        public List<List<Double>>       collisionTimes;
        public int                      particleCount;
    }

}
