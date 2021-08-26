package ar.edu.itba.simulacion.tp2;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.tp2.endCondition.OffLatticeEndCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public final class OffLatticeSimulation {

    private OffLatticeSimulation() {
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

        final OffLatticeAutomata automata = new OffLatticeAutomata(config.spaceWidth, config.actionRadius, config.noise, config.periodicBorder, maxRadius);

        final List<List<Particle2D>> automataStates = automata.run(particles, config.endCondition, step -> {
            if(step % 1000 == 0) {
                // Informamos que la simulacion avanza
                System.out.println("Total states processed so far: " + step);
            }
        });

        mapper.writeValue(new File(config.outputFile), automataStates);

        if(automataStates.size() == OffLatticeAutomata.MAX_ITERATIONS) {
            throw new RuntimeException(
                "El automata corto por haber arribado a la cantidad maxima de iteraciones ("
                + OffLatticeAutomata.MAX_ITERATIONS +
                "). Revisar end condition."
            );
        }
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class OffLatticeConfig {
        public double                   spaceWidth;
        public double                   actionRadius;
        public double                   noise;
        public boolean                  periodicBorder;
        public OffLatticeEndCondition   endCondition;
        public String                   particlesFile;
        public String                   outputFile;
    }
}