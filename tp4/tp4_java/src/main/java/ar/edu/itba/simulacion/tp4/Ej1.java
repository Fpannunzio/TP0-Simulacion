package ar.edu.itba.simulacion.tp4;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Ej1 {
    
    public static void main(String[] args) throws IOException {
        
        final ObjectMapper mapper = new ObjectMapper();
        
        final int count = 10_000;

        final double[][][] results = new double[3][count][];
        
        double A                        = 1;
        double k                        = 10_000;
        double gamma                    = 100;
        double mass                     = 70;

        double tf                       = 5;
        double dt                       = tf / count;

        int degree                      = 6;
        double[] functionCoeficients    = new double[]{-k, -gamma};

        final double initPosition = 1;
        final double initVelocity = -(A * gamma)/(2*mass);

        double[] initialValues          = new double[]{initPosition, initVelocity};
        final MoleculeStateAxis[] initialState = new MoleculeStateAxis[]{new MoleculeStateAxis(initPosition, initVelocity)};

        final Force force = (axis, state) -> -k * state[axis].position - gamma * state[axis].velocity;

        VerletSolver vSolver    = new VerletSolver(1, dt, mass, force, initialState);
        BeemanSolver bSolver    = new BeemanSolver(mass, dt, functionCoeficients, initialValues);
        GearSolver gSolver      = new GearSolver(mass, dt, functionCoeficients, initialValues, degree);

        for (int i = 0; i < count; i++) {
            if(i % (count/10) == 0) {
                System.out.println("1_000");
            }

            results[0][i] = vSolver.oneAxisNextStep().r;
            results[1][i] = bSolver.nextStep();
            results[2][i] = gSolver.nextStep();
        }

        mapper.writeValue(new File("output/ej1.json"), results);
    }
}
