package ar.edu.itba.simulacion.tp3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.particle.ParticleGeneration.ParticleGenerationConfig;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class Ej4 {
    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        
        final ObjectMapper mapper = new ObjectMapper();

        final Ej4Config config = mapper.readValue(new File(args[0]), Ej4Config.class);

        final Ej4Summary summary = new Ej4Summary(new ArrayList<>(config.getRounds()), new ArrayList<>(config.getRounds()));

        config.particleGenerationConfig.get(1).particleCount = config.particleCount;

        for (int r = 0; r < config.getRounds(); r++) {

            final List<Particle2D> initialState = ParticleGeneration.particleGenerator(config.getParticleGenerationConfig());
    
            final Map<Integer, List<Double[]>> remainingParticles = new HashMap<>(initialState.size());

            if (initialState.isEmpty()) {
                throw new IllegalArgumentException("No small particles for initial state found");
            }

            final List<List<Double[]>> roundParticles = new ArrayList<>(initialState.size());
            summary.getPositions().add(roundParticles);
            
            final List<Double> roundTimes = new LinkedList<>();
            summary.getTimes().add(roundTimes);

            for (Particle2D particle: initialState) {
                List<Double[]> states = new LinkedList<>();
                remainingParticles.put(particle.getId(), states);
                roundParticles.add(states);
            }
            
            roundTimes.add(0.0);

            final BrownianParticleSystem brownianSystem = new BrownianParticleSystem(config.spaceWidth, initialState);

            brownianSystem.calculateUntilBigParticleCollision(config.maxIterations, (state, i) -> {
                roundTimes.add(state.getTime());

                state.getParticles()
                    .stream()
                    .filter(particle -> remainingParticles.containsKey(particle.getId()))
                    .filter(particle -> particle.getId() == 0)
                    .forEach(particle -> remainingParticles.get(particle.getId()).add(new Double[]{particle.getX(), particle.getY()}))
                    ;

                if (state.getCollision().isWallCollision()) {
                    remainingParticles.remove(state.getCollision().getParticle1());
                }
            });
        }
        

        mapper.writeValue(new File(config.outputFile), summary);

    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej4Config {
        public int rounds;
        public double spaceWidth;
        public int particleCount;
        public int maxIterations;
        public String outputFile;
        public List<ParticleGenerationConfig> particleGenerationConfig;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej4Summary {
        public List<List<List<Double[]>>> positions;
        public List<List<Double>> times;
    }
}
