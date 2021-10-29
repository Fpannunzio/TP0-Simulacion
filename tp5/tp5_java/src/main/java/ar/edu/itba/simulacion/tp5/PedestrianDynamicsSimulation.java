package ar.edu.itba.simulacion.tp5;

import static ar.edu.itba.simulacion.particle.ParticleUtils.randDouble;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import ar.edu.itba.simulacion.particle.Particle2D;
import ar.edu.itba.simulacion.particle.neighbours.CellIndexMethod;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
public class PedestrianDynamicsSimulation {

    private static final int FAR_AWAY_TARGET = -10;

    // Configuration
    private final double    tau;
    private final double    beta;
    private final double    exitLength;
    private final double    minRadius;
    private final double    maxRadius;
    private final double    desiredVelocity;
    private final double    escapeVelocity;
    private final double    spaceWidth;

    // Derived Configuration
    private final double    dt;
    private final double    exitLeft;
    private final double    exitRight;

    // Internal Configuration
    @Getter(AccessLevel.NONE) private final Random            randomGen;
    @Getter(AccessLevel.NONE) private final CellIndexMethod   cim;

    // Cached derived properties
    @Getter(AccessLevel.NONE) private final double  dr;
    @Getter(AccessLevel.NONE) private final double  leftTargetLimit;
    @Getter(AccessLevel.NONE) private final double  rightTargetLimit;
    @Getter(AccessLevel.NONE) private final double  radiusRange;

    @Builder(setterPrefix = "with")
    public PedestrianDynamicsSimulation(
        final double    tau,
        final double    beta,
        final double    exitLength,
        final double    minRadius,
        final double    maxRadius,
        final double    desiredVelocity,
        final double    escapeVelocity,
        final double    spaceWidth,
        final Random    randomGen) {

        this.tau                = tau;
        this.beta               = beta;
        this.exitLength         = exitLength;
        this.minRadius          = minRadius;
        this.maxRadius          = maxRadius;
        this.spaceWidth         = spaceWidth;
        this.desiredVelocity    = desiredVelocity;
        this.escapeVelocity     = escapeVelocity;

        this.dt                 = minRadius / (2 * Math.max(desiredVelocity, escapeVelocity));
        this.exitLeft           = spaceWidth/2 - exitLength /2;
        this.exitRight          = spaceWidth/2 + exitLength /2;

        this.randomGen          = randomGen;
        this.cim                = new CellIndexMethod(spaceWidth, 0, false);

        this.dr                 = maxRadius * dt / tau;
        this.leftTargetLimit    = exitLeft + 0.2 * exitLength;
        this.rightTargetLimit   = exitRight - 0.2 * exitLength;
        this.radiusRange        = maxRadius - minRadius;
    }

    public void simulate(final List<Particle2D> initialState, final SimulationStateNotifier notifier) {
        int iteration = 0;
        List<Particle2D> lockedParticles    = initialState;
        List<Particle2D> escapedParticles   = List.of();

        while(notifier.notify(iteration, lockedParticles, escapedParticles)) {
            final List<Particle2D> locked    = new ArrayList<>(initialState.size());
            final List<Particle2D> escaped   = new LinkedList<>();
            final Consumer<Particle2D> escapedConsumer = escaped::add;

            advanceLockedParticles(lockedParticles, locked::add, escapedConsumer);
            advanceEscapedParticles(escapedParticles, escapedConsumer);

            lockedParticles  = locked;
            escapedParticles = escaped;
            iteration++;
        }
    }

    public void advanceLockedParticles(final List<Particle2D> lastLockedParticles, final Consumer<Particle2D> lockedConsumer, final Consumer<Particle2D> escapedConsumer) {
        final Map<Integer, Set<Particle2D>> neighbours = cim.calculateNeighbours(lastLockedParticles);

        for(final Particle2D particle : lastLockedParticles) {
            final Particle2D advancedParticle = advanceParticle(particle, neighbours.get(particle.getId()));
            if(advancedParticle.getY() <= 0) {
                escapedConsumer.accept(advancedParticle);
            } else {
                lockedConsumer.accept(advancedParticle);
            }
        }
    }

