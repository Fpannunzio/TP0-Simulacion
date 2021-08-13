package ar.edu.itba.simulacion.tp0;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp0.ParticleGeneration.ParticleGenerationConfig;
import ar.edu.itba.simulacion.tp0.ParticleNeighbours.ParticleNeighboursConfig;
import ar.edu.itba.simulacion.tp0.ParticleNeighbours.Strategy;

public class Simulation {
    public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
        long start;
        long end;
        int counter =0;

        final ObjectMapper mapper = new ObjectMapper();
        final ParticleNeighboursConfig config = mapper.readValue(new File(args[0]), ParticleNeighboursConfig.class);

        final ParticleGenerationConfig particleConfig = new ParticleGenerationConfig(100, 100, true, 1, 2.5, "particles/gen3.json");
        
        if (config.strategy == null) {
            throw new IllegalArgumentException("Strategy must be provided");
        }

        List<Particle> particles = Arrays
                .asList(mapper.readValue(new File(config.particlesFile), Particle[].class));

        final List<BenchmarkTimes> benchmarks = new LinkedList<>();


        config.strategy = Strategy.BRUTE_FORCE;
        benchmarks.add(new BenchmarkTimes(config, 501));
        for (int i = 0; i < 500; i++) {
            start = System.nanoTime();
            ParticleNeighbours.bruteForce(config, particles);
            end = System.nanoTime();
            benchmarks.get(counter).timeList.add(end - start);
        }
        counter++;

        config.strategy = Strategy.CIM;
        for (int i = 0; i < 10; i++) {
            config.M = i + 1;
            benchmarks.add(new BenchmarkTimes(config, 501));
            for (int j = 0; j < 500; j++) {
                start = System.nanoTime();
                ParticleNeighbours.CIM(config, particles);
                end = System.nanoTime();
                benchmarks.get(counter).timeList.add(end - start);
            }
            counter++;
        }
        
        for (int i = 0; i < 10; i++) {
            config.strategy = Strategy.CIM;
            particleConfig.particleCount = (i + 1) * 100;
            ParticleGeneration.particleGenerator(particleConfig);
            particles = Arrays
                .asList(mapper.readValue(new File(config.particlesFile), Particle[].class));
            benchmarks.add(new BenchmarkTimes(config, (i + 1) * 100));
            for (int j = 0; j < 500; j++) {
                start = System.nanoTime();
                ParticleNeighbours.CIM(config, particles);
                end = System.nanoTime();
                benchmarks.get(counter).timeList.add(end - start);
            }
            counter++;
            config.strategy = Strategy.BRUTE_FORCE;
            benchmarks.add(new BenchmarkTimes(config, (i + 1) * 100));
            for (int j = 0; j < 500; j++) {
                start = System.nanoTime();
                ParticleNeighbours.bruteForce(config, particles);
                end = System.nanoTime();
                benchmarks.get(counter).timeList.add(end - start);
            }
            counter++;
        }

        mapper.writeValue(new File("test/test.json"), benchmarks);
    }

    public static class BenchmarkTimes {
        public ParticleNeighboursConfig config;
        public int particles;
        public List<Long> timeList;

        BenchmarkTimes(ParticleNeighboursConfig config, int particles) {
            this.config = config.copy();
            this.particles = particles;
            this.timeList = new LinkedList<>();
        }
    }
}
