package ar.edu.itba.simulacion.tp4.oscillator;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.io.File;
import java.io.IOException;
import java.util.function.DoubleBinaryOperator;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;

public class Ej1sub3 {
    
    public static void main(String[] args) throws IOException {
        
        final ObjectMapper mapper = new ObjectMapper();
        
        final int simCount = 1_000;
        final int simStep = 250;

        final double[][] errores = new double[3][simCount];
        
        double A                        = 1;
        double k                        = 10_000;
        double gamma                    = 100;
        double mass                     = 70;

        double tf                       = 5;
        
        int degree                      = 5;

        final double initPosition = 1;
        final double initVelocity = -(A * gamma)/(2*mass);
        final MoleculeStateAxis[] initialState = new MoleculeStateAxis[]{new MoleculeStateAxis(initPosition, initVelocity)};

        final DoubleBinaryOperator oscillatorForce = (position, velocity) -> Ej1.oscillatorForce(k, gamma, position, velocity);
        final Force force = (axis, state) -> oscillatorForce.applyAsDouble(state[axis].position, state[axis].velocity);

        final double[][] gearInitState = new double[][]{Ej1.gearInitState(initPosition, initVelocity, degree, mass, oscillatorForce)};
        
        for(int sim = 1; sim <= simCount; sim++) {
            final int count         = sim * simStep;
            final double dt         = tf / count;

            final VerletSolver vSolver    = new VerletSolver(1, dt, mass, force, initialState);
            final BeemanSolver bSolver    = new BeemanSolver(1, dt, mass, force, initialState);
            final GearSolver   gSolver    = new GearSolver(1, dt, mass, force, degree, 1, gearInitState);
            
            double t = 0;
            double vError = 0;
            double bError = 0;
            double gError = 0;

            for(int i = 0; i < count; i++) {
                final double analiticVal =  analitic(A, gamma, mass, k, t);
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

    public static double analitic(final double A, final double gamma, final double m, final double k, final double t) {
        return A * Math.exp(-(gamma/(2*m))*t)*Math.cos(Math.sqrt((k/m) - (gamma*gamma)/(4*m*m))*t);
    }
}