    private void advanceEscapedParticles(final List<Particle2D> escapedParticles, final Consumer<Particle2D> escapedConsumer) {
        for(final Particle2D particle : escapedParticles) {
            final double newR           = calculateNewRadius(particle.getRadius());
            final double newVMod        = calculateNewVelocity(newR);
            final double targetDirX     = 0;
            final double targetDirY     = FAR_AWAY_TARGET - particle.getY();
            final double targetDirMod   = Math.hypot(targetDirX, targetDirY);

            final Particle2D advancedParticle = particle.moveCartesian(
                dt,
                newVMod * (targetDirX / targetDirMod),
                newVMod * (targetDirY / targetDirMod),
                newR
            );
            if(advancedParticle.getY() > FAR_AWAY_TARGET) {
                escapedConsumer.accept(advancedParticle);
            }
        }
    }

    private Particle2D advanceParticle(final Particle2D particle, final Set<Particle2D> neighbours) {
        final double x = particle.getX();
        final double y = particle.getY();
        final double r = particle.getRadius();

        double escapeX = 0;
        double escapeY = 0;

        // Verificamos choques con bordes
        if(x - r <= 0) {
            // borde izquierdo
            escapeX += 1;
        } else if(x + r >= spaceWidth) {
            // borde derecho
            escapeX -= 1;
        }
        if(y + r >= spaceWidth) {
            // borde superior
            escapeY -= 1;
        } else if(y - r <= 0) {
            // borde inferior

            // Tenemos en cuenta los casos especiales de colision con puerta
            if(x <= exitLeft || x >= exitRight) {
                // Centro afuera de puerta -> Colision normal
                escapeY += 1;
            } else if(x - r <= exitLeft) {
                // Particula colisiona con borde izquierdo de puerta
                final double diffX = x - exitLeft;
                final double distance = Math.hypot(diffX, y);

                escapeX += diffX / distance;
                escapeY += y / distance;
            } else if(x + r >= exitRight) {
                // Particula colisiona con borde derecho de puerta
                final double diffX = x - exitRight;
                final double distance = Math.hypot(diffX, y);

                escapeX += diffX / distance;
                escapeY += y / distance;
            }
            // Sino, la particula esta completamente dentro de puerta -> No hay colision
        }

        // Calculamos choques con vecinos
        for(final Particle2D neighbour : neighbours) {
            // Ponderamos el vector escape con el resto
            final double diffX      = x - neighbour.getX();
            final double diffY      = y - neighbour.getY();
            final double distance   = Math.hypot(diffX, diffY);

            escapeX += diffX / distance;
            escapeY += diffY / distance;
        }

        final double newVx;
        final double newVy;
        final double newR;
        if(escapeX == 0 && escapeY == 0) {
            // No hay colision
            newR = calculateNewRadius(r);

            final double newVMod = calculateNewVelocity(newR);
            final double targetDirX = calculateTargetX(particle) - x;
            final double targetDirY = -y;
            final double targetDirMod = Math.hypot(targetDirX, targetDirY);

            newVx = newVMod * (targetDirX / targetDirMod);
            newVy = newVMod * (targetDirY / targetDirMod);
        } else {
            // Colision
            final double escapeMod = Math.hypot(escapeX, escapeY);
            newVx   = escapeVelocity * (escapeX / escapeMod);
            newVy   = escapeVelocity * (escapeY / escapeMod);
            newR    = minRadius;
        }

        return particle.moveCartesian(dt, newVx, newVy, newR);
    }

    private double calculateNewRadius(final double lastRadius) {
        return Math.min(maxRadius, lastRadius + dr);
    }

    private double calculateNewVelocity(final double newRadius) {
        return desiredVelocity * Math.pow((newRadius - minRadius) / radiusRange, beta);
    }

    private double calculateTargetX(final Particle2D particle) {
        final double x = particle.getX();

        return x < leftTargetLimit || x > rightTargetLimit
            ? randDouble(randomGen, leftTargetLimit, rightTargetLimit)
            : x
            ;
    }

    /* ----------------------------------------- Clases Auxiliares ----------------------------------------------- */

    @FunctionalInterface
    public interface SimulationStateNotifier {
        boolean notify(
            final int               iteration,
            final List<Particle2D>  state,
            final List<Particle2D>  escapedParticles);
    }
}
