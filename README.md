# Mining Bot
This is the collaborative work of Jonathan Carroll and Adam Cooper for the final project in *CSCI 364 Artificial Intelligence.* This is a RuneScape bot designed to bypass any bot-catching scripts by modelling all mouse movement after human behavior as accurately as possible. This is done by with a convolutional neural network implemented through the Neuroph API. The bot is written using the community RuneScape botting API *Dreambot.*

### Running the Runescape Bot
Make sure you have an updated Java Development Kit (JDK) for your system. Download the dreambot client at dreambot.org. Run the installer as a java.exe file. In order to get the local script working, download the attached Miner.jar file and place it in your \Dreambot\Scripts folder. Run the Dreambot client, open Tools > Script Panel > Local Scripts, and you should see Miner 1.0 as an option. However in order to test the bot, you would need a Runescape account and membership, as the Mining task that the bot completes has account prerequisites. 

### Running the Keylogger
#### Windows
Just make sure you have `KBMTracker.bat` and `KBMTracker.jar` in the same file run the batch script `KBMTracker.bat`. It will open the `.jar` file in a cmd window for you. 
#### OSX/Linux/UNIX/SOLARIS/Samsung Smart Refrigerator
Run `KBMTracker.jar` from the command line using the command `java -jar KBMTracker.jar <mousefile> <keyboardfile>`.
