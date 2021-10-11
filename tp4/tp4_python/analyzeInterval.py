from dataclasses import dataclass
import json
import sys
import numpy as np
from models import from_dict
from typing import Any, Dict, List, Union
from matplotlib import pyplot as plt
from models import Collision, Config, Particle, SimulationState, Wall
from formater import MathTextSciFormatter
from datetime import date, datetime
from matplotlib import cm
import matplotlib.dates as mdates


@dataclass
@from_dict
class IterationMarsDistance:
    startIteration:     int
    startTimeEpoch:     int
    distance:           float

@dataclass
@from_dict
class IntervalAnalysis:
    distances:              List[IterationMarsDistance]
    bestDistance:           IterationMarsDistance


    
def parse_state(data: Dict[str, Any]) -> Union[Collision, SimulationState, Particle]:
    if 'startIteration' in data:
        return IterationMarsDistance.from_dict(data)
    elif 'distances' in data:
        return IntervalAnalysis.from_dict(data)


def main(data_path):
    with open(data_path, 'r') as particles_fd:
        data: IntervalAnalysis = json.load(particles_fd, object_hook=parse_state)

    distances = np.array(list(map(lambda it: (it.startIteration, it.distance, datetime.fromtimestamp(it.startTimeEpoch)), data.distances)))
    dates = list(map(lambda it: datetime.fromtimestamp(it.startTimeEpoch), data.distances))
    
    binSize = 0.1

    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1)
    ax.set_xlabel(r'Dia de despegue', size=20)
    ax.set_ylabel(r'Distancia minima a Marte (km)', size=20)
    ax.grid(which="both")
    # ax.set_yscale('log')
    ax.set_axisbelow(True)

    ax.plot(dates, distances[:,1])
    # ax.xaxis.set_major_formatter(MathTextSciFormatter("%1.2e"))

    ax.xaxis.set_major_formatter(mdates.DateFormatter('%d/%m/%Y'))
    ax.xaxis.set_major_locator(mdates.AutoDateLocator())
    # ax.xaxis.set_major_locator(mdates.DayLocator(interval=len(set(map(lambda d: (d.year, d.month, d.day), dates)))//5))

    # ax.yaxis.set_major_formatter(MathTextSciFormatter("%1.2e"))

    ax.tick_params(labelsize=16)
    # ax.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

    
