package es.ugr.sci2s.soccer.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import calibration.CalibrationConsole;
import calibration.CalibrationController;
import calibration.CalibrationParameter;
import calibration.EcjInterface;
import calibration.ecj.EcjModelEvaluation;
import ec.EvolutionState;
import ec.Evolve;
import ec.eval.MasterProblem;
import ec.eval.Slave;
import ec.util.MersenneTwisterFast;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import ec.util.Version;

/**
 * Hello world!
 *
 */
public class CustomSlave extends Slave 
{
	private boolean running;
	
	public CustomSlave() {
		running=true;
	}
	
	public void stop() {
		running = false;
	}
	
	public static void main(String[] args)
    {
    EvolutionState state = null;
    ParameterDatabase parameters = null;
    Output output = null;
            
    // 0. find the parameter database
	    try
	        {
	        parameters = new ParameterDatabase(
	            // not available in jdk1.1: new File(args[x+1]).getAbsoluteFile(),
	            new File(new File(args[0]).getAbsolutePath()),
	            args);
	                                    
	        // add the fact that I am a slave:      eval.i-am-slave = true
	        // this is used only by the Evaluator to determine whether to use the MasterProblem
	        parameters.set(new Parameter(ec.EvolutionState.P_EVALUATOR).push(ec.Evaluator.P_IAMSLAVE), "true");
	        }
	    catch(FileNotFoundException e)
	        { 
	        Output.initialError(
	            "A File Not Found Exception was generated upon" +
	            "reading the parameter file \"" + args[0] + 
	            "\".\nHere it is:\n" + e); }
	    catch(IOException e)
	        { 
	        Output.initialError(
	            "An IO Exception was generated upon reading the" +
	            "parameter file \"" + args[0] +
	            "\".\nHere it is:\n" + e); 
	        } 
    if (parameters == null)
        Output.initialError("No parameter file was specified." );
    
    //Calibration controller
    
    CalibrationController controller = CalibrationConsole.getCalibrationController(args[1]);
    
    // Number of parameters    
    // Calibration parameters manager
    int numParams = 0;
    
    List<CalibrationParameter> calibrationParams = 
    		controller.getTask().getCalibrationParametersManager().getParameters();
    
    numParams = calibrationParams.size();
    parameters.set(new Parameter("pop.subpop.0.species.genome-size"),String.valueOf(numParams));
    
    // Set the min-max values for each parameter
	for(int i=0; i < numParams; i++) {
		CalibrationParameter calibParam = calibrationParams.get(i);
		String paramName = "pop.subpop.0.species.min-gene." + String.valueOf(i);
		parameters.set(new ec.util.Parameter(paramName), String.valueOf(calibParam.minValue));
		
		paramName = "pop.subpop.0.species.max-gene." + String.valueOf(i);
		parameters.set(new ec.util.Parameter(paramName), String.valueOf(calibParam.maxValue));
	}
	
	EcjInterface ecj = new EcjInterface(controller,
			CalibrationConsole.NO_MASTER_HOST, CalibrationConsole.NO_MASTER_PORT);
    
    
    // 5. Determine whether or not to return entire Individuals or just Fitnesses
    //    (plus whether or not the Individual has been evaluated).
    
    boolean returnIndividuals = parameters.getBoolean(new Parameter(P_RETURNINDIVIDUALS),null,false);
            
    // 5.5 should we silence the whole thing?

    boolean silent = false;

            
    // 6. Open a server socket and listen for requests
    String slaveName = parameters.getString(
        new Parameter(P_EVALSLAVENAME),null);
            
    String masterHost = parameters.getString(
        new Parameter(P_EVALMASTERHOST),null );
    if (masterHost == null)
        Output.initialError("Master Host missing", new Parameter(P_EVALMASTERHOST));
    int masterPort = parameters.getInt(
        new Parameter(P_EVALMASTERPORT),null, 0);
    if (masterPort == -1)
        Output.initialError("Master Port missing", new Parameter(P_EVALMASTERPORT));
    boolean useCompression = parameters.getBoolean(new Parameter(P_EVALCOMPRESSION),null,false);
            
    runTime = parameters.getInt(new Parameter(P_RUNTIME), null, 0); 
            
    runEvolve = parameters.getBoolean(new Parameter(P_RUNEVOLVE),null,false); 

    boolean oneShot = parameters.getBoolean(new Parameter("eval.slave.one-shot"),null,true); 
    
    if (runEvolve && !returnIndividuals)
        {
        Output.initialError("You have the slave running in 'evolve' mode, but it's only returning fitnesses to the master, not whole individuals.  This is almost certainly wrong.",
            new Parameter(P_RUNEVOLVE), new Parameter(P_RETURNINDIVIDUALS));
        }
    
    if (!silent) 
        {
        Output.initialMessage("ECJ Slave");
        if (runEvolve) Output.initialMessage("Running in Evolve mode, evolve time is " + runTime + " milliseconds");
        if (returnIndividuals) Output.initialMessage("Whole individuals will be returned");
        else Output.initialMessage("Only fitnesses will be returned");
        }
    
    // Continue to serve new masters until killed.
    Socket socket = null;
    while (true)
        {
        try
            {
            try
                {
                long connectAttemptCount = 0;
                if (!silent) Output.initialMessage("Connecting to master at "+masterHost+":"+masterPort);
                while (true)
                    {
                    try
                        {
                        socket = new Socket(masterHost, masterPort);
                        break;
                        }
                    catch (ConnectException e)   // it's not up yet...
                        {
                        connectAttemptCount++;
                        try
                            {
                            Thread.sleep(SLEEP_TIME);
                            }
                        catch( InterruptedException f )
                            {
                            }
                        }
                    }
                if (!silent) Output.initialMessage("Connected to master after " + (connectAttemptCount * SLEEP_TIME) + " ms");
                
                DataInputStream dataIn = null;
                DataOutputStream dataOut = null;

                try
                    {
                    InputStream tmpIn = socket.getInputStream();
                    OutputStream tmpOut = socket.getOutputStream();
                    if (useCompression)
                        {
                        tmpIn = Output.makeCompressingInputStream(tmpIn);
                        tmpOut = Output.makeCompressingOutputStream(tmpOut);
                        if (tmpIn == null || tmpOut == null)
                            {
                            String err = "You do not appear to have JZLib installed on your system, and so must set eval.compression=false.  " +
                                "To get JZLib, download from the ECJ website or from http://www.jcraft.com/jzlib/";
                            if (!silent) Output.initialMessage(err);
                            throw new Output.OutputExitException(err);
                            }
                        }
                                            
                    dataIn = new DataInputStream(tmpIn);
                    dataOut = new DataOutputStream(tmpOut);
                    }
                catch (IOException e)
                    {
                    String err = "Unable to open input stream from socket:\n"+e;
                    if (!silent) Output.initialMessage(err);
                    throw new Output.OutputExitException(err);
                    }
                    
                // read the unique number assigned to me
                slaveNum = dataIn.readInt();
                            
                // specify the slaveName
                if (slaveName==null)
                    {                    
                    slaveName = socket.getLocalAddress().toString() + "/" + slaveNum;
                    if (!silent) Output.initialMessage("No slave name specified.  Using: " + slaveName);
                    }
                            
                dataOut.writeUTF(slaveName);
                dataOut.flush();

                // 1. create the output
                // store = parameters.getBoolean(new Parameter(P_STORE), null, false);
            
                if (output != null) output.close();
                output = new Output(false);              // do not store messages, just print them
                output.setThrowsErrors(true);  // don't do System.exit(1);
            
                // stdout is always log #0. stderr is always log #1.
                // stderr accepts announcements, and both are fully verbose
                // by default.
                output.addLog(ec.util.Log.D_STDOUT, false);
                output.addLog(ec.util.Log.D_STDERR, true);

                if (silent)
                    {
                    output.getLog(0).silent = true;
                    output.getLog(1).silent = true;
                    }

                if (!silent) output.systemMessage(Version.message());


                // 2. set up thread values
                int breedthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_BREEDTHREADS));
                int evalthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_EVALTHREADS));

