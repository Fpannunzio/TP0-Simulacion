import json
import sys
from typing import Dict, Any, Optional, List, Union

from models import Config, SimulationState, Particle, Collision


def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    if 'dTime' in data:
        return Collision.from_dict(data)
    elif 'time' in data:
        return SimulationState.from_dict(data)
    elif 'id' in data:
        return Particle.from_dict(data)

def main(config_path):
    with open(config_path, 'r') as config_fd:
        config: Config = json.load(config_fd, object_hook=lambda data: Config.from_dict(data))

    with open(config.outputFile, 'r') as particles_fd:
        states: List[SimulationState] = json.load(particles_fd, object_hook=parse_state)

    print(states)


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
