package ar.edu.itba.simulacion.tp4.oscillator;

import static ar.edu.itba.simulacion.tp4.oscillator.SimulationSettings.*;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;

public final class Ej1sub3 {
    private Ej1sub3() {
        // static
    }

    public static final int     simCount    = 100;
    public static final int     simStep     = 1_000;
    public static final double  tf          = 5;
    
    public static void main(String[] args) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        final double[][] errores = new double[3][simCount];
        
        for(int sim = 1; sim <= simCount; sim++) {
            final int count = sim * simStep;
            final double dt = tf / count;

            final VerletSolver vSolver    = getVerletSolver(dt);
            final BeemanSolver bSolver    = getBeemanSolver(dt);
            final GearSolver   gSolver    = getGearSolver(dt);
            
            double t = 0;
            double vError = 0;
            double bError = 0;
            double gError = 0;

            for(int i = 0; i < count; i++) {
                final double analiticVal =  solveAnalytic(t);
                final double vValue = vSolver.oneAxisNextStep().r[0];
                final double bValue = bSolver.oneAxisNextStep().r[0];
                final double gValue = gSolver.oneAxisNextStep().r[0];

                t += dt;

                vError += Math.pow(analiticVal - vValue, 2);
                bError += Math.pow(analiticVal - bValue, 2);
                gError += Math.pow(analiticVal - gValue, 2);
            }

            errores[0][sim - 1] = vError / count;
            errores[1][sim - 1] = bError / count;
            errores[2][sim - 1] = gError / count;
        }

        mapper.writeValue(new File("output/ej1-3.json"), errores);
    }
}
