import java.io.File;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.ConvolutionalNetwork;
import org.neuroph.nnet.learning.ConvolutionalBackpropagation;

/**
 * Uses a neural net to model human mouse movement
 * @author Adam Cooper
 *
 */
public class MouseBot {
	
	//fields
	private NeuralNetwork<ConvolutionalBackpropagation> net;
	private DataSet trainingSet;
	public static final String NET_FILE_PATH = "mousebot.nnet";
	public static final int DATA_WIDTH = 2;
	private boolean trained;
	
	/**
	 * Constructor
	 * Loads the neural net from the file if there is one
	 */
	public MouseBot() {
		this.net = new ConvolutionalNetwork();
		this.trainingSet = new DataSet(DATA_WIDTH);
		this.trained = false;
	}
	
	
	
	
	/**
	 * Loads the neural net from a file if it exists
	 * @param bot The bot to load the network to
	 * @return True if nnet loaded successfully, false otherwise
	 */
	public static boolean loadNet(MouseBot bot) {
		File file = new File(NET_FILE_PATH);
		if (file.exists()) {
			bot.net = (ConvolutionalNetwork) 
					NeuralNetwork.createFromFile(NET_FILE_PATH);
			bot.trained = true;
			return true;
		} else
			return false;
	}




	/**
	 * Builds training data for this bot's neural net
	 * @param fname The file for the mouse data
	 * @return True if file exists, false otherwise
	 */
	private boolean buildTrainingData(String fname) {
		File file = new File(fname);
		if (!file.exists())
			return false;
		
		//TODO build usable input vectors into this.trainingSet
		//TODO check for improperly formatted files 
		/* (will likely come simply from exceptions thrown
		   while parsing incorrect data types) */
		return true;
	}




	/**
	 * Calls NeuralNetwork.learn using this bot's
	 * network and training set
	 */
	private void learn() {
		this.net.learn(this.trainingSet);
		this.trained = true;
	}
	
	
	
	
	/**
	 * Builds a path based on A to B information
	 * @param input Input vector to stimulate the neural net
	 * @return A path represented as an ordered array of MouseData objects
	 */
	public MouseData[] path(double... input) {
		this.net.setInput(input);
		this.net.calculate();
		@SuppressWarnings("unused")
		double[] networkOutput = this.net.getOutput();
		//TODO turn output into a valid path
		return null;
	}
	
	
	
	
	/** 
	 * Tells if this bot has been trained yet
	 */
	public boolean isTrained() {
		return this.trained;
	}
	
	
	
	
	/**
	 * Main method, used for training
	 * @param args <Mousefile>
	 */
	public static void main(String[] args) {
		MouseBot bot = new MouseBot();
		loadNet(bot);
		
		if (!bot.buildTrainingData(args[0])) {
			System.err.println("Given input file " + args[0] + 
					" does not exist or is improperly formatted.");
			System.exit(1);
		}
		
		bot.learn();
		bot.net.save(NET_FILE_PATH);
	}
	
	
	
	
	/**
	 * Class for holding together mouse data
	 */
	public class MouseData {
		
		//fields
		private int x, y, 
					waitTime;
		
		/**
		 * Constructor
		 * @param x The x coordinate
		 * @param y The y coordinate
		 * @param waitTime Time to wait until next movement
		 */
		public MouseData(int x, int y, int waitTime) {
			this.x = x;
			this.y = y;
			this.waitTime = waitTime;
		}
		
		/**
		 * Get x
		 * @return this.x
		 */
		public int x() { return this.x; }
		
		/**
		 * Get y
		 * @return this.y
		 */
		public int y() { return this.y; }
		
		/**
		 * Get wait time until next movement
		 * @return this.waitTime
		 */
		public int waitTime() { return this.waitTime; }
	}
}
