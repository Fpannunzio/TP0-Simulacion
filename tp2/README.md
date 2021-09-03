# TP2-Simulacion

## Autores

- [Brandy, Tobias](https://github.com/tobiasbrandy)
- [Pannunzio, Faustino](https://github.com/Fpannunzio)
- [Sagues, Ignacio](https://github.com/isagues)

## Dependencias

- Python 3.6+
- Java 11+
- Numpy
- MatplotLib

## Ejecucion
  
Para correr el trabajo practico en su version mas simple basta con usar el script run.sh.

Para esto hay que recibir dos archivos, el primero de generacion de particulas y el segundo de configuracion.

Comando de ejemplo:

`./run.sh config/particle_gen_conf_example.json config/off_lattice_conf_example.json`

Para hacer el grafico de va en funcion de las iteraciones hay que correr `va_vs_iteration.sh` utilizando como parametro el mismo archivo de configuracion que se uso para la simulacion, en este ejemlo seria `va_vs_iteration.sh config/off_lattice_conf_example.json`.

## Configuracion

Para la configuracion del trabajo los parametros se recibiran en un archivo [JSON](https://www.json.org/) los cuales deben contener la siguiente configuracion.

    Generacion de particulas:
    "spaceWidth": Lado de la grilla,
    "particleCount": Cantidad de particulas,
    "periodicBorder": Contorno periodico: true or false,
    "minVelocity": Valor minimo de la velocidad,
    "maxVelocity": Valor maximo de la velocidad,
    "minRadius": Radio minimo,
    "maxRadius": Radio maximo,
    "outputFile": Archivo donde se guardaran las particulas generadas

    Configuracion del sistema:
    "spaceWidth": Lado de la grilla,
    "actionRadius": Radio de interaccion,
    "noise": Coeficiente de ruido,
    "periodicBorder": Contorno periodico: true or false,
    "endCondition": {
        "type": Tipo de condicion de corte: step or stableVa,
        Caso step:
            "endStep": Cantidad de pasos
            "validRangeStart": Cantidad de pasos a partir del cual se tomara en cuenta durante el analisis

    Caso stableVa:
        "targetSTD": Desvio estandar 
        "window": Ultimas N particulas que seran tomadas en cuenta para calcular el desvio estandar
  },
  "particlesFile": "particles/off_lattice_gen.json", Archivo del cual levantara las particulas generadas
  "outputFile": "output/off_lattice_out.json", Archivo al cual se persistiran los estados de la simulacion