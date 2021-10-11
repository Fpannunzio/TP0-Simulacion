package ar.edu.itba.simulacion.tp4.marsMission.simulation;

import java.util.Arrays;
import java.util.List;

import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.Force;
import ar.edu.itba.simulacion.tp4.MolecularDynamicSolver.MoleculeStateAxis;

public class GravitationalForce implements Force {

    public static final int MAX_R       = 0;
    public static final int DIMENSIONS  = 2;

    // Configuration
    private final double                gravitationalConstant;
    private final String                name;
    private final double                mass;
    private final List<CelestialBody>   bodiesAffectedBy;

    // Force Cache
    // Lo usamos porque es muy comun que todos los inputs sean iguales menos el axis. Aprovechamos que ya lo calculamos.
    private int                         lastStateHashcode;
    private final double[]              cachedForce;


    public GravitationalForce(final double gravitationalConstant, final String name, final double mass, final List<CelestialBody> bodiesAffectedBy) {
        this.gravitationalConstant  = gravitationalConstant;
        this.name                   = name;
        this.mass                   = mass;
        this.bodiesAffectedBy       = bodiesAffectedBy;

        this.cachedForce            = new double[DIMENSIONS];
    }

    @Override
    public double apply(final int axis, final MoleculeStateAxis[] state) {
        final int stateHashcode = Arrays.hashCode(state);
        if(stateHashcode == lastStateHashcode) {
            return cachedForce[axis];
        }

        lastStateHashcode = stateHashcode;
        Arrays.fill(cachedForce, 0);

        for(final CelestialBody body : bodiesAffectedBy) {
            final double distance    = body.distanceTo(state[0].position, state[1].position);
            final double normalForce = mass * body.getMass() * gravitationalConstant / (distance * distance);

            cachedForce[0] += normalForce * ((body.getX() - state[0].position) / distance);
            cachedForce[1] += normalForce * ((body.getY() - state[1].position) / distance);
        }

        return cachedForce[axis];
    }

    public double getPotentialEnergy(final double x, final double y) {
        double potentialEnergy = 0;

        for(CelestialBody celestialBody : bodiesAffectedBy) {
            
            if(!name.equals("mars") || !celestialBody.getName().equals("earth")) {
                potentialEnergy += -mass * celestialBody.getMass() * gravitationalConstant / celestialBody.distanceTo(x, y);
            }
        }
        return potentialEnergy;
    }

    public String getName() {
        return name;
    }
}