                // Note that either breedthreads or evalthreads (or both) may be 'auto'.  We don't warn about this because
                // the user isn't providing the thread seeds.
            

                // 3. create the Mersenne Twister random number generators,
                // one per thread

                MersenneTwisterFast[] random = new MersenneTwisterFast[breedthreads > evalthreads ? 
                    breedthreads : evalthreads];
    
                int seed = dataIn.readInt();
                for(int i = 0; i < random.length; i++)
                    random[i] = Evolve.primeGenerator(new MersenneTwisterFast(seed++));  // we prime the generator to be more sure of randomness.

                // 4. Set up the evolution state
            
                // what evolution state to use?
                state = (EvolutionState)
                    parameters.getInstanceForParameter(new Parameter(ec.Evolve.P_STATE),null,
                        EvolutionState.class);
                state.parameters = new ParameterDatabase();
                state.parameters.addParent(parameters);
                state.random = random;
                state.output = output;
                state.evalthreads = evalthreads;
                state.breedthreads = breedthreads;
    
                state.setup(state, null);
                state.population = state.initializer.setupPopulation(state, 0);
                
                if(state.evaluator.p_problem instanceof EcjModelEvaluation)
    				((EcjModelEvaluation)state.evaluator.p_problem).init(ecj);
            
                // 5. Optionally do further loading
                final MasterProblem storage = state.evaluator.masterproblem;
                storage.receiveAdditionalData(state, dataIn);
                storage.transferAdditionalData(state);
                            
