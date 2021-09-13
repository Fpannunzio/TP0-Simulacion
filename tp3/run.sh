#!/bin/bash
echo "Preparando simulacion 🕐️"

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

# Creamos las particulas
mvn -f ../particle exec:java -Dexec.mainClass=ar.edu.itba.simulacion.particle.ParticleGeneration -Dexec.args="$1" -q || exit

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp3_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp3.BrownianMotionSimulation -Dexec.args="$2" -q || exit

echo "Simulacion finalizada ✅️"
