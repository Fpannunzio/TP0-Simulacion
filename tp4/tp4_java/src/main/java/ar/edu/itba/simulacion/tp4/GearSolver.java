package ar.edu.itba.simulacion.tp4;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class GearSolver {
    
    private static final double[][][] ALPHAS = new double[][][]{
        new double[][]{ // f(r)
            new double[]{0,         1.,          1.,      0,      0,      0},     //2
            new double[]{1./6.,       5./6,        1.,      1./3,    0,      0},     //3
            new double[]{19./120.,    3./4,        1.,      1./2,    1./12,   0},     //4
            new double[]{3./20.,      251./360.,    1.,      11./18.,  1./6.,    1./60.},  //5
        },
        new double[][]{ // f(r, r1)
            new double[]{0,             1.,             1.,      0,         0,          0},     //2
            new double[]{1./6.,         5./6.,          1.,      1./3.,     0,          0},     //3
            new double[]{19./90.,       3./4.,          1.,      1./2.,     1./12.,     0},     //4
            new double[]{3.0/16.0,      251.0/360.0,    1.0,     11.0/18.0, 1.0/6.0,    1.0/60.0},  //5
        },
    };
    
    private final double[] functionCoeficients;
    private final Deque<double[]> values;
    private final int degree;
    private final double m;
    private final double dt;


    private final static int KNONW_VALUES = 2;

    private double[] prediction;
    private double[] alpha;
    private double dr2;

    public GearSolver(final double m, final double dt, final double[] functionCoeficients, final double[] initialValues, final int degree) {

        this.functionCoeficients = functionCoeficients;
        this.values = new LinkedList<>();
        this.degree = degree;
        this.m = m;
        this.dt = dt;

        this.prediction = new double[degree];

        setAlpha();

        fillInitialValues(initialValues);
    }

    private void setAlpha() {

        int a = 0;
        int b = degree - functionCoeficients.length - 1;

        if(functionCoeficients[1] != 0) {
            a = 1;
        }

        alpha = ALPHAS[a][b];
    }

    private void fillInitialValues(final double[] initialValues){

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

        double[] currentValues = new double[degree];

        for (int i = 0; i < KNONW_VALUES; i++) {
            currentValues[i] = initialValues[i]; 
        }

        for (int i = KNONW_VALUES; i < degree; i++) {
            currentValues[i] = functionEval(currentValues[i-2], currentValues[i-1]) / m; 
        }

        values.push(currentValues);
    }
    
    public double[] nextStep() {

        predict();

        evaluate();

        correct();

        return values.peek();
    }

    private void predict() {

        // {r0, r1, r2, r3, r4, r5}
        double[] mid_terms = new double[degree];
        
        double[] currentValues = values.peek();

        for(int i = 0; i < degree; i++) {
            mid_terms[degree - i - 1] = currentValues[degree - i - 1];
            for (int k = 0; k < i; k++) {
                mid_terms[degree - k - 1] = mid_terms[degree - k - 1] * dt / (i - k); 
            }
            
            double acum = currentValues[degree - i - 1];

            for (int k = 0; k < i; k++) {
                acum += mid_terms[degree - k - 1]; 
            }
            
            prediction[degree - i - 1] = acum;
        }
    }

    private void evaluate() {
        
        double[] currentValues = values.peek();

        double r2 = functionEval(currentValues[0], currentValues[1]) / m;
        double predict_r2 = prediction[2];

        dr2 = (r2 - predict_r2) * dt * dt / 2;
    }

    private void correct() {

        double acum = 1;

        double[] corrections = new double[degree];

        for (int i = 0; i < degree; i++) {
            corrections[i] = prediction[i] + alpha[i] * dr2 * acum;
            acum = acum * (i + 1) / dt; // el caso de q0 es 0!/dt^0 = 1
        }

        values.push(corrections);
    }

    private double functionEval(final double r0, final double r1) {
        return functionCoeficients[0] * r0 + functionCoeficients[1] * r1;
    }

    public static class GearSolverConfig {

        private final double[] functionCoeficients;

        public GearSolverConfig(double[] functionCoeficients) {
            this.functionCoeficients = functionCoeficients;
        }

        
    }
}
