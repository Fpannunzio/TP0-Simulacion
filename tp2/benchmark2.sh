#!/bin/bash
echo "Preparando simulacion 🕐️"

# Ignore illegal access warnings
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

CONF1=config/particle_gen_conf_example.json
CONF2=config/off_lattice_benchmark2.json
CONF3=config/filardos.json

mvn -f ../ clean install -DskipTests -q || exit

echo "Creando particulas 🕓️"

# Creamos las particulas
# mvn -f ../particle exec:java -Dexec.mainClass=ar.edu.itba.simulacion.particle.ParticleGeneration -Dexec.args="$CONF1" -q|| exit

echo "Particulas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Calculamos vecinos
mvn -f tp2_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp2.VaVsStepBenchmark -Dexec.args="$CONF2" -q|| exit

echo "Simulacion finalizada ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp2_python/va.py "$CONF3" || exit

