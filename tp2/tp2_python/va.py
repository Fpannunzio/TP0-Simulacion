
import json
import sys
from math import cos, hypot, sin
from typing import Any, Dict, List, Optional

import numpy as np
from matplotlib import pyplot as plt
from models import Config, Particle


def parse_config(data: Dict[str, Any]) -> Optional[Config]:
    if 'outputFile' in data:
        return Config.from_dict(data)
    else:
        return None

def main(config_path):
    with open(config_path, 'r') as config_fd:
        config: Config = json.load(config_fd, object_hook=parse_config)

    with open(config.outputFile, 'r') as particles_fd:
        off_lattice_automata_states: List[List[Particle]] = json.load(particles_fd, object_hook=lambda d: Particle.from_dict(d))

    calculate_va(off_lattice_automata_states)


def calculate_va(states):

    va: List[float] = []
    
    particleCount = len(states[0])

    for state in states:
        sum = np.sum(np.array(list(map(lambda p: [sin(p.velocityDir), cos(p.velocityDir)] , state))), axis=0)
        va.append(hypot(*sum) / particleCount)

    plt.xlabel("Iteration number")
    plt.ylabel("Average Normalizaed Velocity")

    plt.plot(list(range(len(states))), va)
    plt.show()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
