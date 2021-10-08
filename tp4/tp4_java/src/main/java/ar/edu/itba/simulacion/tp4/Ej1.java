package ar.edu.itba.simulacion.tp4;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Ej1 {
    
    public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
        
        final ObjectMapper mapper = new ObjectMapper();
        
        final int count = 10_000;

        final double[][][] results = new double[3][count][];
        
        double A                        = 1;
        double k                        = 10_000;
        double gamma                    = 100;
        double m                        = 70;

        double tf                       = 5;
        double dt                       = tf / count;

        int degree                      = 6;
        double[] functionCoeficients    = new double[]{-k, -gamma}; //r0, r1
        double[] initialValues          = new double[]{1, -(A * gamma)/(2*m)}; //r0, r1

        VerletSolver vSolver    = new VerletSolver(m, dt, functionCoeficients, initialValues);
        BeemanSolver bSolver    = new BeemanSolver(m, dt, functionCoeficients, initialValues);
        GearSolver gSolver      = new GearSolver(m, dt, functionCoeficients, initialValues, degree);
        


        for (int i = 0; i < count; i++) {
            if(i % (count/10) == 0) {
                System.out.println("1_000");
            }

            results[0][i] = vSolver.nextStep();
            results[1][i] = bSolver.nextStep();
            results[2][i] = gSolver.nextStep();
        }

        mapper.writeValue(new File("output/ej1.json"), results);
    }
}
