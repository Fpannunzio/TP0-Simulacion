package ar.edu.itba.simulacion.particle.neighbours;

import ar.edu.itba.simulacion.particle.Particle2D;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CellIndexMethod {

    public static final int DYNAMIC_M = -1;

    /** De ser M = DYNAMIC_M, tomamos el valor optimo */
    private       int       M;
    private final double    L;
    private final double    actionRadius;
    private final boolean   periodicOutline;
    private final double    offsetX;
    private final double    offsetY;

    private       double    cellLength;

    public static int optimalM(final double L, final double actionRadius, final double maxRadius) {
        return (int) (L / (actionRadius + 2 * maxRadius));
    }

    public CellIndexMethod(final double L, final double offsetX, final double offsetY, final double actionRadius, final boolean periodicOutline) {
        this(DYNAMIC_M, L, offsetX, offsetY, actionRadius, periodicOutline);
    }

    public CellIndexMethod(final double L, final double actionRadius, final boolean periodicOutline) {
        this(DYNAMIC_M, L, actionRadius, periodicOutline);
    }

    public CellIndexMethod(final int M, final double L, final double actionRadius, final boolean periodicOutline) {
        this(M, L, 0, 0, actionRadius, periodicOutline);
    }

    public CellIndexMethod(final int M, final double L, final double offsetX, final double offsetY, final double actionRadius, final boolean periodicOutline) {
        if(actionRadius < 0) {
            throw new IllegalArgumentException("Action radius must not be negative");
        }
        if(M != DYNAMIC_M && actionRadius > 0) {
            final int maxMValue = (int) (L / actionRadius);
            if(maxMValue < M) {
                throw new IllegalArgumentException("L to M ratio is too small. Max possible value for M is " + maxMValue);
            }
        }

        if(periodicOutline && (offsetX != 0 || offsetY != 0)) {
            // TODO(tobi): Soportarlo
            throw new IllegalArgumentException("Periodic outline with offsets is not yet supported. Sorry!");
        }

        this.M                  = M;
        this.L                  = L;
        this.actionRadius       = actionRadius;
        this.periodicOutline    = periodicOutline;
        this.cellLength         = this.L / this.M;
        this.offsetX            = offsetX;
        this.offsetY            = offsetY;
    }

    private int particleXToCell(final double x) {
        return (int) ((x - offsetX) / cellLength);
    }

    private int particleYToCell(final double y) {
        return (int) ((y - offsetY) / cellLength);
    }

    private static final Comparator<Particle2D> MAX_RADIUS_COMPARATOR = Comparator.comparing(Particle2D::getRadius);

    public Map<Integer, Set<Particle2D>> calculateNeighbours(final List<Particle2D> particles) {
        final boolean dynamic = M == DYNAMIC_M;

        final double maxRadius = particles
            .stream()
            .max(MAX_RADIUS_COMPARATOR)
            .map(Particle2D::getRadius)
            .orElseThrow(() -> new IllegalArgumentException("No particles were supplied"))
            ;

        final int optimalM = optimalM(L, actionRadius, maxRadius);
        if(dynamic) {
            M = optimalM;
            cellLength = L / M;
        } else {
            if(optimalM < M) {
                throw new IllegalArgumentException("L to M ratio is too small. Max possible value for M is " + optimalM);
            }
        }

        final Map<Integer, Set<Particle2D>> ret = new HashMap<>(particles.size());

        // Inicializamos mapa de respuesta
        for(final Particle2D particle : particles) {
            ret.put(particle.getId(), new HashSet<>());
        }

        // Inicializamos celdas
        final List<Particle2D>[][] cells = buildCells(particles);

        for(final List<Particle2D>[] cellRows : cells) {
            for(final List<Particle2D> cellValues : cellRows) {
                for(final Particle2D particle : cellValues) {
                    // Agregamos las particulas de la misma celda
                    addNeighbours(particle, cellValues, ret);

                    // Agregamos las particulas de las celdas vecinas
                    listCellNeighbours(particle, (cellX, cellY) -> addNeighbours(particle, cells[cellX][cellY], ret));
                }
            }
        }

        if(dynamic) {
            M = DYNAMIC_M;
        }

        return ret;
    }

    private List<Particle2D>[][] buildCells(final List<Particle2D> particles) {
        @SuppressWarnings("unchecked")
        final List<Particle2D>[][] ret = new List[M][M];

        // Inicializamos todas las celdas en el mapa, asignandoles un id unico segun su posicion
        for(int x = 0; x < M; x++) {
            for(int y = 0; y < M; y++) {
                ret[x][y] = new LinkedList<>();
            }
        }

        // Distribuimos las particulas en la celda correspondiente
        for(final Particle2D particle : particles) {
            ret[particleXToCell(particle.getX())][particleYToCell(particle.getY())].add(particle);
        }

        return ret;
    }

    private void addNeighbours(final Particle2D particle, final List<Particle2D> potentialNeighbours, final Map<Integer, ? extends Collection<Particle2D>> cellsNeighbours) {
        final Collection<Particle2D> currentNeighbours = cellsNeighbours.get(particle.getId());

        for(final Particle2D neighbour: potentialNeighbours) {
            if(!particle.equals(neighbour) && !currentNeighbours.contains(neighbour) && particle.distanceTo(neighbour, L, periodicOutline) < actionRadius) {
                currentNeighbours.add(neighbour);
                cellsNeighbours.get(neighbour.getId()).add(particle);
            }
        }
    }

    // Como optimizacion, solo listamos la mitad de los vecinos
    // Como todos listan la misma mitad, todos terminan siendo listados
    private void listCellNeighbours(final Particle2D particle, final CoordinateConsumer consumer) {
        final int cellX = particleXToCell(particle.getX());
        final int cellY = particleYToCell(particle.getY());

        // Top
        if(periodicOutline || cellY + 1 < M) {
            consumer.accept(cellX, Math.floorMod(cellY + 1, M));
        }
        // Top-Right
        if(periodicOutline || (cellX + 1 < M && cellY + 1 < M)) {
            consumer.accept(Math.floorMod(cellX + 1, M), Math.floorMod(cellY + 1, M));
        }
        // Right
        if(periodicOutline || cellX + 1 < M) {
            consumer.accept(Math.floorMod(cellX + 1, M), cellY);
        }
        // Bottom-Right
        if (periodicOutline || (cellX + 1 < M && cellY - 1 < 0)) {
            consumer.accept(Math.floorMod(cellX + 1, M), Math.floorMod(cellY - 1, M));
        }
    }

    @FunctionalInterface
    private interface CoordinateConsumer {
        void accept(final int a, final int b);
    }
}