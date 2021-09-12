package ar.edu.itba.simulacion.tp3;

import ar.edu.itba.simulacion.particle.Particle2D;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

public class BrownianParticleSystem {

    @Getter private       double                currentTime;
    @Getter private final double                spaceWidth;
    @Getter private final List<SimulationState> states;
    /**
     *  Matriz triangular superior (sin diagonal) donde guardamos cuanto falta para el choque entre dos particulas.
     *  Elegimos esta estructura de datos pues:
     *  1. Los id de las particulas arrancan en 0 y son secuenciales
     *  2. Como una particula no puede chocar con si misma, la diagonal no nos importa
     *  3. Es simetrica, es equivalente que a choque con b, que visceversa
     *
     *  Arbitrariamente elegimos triangular superior, es decir, siempre hacemos el lookup con el menor id primero.
     */
    private final double[]              particleCollisions;
    private final Collision[]           wallCollisions;
    private       Collision             nextCollision;

    /** Se asume que el id de las particulas coincide con su posicion en la lista */
    public BrownianParticleSystem(final double spaceWidth, final List<Particle2D> initialState) {
        this.spaceWidth         = spaceWidth;
        this.currentTime        = 0L;
        this.states             = new LinkedList<>();
        this.particleCollisions = new double[particleCollisionsSize(initialState.size())];
        this.wallCollisions     = new Collision[initialState.size()];

        // Queremos que las particles se puedan referenciar por id en tiempo constante
        final List<Particle2D> randomAccessInitialState;
        if(initialState instanceof RandomAccess) {
            randomAccessInitialState = initialState;
        } else {
            randomAccessInitialState = new ArrayList<>(initialState);
        }

        this.states.add(new SimulationState(this.currentTime, randomAccessInitialState, null));
        final Collision minWallCollision        = calculateInitialWallCollisions(initialState);
        final Collision minParticleCollision    = calculateInitialParticleCollisions(initialState);

        this.nextCollision = minWallCollision.getDTime() < minParticleCollision.getDTime() ?
            minWallCollision :
            minParticleCollision
            ;
    }

    public SimulationState calculateNextCollision() {
        final List<Particle2D> lastState = states.get(states.size() - 1).getParticles();
        final int particleCount = lastState.size();
        final List<Particle2D> newState = new ArrayList<>(particleCount);

        final Collision collision = nextCollision;
        final List<Particle2D> collisionParticles = collision.computeCollision(lastState, spaceWidth);
        final double dTime = collision.getDTime(); // Un dTime ya masajeado

        final Particle2D collisionParticle1 = collisionParticles.get(0);  // Si o si esta
        final Particle2D collisionParticle2 = collision.isParticleCollision() ? collisionParticles.get(1) : null; // Solo esta cuando hubo colision de particulas

        double minTime = Double.MAX_VALUE;
        Collision minCollision = null;

        int currentParticleId = 0;
        for(final Particle2D currentParticle : lastState) {
            final Particle2D collisionParticle = collisionParticle(currentParticleId, collisionParticle1, collisionParticle2);
            if(collisionParticle == null) {
                // Movemos la particula hasta el tiempo actual
                newState.add(currentParticle.move(dTime));

                // Actualizamos los tiempos en columna, es decir, lo que estan atras del current
                for(int collisionId = 0; collisionId < currentParticleId; collisionId++) {
                    final double newDTime;

                    final Particle2D collisioningParticle = collisionParticle(collisionId, collisionParticle1, collisionParticle2);
                    if(collisioningParticle == null) {
                        // collisionId es una particula que colisione -> calculamos de nuevo su tiempo
                        newDTime = decrementParticlesCollisionTime(collisionId, currentParticleId, dTime);
                    } else {
                        newDTime = calculateParticleCollisionTime(collisioningParticle, newState.get(currentParticleId));
                        setParticlesCollisionTime(collisionId, currentParticleId, newDTime);
                    }

                    if(newDTime < minTime) {
                        minTime = newDTime;
                        minCollision = new Collision(minTime, collisionId, currentParticleId);
                    }
                }

                // Actualizamos la colision contra pared
                final Collision wallCollision = wallCollisions[currentParticleId];
                if(wallCollision != null) {
                    final double newDTime = wallCollision.decrementDTime(dTime);
                    if (newDTime < minTime) {
                        minTime = newDTime;
                        minCollision = wallCollision;
                    }
                }
            } else {
                // Es una particula que colisiono -> ya la calculamos
                newState.add(collisionParticle);

                // Actualizamos los tiempos en columna, es decir, lo que estan atras del current
                // Como involucran una particula que colisiono, lo recalculo
                for(int collisionId = 0; collisionId < currentParticleId; collisionId++) {
                    final double newDTime = calculateParticleCollisionTime(newState.get(collisionId), collisionParticle);
                    setParticlesCollisionTime(collisionId, currentParticleId, newDTime);
                    if(newDTime < minTime) {
                        minTime = newDTime;
                        minCollision = new Collision(minTime, collisionId, currentParticleId);
                    }
                }

                // Como es una particula que colisiono, recalculamos la colision con pared
                final Collision newWallCollision = getWallCollision(collisionParticle);
                wallCollisions[currentParticleId] = newWallCollision;
                if(newWallCollision != null && newWallCollision.getDTime() < minTime) {
                    minTime = newWallCollision.getDTime();
                    minCollision = newWallCollision;
                }
            }

            currentParticleId++;
        }

        currentTime += dTime;
        nextCollision = minCollision;

        final SimulationState ret = new SimulationState(currentTime, newState, collision);
        states.add(ret);

        return ret;
    }

