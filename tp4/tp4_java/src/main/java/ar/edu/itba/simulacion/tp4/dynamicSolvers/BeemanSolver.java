package ar.edu.itba.simulacion.tp4.dynamicSolvers;

import java.util.Arrays;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;

public class BeemanSolver implements MolecularDynamicSolver {

    // Configuration
    private final int                   dim;
    private final double                dt;
    private final double                mass;
    private final Force                 force;

    // Mutable State
    private MoleculeStateAxis[]         currentState;
    private double[]                    prev_r2;

    public BeemanSolver(
        final int                   dimensions,
        final double                dt,
        final double                mass,
        final Force                 force,
        final MoleculeStateAxis[]   initialState) {

        if(initialState.length != dimensions) {
            throw new IllegalArgumentException("dimensions and initial state length must be the same");
        }

        this.dim            = dimensions;
        this.dt             = dt;
        this.mass           = mass;
        this.force          = force;
        this.currentState   = initialState;

        // Calculamos estado anterior mediante Euler Modificado para obtener aceleraciones
        this.prev_r2 = new double[dim];
        final MoleculeStateAxis[] prevState = new MoleculeStateAxis[AXIS_DIM];
        for(int axis = 0; axis < dim; axis++) {
            final double r2         = calculateAcceleration(axis, currentState);                    // r2(t)
            final double prev_r0    = modifiedEulerPosition(currentState[axis].r, r2, -this.dt);    // r0(t - dt)
            final double prev_r1    = modifiedEulerVelocity(prev_r0, r2, -this.dt);                 // r1(t - dt)

            prevState[axis] = new MoleculeStateAxis(prev_r0, prev_r1);
        }
        for(int axis = 0; axis < dim; axis++) {
            prev_r2[axis] = calculateAcceleration(axis, prevState);
        }
    }

    @Override
    public MoleculeStateAxis[] nextStep() {
        double[] r;                                             // r(t)
        double[] r_dt;                                          // r(t + dt)
        double[] r_dt_p;                                        // predicted r(t + dt)
        final double[][][] r_by_axis = new double[dim][3][];    // r_by_axis[axis] = {r, r_dt, r_dt_p}

        final MoleculeStateAxis[] predictedNextState = new MoleculeStateAxis[dim];
        for(int axis = 0; axis < dim; axis++) {
            r               = Arrays.copyOf(currentState[axis].r, AXIS_DIM + 1);
            r[2]            = calculateAcceleration(axis, currentState);
            r_dt            = new double[AXIS_DIM];
            r_dt_p          = new double[AXIS_DIM + 1];
            r_by_axis[axis] = new double[][]{r, r_dt, r_dt_p};

            r_dt[0] = r[0] + (r[1] * dt) + ((2.0 / 3.0) * r[2] * dt * dt) - ((1.0 / 6.0) * prev_r2[axis] * dt * dt);

            r_dt_p[1] = r[1] + ((3.0 / 2.0) * r[2] * dt) - ((1.0 / 2.0) * prev_r2[axis] * dt);

            predictedNextState[axis] = new MoleculeStateAxis(r_dt[0], r_dt_p[1]);
        }

        final MoleculeStateAxis[] newState = new MoleculeStateAxis[dim];
        for(int axis = 0; axis < dim; axis++) {
            r       = r_by_axis[axis][0];
            r_dt    = r_by_axis[axis][1];
            r_dt_p  = r_by_axis[axis][2];

            r_dt_p[2] = calculateAcceleration(axis, predictedNextState);

            r_dt[1] = r[1] + ((1.0/3.0) * r_dt_p[2] * dt) + ((5.0/6.0) * r[2] * dt) - ((1.0/6.0) * prev_r2[axis] * dt);

            prev_r2[axis] = r[2];

            newState[axis] = new MoleculeStateAxis(r_dt);
        }

        currentState = newState;
        return currentState;
    }

    private double calculateAcceleration(final int axis, final MoleculeStateAxis[] state) {
        return force.apply(axis, state) / mass;
    }

    private double modifiedEulerPosition(final double[] r, final double r2, final double dt) {
        return r[0] + (dt * r[1]) + ((dt * dt) / 2) * r2;
    }

    private double modifiedEulerVelocity(final double r1, final double r2, final double dt) {
        return r1 + (dt * r2);
    }

    @Override
    public int getDim() {
        return dim;
    }

    @Override
    public double getDt() {
        return dt;
    }

    @Override
    public double getMass() {
        return mass;
    }

    @Override
    public Force getForce() {
        return force;
    }

    @Override
    public BeemanSolver copyState(final MolecularDynamicSolver solver) {
        if(!(solver instanceof BeemanSolver)) {
            throw new IllegalArgumentException("solver debe extender " + BeemanSolver.class);
        }
        final BeemanSolver s = (BeemanSolver) solver;

        currentState    = s.currentState.clone();
        prev_r2         = s.prev_r2.clone();

        return this;
    }
}
