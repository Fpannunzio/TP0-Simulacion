package ar.edu.itba.simulacion.tp0;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParticleGeneration {

    private static final int MAX_FAILURE_TOLERANCE = 10_000;

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final ParticleGenerationConfig config = mapper.readValue(Path.of(args[0]).toFile(), ParticleGenerationConfig.class);

        final List<Particle> ret = new ArrayList<>(config.particleCount);

        int tries = 0;
        int particleCount = 0;
        while(particleCount < config.particleCount && tries < MAX_FAILURE_TOLERANCE) {
            final Particle particle = Particle.randomParticle(particleCount, config.L, config.maxRadius);
            if(!particle.collides(ret)) {
                ret.add(particle);
                particleCount++;
            }
            tries++;
        }

        final List<Particle> particles = IntStream
            .range(0, config.particleCount)
            .mapToObj(i -> Particle.randomParticle(i, config.L, config.maxRadius))
            .collect(Collectors.toList())
            ;

        mapper.writeValue(Path.of(config.outputFile).toFile(), particles);
    }

    private static class ParticleGenerationConfig {
        public int          particleCount;
        public double       L;
        public double       maxRadius;
        public String       outputFile;
    }
}
