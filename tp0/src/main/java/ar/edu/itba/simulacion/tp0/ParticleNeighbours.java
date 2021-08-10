package ar.edu.itba.simulacion.tp0;

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
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class ParticleNeighbours {

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }

        final ObjectMapper mapper = new ObjectMapper();

        final ParticleNeighboursConfig config = mapper.readValue(new File(args[0]), ParticleNeighboursConfig.class);
        if(config.strategy == null) {
            throw new IllegalArgumentException("Strategy must be provided");
        }

        final List<Particle> particles = Arrays.asList(mapper.readValue(new File(config.particlesFile), Particle[].class));

        final long start = System.nanoTime();

        final Map<Integer, ? extends Collection<Particle>> ret = config.strategy.strategy.apply(config, particles);

        final long end = System.nanoTime();

        // Queremos hacer otra cosa con esto??
        System.out.println(end - start);

        mapper.writeValue(new File(config.outputFile), ret);
    }

    private static Map<Integer, Set<Particle>> CIM(final ParticleNeighboursConfig config, final List<Particle> particles) {
        return config.toCim().calculateNeighbours(particles);
    }

    private static Map<Integer, List<Particle>> bruteForce(final ParticleNeighboursConfig config, final List<Particle> particles) {
        final Map<Integer, List<Particle>> ret = new HashMap<>();
        for(final Particle particle : particles) {
            ret.put(particle.getId(), new ArrayList<>(particles.size()));
        }

        particles.sort(Comparator.comparing(Particle::getId));

        int i = 0;
        for(final Particle particle : particles) {
            final ListIterator<Particle> possibleNeighbours = particles.listIterator(i);
            while(possibleNeighbours.hasNext()) {
                final Particle possibleNeighbour = possibleNeighbours.next();

                if(particle.distanceTo(possibleNeighbour) < config.actionRadius) {
                    ret.get(particle.getId()).add(possibleNeighbour);
                    ret.get(possibleNeighbour.getId()).add(particle);
                }
            }
        }

        return ret;
    }

    private enum Strategy {
        CIM         (ParticleNeighbours::CIM),
        BRUTE_FORCE (ParticleNeighbours::bruteForce),
        ;

        private final BiFunction<ParticleNeighboursConfig, List<Particle>, Map<Integer, ? extends Collection<Particle>>> strategy;

        Strategy(final BiFunction<ParticleNeighboursConfig, List<Particle>, Map<Integer, ? extends Collection<Particle>>> strategy) {
            this.strategy = strategy;
        }
    }

    private static class ParticleNeighboursConfig {
        public Strategy     strategy;
        public int          M;
        public double       L;
        public double       actionRadius;
        public boolean      periodicOutline;
        public String       particlesFile;
        public String       outputFile;

        public CellIndexMethod toCim() {
            return new CellIndexMethod(M, L, actionRadius, periodicOutline);
        }
    }
}