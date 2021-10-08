from dataclasses import dataclass
import json
from functools import reduce
from os import error
import sys
import numpy as np
from matplotlib import colors, pyplot as plt
from models import from_dict
from typing import Any, Dict, List, Union
from matplotlib import cm
from models import Collision, Config, Particle, SimulationState, Wall
from plot import Plotter
from math import ceil
from formater import MathTextSciFormatter

@dataclass
@from_dict
class Summary:
    positions:          List[List[List[float]]]
    times:              List[List[float]]


def main(data_path):
    with open(data_path, 'r') as particles_fd:
        summary: Summary = json.load(particles_fd, object_hook=Summary.from_dict)

    positions   = list(map(lambda r: list(map(lambda p: np.array(p), r)), summary.positions))
    times       = list(map(lambda t: np.array(t), summary.times))


    clockStep = 8.5e-4

    (clockIndices, minClockIndex) = calcClockIndices(times, clockStep)

    # msd         = calcMsd(positions, clockIndices, minClockIndex, particle=0)
    small_p_msd = calc_msd_small_particles(positions, clockIndices, minClockIndex)

    # msdMean             = msd[:, 0]
    small_p_msd_mean    = small_p_msd[:, 0]

    clock = generateClock(0, clockStep * minClockIndex, clockStep)

    # (polys, errors)                 = regression(msdMean, clock[:msdMean.size], polyCount=10_000)
    small_p_polys, small_p_errors   = regression(small_p_msd_mean, clock[:small_p_msd_mean.size], polyCount=10_000)

    # plotMsdAndRegression(clock, msd, polys[np.argmin(errors)])
    plotMsdAndRegression(clock, small_p_msd, small_p_polys[np.argmin(small_p_errors)], small_p=True)

    # plotRegressionError(polys[:, 0], errors)
    plotRegressionError(small_p_polys[:, 0], small_p_errors, small_p=True)

    plt.show()

def regression(x, y, start=0, end=0.2, polyCount=10_000):

    polys = np.empty((polyCount,2))
    errors = np.empty(polyCount)

    polys[:,0] = np.linspace(start, end, polyCount)
    polys[:,1] = np.zeros(polyCount)

    for r in range(polyCount):
        errors[r] = np.sum((x - np.polyval(polys[r], y))**2)

    return (polys, errors)

# Desplazamiento cuadratica media
# positions[round][particleId][event]
# indices[round][event]
def calcMsd(positions, indices, minEventCount, particle=0):

    msd = np.empty((minEventCount, 2))
    tempMsd = np.empty(len(positions))
    
    for t in range(minEventCount):
        for r in range(len(positions)):
            endIndex    = indices[r][t].astype(int)
            startIndex  = indices[r][0].astype(int)

            tempMsd[r]  = np.sum((positions[r][particle][endIndex] - positions[r][particle][startIndex]) ** 2)
        
        msd[t][0] = np.mean(tempMsd)
        msd[t][1] = np.std(tempMsd)

    return msd

def calc_msd_small_particles(positions, indices, min_event_count):
    msd = np.empty((min_event_count, 2))
    
    for t in range(min_event_count):
        
        tempMsd = []
        for r in range(len(positions)):
            endIndex = indices[r][t].astype(int)
            startIndex = indices[r][0].astype(int)

            for p in positions[r][1:]:
                if len(p) > endIndex:
                    tempMsd.append(np.sum((p[endIndex] - p[startIndex]) ** 2))

        tempMsd = np.array(tempMsd)

        msd[t][0] = np.mean(tempMsd)
        msd[t][1] = np.std(tempMsd)

    return msd

def generateClock(start, end, step):
    count = ceil(end / step)
    end = count * step

    return np.linspace(start, end, count)

def calcClockIndices(times, step):
    
    clockIndices = []

    # No todos tienen la misma cantidad de iteraciones, analisamos la minima.
    minClockSize = sys.maxsize

    for ts in times:
        
        clock = generateClock(0, ts[-1], step)

        currentClockIndex = np.empty(clock.size)
        clockIndices.append(currentClockIndex)


        if clock.size < minClockSize:
            minClockSize = clock.size 

        # Determinacion de los indices representativos para cada paso del reloj
        k = 0
        for i in range(len(clock)):
            while k < ts.size - 2 and not (clock[i] >= ts[k] and  clock[i] < ts[k+1]):
                k += 1

            currentClockIndex[i] = k

    return (clockIndices, minClockSize)

def plotMsdAndRegression(clock, msd, poly, small_p=False):
    
    fig = plt.figure(f'MSD {small_p}', figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.errorbar(clock[:msd[:,0].size], msd[:,0], yerr=msd[:,1], color=cm.get_cmap('tab20c')(7), alpha=0.03, capthick=2)
    ax.plot(clock[:msd[:,0].size], np.polyval(poly, clock[:msd[:,0].size]), label=f'D={poly[0]/2:.3f}' + r'$m^2/s$', color='black')
    ax.plot(clock[:msd[:,0].size], msd[:,0], color=cm.get_cmap('tab20c')(4))

    ax.grid(which="both")
    ax.set_axisbelow(True)

    ax.set_ylabel(r'$<z^2>$: Desplazamiento cuadratico medio ($m^2$)', size=20)
    ax.set_xlabel(r'Tiempo (s)', size=20)
    ax.tick_params(labelsize=16)
    ax.legend()

def plotRegressionError(polys, errors, small_p=False):
    
    fig = plt.figure(f'Error {small_p}', figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)
    
    ax.grid(which="both")
    ax.set_axisbelow(True)
    ax.plot(polys, errors)
    
    ax.plot(polys[np.argmin(errors)], errors[np.argmin(errors)], marker='x', color='r')
    
    print(errors[np.argmin(errors)])

    ax.set_ylabel(r'Error ($m^2$)', size=20)
    ax.set_xlabel(r'Pendiente del ajuste ($m^2/s$)', size=20)
    ax.yaxis.set_major_formatter(MathTextSciFormatter("%1.1e"))
    ax.tick_params(labelsize=16)



if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
