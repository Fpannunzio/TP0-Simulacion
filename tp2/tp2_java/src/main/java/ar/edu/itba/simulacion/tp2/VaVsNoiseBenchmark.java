package ar.edu.itba.simulacion.tp2;

import ar.edu.itba.simulacion.particle.Particle2D;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.math3.stat.StatUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class VaVsNoiseBenchmark {

    private VaVsNoiseBenchmark() {
        // static class
    }

    private static final double INITIAL_NOISE       = 0;
    private static final double NOISE_LIMIT         = 2 * Math.PI;
    private static final double NOISE_STEP_DEFAULT  = 0.1;

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final OffLatticeSimulation.OffLatticeConfig config = mapper.readValue(new File(args[0]), OffLatticeSimulation.OffLatticeConfig.class);

        final List<Particle2D> initialState = List.of(mapper.readValue(new File(config.particlesFile), Particle2D[].class));
        if(initialState.isEmpty()) {
            throw new IllegalArgumentException("No particles found");
        }

        final double maxRadius = initialState.stream().mapToDouble(Particle2D::getRadius).max().orElseThrow();

        // Usamos noise de config como step
        final double noiseStep      = config.noise > 0 ? config.noise : NOISE_STEP_DEFAULT;
        final int totalNoiseSteps   = ((int) (NOISE_LIMIT / noiseStep)) + 1;

        final double[] vaMean   = new double[totalNoiseSteps];
        final double[] vaStd    = new double[totalNoiseSteps];

        double noise = INITIAL_NOISE;
        for(int i = 0; i < totalNoiseSteps; i++, noise += noiseStep) {
            final List<List<Particle2D>> automataStates = new OffLatticeAutomata(
                config.spaceWidth, config.actionRadius, noise, config.periodicBorder, maxRadius
            ).run(initialState, config.endCondition, null);

            final double[] vaList = automataStates
                    .subList(config.endCondition.validRangeStart(), automataStates.size())
                    .stream()
                    .mapToDouble(OffLatticeAutomata::calculateStableNormalizedVelocity)
                    .toArray()
                    ;

            vaMean[i]   = StatUtils.geometricMean(vaList);
            vaStd[i]    = StatUtils.populationVariance(vaList, vaMean[i]);

            config.endCondition.reset();

            System.out.println(i + " of " + totalNoiseSteps + " - Mean: " + vaMean[i] + " Std: " + vaStd[i]);
        }

        mapper.writeValue(new File(config.outputFile), VaVsNoiseBenchmarkResult.builder()
            .withNoiseStep(noiseStep)
            .withVaMean(vaMean)
            .withVaStd(vaStd)
            .build()
        );
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class VaVsNoiseBenchmarkResult {
        public double       noiseStep;
        public double[]     vaMean;
        public double[]     vaStd;
    }
}
