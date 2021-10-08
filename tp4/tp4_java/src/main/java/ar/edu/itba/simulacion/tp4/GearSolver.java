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
        final double[] mid_terms = new double[degree];
        
        final double[] currentValues = values.peek();

        for(int i = degree - 1; i >= 0; i--) {
            
            double acum = currentValues[i];
            mid_terms[i] = currentValues[i];
            
            for(int k = 1; k < degree - i; k++) {
                mid_terms[degree - k] = mid_terms[degree - k] * dt / (degree - i - k); 
                acum += mid_terms[degree - k]; 
            }
            
            prediction[i] = acum;
        }
    }

    private void evaluate() {

        final double predict_r0 = prediction[0];
        final double predict_r1 = prediction[1];

        // r2(t+dt)
        final double next_r2 = functionEval(predict_r0, predict_r1) / m;
        final double predict_r2 = prediction[2];

        dr2 = (next_r2 - predict_r2) * dt * dt / 2;
    }

    private void correct() {

        // dt^n/n!
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
}
