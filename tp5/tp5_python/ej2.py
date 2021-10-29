from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from matplotlib import pyplot as plt
from matplotlib import cm
from typing import Any, Dict, List, Union

from formater import MathTextSciFormatter

@dataclass
@from_dict
class RoundSummary:         
    timesByParticle:               List[List[float]]

def parse_state(data: Dict[str, Any]) -> RoundSummary:
    
    return RoundSummary.from_dict(data)

def main(data_path):
    with open(data_path, 'r') as particles_fd:
        rounds: RoundSummary = json.load(particles_fd, object_hook=parse_state)

    times = list(map(lambda e: list(map(lambda p: p, e)), rounds.timesByParticle))
    particleCount = len(times)
    meanStd = calcMeanStd(times)

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)

    ax.tick_params(labelsize=16)

    ax.set_xlabel(r'$t$ (s)', size=20)
    ax.set_ylabel(r'n (t) (1/s)', size=20)

    ax.errorbar(
        range(particleCount)
        , meanStd[:,0]
        , xerr=meanStd[:,1]
        , color=cm.get_cmap('tab10')(0)
        , capsize=2
    )
    
    plt.legend(fontsize=14)
    plt.show()

def calcMeanStd(times):

    iterations = len(times)
    meanStd = np.empty((iterations, 2))
    
    for i in range(iterations):
        meanStd[i][0] = np.mean(times[i])
        meanStd[i][1] = np.std(times[i])

    return meanStd

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
