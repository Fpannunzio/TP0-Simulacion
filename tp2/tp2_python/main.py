import json
import sys
from typing import List

from models import Config, Particle
from plot import Plotter


def main(config_path):
    with open(config_path, 'r') as config_fd:
        config: Config = json.load(config_fd, object_hook=lambda d: Config.from_dict(d))

    with open(config.outputFile, 'r') as particles_fd:
        off_lattice_automata_states: List[List[Particle]] = json.load(particles_fd, object_hook=lambda d: Particle.from_dict(d))

    ani = Plotter(config.spaceWidth, config.actionRadius, config.periodicBorder, off_lattice_automata_states).plot()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

