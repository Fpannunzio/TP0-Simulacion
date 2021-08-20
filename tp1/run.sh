#!/bin/bash
echo "Ejecutando Simulacion 🕐️"

# Ignore illegal access warnings
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

mvn -f ../ package -DskipTests || exit

echo "Creando particulas 🕓️"

# Creamos las particulas
mvn -f ../particle exec:java -Dexec.mainClass=ar.edu.itba.simulacion.particle.ParticleGeneration -Dexec.args="$1" -q || exit

echo "Particluas creadas ✅️"

echo "Calculando particulas vecinas 🕘️"

# Calculamos vecinos
mvn -f tp1_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp1.ParticleNeighbours -Dexec.args="$2" -q || exit

echo "Vecinos calculados ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp1_python/main.py "$2" || exit

