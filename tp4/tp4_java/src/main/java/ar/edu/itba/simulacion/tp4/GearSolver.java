package ar.edu.itba.simulacion.tp4;

import java.util.Arrays;

public class GearSolver {

    private final double[] functionCoeficients;
    private final double[] initialValues;
    private final int degree;
    private final double m;
    private final double dt;

    private static final double[][][] ALPHAS = new double[][][]{
        new double[][]{ // f(r)
            new double[]{0,         1,          1,      0,      0,      0},     //2
            new double[]{1/6,       5/6,        1,      1/3,    0,      0},     //3
            new double[]{19/120,    3/4,        1,      1/2,    1/12,   0},     //4
            new double[]{3/20,      251/360,    1,      11/18,  1/6,    1/60},  //5
        },
        new double[][]{ // f(r, r1)
            new double[]{0,         1,          1,      0,      0,      0},     //2
            new double[]{1/6,       5/6,        1,      1/3,    0,      0},     //3
            new double[]{19/90,     3/4,        1,      1/2,    1/12,   0},     //4
            new double[]{3/16,      251/360,    1,      11/18,  1/6,    1/60},  //5
        },
    };

    private final static int KNONW_VALUES = 2;

    private double[] lastPredictions;
    private double[] alpha;
    private double dr2;
    private double[] lastCorrections;

    public GearSolver(final double m, final double dt, final double[] functionCoeficients, final double[] initialValues, final int degree) {

        this.functionCoeficients = functionCoeficients;
        this.initialValues = new double[degree];
        this.degree = degree;
        this.m = m;
        this.dt = dt;

        this.lastCorrections = new double[degree];
        this.lastPredictions = new double[degree];

        setAlpha();

        System.arraycopy(initialValues, 0, this.initialValues, 0, KNONW_VALUES);

        fillInitialValues();
    }

    private void setAlpha() {

        int a = 0;
        int b = functionCoeficients.length;

        if(functionCoeficients[1] != 0) {
            a = 1;
        }

        alpha = ALPHAS[a][b];
    }

    public double[] nextStep() {

        predict();

        evaluate();

        correct();

        return Arrays.copyOf(lastCorrections, lastCorrections.length);
    }

    private void predict() {

        // {r5, r4, r3, r2, r1, r0}
        double[] prev = new double[degree];
        
        for (int i = 0; i < degree; i++) {
            prev[degree - i - 1] = initialValues[degree - i - 1];
            for (int k = 0; k < i; k++) {
                prev[degree - k - 1] = prev[degree - k - 1] * dt / (i - k); 
            }
            
            double tmp = initialValues[degree - i - 1];

            for (int k = 0; k < i; k++) {
                tmp += prev[degree - k - 1]; 
            }
            
            lastPredictions[degree - i - 1] = tmp;
        }
    }

    private void evaluate() {
    
        double a = functionEval(initialValues[1], initialValues[0]) / m;
        double pa = lastPredictions[2];

        dr2 = (a - pa) * dt * dt / 2;
    }

    private void correct() {

        double acum = 1;

        for (int i = 0; i < degree; i++) {
            lastCorrections[i] = lastPredictions[i] + alpha[i] * dr2 * acum;
            acum = acum * (i + 1) / dt; // el caso de q0 es 0!/dt^0 = 1
        }
    }

    private double functionEval(final double r0, final double r1) {
        return functionCoeficients[0] * r0 + functionCoeficients[1] * r1;
    }

    private void fillInitialValues(){

        /**
         * f mr2 = a*r0 + b*r1
         * 
         * r0
         * r1
         * r2 = (a*r0 + b*r1)/m
         * r3 = (a*r1 + b*r2)/m
         * r4 = (a*r2 + b*r3)/m
         * r5 = (a*r3 + b*r4)/m
         * 
        */

        for (int i = KNONW_VALUES; i < degree; i++) {
            initialValues[i] = functionEval(initialValues[i-2], initialValues[i-1]) / m; 
        }
    }
    
    public static class GearSolverConfig {

        private final double[] functionCoeficients;

        public GearSolverConfig(double[] functionCoeficients) {
            this.functionCoeficients = functionCoeficients;
        }

        
    }
}
