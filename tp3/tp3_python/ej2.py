from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from typing import Any, Dict, List, Union
from matplotlib import pyplot as plt
from models import Collision, Config, Particle, SimulationState, Wall
from plot import Plotter
from matplotlib import cm

@dataclass
@from_dict
class Round:
    lastThirdVelocities:            List[float]
    initialVelocities:              List[float]
    particleCount:                  int

@dataclass
@from_dict
class RoundSummary:
    roundsList:                     List[List[Round]]

    
def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    if 'roundsList' in data:
        return RoundSummary.from_dict(data)
    elif 'lastThirdVelocities' in data:
        return Round.from_dict(data)


def main(data_path):
    with open(data_path, 'r') as particles_fd:
        states: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    lastThirdValues = list(map(lambda state: np.array(list(map(lambda r: r.lastThirdVelocities, state))), states.roundsList))
    initialVelocities = list(map(lambda state: np.array(list(map(lambda r: r.initialVelocities, state))), states.roundsList))
    particleCounts = list(map(lambda state: state[0].particleCount, states.roundsList))
    rounds = np.size(lastThirdValues[0], 0)
    # iterations = np.size(lastThirdValues[0], 1)
    
    binSize = 0.1

    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.set_title(f'Ultimo tercio vs Valores Iniciales')
    ax.set_xlabel(r'$v$: Modulo de la velocidad (m/s)', size=20)
    ax.set_ylabel(r'Probabilidad del intervalo', size=20)
    
    for i in range(len(particleCounts)):
        allInitials: np.ndarray = initialVelocities[i].flatten() 
        allThird: np.ndarray = lastThirdValues[i].flatten() 

        thirdHist, thirdBin         = np.histogram(allThird.flatten(), bins=np.arange(0, np.max(allThird.flatten()), binSize))
        initialsHist, initialsBin   = np.histogram(allInitials, bins=np.arange(0, np.max(allInitials), binSize*3))

        ax.plot(thirdBin[:-1], thirdHist / allThird.size, marker='o', color=cm.get_cmap('tab10')(i),
            label=f'N={int(particleCounts[i])}. BinSize: {binSize}, Ultimo tercio, Total Velocidades analizadas={len(lastThirdValues[i][0])}')
        ax.plot(initialsBin[:-1], initialsHist / allInitials.size, marker='o', color=cm.get_cmap('Set1')(i + 6),
            label=f'N={int(particleCounts[i])}. BinSize: {3 * binSize} Valores Iniciales')

        
    ax.legend()
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

    
