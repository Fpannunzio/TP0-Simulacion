import math
from typing import List, Dict, Optional

import matplotlib.pyplot as plt

from models import Particle

class Plotter:

    def __init__(self, particles: List[Particle], initial_particle: int, action_radius: float, neighbours_dict: Dict[str, List[int]], M: int, L: float, periodic_outline: bool) -> None:
        self.particles: List[Particle] = particles
        self.selected_particle: int = initial_particle
        self.action_radius: float = action_radius
        self.neighbours_dict: Dict[str, List[int]] = neighbours_dict
        self.M: int = M
        self.L: float = L
        self.periodic_outline: bool = periodic_outline

        # Plot init
        figure, self.axes = plt.subplots()
        self.axes.set_aspect(1)

    def is_selected(self, particle: Particle):
        return particle.id == self.selected_particle

    def is_selected_neighbour(self, particle: Particle):
        return particle.id in self.neighbours_dict[str(self.selected_particle)]

    def get_circle_color(self, particle: Particle) -> str:
        if self.is_selected(particle):
            return 'red'
        elif self.is_selected_neighbour(particle):
            return 'green'
        else:
            return 'purple'

    def build_particle_circle(self, particle: Particle, axes) -> None:
        draw_particle_radius = plt.Circle((particle.x, particle.y), particle.radius, color=(self.get_circle_color(particle)), alpha=0.5, picker=True)
        draw_particle_radius.set_label(particle.id)
        axes.add_artist(draw_particle_radius)

    def build_interaction_radius(self, particle: Particle, axes) -> None:
        draw_interaction_radius = plt.Circle((particle.x, particle.y), self.action_radius + particle.radius, fill=False)
        axes.add_artist(draw_interaction_radius)

    def get_particle_of_coordinates(self, x: float, y: float) -> Optional[Particle]:
        for p in self.particles:
            if math.isclose(x, p.x) and math.isclose(y, p.y):
                return p
        return None

    def on_circle_click(self, event):
        circle = event.artist
        selected_particle: Optional[Particle] = self.get_particle_of_coordinates(*circle.get_center())
        if selected_particle is not None:
            self.selected_particle = selected_particle.id
            self.axes.cla()
            self.build_plot()
            plt.draw()

    def plot(self):
        self.build_plot()
        plt.connect('pick_event', lambda event: self.on_circle_click(event))
        plt.show()

    def build_plot(self):
        for particle in self.particles:
            self.plot_particle(particle, self.axes)

        particles_x: List[int] = list(map(lambda p: p.x, self.particles))
        particles_y: List[int] = list(map(lambda p: p.y, self.particles))
        particles_r: List[int] = list(map(lambda p: p.radius, self.particles))
        particles_color: List[str] = list(map(lambda p: self.get_circle_color(p), self.particles))

        # Centros
        plt.scatter(particles_x, particles_y, s=particles_r, color=particles_color, alpha=0.5)

        ticks = [self.M * i for i in range(int(self.L / self.M) + 1)]
        self.axes.set_xticks(ticks)
        self.axes.set_yticks(ticks)

        plt.title('Cell Index')

        plt.xlim([0, self.L])
        plt.ylim([0, self.L])

        plt.grid()

    def plot_particle(self, particle: Particle, axes):
        # Ploteamos el circulo principal
        self.build_particle_circle(particle, axes)

        if self.is_selected(particle):
            self.build_interaction_radius(particle, axes)
            self.axes.annotate(particle.id, xy=(particle.x, particle.y), xycoords="data", va="center", ha="center", bbox=dict(boxstyle="round", fc="yellow"))

        if not self.periodic_outline:
            # Si no hay periodic outline, no hacemos nada especial
            return

        x: float = particle.x
        y: float = particle.y
        r: float = particle.radius
        ar: float = self.action_radius

        def build_particle_circle(x, y):
            self.build_particle_circle(Particle(particle.id, x, y, particle.radius), axes)
            
        # Derecha
        if x + r > self.L:
            build_particle_circle(-x, y)
        # Izquierda
        elif x - r < 0:
            build_particle_circle(self.L + x, y)

        # Arriba
        if y + r > self.L:
            build_particle_circle(x, -y)
        # Abajo
        elif y - r < 0:
            build_particle_circle(x, self.L + y)
    
        if self.is_selected(particle):
            def build_interaction_radius(x, y):
                self.build_interaction_radius(Particle(particle.id, x, y, particle.radius), axes)

            # Derecha
            if x + r + ar > self.L:
                build_interaction_radius(-x, y)
            # Izquierda
            elif x - r - ar < 0:
                build_interaction_radius(self.L + x, y)

            # Arriba
            if y + r + ar > self.L:
                build_interaction_radius(x, -y)
            # Abajo
            elif y - r - ar < 0:
                build_interaction_radius(x, self.L + y)
