package ar.edu.itba.simulacion.tp4.dynamicSolvers;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver;

import lombok.Builder;

public class GearSolver implements MolecularDynamicSolver {

    private final static int MIN_DEGREE = 2;
    private final static int MAX_DEGREE = 5;

    // ALPHAS[forceAxisMaxR][degree]: Primero es hasta que derivada usa la fuerza, luego es la cantidad de derivadas a usar (cuanto mas mejor)
    private static final double[][][] ALPHAS = new double[][][] {
        { // f(r)
            {0,             1,              1,          0,          0,          0},      //2
            {1/6.,          5/6.,           1,          1/3.,       0,          0},      //3
            {19/120.,       3/4.,           1,          1/2.,       1/12.,      0},      //4
            {3/20.,         251/360.,       1,          11/18.,     1/6.,       1/60.},  //5
        },
        { // f(r, r1)
            {0,             1,              1,          0,          0,          0},      //2
            {1/6.,          5/6.,           1,          1/3.,       0,          0},      //3
            {19/90.,        3/4.,           1,          1/2.,       1/12.,      0},      //4
            {3/16.,         251/360.,       1,          11/18.,     1/6.,       1/60.},  //5
        },
    };

    private static void validateInitialState(final double[][] initialState, final int dimensions, final int degree) {
        if(initialState.length != dimensions) {
            throw new IllegalArgumentException("dimensions and initial state length must be the same");
        }
        for(int axis = 0; axis < dimensions; axis++) {
            if(initialState[axis].length != degree + 1) {
                throw new IllegalArgumentException("All axis must have " + (degree + 1) + " coordinates");
            }
        }
    }

    private static double[][] deepCopy(final double[][] arr) {
        final double[][] ret = arr.clone();
        for(int i = 0; i < arr.length; i++) {
            ret[i] = ret[i].clone();
        }
        return ret;
    }

    // Configuration
    private final int       dim;
    private final double    dt;
    private final double    mass;
    private final Force     force;
    private final int       degree;
    private final double[]  alpha;

    // Mutable state
    private final double[][]            currentState;   // por cada axis, r(t) de dimension degree

    @Builder(setterPrefix = "with")
    public GearSolver(
        final int           dimensions,
        final double        dt,
        final double        mass,
        final Force         force,
        final int           degree,
        final int           forceAxisMaxR,  // El valor maximo de derivada que afecta a la fuerza
        final double[][]    initialState) { // El initial state incluye hasta el degree seleccionado

        if(degree < MIN_DEGREE || degree > MAX_DEGREE) {
            throw new IllegalArgumentException("degree must be between " + MIN_DEGREE + " and " + MAX_DEGREE);
        }
        if(forceAxisMaxR >= AXIS_DIM) {
            throw new IllegalArgumentException("forceAxisMaxR es como maximo " + (AXIS_DIM - 1));
        }
        validateInitialState(initialState, dimensions, degree);

        this.dim            = dimensions;
        this.dt             = dt;
        this.mass           = mass;
        this.force          = force;
        this.degree         = degree + 1;
        this.alpha          = ALPHAS[forceAxisMaxR][degree - MIN_DEGREE];
        this.currentState   = deepCopy(initialState);
    }

    @Override
    public MoleculeStateAxis[] nextStep() {
        final MoleculeStateAxis[] predictedState = new MoleculeStateAxis[dim];

        final double[][] predictions = new double[dim][];
        for(int axis = 0; axis < dim; axis++) {
            final double[] prediction = predict(axis);
            predictions[axis] = prediction;
            predictedState[axis] = new MoleculeStateAxis(prediction[0], prediction[1]);
        }

        final double[] dR2 = new double[dim];
        for(int axis = 0; axis < dim; axis++) {
            dR2[axis] = evaluate(axis, predictions, predictedState);
        }

        final MoleculeStateAxis[] ret = new MoleculeStateAxis[dim];
        for(int axis = 0; axis < dim; axis++) {
            correct(axis, predictions, dR2);
            final double[] axisState = currentState[axis];
            ret[axis] = new MoleculeStateAxis(axisState[0], axisState[1]);
        }

        return ret;
    }

    private double[] predict(final int axis) {
        final double[] axisState = currentState[axis];
        final double[] prediction = new double[this.degree];  // Prediction

        // {r0, r1, r2, r3, r4, r5}
        final double[] mid_terms = new double[degree];

        for(int i = degree - 1; i >= 0; i--) {
            double acum     = axisState[i];
            mid_terms[i]    = axisState[i];
            
            for(int k = 1; k < degree - i; k++) {
                mid_terms[degree - k] = mid_terms[degree - k] * dt / (degree - i - k);
                acum += mid_terms[degree - k];
            }
            
            prediction[i] = acum;
        }

        return prediction;
    }

    // Retorna dR2
    private double evaluate(final int axis, final double[][] prediction, final MoleculeStateAxis[] predictedState) {
        final double next_r2 = force.apply(axis, predictedState) / mass;
        return (next_r2 - prediction[axis][2]) * dt * dt / 2;
    }

    private void correct(final int axis, final double[][] prediction, final double[] dR2) {
        double acum = 1;  // dt^n/n!

        for(int i = 0; i < degree; i++) {
            currentState[axis][i] = prediction[axis][i] + alpha[i] * dR2[axis] * acum;
            acum = acum * (i + 1) / dt; // el caso de q0 es 0!/dt^0 = 1
        }
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
}
