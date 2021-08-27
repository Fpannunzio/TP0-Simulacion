package ar.edu.itba.simulacion.tp2;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VaVsStepBenchmark {

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final OffLatticeSimulation.OffLatticeConfig config = mapper.readValue(new File(args[0]), OffLatticeSimulation.OffLatticeConfig.class);

        final List<Particle2D> particles = List.of(mapper.readValue(new File(config.particlesFile), Particle2D[].class));
        if(particles.isEmpty()) {
            throw new IllegalArgumentException("No particles found");
        }

        final double maxRadius = particles.stream().mapToDouble(Particle2D::getRadius).max().orElseThrow();
        final Random randomGen = new Random();
        if(config.seed != null) {
            randomGen.setSeed(config.seed);
        }

        final OffLatticeAutomata automata = new OffLatticeAutomata(
            config.spaceWidth, config.actionRadius, config.noise, config.periodicBorder, maxRadius, randomGen
        );

        final List<List<Particle2D>> automataStates = automata.run(particles, config.endCondition, step -> {
            if(step % 1000 == 0) {
                // Informamos que la simulacion avanza
                System.out.println("Total states processed so far: " + step);
            }
        });

        mapper.writeValue(new File(config.outputFile), automataStates
            .stream()
            .map(OffLatticeAutomata::calculateStableNormalizedVelocity)
            .collect(Collectors.toList())
        );

        if(automataStates.size() == OffLatticeAutomata.MAX_ITERATIONS) {
            throw new RuntimeException(
                "El automata corto por haber arribado a la cantidad maxima de iteraciones ("
                    + OffLatticeAutomata.MAX_ITERATIONS +
                    "). Revisar end condition."
            );
        }
    }
}
