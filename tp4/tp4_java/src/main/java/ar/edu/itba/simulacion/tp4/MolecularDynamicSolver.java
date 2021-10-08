package ar.edu.itba.simulacion.tp4;

import lombok.Value;

public interface MolecularDynamicSolver {
    int AXIS_DIM = 2;

    MoleculeStateAxis[] nextStep();

    int getDim();

    double getDt();

    double getMass();

    Force getForce();

    /**
     * Metodo de utilidad.
     * Si la dimension del solver es 1, este metodo directamente retorna el axis correspondiente.
     * @throws IllegalStateException si la dimension no es 1
     * */
    default MoleculeStateAxis oneAxisNextStep() throws IllegalStateException {
        if(getDim() != 1) {
            throw new IllegalStateException("Dimension debe ser 1 para llamar a este metodo");
        }
        return nextStep()[0];
    }

    @FunctionalInterface
    interface Force {
        double apply(final int axis, final MoleculeStateAxis[] state);
    }

    @Value
    class MoleculeStateAxis {
        public double   position;
        public double   velocity;
        // No modifiques los valores de r, plischu
        public double[] r;

        public MoleculeStateAxis(final double position, final double velocity) {
            this.position = position;
            this.velocity = velocity;
            this.r = new double[]{position, velocity};
        }
        public MoleculeStateAxis(final double[] r) {
            if(r.length != AXIS_DIM) {
                throw new IllegalArgumentException("r must be of length 2, because axis is of dimension 2");
            }
            this.r          = r;
            this.position   = r[0];
            this.velocity   = r[1];
        }
    }
}
