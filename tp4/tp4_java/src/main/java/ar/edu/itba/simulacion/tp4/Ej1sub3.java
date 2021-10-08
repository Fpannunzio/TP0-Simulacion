package ar.edu.itba.simulacion.tp4;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Ej1sub3 {
    
    public static void main(String[] args) throws IOException {
        
        final ObjectMapper mapper = new ObjectMapper();
        
        final int simCount = 1_000;
        final int simStep = 250;

        final double[][] errores = new double[3][simCount];
        
        double A                        = 1;
        double k                        = 10_000;
        double gamma                    = 100;
        double mass                        = 70;

        double tf                       = 5;
        
        int degree                      = 6;
        double[] functionCoeficients    = new double[]{-k, -gamma};

        final double initPosition = 1;
        final double initVelocity = -(A * gamma)/(2*mass);

        double[] initialValues          = new double[]{initPosition, initVelocity};
        final MoleculeStateAxis[] initialState = new MoleculeStateAxis[]{new MoleculeStateAxis(initPosition, initVelocity)};

        final Force force = (axis, state) -> -k * state[axis].position - gamma * state[axis].velocity;
        
        for(int sim = 1; sim <= simCount; sim++) {
            
            final int count         = sim * simStep;
            final double dt         = tf / count;
            VerletSolver vSolver    = new VerletSolver(1, dt, mass, force, initialState);
            BeemanSolver bSolver    = new BeemanSolver(mass, dt, functionCoeficients, initialValues);
            GearSolver gSolver      = new GearSolver(mass, dt, functionCoeficients, initialValues, degree);
            
            double t = 0;
            double vError = 0;
            double bError = 0;
            double gError = 0;

            for (int i = 0; i < count; i++) {
                final double analiticVal =  analitic(A, gamma, mass, k, t);
                final double vValue = vSolver.oneAxisNextStep().r[0];
                final double bValue = bSolver.nextStep()[0];
                final double gValue = gSolver.nextStep()[0];

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

    private static double analitic(final double A, final double gamma, final double m, final double k, final double t) {
        return A * Math.exp(-(gamma/(2*m))*t)*Math.cos(Math.sqrt((k/m) - (gamma*gamma)/(4*m*m))*t);
    }

}
