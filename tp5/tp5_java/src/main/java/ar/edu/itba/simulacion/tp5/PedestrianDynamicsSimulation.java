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

    private static final double EXIT_TARGET_POSITION       = 0;
    private static final double FAR_AWAY_TARGET_POSITION   = -10;
    private static final double FAR_AWAY_TARGET_LENGTH     = 3;
    private static final double TARGET_LIMIT_COEFFICIENT   = 0.2;

    // Flag para ver que opcion de colision con el borde de la puerta es mejor
    private static final boolean BORDER_COLLISIONS = true;

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
    @Getter(AccessLevel.NONE) private final double  leftFarAwayTargetLimit;
    @Getter(AccessLevel.NONE) private final double  rightFarAwayTargetLimit;
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
        this.cim                = new CellIndexMethod(spaceWidth - FAR_AWAY_TARGET_POSITION, 0, FAR_AWAY_TARGET_POSITION, 0, false);

        this.dr                 = maxRadius * dt / tau;
        this.leftTargetLimit    = exitLeft + TARGET_LIMIT_COEFFICIENT * exitLength;
        this.rightTargetLimit   = exitRight - TARGET_LIMIT_COEFFICIENT * exitLength;
        this.radiusRange        = maxRadius - minRadius;
        this.leftFarAwayTargetLimit    = (spaceWidth/2 - FAR_AWAY_TARGET_LENGTH/2) + TARGET_LIMIT_COEFFICIENT * FAR_AWAY_TARGET_LENGTH;
        this.rightFarAwayTargetLimit   = (spaceWidth/2 + FAR_AWAY_TARGET_LENGTH/2) - TARGET_LIMIT_COEFFICIENT * FAR_AWAY_TARGET_LENGTH;
    }

    public void simulate(final List<Particle2D> initialState, final SimulationStateNotifier notifier) {
        int iteration = 0;
        List<Particle2D> currentState   = initialState;
        int lastLockedCount             = currentState.size();
        int lastEscapedCount            = 0;

        boolean continueIteration       = notifier.notify(iteration, currentState, List.of(), List.of());
        while(continueIteration) {
            iteration++;

            final List<Particle2D> locked       = new ArrayList<>(lastLockedCount);
            final List<Particle2D> escaped      = new ArrayList<>(lastEscapedCount);
            final List<Particle2D> justEscaped  = new LinkedList<>();

            currentState = advanceParticles(currentState, locked::add, escaped::add, justEscaped::add);

            continueIteration = notifier.notify(iteration, locked, escaped, justEscaped) && (!locked.isEmpty() || !escaped.isEmpty());
        }
    }

    public List<Particle2D> advanceParticles(
        final List<Particle2D>      lastState,
        final Consumer<Particle2D>  lockedConsumer,
        final Consumer<Particle2D>  escapedConsumer,
        final Consumer<Particle2D>  justEscapedConsumer) {

        final List<Particle2D> ret = new ArrayList<>(lastState.size());

        final Map<Integer, Set<Particle2D>> neighbours = cim.calculateNeighbours(lastState);

        for(final Particle2D particle : lastState) {
            final Particle2D advancedParticle = advanceParticle(particle, neighbours.get(particle.getId()));

            if(!isOutOfBounds(advancedParticle)) {
                ret.add(advancedParticle);

                if(isEscaped(advancedParticle)) {
                    escapedConsumer.accept(advancedParticle);

                    if(!isEscaped(particle)) {
                        justEscapedConsumer.accept(advancedParticle);
                    }
                } else {
                    lockedConsumer.accept(advancedParticle);
                }
            }
        }

        return ret;
    }

    private Particle2D advanceParticle(final Particle2D particle, final Set<Particle2D> neighbours) {
        final boolean escaped = isEscaped(particle);
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
        } else if((escaped && y + r >= 0) || (!escaped && y - r <= 0)) {
            // borde inferior
            // Tenemos en cuenta los casos especiales de colision con puerta
            if(x <= exitLeft || x >= exitRight) {
                // Centro afuera de puerta -> Colision normal
                escapeY += 1;
            } else if(BORDER_COLLISIONS) {
                if(x - r <= exitLeft) {
                    // Particula colisiona con borde izquierdo de puerta
                    final double diffX = x - exitLeft;
                    final double diffY = y - EXIT_TARGET_POSITION;
                    final double distance = Math.hypot(diffX, diffY);

                    escapeX += diffX / distance;
                    escapeY += diffY / distance;
                } else if(x + r >= exitRight) {
                    // Particula colisiona con borde derecho de puerta
                    final double diffX = x - exitRight;
                    final double diffY = y - EXIT_TARGET_POSITION;
                    final double distance = Math.hypot(diffX, diffY);

                    escapeX += diffX / distance;
                    escapeY += diffY / distance;
                }
                // Sino, la particula esta completamente dentro de puerta -> No hay colision
            }
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
            newR = Math.min(maxRadius, r + dr);

            final double newVMod = desiredVelocity * Math.pow((newR - minRadius) / radiusRange, beta);
            final double targetDirX = targetVectorX(x, escaped);
            final double targetDirY = targetVectorY(y, escaped);
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

        return particle.eagerMoveCartesian(dt, newVx, newVy, newR);
    }

    private boolean isEscaped(final Particle2D particle) {
        return particle.getY() <= EXIT_TARGET_POSITION;
    }
    private boolean isOutOfBounds(final Particle2D particle) {
        return particle.getY() <= FAR_AWAY_TARGET_POSITION;
    }

    private double targetVectorX(final double x, final boolean escaped) {
        final double leftLimit  = escaped ? leftFarAwayTargetLimit  : leftTargetLimit;
        final double rightLimit = escaped ? rightFarAwayTargetLimit : rightTargetLimit;

        return x < leftLimit || x > rightLimit
            ? randDouble(randomGen, leftLimit, rightLimit) - x
            : 0
            ;
    }

    private double targetVectorY(final double y, final boolean escaped) {
        return escaped ? FAR_AWAY_TARGET_POSITION - y : EXIT_TARGET_POSITION - y;
    }

    /* ----------------------------------------- Clases Auxiliares ----------------------------------------------- */

    @FunctionalInterface
    public interface SimulationStateNotifier {
        boolean notify(
            final int               iteration,
            final List<Particle2D>  locked,
            final List<Particle2D>  escaped,
            final List<Particle2D>  justEscaped);
    }
}
