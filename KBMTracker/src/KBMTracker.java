//import java.awt.GraphicsEnvironment;
//import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class KBMTracker {
	
	//fields
	private MouseTracker mouseThread;
	private KBTracker kbThread;
	private static final Scanner consoleIn = new Scanner(System.in);
	/** name of mouse thread **/
	//private static final String MOUSE = "MOOSE";
	/** name of KB thread **/
	//private static final String KEYBOARD = "KAYBIRD";
	/** name of directory to store tracking data **/
	private static final String DIR = "data";
	/** the key pressed to start tracking **/
	private static final char START_KEY = 's';
	/** the key pressed to stop tracking **/
	private static final char STOP_KEY = 'q';
	/** the key pressed to pause tracking **/
	private static final char PAUSE_KEY = 'p';
	/** line offset for formatting **/
	private static final String LINE_OFFSET = "----------";
	
	/**
	 * Constructor
	 * @param mouseFile The file to write mouse data to
	 * @param kbFile The file to write KB data to
	 */
	public KBMTracker(String mouseFile, String kbFile) {
		File dir = new File(DIR);
		//create data dir if it doesn't exist
		if (!dir.exists())
			dir.mkdir();
		
		//this.mouseThread = new Thread(new MouseTracker(mouseFile), MOUSE);
		//this.kbThread = new Thread(new KBTracker(kbFile), KEYBOARD);
		
		this.mouseThread = new MouseTracker(mouseFile);
		this.kbThread = new KBTracker(kbFile);
		
		setUpIO();
		
		//TODO make tracking begin automatically when RuneScape is in focus
		System.out.println("Type " + START_KEY + " to begin tracking");
		System.out.println("Type " + STOP_KEY + " to quit");
		System.out.println("Type h for help");
		
		while (true)
			runCommand(readValidInput(true, true, false));
	}
	
	
	
	
	/**
	 * Sets up console IO so the program can be run from a console window when using a jar
	 */
	private static void setUpIO() {
		// Clear previous logging configurations.
		LogManager.getLogManager().reset();

		// Get the logger for "org.jnativehook" and set the level to off.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
				
		/*Console console = System.console();
		
		if(console == null && !GraphicsEnvironment.isHeadless()){
            String filename = KBMTracker.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            System.out.println(filename);
            try {
				Runtime.getRuntime().exec(new String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
			} catch (IOException e) {
				System.out.println("Couldn't build console");
				e.getMessage();
				System.exit(1);
			}
		} else {
			System.out.println("Couldn't build console");
			System.exit(1);
		}*/
	}




	/**
	 * Ensures valid commands are passed
	 * @param start Are we looking for start
	 * @param stop Are we looking for stop
	 * @param pause Are we looking for pause
	 * @return The command given, once it's valid
	 */
	private static char readValidInput(boolean start, boolean stop, boolean pause) {
		String in = "";
		while (true) {
			in = consoleIn.nextLine().toLowerCase().trim();
			if (in.startsWith("h"))
				return 'h';
			if (in.length() != 1)
				continue;
			char cmd = in.charAt(0);
			switch(cmd) {
				case START_KEY:
					if (start)
						return cmd;
					break;
				case STOP_KEY:
					if (stop)
						return cmd;
					break;
				case PAUSE_KEY:
					if (pause)
						return cmd;
					break;
				default:
					continue;
			}
		}
	}




	private void stop() {
		this.kbThread.stop();
		this.mouseThread.stop();
		System.exit(0);
	}
	
	
	
	
	private void suspend() {
		this.kbThread.suspend();
		this.mouseThread.suspend();
	}




	/**
	 * Begins recording
	 */
	private void launch() {
		if (!GlobalScreen.isNativeHookRegistered()) {
			try {
				GlobalScreen.registerNativeHook();
			} catch (NativeHookException e) {
				System.err.println("Trouble registering the native hook!");
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		System.out.println("Tracking KBM...");
		this.mouseThread.run();
		this.kbThread.run();
		
		//TODO make tracking suspend automatically when RuneScape is out of focus
		System.out.println("Press " + PAUSE_KEY + " to pause tracking.\n");
		
		runCommand(readValidInput(false, true, true));
	}




	/**
	 * Runs a command 
	 * @param cmd The command to run
	 */
	private void runCommand(char cmd) {
		switch(cmd) {
			case START_KEY:
				launch();
				break;
			case STOP_KEY:
				stop();
				break;
			case PAUSE_KEY:
				suspend();
				break;
			case 'h':
				printHelp();
				break;
			default:
				return;
		}
	}

	


	/**
	 * Prints out a list of possible commands and what they do for the user
	 */
	private void printHelp() {
		System.out.println("Commands:");
		System.out.println(START_KEY + " Start/resume recording");
		System.out.println(STOP_KEY + " Stop recording");
		System.out.println(PAUSE_KEY + " Pause recording");
		System.out.println("h Help");
	}




	/**
	 * Writes the date, formatted as HH:mm:ss.SSS MM/dd/yyyy
	 * @param date The date to format
	 * @return A formatted date string
	 */
	public static String getFormattedTime(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS MM/dd/yyyy");
		return formatter.format(date);
	}
	
	
	
	
	private class MouseTracker implements Runnable {
		
		//fields
		protected long startTime;
		private String fileName;
		protected FileOutputStream fos;
		private MouseListener listener;
		
		/**
		 * Constructor
		 * @param fileName The file to save mouse-tracking data
		 */
		public MouseTracker(String fileName) {
			this.startTime = 0L;
			this.fileName = fileName;
			//set up output stream
			try {
				File dataFile = new File(DIR, fileName);
				if (!dataFile.exists() || dataFile.isDirectory())
					dataFile.createNewFile();
				//append to file
				this.fos = new FileOutputStream(dataFile, true);
			} catch (IOException e) {
				System.err.println("Error creating file " + fileName);
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}

		/**
		 * Stops mouse listening
		 */
		public void stop() {
			GlobalScreen.removeNativeMouseListener(this.listener);
			System.out.println("Mouse listening ended.");
			try {
				this.fos.close();
			} catch (IOException e) {
				System.err.println("Trouble closing file!");
				e.printStackTrace();
				return;
			} finally {
				System.out.println("Mouse file closed.");
			}
		}

		/**
		 * Suspends mouse listening
		 */
		public void suspend() {
			GlobalScreen.removeNativeMouseListener(this.listener);
			System.out.println("Mouse listening suspended...");
		}

		/**
		 * Starts the thread
		 */
		@Override
		public void run() {
			//get current time
			Date now = new Date();
			this.startTime = now.getTime();
			
			System.out.print(LINE_OFFSET);
			System.out.println("Beginning " + this.getClass().getName() + 
					"... (" + getFormattedTime(now) + ")");
			System.out.print(LINE_OFFSET);
			System.out.println("Saving data to " + this.fileName);
			this.track();
		}
		
		/**
		 * Begins tracking for the mouse
		 */
		protected void track() {
			this.listener = new MouseListener(this.fos, this.startTime);
			GlobalScreen.addNativeMouseMotionListener(this.listener);
		}
		
		/**
		 * Global Mouse Listener
		 */
		public class MouseListener implements NativeMouseInputListener {
			//fields
			private FileOutputStream fos;
			private long startTime;

			/**
			 * Constructor
			 * @param fos The output stream used for the mouse
			 * @param startTime The start time of tracking
			 */
			public MouseListener(FileOutputStream fos, long startTime) {
				this.fos = fos;
				this.startTime = startTime;
			}
			
			/**
			 * Writes event data to this.fos
			 * @param type The type of event
			 * @param button The button pressed
			 * @param x The x coordinate of the event
			 * @param y The y coordinate of the event
			 * @param clickCount The number of clicks associated with this event
			 */
			private void writeToFile(char type, int button, int x, int y, int clickCount) {
				StringBuilder sb = new StringBuilder();
				sb.append(type).append(' ').append(button).append(' ').
					append(x).append(' ').append(y).append(' ').append(clickCount).append(' ');
				sb.append(' ' + (System.currentTimeMillis() - startTime)).append('\n');
				
				try {
					fos.write(sb.toString().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					try {
						this.fos.flush();
						this.fos.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					System.exit(1);
				}
			}

			@Override
			public void nativeMouseClicked(NativeMouseEvent click) {
				this.writeToFile('c', click.getButton(), 
						click.getX(), click.getY(), click.getClickCount());
			}

			@Override
			public void nativeMousePressed(NativeMouseEvent press) {
				this.writeToFile('p', press.getButton(), 
						press.getX(), press.getY(), press.getClickCount());
			}

			@Override
			public void nativeMouseReleased(NativeMouseEvent release) {
				this.writeToFile('r', release.getButton(), 
						release.getX(), release.getY(), 
						release.getClickCount());
			}

			@Override
			public void nativeMouseDragged(NativeMouseEvent drag) {
				this.writeToFile('d', drag.getButton(), 
						drag.getX(), drag.getY(), drag.getClickCount());
			}

			@Override
			public void nativeMouseMoved(NativeMouseEvent move) {
				this.writeToFile('m', move.getButton(), 
						move.getX(), move.getY(), move.getClickCount());
			}
		}
	}
	
	
	
	
	private class KBTracker extends MouseTracker implements Runnable {
		
		//fields 
		private KBListener listener;
		
		/**
		 * Constructor
		 * @param fileName The file to save KB tracking data
		 */
		public KBTracker(String fileName) {
			super(fileName);
		}
		
		public void stop() {
			GlobalScreen.removeNativeKeyListener(this.listener);
			System.out.println("Keyboard listening stopped.");
			try {
				this.fos.close();
			} catch (IOException e) {
				System.err.println("Trouble closing file!");
				e.printStackTrace();
				return;
			} finally {
				System.out.println("Keyboard file closed.");
			}
		}

		public void suspend() {
			GlobalScreen.removeNativeKeyListener(this.listener);
			System.out.println("Keyboard listening suspended.");
		}

		@Override
		protected void track() {
			this.listener = new KBListener(fos, startTime);
			GlobalScreen.addNativeKeyListener(this.listener);
		}
		
		/**
		 * Global Keyboard Listener
		 */
		public class KBListener implements NativeKeyListener {
			
			//fields
			private FileOutputStream fos;
			private long startTime;
			
			
			/**
			 * Constructor
			 * @param fos The output stream to write key data to
			 * @param startTime The time at which tracking was started
			 */
			public KBListener(FileOutputStream fos, long startTime) {
				this.fos = fos;
				this.startTime = startTime;
			}

			/**
			 * Writes keypress data to the output stream
			 */
			@Override
			public void nativeKeyPressed(NativeKeyEvent keyPress) {
				this.writeToFile('p', keyPress.getKeyCode());
			}

			@Override
			public void nativeKeyReleased(NativeKeyEvent keyRelease) {
				this.writeToFile('r', keyRelease.getKeyCode());
			}

			@Override
			public void nativeKeyTyped(NativeKeyEvent keyType) {
				this.writeToFile('t', keyType.getKeyCode());
			}
			
			/**
			 * Writes keyboard event data to the keyboard tracking file
			 * @param eventType p for press, r for release, t for type
			 * @param keyCode The key used in the event
			 */
			private void writeToFile(char eventType, int keyCode) {
				StringBuilder sb = new StringBuilder();
				//append press indicator
				sb.append(eventType);
				//append pressed key
				sb.append(' ' + NativeKeyEvent.getKeyText(keyCode) + ' ');
				//time since tracking started
				sb.append(' ' + (System.currentTimeMillis() - this.startTime));
				sb.append('\n');
				
				//System.out.println(new String(sb));
				
				try {
					fos.write(sb.toString().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					try {
						this.fos.flush();
						this.fos.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					System.exit(1);
				}			
			}
		}
	}
	
	
	
	
	/**
	 * Main
	 * @param args <mouse_data_file> <kb_data_file>
	 */
	public static void main(String[] args) {
		//need 2 args for filenames
		if (args.length != 2) {
			System.err.println
				("Usage: java IOTracker <mouse_data_file> <kb_data_file>");
			System.exit(1);
		} else if (args[0].equals(args[1])) {
			System.out.println("Need two different files for data output");
			System.exit(1);
		}
		
		//TODO listen for stop key or quit
		//construct new instance
		@SuppressWarnings("unused")
		KBMTracker kbmTracker = new KBMTracker(args[0], args[1]);
	}
}
