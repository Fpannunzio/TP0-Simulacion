package ar.edu.itba.simulacion.tp4;

import static ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.AXIS_DIM;

import java.util.Arrays;
import java.util.List;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.Force;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.MoleculeStateAxis;

public class GravitationalForce implements Force {

    // Configuration
    private final List<CelestialBody>   celestialBodies;
    private final double                affectedBodyMass;
    private final double                gravitationalConstant;

    // Force Cache
    // Lo usamos porque es muy comun que todos los inputs sean iguales menos el axis. Aprovechamos que ya lo calculamos.
    private int                   lastStateHashcode;
    private final double[]        cachedForce;


    public GravitationalForce(final List<CelestialBody> celestialBodies, final double affectedBodyMass, final double gravitationalConstant) {
        this.celestialBodies        = celestialBodies;
        this.affectedBodyMass       = affectedBodyMass;
        this.gravitationalConstant  = gravitationalConstant;

        this.cachedForce            = new double[AXIS_DIM];
    }

    @Override
    public double apply(final int axis, final MoleculeStateAxis[] state) {
        final int stateHashcode = Arrays.hashCode(state);
        if(stateHashcode == lastStateHashcode) {
            return cachedForce[axis];
        }

        lastStateHashcode = stateHashcode;
        Arrays.fill(cachedForce, 0);

        for(final CelestialBody body : celestialBodies) {
            final double distance = body.distanceTo(state[0].position, state[1].position);
            final double normalForce = affectedBodyMass * body.getMass() * gravitationalConstant / (distance * distance);

            cachedForce[0] += normalForce * ((body.getX() - state[0].position) / distance);
            cachedForce[1] += normalForce * ((body.getY() - state[1].position) / distance);
        }

        return cachedForce[axis];
    }
  
}
