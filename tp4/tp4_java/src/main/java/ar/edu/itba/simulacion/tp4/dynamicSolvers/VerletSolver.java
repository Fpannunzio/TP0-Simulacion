package ar.edu.itba.simulacion.tp4.dynamicSolvers;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;

public class VerletSolver implements MolecularDynamicSolver {

    // Configuration
    private final int                   dim;
    private final double                dt;
    private final double                mass;
    private final Force                 force;

    // Mutable state
    private MoleculeStateAxis[]     currentState;   // r(t)
    private double[]                prev_r0;        // r0(t - dt)

    public VerletSolver(
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

        // euler para conseguir prev_r0 = r0(-dt)
        prev_r0 = new double[dim];
        for(int axis = 0; axis < dim; axis++) {
            prev_r0[axis] = modifiedEulerSolver(axis, -this.dt);
        }
    }

    @Override
    public MoleculeStateAxis[] nextStep() {
        final MoleculeStateAxis[] newState = new MoleculeStateAxis[dim];

        for(int axis = 0; axis < dim; axis++) {
            newState[axis] = nextStepAxis(axis);
            prev_r0[axis] = currentState[axis].r[0];
        }
        currentState = newState;

        return newState;
    }

    private MoleculeStateAxis nextStepAxis(final int axis) {
        final double[] r_dt = new double[AXIS_DIM]; // r(t + dt)
        final double[] r = currentState[axis].r;    // r(t)

        r_dt[0] = 2 * r[0] - prev_r0[axis] + ((dt*dt) / mass) * force.apply(axis, currentState);

        // r_dt[1] = (r_dt[0] - prev_r0[axis]) / (2 * dt); // Es error cuadratico, pero te va r1(t) :(
        r_dt[1] = (r_dt[0] - r[0]) / dt;                   // Error lineal, pero nos da r1(t + dt) :)

        return new MoleculeStateAxis(r_dt);
    }

    private double modifiedEulerSolver(final int axis, final double dt) {
        final double[] r = currentState[axis].r;
        return r[0] + (dt * r[1]) + ((dt * dt) / (2 * mass)) * force.apply(axis, currentState);
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
    public VerletSolver copyState(final MolecularDynamicSolver solver) {
        if(!(solver instanceof VerletSolver)) {
            throw new IllegalArgumentException("solver debe extender " + VerletSolver.class);
        }
        final VerletSolver s = (VerletSolver) solver;

        currentState    = s.currentState.clone();
        prev_r0         = s.prev_r0.clone();

        return this;
    }
}
