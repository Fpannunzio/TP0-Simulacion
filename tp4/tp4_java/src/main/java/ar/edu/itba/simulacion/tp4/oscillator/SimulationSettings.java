package ar.edu.itba.simulacion.tp4.oscillator;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.*;

import java.util.function.DoubleBinaryOperator;

import ar.edu.itba.simulacion.tp4.dynamicSolvers.BeemanSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.GearSolver;
import ar.edu.itba.simulacion.tp4.dynamicSolvers.VerletSolver;

public final class SimulationSettings {
    private SimulationSettings() {
        // static
    }

    // Oscillator Configurator
    public static final double  A       = 1;
    public static final double  k       = 10_000;
    public static final double  gamma   = 100;
    public static final double  mass    = 70;

    // Solver Configuration
    public static final int gearDegree      = 5;
    public static final int forceAxisMaxR   = 1;

    // Oscillator Initial State
    public static final double initPosition = 1;
    public static final double initVelocity = -(A * gamma)/(2 * mass);

    // Oscillator Force
    public static final DoubleBinaryOperator    oscillatorForce = (position, velocity) -> oscillatorForce(k, gamma, position, velocity);
    public static final Force                   force = (axis, state) -> oscillatorForce.applyAsDouble(state[axis].position, state[axis].velocity);

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
            initState[i] = force.applyAsDouble(initState[i - 2], initState[i - 1]) / mass;
        }

        return initState;
    }

    public static double oscillatorForce(final double k, final double gamma, final double position, final double velocity) {
        return -k * position - gamma * velocity;
    }

    public static VerletSolver getVerletSolver(final double dt) {
        return new VerletSolver(1, dt, mass, force, new MoleculeStateAxis[]{new MoleculeStateAxis(initPosition, initVelocity)});
    }

    public static BeemanSolver getBeemanSolver(final double dt) {
        return new BeemanSolver(1, dt, mass, force, new MoleculeStateAxis[]{new MoleculeStateAxis(initPosition, initVelocity)});
    }

    public static GearSolver getGearSolver(final double dt) {
        return new GearSolver(1, dt, mass, force, gearDegree, forceAxisMaxR,
            new double[][]{gearInitState(initPosition, initVelocity, gearDegree, mass, oscillatorForce)}
        );
    }

    public static double solveAnalytic(final double t) {
        return A * Math.exp(-(gamma / (2 * mass)) * t) * Math.cos(Math.sqrt((k / mass) - (gamma * gamma)/(4 * mass * mass)) * t);
    }
}
