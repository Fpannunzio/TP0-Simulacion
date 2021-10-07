package ar.edu.itba.simulacion.tp4;

import java.util.Deque;
import java.util.LinkedList;



public class BeemanSolver {
    
    private final double[] functionCoeficients;
    private final double m;
    private final double dt;

    private Deque<double[]> values;
    private double prev_r2;


    public BeemanSolver(final double m, final double dt, final double[] functionCoeficients, final double[] initialValues) {
        
        this.functionCoeficients = functionCoeficients;
        this.values = new LinkedList<>();
        this.values.push(initialValues);

        this.m = m;
        this.dt = dt;

        initialAceleration();
    }

    public double[] nextStep() {

        final double[] currentValues = values.peek();
        
        final double r0 = currentValues[0];
        final double r1 = currentValues[1];

        final double[] nextValues = new double[2];

        // r2 = f/m
        final double r2 = acelerationEval(r0, r1);

        //r0(t +dt)
        nextValues[0] = r0 + (r1*dt) + ((2.0/3.0) * r2 * dt*dt) - ((1.0/6.0) * prev_r2 * dt*dt);  
        
        //r1_p(t +dt)
        final double predict_r1 = r1 + ((3.0/2.0) * r2 * dt) - ((1.0/2.0) * prev_r2 * dt);
        
        //r2_p(t +dt)
        final double predict_r2 = acelerationEval(nextValues[0], predict_r1);

        //r1(t + dt)
        nextValues[1] = r1 + ((1.0/3.0) * predict_r2 * dt) + ((5.0/6.0) * r2 * dt) - ((1.0/6.0) * prev_r2 * dt);

        prev_r2 = r2; // TODO: PUEDE QUE TENGA QUE SER `predicted_r2`

        values.push(nextValues);

        return nextValues;
    }

    private double functionEval(final double r0, final double r1) {
        return functionCoeficients[0] * r0 + functionCoeficients[1] * r1;
    }

    private double acelerationEval(final double r0, final double r1) {
        return functionEval(r0, r1) / m;
    }

    private double eulerPositionStep(final double r0, final double r1, final double m, final double dt) {
        return r0 + (dt * r1) + ((dt * dt) / 2.0) * acelerationEval(r0, r1);
    }

    private double eulerVelocityStep(final double r0, final double r1, final double m, final double dt) {
        return r1 + (dt * acelerationEval(r0, r1));
    }

    private void initialAceleration() {

        final double[] currentValues = values.peek();

        final double r0 = currentValues[0];
        final double r1 = currentValues[1];

        // r0(-dt)
        final double prev_r0 = eulerPositionStep(r0, r1, m, -dt);

        // r1(-dt)
        final double prev_r1 = eulerVelocityStep(r0, r1, m, -dt);

        // r2(-dt)
        prev_r2 = acelerationEval(prev_r0, prev_r1);
    }
}
