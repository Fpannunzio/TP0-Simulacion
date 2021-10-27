#!/bin/bash
echo "Preparando simulacion 🕐️"

CONFIG=config/pedestrian_dynamics_config.json

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp4_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp5.XYZAnimation -Dexec.args="$CONFIG" -q || exit
