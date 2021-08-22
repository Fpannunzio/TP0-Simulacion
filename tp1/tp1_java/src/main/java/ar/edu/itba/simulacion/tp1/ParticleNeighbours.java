package ar.edu.itba.simulacion.tp1;

import ar.edu.itba.simulacion.particle.neighbours.BruteForceMethod;
import ar.edu.itba.simulacion.particle.neighbours.CellIndexMethod;
import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class ParticleNeighbours {

    private ParticleNeighbours() {
        // static class
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final ParticleNeighboursConfig config = mapper.readValue(new File(args[0]), ParticleNeighboursConfig.class);
        if(config.strategy == null) {
            throw new IllegalArgumentException("Strategy must be provided");
        }

        final List<Particle2D> particles = Arrays.asList(mapper.readValue(new File(config.particlesFile), Particle2D[].class));

        final long start = System.nanoTime();

        final Map<Integer, ? extends Collection<Particle2D>> neighbours = config.strategy.apply(particles, config);
        final Map<Integer, List<Integer>> ret = neighbours.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
                .stream()
                .map(Particle2D::getId)
                .collect(Collectors.toList()))
            );

        final long end = System.nanoTime();
        final long executionNanos = end - start;

        // Queremos hacer otra cosa con esto??
        System.out.printf(Locale.ROOT, "Tiempo de ejecución: %d sec, %d millis\n",
            TimeUnit.NANOSECONDS.toSeconds(executionNanos),
            TimeUnit.NANOSECONDS.toMillis(executionNanos) - TimeUnit.MINUTES.toMillis(TimeUnit.NANOSECONDS.toSeconds(executionNanos))
        );

        mapper.writeValue(new File(config.outputFile), ret);
    }

    public enum Strategy {
        CIM         ((particles, config) -> config.toCim().calculateNeighbours(particles)),
        BRUTE_FORCE ((particles, config) -> BruteForceMethod.calculateNeighbours(particles, config.l, config.actionRadius, config.periodicOutline)),
        ;

        private final BiFunction<List<Particle2D>,ParticleNeighboursConfig, Map<Integer, ? extends Collection<Particle2D>>> strategy;

        Strategy(final BiFunction<List<Particle2D>,ParticleNeighboursConfig, Map<Integer, ? extends Collection<Particle2D>>> strategy) {
            this.strategy = strategy;
        }

        public Map<Integer, ? extends Collection<Particle2D>> apply(final List<Particle2D> particles, final ParticleNeighboursConfig config) {
            return strategy.apply(particles, config);
        }
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class ParticleNeighboursConfig {
        public Strategy     strategy;
        public int          m;
        public double       l;
        public double       actionRadius;
        public boolean      periodicOutline;
        public String       particlesFile;
        public String       outputFile;

        public CellIndexMethod toCim() {
            return new CellIndexMethod(m, l, actionRadius, periodicOutline);
        }

        public ParticleNeighboursConfig copy() {
            return new ParticleNeighboursConfig(strategy, m, l, actionRadius, periodicOutline, particlesFile, outputFile);
        }
    }
}