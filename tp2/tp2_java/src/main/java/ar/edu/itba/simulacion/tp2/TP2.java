package ar.edu.itba.simulacion.tp2;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public final class TP2 {

    // Temporal, para probar graficar
    private static final int STEPS = 100;

    private TP2() {
        // static class
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();
        
        final OffLatticeConfig config = mapper.readValue(new File(args[0]), OffLatticeConfig.class);

        final List<Particle2D> particles = List.of(mapper.readValue(new File(config.particlesFile), Particle2D[].class));
        if(particles.isEmpty()) {
            throw new IllegalArgumentException("No particles found");
        }

        final double maxRadius = particles.stream().mapToDouble(Particle2D::getRadius).max().orElseThrow();

        final OffLattice automata = new OffLattice(config.spaceWidth, config.actionRadius, config.periodicBorder, maxRadius);

        final List<List<Particle2D>> automataStates = automata.doNSteps(particles, STEPS);

        mapper.writeValue(new File(config.outputFile), automataStates);
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class OffLatticeConfig {
        public double       spaceWidth;
        public double       actionRadius;
        public boolean      periodicBorder;
        public String       particlesFile;
        public String       outputFile;
    }
}