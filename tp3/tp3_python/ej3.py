from dataclasses import dataclass
import json
import sys
import numpy as np
from matplotlib import pyplot as plt
from models import from_dict
from typing import Any, Dict, List, Union

from models import Collision, Config, Particle, SimulationState, Wall
from plot import Plotter

@dataclass
@from_dict
class RoundSummary:
    states:         List[List[float]]
    temp:           int


def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    return RoundSummary.from_dict(data)
    

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: List[RoundSummary] = json.load(particles_fd, object_hook=parse_state)

    positions = list(map(lambda r: np.array(r.states), rounds))
    temps = list(map(lambda state: state.temp, rounds))

    iterations = positions[0].shape[0]

    for i in range(len(temps)):

        fig = plt.figure(i, figsize=(10, 10))
        ax = fig.add_subplot(1, 1, 1)
        ax.set_xlim([0, 6])
        ax.set_ylim([0, 6])
        ax.plot(*positions[i].T)
        start = ax.add_artist(plt.Circle((positions[i].T[0, 0],positions[i].T[1, 0]),       0.1, color='green', alpha=0.3))
        end = ax.add_artist(plt.Circle((positions[i].T[0, -1],positions[i].T[1, -1]),       0.1, color='red', alpha=0.3))
        ax.set_title(f'Temp={temps[i]}. Iterations: {iterations}')
        ax.set_yticks([])
        ax.set_xticks([])

    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
