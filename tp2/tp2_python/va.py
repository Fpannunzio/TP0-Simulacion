
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
        files: Config = json.load(config_fd)

    vas = []

    for f in files:
        with open(f, 'r') as f_fd:
            vas.append(json.load(f_fd))
    
    for va in vas:
        plt.plot(list(range(len(va))), va)
    
    plt.xlabel("Iteration number")
    plt.ylabel("Average Normalizaed Velocity")
    plt.ylim([0, 1])
    plt.show()


def calculate_va(states):

    va: List[float] = []
    
    particleCount = len(states[0])

    for state in states:
        sum = np.sum(np.array(list(map(lambda p: [sin(p.velocityDir), cos(p.velocityDir)] , state))), axis=0)
        va.append(hypot(*sum) / particleCount)

    plt.xlabel("Iteration number")
    plt.ylabel("Average Normalizaed Velocity")
    plt.ylim([0, 1])


    plt.plot(list(range(len(states))), va)
    plt.show()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
