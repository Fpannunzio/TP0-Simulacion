from typing import List

from matplotlib import pyplot as plt

from models import Particle


class Plotter:

    def __init__(self, space_width: float, action_radius: float, periodic_border: bool, states: List[List[Particle]]) -> None:
        self.space_width = space_width
        self.action_radius = action_radius
        self.periodic_border = periodic_border
        self.states: List[List[Particle]] = states

        # Plot init
        figure, self.axes = plt.subplots()
        self.axes.set_aspect(1)

    def plot(self):
        # Graficamos el estado inicial como test
        first_state: List[Particle] = self.states[0]
        first_state_x: List[float] = list(map(lambda p: p.x, first_state))
        first_state_y: List[float] = list(map(lambda p: p.y, first_state))

        plt.scatter(first_state_x, first_state_y, s=self.space_width / 100, color='blue', alpha=0.5)

        plt.show()
