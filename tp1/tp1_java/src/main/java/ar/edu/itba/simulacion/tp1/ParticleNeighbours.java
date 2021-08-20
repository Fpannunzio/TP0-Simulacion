package ar.edu.itba.simulacion.tp1;

import ar.edu.itba.simulacion.particle.CellIndexMethod;
import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

        final Map<Integer, ? extends Collection<Particle2D>> neighbours = config.strategy.apply(config, particles);
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

    public static Map<Integer, Set<Particle2D>> CIM(final ParticleNeighboursConfig config, final List<Particle2D> particles) {
        return config.toCim().calculateNeighbours(particles);
    }

    public static Map<Integer, List<Particle2D>> bruteForce(final ParticleNeighboursConfig config, final List<Particle2D> particles) {
        final Map<Integer, List<Particle2D>> ret = new HashMap<>();
        for(final Particle2D particle : particles) {
            ret.put(particle.getId(), new ArrayList<>(particles.size()));
        }

        particles.sort(Comparator.comparing(Particle2D::getId));

        final int particleCount = particles.size();

        int i = 0;
        for(final Particle2D particle : particles) {
            if(i + 1 < particleCount) {
                final ListIterator<Particle2D> possibleNeighbours = particles.listIterator(i + 1);
                while (possibleNeighbours.hasNext()) {
                    final Particle2D possibleNeighbour = possibleNeighbours.next();

                    if(particle.distanceTo(possibleNeighbour, config.L, config.periodicOutline) < config.actionRadius) {
                        ret.get(particle.getId()).add(possibleNeighbour);
                        ret.get(possibleNeighbour.getId()).add(particle);
                    }
                }
            }
            i++;
        }

        return ret;
    }

    public enum Strategy {
        CIM         (ParticleNeighbours::CIM),
        BRUTE_FORCE (ParticleNeighbours::bruteForce),
        ;

        private final BiFunction<ParticleNeighboursConfig, List<Particle2D>, Map<Integer, ? extends Collection<Particle2D>>> strategy;

        Strategy(final BiFunction<ParticleNeighboursConfig, List<Particle2D>, Map<Integer, ? extends Collection<Particle2D>>> strategy) {
            this.strategy = strategy;
        }

        public Map<Integer, ? extends Collection<Particle2D>> apply(final ParticleNeighboursConfig config, final List<Particle2D> particles) {
            return strategy.apply(config, particles);
        }
    }

    public static class ParticleNeighboursConfig {
        public Strategy     strategy;
        public int          M;
        public double       L;
        public double       actionRadius;
        public boolean      periodicOutline;
        public String       particlesFile;
        public String       outputFile;

        private ParticleNeighboursConfig() {
            //Deserialization
        }

        public ParticleNeighboursConfig(Strategy strategy, int M, double L, double actionRadius, boolean periodicOutline, String particlesFile, String outputFile) {
            this.strategy = strategy;
            this.M = M;
            this.L = L;
            this.actionRadius = actionRadius;
            this.periodicOutline = periodicOutline;
            this.particlesFile = particlesFile;
            this.outputFile = outputFile;
        }      

        public CellIndexMethod toCim() {
            return new CellIndexMethod(M, L, actionRadius, periodicOutline);
        }

        public ParticleNeighboursConfig copy() {
            return new ParticleNeighboursConfig(strategy, M, L, actionRadius, periodicOutline, particlesFile, outputFile);
        }
    }
}