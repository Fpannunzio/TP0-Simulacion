package ar.edu.itba.simulacion.tp2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TP2 {
    
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();
        
        final OffLaticeConfig config = mapper.readValue(new File(args[0]), OffLaticeConfig.class);

        List<Particle2D> particles = Arrays
            .asList(mapper.readValue(new File(config.particlesFile), Particle2D[].class));

           
      

    }

    public static class OffLaticeConfig {
        public int          time;
        public int          M;
        public double       L;
        public double       actionRadius;
        public String       particlesFile;
        public String       outputFile;

        private OffLaticeConfig() {
            //Deserialization
        }

        public OffLaticeConfig(int time, int M, double L, double actionRadius, String particlesFile, String outputFile) {
            this.time = time;
            this.M = M;
            this.L = L;
            this.actionRadius = actionRadius;
            this.particlesFile = particlesFile;
            this.outputFile = outputFile;
        }      
    }
}