import sys
from dataclasses import dataclass
from typing import Any, Dict, List, Union

import numpy as np
import pandas as pd
from matplotlib import cm
from matplotlib import pyplot as plt
from pandas.core.frame import DataFrame


def main(data_path):
    data: np.ndarray = pd.read_csv(data_path, names= ['dt','time', 'energy']).values

    errors = []
    
    count = 10_000

    for i in range(data.shape[0] // count):
        errors.append(data[i*count:(i+1)*count:])

    x = np.arange(count)
    fig = plt.figure(figsize=(10, 10))
    ax = fig.add_subplot(1, 1, 1)
    # ax.set_yscale('log')
    ax.set_xlabel(r'Iteracion', size=20)
    ax.set_ylabel(r'Energia (J)', size=20)
    ax.grid(which="both")

    for i in range(len(errors)-2):
        ax.plot(errors[i][:,1], np.abs(errors[i][:,2]), label=f'd_t= {errors[i][0,0]} s:')

    plt.legend(fontsize=14)
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass
