#!/bin/bash
echo "Preparando simulacion 🕐️"

# Ignore illegal access warnings
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

mvn -f ../ clean install -DskipTests -q || exit

echo "Creando particulas 🕓️"

# Creamos las particulas
mvn -f ../particle exec:java -Dexec.mainClass=ar.edu.itba.simulacion.particle.ParticleGeneration -Dexec.args="$1" -q || exit

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Calculamos vecinos
mvn -f tp2_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp2.OffLatticeSimulation -Dexec.args="$2" -q || exit

echo "Simulacion finalizada ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp2_python/main.py "$2" || exit