    public void calculateNCollision(final int iterations) {
        for (int i = 0; i < iterations; i++) {
            calculateNextCollision();
        }
    }

    private Collision getWallCollision(final Particle2D particle) {
        final Collision collisionX = Collision.getXWallCollision(spaceWidth, particle);
        final Collision collisionY = Collision.getYWallCollision(spaceWidth, particle);

        if(collisionX == null && collisionY == null) {
            return null;
        } else if(collisionX == null) {
            return collisionY;
        } else if(collisionY == null) {
            return collisionX;
        }

        return collisionX.getDTime() < collisionY.getDTime() ? collisionX : collisionY;
    }

    private Collision calculateInitialWallCollisions(final List<Particle2D> initialState) {
        double minTime = Double.MAX_VALUE;
        Collision minCollision = null;

        int particleCount = 0;
        for(final Particle2D particle : initialState) {
            final Collision collision = getWallCollision(particle);
            wallCollisions[particleCount] = collision;

            if(collision != null && collision.getDTime() < minTime) {
                if(collision.getDTime() < 0) {
                    System.out.println(collision);
                }
                minTime = collision.getDTime();
                minCollision = collision;
            }

            particleCount++;
        }

        return minCollision;
    }

    /** Calculamos la colision entre cada particula y todas las que tiene adelante */
    private Collision calculateInitialParticleCollisions(final List<Particle2D> initialState) {
        double minTime = Double.MAX_VALUE;
        Collision minCollision = new Collision(minTime, 0, 0); // Valor de mentira

        final int particleCount = initialState.size();
        int p1Id = 0;
        for(final Particle2D p1 : initialState) {
            if(p1Id + 1 < particleCount) {
                int p2Id = p1Id + 1;
                final ListIterator<Particle2D> p2Iter = initialState.listIterator(p2Id);
                while(p2Iter.hasNext()) {
                    final Particle2D p2 = p2Iter.next();

                    final double collisionTime = calculateParticleCollisionTime(p1, p2);
                    setParticlesCollisionTime(p1Id, p2Id, collisionTime);

                    if(collisionTime < minTime) {
                        minTime = collisionTime;
                        minCollision = new Collision(minTime, p1Id, p2Id);
                    }

                    p2Id++;
                }
            }
            p1Id++;
        }

        return minCollision;
    }

    private static double calculateParticleCollisionTime(final Particle2D p1, final Particle2D p2) {
        final double sigma  = p1.getRadius() + p2.getRadius();
        final double dx     = p1.getX() - p2.getX();
        final double dy     = p1.getY() - p2.getY();
        final double dvx    = p1.getVelocityX() - p2.getVelocityX();
        final double dvy    = p1.getVelocityY() - p2.getVelocityY();

        final double dvr = dvx*dx + dvy*dy;
        if(dvr >= 0) {
            return Double.MAX_VALUE;
        }

        final double drr = dx*dx + dy*dy;
        final double dvv = dvx*dvx + dvy*dvy;

        final double d = dvr*dvr - dvv*(drr - sigma*sigma);
        if(d < 0) {
            return Double.MAX_VALUE;
        }

        return - (dvr + Math.sqrt(d)) / dvv;
    }

