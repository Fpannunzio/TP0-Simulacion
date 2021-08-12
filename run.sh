#!/bin/bash
echo "Ejecutando Simulacion 🕐️"

# Ignore illegal access warnings
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

mvn -f tp0_java/ compile -q || exit

echo "Creando particulas... 🕓️"

# Creamos las particulas
mvn -f tp0_java/ exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp0.ParticleGeneration -Dexec.args="$1" -q || exit

echo "Particluas creadas ✅️"

echo "Calculando particulas vecinas... 🕘️"

# Calculamos vecinos
mvn -f tp0_java/ exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp0.ParticleNeighbours -Dexec.args="$2" -q || exit

echo "Vecinos calculados ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp0_python/main.py $2 || exit

