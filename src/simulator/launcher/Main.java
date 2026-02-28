package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.Factory;
import simulator.factories.SheepBuilder;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DinamicSupplyRegionBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;

public class Main {

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String tag;
		private String desc;

		private ExecMode(String modeTag, String modeDesc) {
			tag = modeTag;
			desc = modeDesc;
		}

		public String getTag() {
			return tag;
		}

		public String getDesc() {
			return desc;
		}
	}

	// default values for some parameters
	//
	private final static Double DEFAULT_TIME = 10.0; // in seconds
	private final static Double DEFAULT_DELTA_TIME = 0.3;

	// some attributes to stores values corresponding to command-line parameters
	//
	private static Double time = null;
	private static Double deltaTime = null;
	private static Boolean sv = false;
	
	private static String inFile = null;
	private static String outFile = null;
	private static ExecMode mode = ExecMode.BATCH;
	private static Factory<Animal> factoriaAnimal;
	private static Factory<Region> factoriaRegiones;
	private static Factory<SelectionStrategy> selectionStrategy;


	private static void parseArgs(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = buildOptions();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parseHelpOption(line, cmdLineOptions);
			parseInFileOption(line);
			parseTimeOption(line);
			parseSimpleViewerOption(line, cmdLineOptions);
			parseDeltaTimeOption(line);
			parseOutFileOption(line);


			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options buildOptions() {
		Options cmdLineOptions = new Options();

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// simple viewer
		cmdLineOptions.addOption(Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

		
		// input file
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("Initial configuration file.").build());
		
		// output file
		
		cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

		// steps
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("A real number representing the total simulation time in seconds. Default value: "
						+ DEFAULT_TIME + ".")
				.build());
		
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representing actual time, in  \n"
						+ "seconds, per simulation step. Default value: " + DEFAULT_DELTA_TIME +  ".")
				.build());
		
		

		return cmdLineOptions;
	}

	private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}
	
	private static void parseSimpleViewerOption(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("sv")) {
			sv = true;
		}
	}

	private static void parseOutFileOption(CommandLine line) throws ParseException {
		outFile = line.getOptionValue("o");
		if (mode == ExecMode.BATCH && outFile == null) {
			throw new ParseException("In batch mode an output configuration file is required");
		}
	}
	
	private static void parseInFileOption(CommandLine line) throws ParseException {
		inFile = line.getOptionValue("i");
		if (mode == ExecMode.BATCH && inFile == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parseTimeOption(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", DEFAULT_TIME.toString());
		try {
			time = Double.parseDouble(t);
			assert (time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}
	
	private static void parseDeltaTimeOption(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", DEFAULT_DELTA_TIME.toString());
		try {
			deltaTime = Double.parseDouble(dt);
			assert (deltaTime >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + dt);
		}
	}

	private static void initFactories() {  
		List<Builder<SelectionStrategy>> selectionStrategyBuilders = new ArrayList<>();  
		selectionStrategyBuilders.add(new SelectFirstBuilder());  
		selectionStrategyBuilders.add(new SelectClosestBuilder());  
		selectionStrategyBuilders.add(new SelectYoungestBuilder());
		selectionStrategy = new BuilderBasedFactory<SelectionStrategy>(selectionStrategyBuilders);
		
		List<Builder<Animal>> selectionAnimalBuilders = new ArrayList<>();  
		selectionAnimalBuilders.add(new SheepBuilder(selectionStrategy));  
		selectionAnimalBuilders.add(new WolfBuilder(selectionStrategy));  
		factoriaAnimal = new BuilderBasedFactory<Animal>(selectionAnimalBuilders);
		
		List<Builder<Region>> selectionRegionsBuilders = new ArrayList<>();  
		selectionRegionsBuilders.add(new DefaultRegionBuilder());  
		selectionRegionsBuilders.add(new DinamicSupplyRegionBuilder());  
		factoriaRegiones= new BuilderBasedFactory<Region>(selectionRegionsBuilders);
		
		
	}
	

	private static JSONObject loadJSONFile(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}


	private static void startBatchMode() throws Exception {
		InputStream is = new FileInputStream(new File(inFile));
		OutputStream out;
		if (outFile == null) {
			out = System.out;
		}
		else out =  new FileOutputStream(outFile);
		JSONObject loadFile = loadJSONFile(is);
		
		int width = loadFile.getInt("width");
		int height = loadFile.getInt("height");
		int rows = loadFile.getInt("rows");
		int cols = loadFile.getInt("cols");
		
		
		
		Simulator sim = new Simulator(cols, rows, width, height, factoriaAnimal, factoriaRegiones);
		Controller controller = new Controller(sim);
		controller.loadData(loadFile);
		controller.run(time, deltaTime, sv, out);
		out.close();

		
	}

	private static void startGUIMode() throws Exception {
		throw new UnsupportedOperationException("GUI mode is not ready yet ...");
	}

	private static void start(String[] args) throws Exception {
		initFactories();
		parseArgs(args);
		switch (mode) {
		case BATCH:
			startBatchMode();
			break;
		case GUI:
			startGUIMode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils.RAND.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}
