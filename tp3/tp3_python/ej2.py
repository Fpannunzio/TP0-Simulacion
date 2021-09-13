from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from typing import Any, Dict, List, Union
from matplotlib import pyplot as plt
from models import Collision, Config, Particle, SimulationState, Wall
from plot import Plotter

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


def main():
    with open("output/ej2-out.json", 'r') as particles_fd:
        states: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    lastThirdValues = list(map(lambda state: np.array(list(map(lambda r: r.lastThirdVelocities, state))), states.roundsList))
    initialVelocities = list(map(lambda state: np.array(list(map(lambda r: r.initialVelocities, state))), states.roundsList))
    particleCounts = list(map(lambda state: state[0].particleCount, states.roundsList))
    rounds = np.size(lastThirdValues[0], 0)
    iterations = np.size(lastThirdValues[0], 1)
    
    for i in range(len(particleCounts)):
        allInitials: np.ndarray = initialVelocities[i].flatten() 
        allThird: np.ndarray = lastThirdValues[i].flatten() 

        fig = plt.figure(i, figsize=(10, 10))
        ax = fig.add_subplot(1, 1, 1)
        ax.hist(allThird , bins=20, range=(0, 2), density=True)
        ax.hist(allInitials, bins=20, alpha=0.3, range=(0, 2), density=True)
        ax.set_title(f'N={particleCounts[i]}. Rounds: {rounds}')
        ax.set_xlabel(r'$V_c$: Modulo de la velocidad', size=20)
        ax.set_ylabel(r'Densidad del intervalo', size=20)
    
    plt.show()


if __name__ == '__main__':
    # if len(sys.argv) < 2:
    #     raise ValueError('Config path must be given by argument')
        # main(sys.argv[1])
    main()
    
