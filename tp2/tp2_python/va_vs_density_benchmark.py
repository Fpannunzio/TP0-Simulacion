import json
import math
import sys
from formater import multiple_formatter
from typing import Any, Dict, Optional, Union

import numpy as np
from matplotlib import pyplot as plt

from matplotlib import cm

from models import VaVsDensityBenchmarkResult

def main(result_path):
    with open(result_path, 'r') as particles_fd:
        benchmark_result: VaVsDensityBenchmarkResult = json.load(particles_fd, object_hook=lambda d: VaVsDensityBenchmarkResult.from_dict(d))

    fig2 = plt.figure(figsize=(16, 8))
    axis = fig2.add_subplot(1, 1, 1)

    axis.errorbar(
        benchmark_result.density
        , benchmark_result.vaMean
        , yerr=benchmark_result.vaStd
        , color=cm.get_cmap('tab10')(0)
        , capsize=2
    )

    axis.set_xlabel(r'$\rho$: Density', size=20)
    axis.set_ylabel(r'$v_a$: Average Normalized Velocity', size=20)

    axis.grid()
    axis.legend()

    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

