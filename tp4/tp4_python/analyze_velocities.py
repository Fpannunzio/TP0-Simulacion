import json
import sys
from typing import List
from matplotlib import pyplot as plt
from matplotlib.ticker import AutoMinorLocator

import numpy as np
from formater import MathTextSciFormatter

from models import VelocityAnalysisInfo


def main(data_path):
    with open(data_path, 'r') as result_fd:
        result: VelocityAnalysisInfo = json.load(result_fd, object_hook=VelocityAnalysisInfo.from_dict)

    durations = np.array(result.tripDuration)
    durationCount = len(durations)
    filter = durations > 0
    
    velocities = np.linspace(result.initialVelocity, result.initialVelocity + result.velocityStep * durationCount, durationCount)[filter]
    durations = durations[filter]

    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(1, 1, 1)
    ax.yaxis.set_major_formatter(MathTextSciFormatter("%1.4e"))
    ax.yaxis.set_minor_formatter(MathTextSciFormatter("%1.5e"))
    ax.grid(which="both")
    ax.set_axisbelow(True)
    ax.yaxis.set_minor_locator(AutoMinorLocator(n = 2))

    ax.set_xlabel(r'$v_0$: Velocidad inicial de despegue (km/s)', size=20, labelpad=20)
    ax.set_ylabel(r'Tiempo de viaje hasta colisionar (min)', size=20, labelpad=20)
    ax.tick_params(labelsize=16)
    ax.tick_params(labelsize=12, which='minor')

    ax.scatter(velocities, durations / 60, marker='*', s=70)

    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
