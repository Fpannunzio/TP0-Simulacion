package ar.edu.itba.simulacion.tp3;

import java.io.File;
import java.io.IOException;
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

        final Ej4Summary summary = new Ej4Summary(new LinkedList<>(), new LinkedList<>());

        config.particleGenerationConfig.get(1).particleCount = config.particleCount;

        final List<Particle2D> initialState = ParticleGeneration
                .particleGenerator(config.getParticleGenerationConfig());
        
        final Map<Integer, List<Particle2D>> particles = new HashMap<>(initialState.size());
      

        if (initialState.isEmpty()) {
            throw new IllegalArgumentException("No small particles for initial state found");
        }

        for (Particle2D particle: initialState) {
            List<Particle2D> states = new LinkedList<>();
            particles.put(particle.getId(), states);
            summary.getParticleEvents().add(states);
        }

        final BrownianParticleSystem brownianSystem = new BrownianParticleSystem(config.spaceWidth, initialState);

        brownianSystem.calculateUntilBigParticleCollision(config.maxIterations, (state, i) -> {
            summary.getTimes().add(state.getTime());

            state.getParticles().stream().filter(particle -> particles.containsKey(particle.getId())).forEach(particle -> particles.get(particle.getId()).add(particle));

            if (state.getCollision().isWallCollision()) {
                particles.remove(state.getCollision().getParticle1());
            }
        });

        mapper.writeValue(new File(config.outputFile), summary);

    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej4Config {
        public List<ParticleGenerationConfig> particleGenerationConfig;
        public int particleCount;
        public double spaceWidth;
        public int maxIterations;
        public String particlesFile;
        public String outputFile;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class Ej4Summary {
        public List<List<Particle2D>> particleEvents;
        public List<Double> times;
    }
}
