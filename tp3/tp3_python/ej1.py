from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from typing import Any, Dict, List, Union

from models import Collision, Config, Particle, SimulationState, Wall
from plot import Plotter

@dataclass
@from_dict
class RoundSummary:
    collisionTimes:         List[List[float]]
    particleCount:          int


def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    return RoundSummary.from_dict(data)

def main():
    # with open(config_path, 'r') as config_fd:
    #     config: Config = json.load(config_fd, object_hook=lambda data: Config.from_dict(data))

    with open("output/ej1-out.json", 'r') as particles_fd:
        states: List[RoundSummary] = json.load(particles_fd, object_hook=parse_state)

    values = np.array(list(map(lambda state: np.array(state.collisionTimes), states)))
    particleCounts = list(map(lambda state: state.particleCount, states))

    mean = np.mean(500 / values.sum(axis=2), axis = 1)

    print(f'Rounds: {values.shape[1]}. Iterations: {values.shape[2]}')
    for i in range(len(mean)):
        print(f'Mean for N={particleCounts[i]} is: {mean[i]}')
    # plotter: Plotter = Plotter(config.spaceWidth, config.iterations, states).plot()


if __name__ == '__main__':
    # if len(sys.argv) < 2:
    #     raise ValueError('Config path must be given by argument')
    try:
        # main(sys.argv[1])
        main()
    except KeyboardInterrupt:
        pass
