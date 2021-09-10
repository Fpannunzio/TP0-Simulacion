package ar.edu.itba.simulacion;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
        if(args.length < 1) {
            throw new IllegalArgumentException("First argument must be config path");
        }
        final ObjectMapper mapper = new ObjectMapper();

        final VaVsDensityBenchmarkConfig config = mapper.readValue(new File(args[0]), VaVsDensityBenchmarkConfig.class);

        final Random randomGen = new Random();
        if(config.seed != null) {
            randomGen.setSeed(config.seed);
        }

        mapper.writeValue(new File(config.outputFile), doIteration(config, randomGen));
    }
