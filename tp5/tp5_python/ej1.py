from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from matplotlib import pyplot as plt
from matplotlib import cm
from typing import Any, Dict, List, Union


@dataclass
@from_dict
class EscapeTime:
    time:                       float
    escapeCount:                int             

@dataclass
@from_dict
class RoundSummary:           
    escapesByRun:               List[List[EscapeTime]]

def parse_state(data: Dict[str, Any]) -> Union[RoundSummary, EscapeTime]:
    if 'escapesByRun' in data:
        return RoundSummary.from_dict(data)
    elif 'time' in data:
        return EscapeTime.from_dict(data)

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    times = list(map(lambda e: list(map(lambda p: p.time, e)), rounds.escapesByRun))
    freedParticles = list(map(lambda e: list(map(lambda p: p.escapeCount, e)), rounds.escapesByRun))

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)

    ax.set_xlabel(r'$t$ (s)', size=20)
    ax.set_ylabel(r'descarga(t) (1/s)', size=20)


    for i in range(len(times)): 
        ax.scatter(times[i], freedParticles[i], marker='o', color=cm.get_cmap('tab20c')(i))## TODO ponemos la seed?
        # ax.plot(times[i], freedParticles[i], label=f'n(t) iteracion {i}', color=cm.get_cmap('tab20c')(i))
    
    ax.grid(which="both")
    ax.set_axisbelow(True)
    #plt.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
