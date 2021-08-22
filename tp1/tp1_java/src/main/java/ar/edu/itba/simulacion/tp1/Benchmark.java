package ar.edu.itba.simulacion.tp1;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.ParticleGeneration;
import ar.edu.itba.simulacion.particle.ParticleGeneration.ParticleGenerationConfig;
import ar.edu.itba.simulacion.tp1.ParticleNeighbours.ParticleNeighboursConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import static ar.edu.itba.simulacion.tp1.ParticleNeighbours.*;

public class Benchmark {
    public static void main(String[] args) throws IOException {
        long start;
        long end;
        int counter =0;

        final ObjectMapper mapper = new ObjectMapper();
        final ParticleNeighboursConfig config = ParticleNeighboursConfig.builder()
            .withStrategy(Strategy.BRUTE_FORCE)
            .withM(10)
            .withL(100)
            .withActionRadius(5)
            .withPeriodicOutline(true)
            .withParticlesFile("particles/gen3.json")
            .withOutputFile("output/cim.json")
            .build()
            ;

        final ParticleGenerationConfig particleConfig = ParticleGenerationConfig.builder()
            .withParticleCount(505)
            .withSpaceWidth(100)
            .withPeriodicBorder(true)
            .withMinVelocity(0)
            .withMaxRadius(0)
            .withMinRadius(1)
            .withMaxRadius(2.5)
            .withOutputFile("particles/gen3.json")
            .build()
            ;

        ParticleGeneration.particleGenerator(particleConfig);

        List<Particle2D> particles = Arrays.asList(mapper.readValue(new File(config.particlesFile), Particle2D[].class));

        final List<BenchmarkTimes> benchmarks = new LinkedList<>();

        config.strategy = Strategy.BRUTE_FORCE;
        benchmarks.add(new BenchmarkTimes(config, 501));
        for (int i = 0; i < 500; i++) {
            start = System.nanoTime();
            Strategy.BRUTE_FORCE.apply(particles, config);
            end = System.nanoTime();
            benchmarks.get(counter).timeList.add(end - start);
        }
        counter++;

        config.strategy = Strategy.CIM;
        for (int i = 0; i < 10; i++) {
            config.m = i + 1;
            benchmarks.add(new BenchmarkTimes(config, 501));
            for (int j = 0; j < 500; j++) {
                start = System.nanoTime();
                Strategy.CIM.apply(particles, config);
                end = System.nanoTime();
                benchmarks.get(counter).timeList.add(end - start);
            }
            counter++;
        }

        for (int i = 0; i < 10; i++) {
            config.strategy = Strategy.CIM;
            particleConfig.particleCount = (i + 1) * 100;
            ParticleGeneration.particleGenerator(particleConfig);
            particles = Arrays.asList(mapper.readValue(new File(config.particlesFile), Particle2D[].class));
            benchmarks.add(new BenchmarkTimes(config, (i + 1) * 100));
            for (int j = 0; j < 500; j++) {
                start = System.nanoTime();
                Strategy.CIM.apply(particles, config);
                end = System.nanoTime();
                benchmarks.get(counter).timeList.add(end - start);
            }
            counter++;
            config.strategy = Strategy.BRUTE_FORCE;
            benchmarks.add(new BenchmarkTimes(config, (i + 1) * 100));
            for (int j = 0; j < 500; j++) {
                start = System.nanoTime();
                Strategy.BRUTE_FORCE.apply(particles, config);
                end = System.nanoTime();
                benchmarks.get(counter).timeList.add(end - start);
            }
            counter++;
        }

        mapper.writeValue(new File("test/test.json"), benchmarks);
    }

    @Data
    public static class BenchmarkTimes {
        public ParticleNeighboursConfig     config;
        public int                          particles;
        public List<Long>                   timeList;

        @Jacksonized
        @Builder(setterPrefix = "with")
        BenchmarkTimes(final ParticleNeighboursConfig config, final int particles) {
            this.config     = config.copy();
            this.particles  = particles;
            this.timeList   = new LinkedList<>();
        }
    }
}
