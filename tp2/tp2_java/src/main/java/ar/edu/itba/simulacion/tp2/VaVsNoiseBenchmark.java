package ar.edu.itba.simulacion.tp2;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.tp2.VaVsNoiseBenchmark.VaVsNoiseBenchmarkSummary.VaVsNoiseBenchmarkSummaryBuilder;
import ar.edu.itba.simulacion.tp2.endCondition.OffLatticeEndCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.math3.stat.StatUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class VaVsNoiseBenchmark {

    private VaVsNoiseBenchmark() {
        // static class
    }

    private static final double INITIAL_NOISE       = 0;
    private static final double NOISE_LIMIT         = 2 * Math.PI;
    private static final double NOISE_STEP_DEFAULT  = 0.1;
    private static final int REP = 15;

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final VaVsNoiseBenchmarkConfig config = mapper.readValue(new File(args[0]), VaVsNoiseBenchmarkConfig.class);

        if(config.particleCounts.length <= 0) {
            throw new IllegalArgumentException("particleCounts cannot be empty");
        }

        final double noiseStep = config.noiseStep > 0 ? config.noiseStep : NOISE_STEP_DEFAULT;
        final double density = config.particleCounts[0] / config.spaceWidth;
        final Random randomGen = new Random();
        if(config.seed != null) {
            randomGen.setSeed(config.seed);
        }

        // Ordenamos el array por las dudas
        Arrays.sort(config.particleCounts);

//        final List<VaVsNoiseBenchmarkResult> variableDensityBenchmarks = new ArrayList<>(config.particleCounts.length);
        final List<VaVsNoiseBenchmarkResult> constantDensityBenchmarks = new ArrayList<>(config.particleCounts.length);

        final VaVsNoiseBenchmarkSummaryBuilder summary = VaVsNoiseBenchmarkSummary.builder()
            .withNoiseStep                (noiseStep)
//            .withVariableDensityBenchmarks(variableDensityBenchmarks)
            .withConstantDensityBenchmarks(constantDensityBenchmarks)
            ;

        for(final int particleCount : config.particleCounts) {
            final double spaceWidth = particleCount / density;

            // Variamos la cantidad de particulas

            // Al parecer no hay que hacerlo :(
//            variableDensityBenchmarks.add(calculateBenchmark(
//                particles, config.spaceWidth, noiseStep, config.actionRadius, config.periodicBorder, config.endCondition, randomGen
//            ));

            config.endCondition.reset();

            // Calculamos el tamanio del espacio segun la cantidad de puntos y la densidad deseada
            constantDensityBenchmarks.add(calculateBenchmark(
                spaceWidth, noiseStep, config.actionRadius, config.velocity, particleCount,  config.periodicBorder, config.endCondition, randomGen
            ));

            config.endCondition.reset();

            System.out.println("Calculated benchmarks for " + particleCount + " particles");
        }

        mapper.writeValue(new File(config.outputFile), summary.build());
    }

    private static VaVsNoiseBenchmarkResult calculateBenchmark(
        final double spaceWidth, final double noiseStep, final double actionRadius, final double velocity, final int particleCount,
        final boolean periodicBorder, final OffLatticeEndCondition endCondition, final Random randomGen
    ) {
        final int totalNoiseSteps   = ((int) (NOISE_LIMIT / noiseStep)) + 1;

        final double[] vaMean   = new double[totalNoiseSteps];
        final double[] vaStd    = new double[totalNoiseSteps];

        double noise = INITIAL_NOISE;
        for(int i = 0; i < totalNoiseSteps; i++, noise += noiseStep) {
            

            final double[] tmpMean   = new double[REP];
            final double[] tmpStd    = new double[REP];

            for(int k = 0; k < REP; k++) {

                final List<Particle2D> particles = new ArrayList<>(particleCount);
                ParticleGeneration.generateAdditionalParticles(
                    particles,  particleCount,
                    spaceWidth, periodicBorder,
                    0,     spaceWidth,
                    0,     spaceWidth,
                    velocity,   velocity,
                    0,  0,
                    0, 0
                );
    
                final List<List<Particle2D>> automataStates = new OffLatticeAutomata(
                    spaceWidth, actionRadius, noise, periodicBorder, 0, randomGen
                ).run(particles, endCondition, null);
    
                final double[] vaList = automataStates
                    .subList(endCondition.validRangeStart(), automataStates.size())
                    .stream()
                    .mapToDouble(OffLatticeAutomata::calculateStableNormalizedVelocity)
                    .toArray()
                    ;

                tmpMean[k]  = StatUtils.geometricMean(vaList);
                tmpStd[k]   = StatUtils.variance(vaList, tmpMean[k]);
                
                endCondition.reset();
            }

            vaMean[i]   = StatUtils.geometricMean(tmpMean);
            vaStd[i]    = StatUtils.geometricMean(tmpStd);


            System.out.println(i + " of " + totalNoiseSteps + " - Mean: " + vaMean[i] + " Std: " + vaStd[i]);
        }

        return VaVsNoiseBenchmarkResult.builder()
            .withSpaceWidth(spaceWidth)
            .withParticleCount(particleCount)
            .withVaMean(vaMean)
            .withVaStd(vaStd)
            .build()
            ;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class VaVsNoiseBenchmarkSummary {
        public double                           noiseStep;
//        public List<VaVsNoiseBenchmarkResult>   variableDensityBenchmarks;
        public List<VaVsNoiseBenchmarkResult>   constantDensityBenchmarks;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class VaVsNoiseBenchmarkResult {
        public double       spaceWidth;
        public double       particleCount;
        public double[]     vaMean;
        public double[]     vaStd;
    }

    @Data
    @Jacksonized
    @Builder(setterPrefix = "with")
    public static class VaVsNoiseBenchmarkConfig {
        public double                   spaceWidth;
        public double                   actionRadius;
        public double                   noiseStep;
        public boolean                  periodicBorder;
        public double                   velocity;
        public Long                     seed;
        public OffLatticeEndCondition   endCondition;
        public int[]                    particleCounts;
        public String                   outputFile;
    }
}