    // Utilidad
    private static Particle2D collisionParticle(final int particleId, final Particle2D p1, final Particle2D p2) {
        final Particle2D ret;
        if(p1.getId() == particleId) {
            ret = p1;
        } else if(p2 != null && p2.getId() == particleId) {
            ret = p2;
        } else {
            ret = null;
        }
        return ret;
    }

    /* ---------------------- Particle Collision Matrix Management -------------------- */

    private int particleCollisionsSize(final int particleCount) {
        return upperMatrixListIdxMapping(0, particleCount);
    }

    private int upperMatrixListIdxMapping(final int row, final int col) {
        return (col*col - col) / 2 + row;
    }

    private double getParticlesCollisionTime(final int particle1, final int particle2) {
        final double ret;
        if(particle1 < particle2) {
            ret = particleCollisions[upperMatrixListIdxMapping(particle1, particle2)];
        } else if(particle2 < particle1) {
            ret = particleCollisions[upperMatrixListIdxMapping(particle2, particle1)];
        } else {
            throw new ArrayIndexOutOfBoundsException("A particle doesn't have a collision time with itself");
        }
        return ret;
    }

    private void setParticlesCollisionTime(final int particle1, final int particle2, final double timeForCollision) {
        if(particle1 < particle2) {
            particleCollisions[upperMatrixListIdxMapping(particle1, particle2)] = timeForCollision;
        } else if(particle2 < particle1) {
            particleCollisions[upperMatrixListIdxMapping(particle2, particle1)] = timeForCollision;
        } else {
            throw new ArrayIndexOutOfBoundsException("A particle doesn't have a collision time with itself");
        }
    }

    private double decrementParticlesCollisionTime(final int particle1, final int particle2, final double timeToDecrement) {
        final double oldValue;
        if(particle1 < particle2) {
            oldValue = particleCollisions[upperMatrixListIdxMapping(particle1, particle2)];
            particleCollisions[upperMatrixListIdxMapping(particle1, particle2)] -= timeToDecrement;
        } else if(particle2 < particle1) {
            oldValue = particleCollisions[upperMatrixListIdxMapping(particle2, particle1)];
            particleCollisions[upperMatrixListIdxMapping(particle2, particle1)] -= timeToDecrement;
        } else {
            throw new ArrayIndexOutOfBoundsException("A particle doesn't have a collision time with itself");
        }
        return oldValue - timeToDecrement;
    }

    /* ------------------------------- Auxiliary Classes ----------------------------- */

    public enum Wall {
        UP {
            @Override
            public double updateVelocityX(final double velocityX) {
                return velocityX;
            }

            @Override
            public double updateVelocityY(final double velocityY) {
                if(velocityY < 0) {
                    throw new IllegalStateException("Particle cannot collision with upper wall with negative y velocity");
                }
                return -velocityY;
            }
        },
        DOWN {
            @Override
            public double updateVelocityX(final double velocityX) {
                return velocityX;
            }

            @Override
            public double updateVelocityY(final double velocityY) {
                if(velocityY > 0) {
                    throw new IllegalStateException("Particle cannot collision with lower wall with positive y velocity");
                }
                return -velocityY;
            }
        },
        LEFT {
            @Override
            public double updateVelocityX(final double velocityX) {
                if(velocityX > 0) {
                    throw new IllegalStateException("Particle cannot collision with left wall with positive x velocity");
                }
                return -velocityX;
            }

            @Override
            public double updateVelocityY(final double velocityY) {
                return velocityY;
            }
        },
        RIGHT {
            @Override
            public double updateVelocityX(final double velocityX) {
                if(velocityX < 0) {
                    throw new IllegalStateException("Particle cannot collision with left wall with negative x velocity");
                }
                return -velocityX;
            }

            @Override
            public double updateVelocityY(final double velocityY) {
                return velocityY;
            }
        },
        ;

        public abstract double updateVelocityX(final double velocityX);
        public abstract double updateVelocityY(final double velocityY);
    }

    @Value
    public static class Collision {
        @NonFinal
        double dTime;
        int particle1;

        // Only one of this is used, not both
        Integer particle2;
        Wall wall;

        public Collision(final double dTime, final int particle1, final int particle2) {
            this.dTime      = dTime;
            this.particle1  = particle1;
            this.particle2  = particle2;
            this.wall       = null;
        }

        public Collision(final double dTime, final int particle1, final Wall wall) {
            this.dTime      = dTime;
            this.particle1  = particle1;
            this.wall       = wall;
            this.particle2  = null;
        }

