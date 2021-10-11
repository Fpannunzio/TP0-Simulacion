package ar.edu.itba.simulacion.tp4.oscillator;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.io.File;
import java.io.IOException;
import java.util.function.DoubleBinaryOperator;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;

public class Ej1 {

    public static double oscillatorForce(final double k, final double gamma, final double position, final double velocity) {
        return -k * position - gamma * velocity;
    }
    
    public static void main(String[] args) throws IOException {
        
        final ObjectMapper mapper = new ObjectMapper();
        
        final int count = (int) (5 * 1e4);

        final double[][][] results = new double[3][count][];
        
        double A                        = 1;
        double k                        = 10_000;
        double gamma                    = 100;
        double mass                     = 70;

        double tf                       = 5;
        double dt                       = tf / count;

        int degree                      = 5;

        final double initPosition = 1;
        final double initVelocity = -(A * gamma)/(2*mass);
        final MoleculeStateAxis[] initialState = new MoleculeStateAxis[]{new MoleculeStateAxis(initPosition, initVelocity)};

        final DoubleBinaryOperator oscillatorForce = (position, velocity) -> oscillatorForce(k, gamma, position, velocity);
        final Force force = (axis, state) -> oscillatorForce.applyAsDouble(state[axis].position, state[axis].velocity);

        final double[][] gearInitState = new double[][]{gearInitState(initPosition, initVelocity, degree, mass, oscillatorForce)};

        final VerletSolver  vSolver    = new VerletSolver(1, dt, mass, force, initialState);
        final BeemanSolver  bSolver    = new BeemanSolver(1, dt, mass, force, initialState);
        final GearSolver    gSolver    = new GearSolver(1, dt, mass, force, degree, 1, gearInitState);

        for (int i = 0; i < count; i++) {
            if(i % (count/10) == 0) {
                System.out.println("1_000");
            }

            results[0][i] = vSolver.oneAxisNextStep().r;
            results[1][i] = bSolver.oneAxisNextStep().r;
            results[2][i] = gSolver.oneAxisNextStep().r;
        }

        mapper.writeValue(new File("output/ej1.json"), results);
    }

    public static double[] gearInitState(
        final double                initPosition,
        final double                initialVelocity,
        final int                   degree,
        final double                mass,
        final DoubleBinaryOperator  force) {

        final double[] initState = new double[degree + 1];
        initState[0] = initPosition;
        initState[1] = initialVelocity;

        for(int i = 2; i <= degree; i++) {
            initState[i] = force.applyAsDouble(initState[i-2], initState[i-1]) / mass;
        }

        return initState;
    }
}