                try
                    {
                    while (true)
                        {
                        EvolutionState newState = state;
                    
                        if (runEvolve) 
                            {
                            // Construct and use a new EvolutionState.  This will be inefficient the first time around
                            // as we've set up TWO EvolutionStates in a row with no good reason.
                            ParameterDatabase coverDatabase = new ParameterDatabase();  // protect the underlying one
                            coverDatabase.addParent(state.parameters);
                            newState = (EvolutionState) Evolve.initialize(coverDatabase, 0);
                            newState.startFresh();
                            newState.output.message("Replacing random number generators, ignore above seed message");
                            newState.random = state.random;  // continue with RNG
                            storage.transferAdditionalData(newState);  // load the arbitrary data again
                            }
                    
                        // 0 means to shut down
                        // System.err.println("reading next problem");
                        int problemType = dataIn.readByte();
                        // System.err.println("Read problem: " + (int)problemType);
                        switch (problemType)
                            {
                            case V_SHUTDOWN:
                            {
                            socket.close();
                            if (oneShot)
                                return;  // we're outa here
                            else
                                throw new Output.OutputExitException("SHUTDOWN");
                            }
                            case V_EVALUATESIMPLE:
                                evaluateSimpleProblemForm(newState, returnIndividuals, dataIn, dataOut, args);
                                break;
                            case V_EVALUATEGROUPED:
                                evaluateGroupedProblemForm(newState, returnIndividuals, dataIn, dataOut);
                                break;
                            default:
                                state.output.fatal("Unknown problem form specified: "+problemType);
                            }
                        }

                    } 
                catch (IOException e)    
                    {
                    // Since an IOException can happen here if the peer closes the socket
                    // on it's end, we don't necessarily have to exit.  Maybe we don't
                    // even need to print a warning, but we'll do so just to indicate
                    // something happened.
                    state.output.fatal("Unable to read type of evaluation from master.  Maybe the master closed its socket and exited?:\n"+e);
                    }
                } 
            catch (UnknownHostException e)
                {
                if (state != null)
                    state.output.fatal(e.getMessage());
                else if (!silent) System.err.println("FATAL ERROR (EvolutionState not created yet): " + e.getMessage());
                }
            catch (IOException e)
                {
                if (state != null)
                    state.output.fatal("Unable to connect to master:\n" + e);
                else if (!silent) System.err.println("FATAL ERROR (EvolutionState not created yet): " + e);
                }
            }
        catch (Output.OutputExitException e)
            {
            // here we restart if necessary
            try { socket.close(); } catch (Exception e2) { }
            if (oneShot) System.exit(0);
            }
        catch (OutOfMemoryError e)
            {
            // Let's try fixing things
            state = null;
            System.gc();
            try { socket.close(); } catch (Exception e2) { }
            socket = null;
            System.gc();
            System.err.println(e);
            if (oneShot) System.exit(0);
            }
        if (!silent) Output.initialMessage("\n\nResetting Slave");
        }
    }
	
	
	public static ParameterDatabase getParameterDatabase(String filePath) {
		ParameterDatabase parameters = null; 
		try
	        {
	        parameters = new ParameterDatabase(
	        		new File(new File(filePath).getAbsolutePath()));
	            // not available in jdk1.1: new File(args[x+1]).getAbsoluteFile(),
//	            new File(new File(algorithmConfigFile).getAbsolutePath()),
//	            args);
	                                    
	        // add the fact that I am a slave:      eval.i-am-slave = true
	        // this is used only by the Evaluator to determine whether to use the MasterProblem
	        parameters.set(new Parameter(ec.EvolutionState.P_EVALUATOR).push(ec.Evaluator.P_IAMSLAVE), "true");
	        }
	    catch(FileNotFoundException e)
	        { 
	        Output.initialError(
	            "A File Not Found Exception was generated upon" +
	            "reading the parameter file \"" + filePath + 
	            "\".\nHere it is:\n" + e); }
	    catch(IOException e)
	        { 
	        Output.initialError(
	            "An IO Exception was generated upon reading the" +
	            "parameter file \"" + filePath +
	            "\".\nHere it is:\n" + e); 
	        } 
	    if (parameters == null)
	        Output.initialError("No parameter file was specified." );
	    
	    return parameters;
	}

	public void runSlave(
			CalibrationController controller, 
			ParameterDatabase parameters,
			String masterHost,
			String masterListeningPort
		) {
		EvolutionState state = null;
	    Output output = null;
	    String[] args = {""};
	    
	    // Number of parameters    
	    // Calibration parameters manager
	    int numParams = 0;
	    
	    List<CalibrationParameter> calibrationParams = 
	    		controller.getTask().getCalibrationParametersManager().getParameters();
	    
	    numParams = calibrationParams.size();
	    parameters.set(new Parameter("pop.subpop.0.species.genome-size"),String.valueOf(numParams));
	    
	    // Set the min-max values for each parameter
		for(int i=0; i < numParams; i++) {
			CalibrationParameter calibParam = calibrationParams.get(i);
			String paramName = "pop.subpop.0.species.min-gene." + String.valueOf(i);
			parameters.set(new ec.util.Parameter(paramName), String.valueOf(calibParam.minValue));
			
			paramName = "pop.subpop.0.species.max-gene." + String.valueOf(i);
			parameters.set(new ec.util.Parameter(paramName), String.valueOf(calibParam.maxValue));
		}
		
		EcjInterface ecj = new EcjInterface(controller, 
				CalibrationConsole.NO_MASTER_HOST, CalibrationConsole.NO_MASTER_PORT);
	    
		// Fix the path to custom classes
		ecj.fixClassValues(parameters);
	    
	    // 5. Determine whether or not to return entire Individuals or just Fitnesses
	    //    (plus whether or not the Individual has been evaluated).
	    
	    boolean returnIndividuals = parameters.getBoolean(new Parameter(P_RETURNINDIVIDUALS),null,false);
	            
	    // 5.5 should we silence the whole thing?

	    boolean silent = false;

	            
	    // 6. Open a server socket and listen for requests
	    String slaveName = parameters.getString(
	        new Parameter(P_EVALSLAVENAME),null);
	            
	    if (masterHost == null || masterListeningPort==null)
	        Output.initialError("Master Host or Port missing", new Parameter(P_EVALMASTERHOST));
	    int masterPort = Integer.parseInt(masterListeningPort);

	    boolean useCompression = parameters.getBoolean(new Parameter(P_EVALCOMPRESSION),null,false);
	            
	    runTime = parameters.getInt(new Parameter(P_RUNTIME), null, 0); 
	            
	    runEvolve = parameters.getBoolean(new Parameter(P_RUNEVOLVE),null,false); 

	    boolean oneShot = parameters.getBoolean(new Parameter("eval.slave.one-shot"),null,true); 
	    
	    if (runEvolve && !returnIndividuals)
	        {
	        Output.initialError("You have the slave running in 'evolve' mode, but it's only returning fitnesses to the master, not whole individuals.  This is almost certainly wrong.",
	            new Parameter(P_RUNEVOLVE), new Parameter(P_RETURNINDIVIDUALS));
	        }
	    
	    if (!silent) 
	        {
	        Output.initialMessage("ECJ Slave");
	        if (runEvolve) Output.initialMessage("Running in Evolve mode, evolve time is " + runTime + " milliseconds");
	        if (returnIndividuals) Output.initialMessage("Whole individuals will be returned");
	        else Output.initialMessage("Only fitnesses will be returned");
	        }
	    
	    // Continue to serve new masters until killed.
	    Socket socket = null;
	    while (running)
	        {
	        try
	            {
	            try
	                {
	                long connectAttemptCount = 0;
	                if (!silent) Output.initialMessage("Connecting to master at "+masterHost+":"+masterPort);
	                while (true)
	                    {
	                    try
	                        {
	                        socket = new Socket(masterHost, masterPort);
	                        break;
	                        }
	                    catch (ConnectException e)   // it's not up yet...
	                        {
	                        connectAttemptCount++;
	                        try
	                            {
	                            Thread.sleep(SLEEP_TIME);
	                            }
	                        catch( InterruptedException f )
	                            {
	                            }
	                        }
	                    }
	                if (!silent) Output.initialMessage("Connected to master after " + (connectAttemptCount * SLEEP_TIME) + " ms");
	                
	                DataInputStream dataIn = null;
	                DataOutputStream dataOut = null;

	                try
	                    {
	                    InputStream tmpIn = socket.getInputStream();
	                    OutputStream tmpOut = socket.getOutputStream();
	                    if (useCompression)
	                        {
	                        tmpIn = Output.makeCompressingInputStream(tmpIn);
	                        tmpOut = Output.makeCompressingOutputStream(tmpOut);
	                        if (tmpIn == null || tmpOut == null)
	                            {
	                            String err = "You do not appear to have JZLib installed on your system, and so must set eval.compression=false.  " +
	                                "To get JZLib, download from the ECJ website or from http://www.jcraft.com/jzlib/";
	                            if (!silent) Output.initialMessage(err);
	                            throw new Output.OutputExitException(err);
	                            }
	                        }
	                                            
	                    dataIn = new DataInputStream(tmpIn);
	                    dataOut = new DataOutputStream(tmpOut);
	                    }
	                catch (IOException e)
	                    {
	                    String err = "Unable to open input stream from socket:\n"+e;
	                    if (!silent) Output.initialMessage(err);
	                    throw new Output.OutputExitException(err);
	                    }
	                    
	                // read the unique number assigned to me
	                slaveNum = dataIn.readInt();
	                            
	                // specify the slaveName
	                if (slaveName==null)
	                    {                    
	                    slaveName = socket.getLocalAddress().toString() + "/" + slaveNum;
	                    if (!silent) Output.initialMessage("No slave name specified.  Using: " + slaveName);
	                    }
	                            
	                dataOut.writeUTF(slaveName);
	                dataOut.flush();

	                // 1. create the output
	                // store = parameters.getBoolean(new Parameter(P_STORE), null, false);
	            
	                if (output != null) output.close();
	                output = new Output(false);              // do not store messages, just print them
	                output.setThrowsErrors(true);  // don't do System.exit(1);
	            
	                // stdout is always log #0. stderr is always log #1.
	                // stderr accepts announcements, and both are fully verbose
	                // by default.
	                output.addLog(ec.util.Log.D_STDOUT, false);
	                output.addLog(ec.util.Log.D_STDERR, true);

	                if (silent)
	                    {
	                    output.getLog(0).silent = true;
	                    output.getLog(1).silent = true;
	                    }

	                if (!silent) output.systemMessage(Version.message());


	                // 2. set up thread values
	                int breedthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_BREEDTHREADS));
	                int evalthreads = Evolve.determineThreads(output, parameters, new Parameter(Evolve.P_EVALTHREADS));

	                // Note that either breedthreads or evalthreads (or both) may be 'auto'.  We don't warn about this because
	                // the user isn't providing the thread seeds.
	            

	                // 3. create the Mersenne Twister random number generators,
	                // one per thread

	                MersenneTwisterFast[] random = new MersenneTwisterFast[breedthreads > evalthreads ? 
	                    breedthreads : evalthreads];
	    
	                int seed = dataIn.readInt();
	                for(int i = 0; i < random.length; i++)
	                    random[i] = Evolve.primeGenerator(new MersenneTwisterFast(seed++));  // we prime the generator to be more sure of randomness.

	                // 4. Set up the evolution state
	            
	                // what evolution state to use?
	                state = (EvolutionState)
	                    parameters.getInstanceForParameter(new Parameter(ec.Evolve.P_STATE),null,
	                        EvolutionState.class);
	                state.parameters = new ParameterDatabase();
	                state.parameters.addParent(parameters);
	                state.random = random;
	                state.output = output;
	                state.evalthreads = evalthreads;
	                state.breedthreads = breedthreads;
	    
	                state.setup(state, null);
	                state.population = state.initializer.setupPopulation(state, 0);
	                
	                if(state.evaluator.p_problem instanceof EcjModelEvaluation)
	    				((EcjModelEvaluation)state.evaluator.p_problem).init(ecj);
	            
	                // 5. Optionally do further loading
	                final MasterProblem storage = state.evaluator.masterproblem;
	                storage.receiveAdditionalData(state, dataIn);
	                storage.transferAdditionalData(state);
	                            
	                try
	                    {
	                    while (running)
	                        {
	                        EvolutionState newState = state;
	                    
	                        if (runEvolve) 
	                            {
	                            // Construct and use a new EvolutionState.  This will be inefficient the first time around
	                            // as we've set up TWO EvolutionStates in a row with no good reason.
	                            ParameterDatabase coverDatabase = new ParameterDatabase();  // protect the underlying one
	                            coverDatabase.addParent(state.parameters);
	                            newState = (EvolutionState) Evolve.initialize(coverDatabase, 0);
	                            newState.startFresh();
	                            newState.output.message("Replacing random number generators, ignore above seed message");
	                            newState.random = state.random;  // continue with RNG
	                            storage.transferAdditionalData(newState);  // load the arbitrary data again
	                            }
	                    
	                        // 0 means to shut down
	                        // System.err.println("reading next problem");
	                        int problemType = dataIn.readByte();
	                        // System.err.println("Read problem: " + (int)problemType);
	                        switch (problemType)
	                            {
	                            case V_SHUTDOWN:
	                            {
	                            socket.close();
	                            if (oneShot)
	                                return;  // we're outa here
	                            else
	                                throw new Output.OutputExitException("SHUTDOWN");
	                            }
	                            case V_EVALUATESIMPLE:
	                                evaluateSimpleProblemForm(newState, returnIndividuals, dataIn, dataOut, args);
	                                break;
	                            case V_EVALUATEGROUPED:
	                                evaluateGroupedProblemForm(newState, returnIndividuals, dataIn, dataOut);
	                                break;
	                            default:
	                                state.output.fatal("Unknown problem form specified: "+problemType);
	                            }
	                        }

	                    } 
	                catch (IOException e)    
	                    {
	                    // Since an IOException can happen here if the peer closes the socket
	                    // on it's end, we don't necessarily have to exit.  Maybe we don't
	                    // even need to print a warning, but we'll do so just to indicate
	                    // something happened.
	                    state.output.fatal("Unable to read type of evaluation from master.  Maybe the master closed its socket and exited?:\n"+e);
	                    }
	                } 
	            catch (UnknownHostException e)
	                {
	                if (state != null)
	                    state.output.fatal(e.getMessage());
	                else if (!silent) System.err.println("FATAL ERROR (EvolutionState not created yet): " + e.getMessage());
	                }
	            catch (IOException e)
	                {
	                if (state != null)
	                    state.output.fatal("Unable to connect to master:\n" + e);
	                else if (!silent) System.err.println("FATAL ERROR (EvolutionState not created yet): " + e);
	                }
	            }
	        catch (Output.OutputExitException e)
	            {
	            // here we restart if necessary
	            try { socket.close(); } catch (Exception e2) { }
	            if (oneShot) System.exit(0);
	            }
	        catch (OutOfMemoryError e)
	            {
	            // Let's try fixing things
	            state = null;
	            System.gc();
	            try { socket.close(); } catch (Exception e2) { }
	            socket = null;
	            System.gc();
	            System.err.println(e);
	            if (oneShot) System.exit(0);
	            }
	        if (!silent) Output.initialMessage("\n\nResetting Slave");
	        }
	}
}
