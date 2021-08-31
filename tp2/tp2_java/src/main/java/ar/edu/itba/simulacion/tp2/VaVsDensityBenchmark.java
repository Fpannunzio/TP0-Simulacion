package ar.edu.itba.simulacion.tp2;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.tp2.endCondition.OffLatticeEndCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.math3.stat.StatUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class VaVsDensityBenchmark {

    private VaVsDensityBenchmark() {
        // static class
    }

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final VaVsDensityBenchmarkConfig config = mapper.readValue(new File(args[0]), VaVsDensityBenchmarkConfig.class);

        final Random randomGen = new Random();
        if(config.seed != null) {
            randomGen.setSeed(config.seed);
        }

        mapper.writeValue(new File(config.outputFile), doIteration(config, randomGen));
    }

    public static VaVsDensityBenchmarkResult doIteration(final VaVsDensityBenchmarkConfig config, final Random randomGen) {
        final int totalSteps   = (((config.finalParticleCount - config.initParticleCount) / config.particleCountStep)) + 1;

        final double[] density  = new double[totalSteps];
        final double[] vaMean   = new double[totalSteps];
        final double[] vaStd    = new double[totalSteps];

        final List<Particle2D> particles = new ArrayList<>(config.finalParticleCount + 1);

        int particleCount = config.initParticleCount;
        for(int i = 0; i < totalSteps; i++, particleCount += config.particleCountStep) {
            ParticleGeneration.generateAdditionalParticles(
                particles, particleCount, config.spaceWidth, config.periodicBorder,
                config.velocity, config.velocity, 0, 0
            );

            final List<List<Particle2D>> automataStates = new OffLatticeAutomata(
                config.spaceWidth, config.actionRadius, config.noise, config.periodicBorder, 0, randomGen
            ).run(particles, config.endCondition, null);

            final double[] vaList = automataStates
                .subList(config.endCondition.validRangeStart(), automataStates.size())
                .stream()
                .mapToDouble(OffLatticeAutomata::calculateStableNormalizedVelocity)
                .toArray()
                ;

            density[i]  = (double) particleCount / config.spaceWidth;
            vaMean[i]   = StatUtils.geometricMean(vaList);
            vaStd[i]    = StatUtils.variance(vaList, vaMean[i]);

            config.endCondition.reset();

            System.out.printf(Locale.ROOT, "%d of %d - Density: %f; Mean: %f; Std: %s%n", i, totalSteps, density[i], vaMean[i], vaStd[i]);
        }

        return VaVsDensityBenchmarkResult.builder()
            .withDensity(density)
            .withVaMean(vaMean)
            .withVaStd(vaStd)
            .build()
            ;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class VaVsDensityBenchmarkResult {
        public double[]     density;
        public double[]     vaMean;
        public double[]     vaStd;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class VaVsDensityBenchmarkConfig {
        public double                   spaceWidth;
        public double                   actionRadius;
        public double                   noise;
        public boolean                  periodicBorder;
        public double                   velocity;
        public Long                     seed;
        public OffLatticeEndCondition   endCondition;
        public int                      initParticleCount;
        public int                      finalParticleCount;
        public int                      particleCountStep;
        public String                   outputFile;
    }
}