        // Permitimos esta mutacion para no tener que crear un objeto cada vez que actualizamos el dTime de las wall collisions
        private double decrementDTime(final double timeToDecrement) {
            dTime -= timeToDecrement;
            return dTime;
        }

        public boolean isWallCollision() {
            return wall != null;
        }

        public boolean isParticleCollision() {
            return particle2 != null;
        }

        /**
         * Retorna una lista con las nuevas particulas luego de la colision, en el orden establecido por la colision
         * En el caso de ser una colision entre particulas, retorna 2 elementos. En caso de ser contra una pared, solo 1.
         */
        public List<Particle2D> computeCollision(final List<Particle2D> lastState, final double spaceWidth) {
            final Particle2D p1 = lastState.get(particle1);
            final List<Particle2D> ret;

            if(wall != null) {
                final double vx = wall.updateVelocityX(p1.getVelocityX());
                final double vy = wall.updateVelocityY(p1.getVelocityY());

                double massagedDTime = dTime;
                Particle2D newP1 = p1.moveCartesian(massagedDTime, vx, vy);
                double x = newP1.getX();
                double y = newP1.getY();
                double r = newP1.getRadius();

                // Nos aseguramos que no traspase la pared
//                while(
//                    x - r <  Double.MIN_VALUE   ||
//                    x + r >= spaceWidth         ||
//                    y - r <  Double.MIN_VALUE   ||
//                    y + r >= spaceWidth
//                ) {
//                    massagedDTime -= EPSILON;
//                    newP1 = p1.moveCartesian(massagedDTime, vx, vy);
//                    x = newP1.getX();
//                    y = newP1.getY();
//                    r = newP1.getRadius();
//                }

                dTime = massagedDTime;

                ret = List.of(newP1);
            } else if(particle2 != null) {
                // Particle collision
                final Particle2D p2 = lastState.get(particle2);

                final double vx1    = p1.getVelocityX();
                final double vx2    = p2.getVelocityX();
                final double vy1    = p1.getVelocityY();
                final double vy2    = p2.getVelocityY();
                final double m1     = p1.getMass();
                final double m2     = p2.getMass();

                final double sigma  = p1.getRadius() + p2.getRadius();
                final double dx     = p1.getNextX(dTime) - p2.getNextX(dTime);
                final double dy     = p1.getNextY(dTime) - p2.getNextY(dTime);
                final double dvx    = vx1 - vx2;
                final double dvy    = vy1 - vy2;

                final double dvr = dvx*dx + dvy*dy;

                final double J  = (2 * m1 * m2 * dvr) / (sigma * (m1 + m2));
                final double Jx = (J * dx) / sigma;
                final double Jy = (J * dy) / sigma;

                final double vfx1 = vx1 - Jx / m1;
                final double vfy1 = vy1 - Jy / m1;
                final double vfx2 = vx2 + Jx / m2;
                final double vfy2 = vy2 + Jy / m2;

                Particle2D newP1 = p1.moveCartesian(dTime, vfx1, vfy1);
                Particle2D newP2 = p2.moveCartesian(dTime, vfx2, vfy2);

                ret = List.of(newP1, newP2);
            } else {
                throw new IllegalStateException();
            }

            return ret;
        }

        public static Collision getXWallCollision(final double spaceWidth, final Particle2D particle) {
            final double vx = particle.getVelocityX();

            final Collision ret;
            if (vx == 0) {
                ret = null;
            } else if(vx > 0) {
                ret = new Collision((spaceWidth - particle.getRadius() - particle.getX()) / vx, particle.getId(), Wall.RIGHT);
            } else {
                ret = new Collision((particle.getRadius() - particle.getX()) / vx, particle.getId(), Wall.LEFT);
            }

            return ret;
        }

        public static Collision getYWallCollision(final double spaceWidth, final Particle2D particle) {
            final double vy = particle.getVelocityY();

            final Collision ret;
            if (vy == 0) {
                ret = null;
            } else if(vy > 0) {
                ret = new Collision((spaceWidth - particle.getRadius() - particle.getY()) / vy, particle.getId(), Wall.UP);
            } else {
                ret = new Collision((particle.getRadius() - particle.getY()) / vy, particle.getId(), Wall.DOWN);
            }

            return ret;
        }
    }

    @Value
    public static class SimulationState {
        double              time;
        List<Particle2D>    particles;
        Collision           collision;
    }
}
