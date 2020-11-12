/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package calibration.ecj.multimodal.nmmso;
import ec.util.*;
import ec.simple.SimpleEvolutionState;
import ec.simple.SimpleFitness; 
import calibration.ecj.EcjModelEvaluation;

  
 

public class NMMSOEvolutionState extends SimpleEvolutionState
    {
    private static final long serialVersionUID = 1;

    public void startFresh() 
    {
    output.message("Setting up");
    setup(this,null);  // a garbage Parameter

    // POPULATION INITIALIZATION
    output.message("Initializing Generation 0");
    statistics.preInitializationStatistics(this);
    population = initializer.initialPopulation(this, 0); // unthreaded
    statistics.postInitializationStatistics(this);
    
    // Compute generations from evaluations if necessary
    if (numEvaluations > UNDEFINED)
        {
        // compute a generation's number of individuals
        int generationSize = 0;
        for (int sub=0; sub < population.subpops.length; sub++)  
            { 
            generationSize += population.subpops[sub].individuals.length;  // so our sum total 'generationSize' will be the initial total number of individuals
            }
            
        if (numEvaluations < generationSize)
            {
            numEvaluations = generationSize;
            numGenerations = 1;
            output.warning("Using evaluations, but evaluations is less than the initial total population size (" + generationSize + ").  Setting to the populatiion size.");
            }
        else 
            {
            if (numEvaluations % generationSize != 0)
                output.warning("Using evaluations, but initial total population size does not divide evenly into it.  Modifying evaluations to a smaller value ("
                    + ((numEvaluations / generationSize) * generationSize) +") which divides evenly.");  // note integer division
            numGenerations = (int)(numEvaluations / generationSize);  // note integer division
            numEvaluations = numGenerations * generationSize;
            } 
         output.message("Evaluations will be " + numEvaluations);

        }    

    // INITIALIZE CONTACTS -- done after initialization to allow
    // a hook for the user to do things in Initializer before
    // an attempt is made to connect to island models etc.
    exchanger.initializeContacts(this);
    evaluator.initializeContacts(this);
    }
    
    public int evolve()
    {
    if(generation==0)
    	for(int i=0;i<this.population.subpops[0].individuals.length;i++)
	    	((SimpleFitness)this.population.subpops[0].individuals[i].fitness).setFitness(this,-1000.0,false);	
    if (generation > 0) {
        output.message("Generation " + generation);
	    // EVALUATION
	    statistics.preEvaluationStatistics(this);
	    
	   // 	this.population.subpops[0].individuals[i].evaluated=true;
	    //evaluator.evaluatePopulation(this); //WE SHOULD NOT EVALUATE ANYTHING
	    statistics.postEvaluationStatistics(this);
	    }
    // SHOULD WE QUIT?
    if (evaluator.runComplete(this) && quitOnRunComplete)
        {
        output.message("Found Ideal Individual");
        return R_SUCCESS;
        }

    // SHOULD WE QUIT?
    if( EcjModelEvaluation.getCurrentEvaluations() >= numEvaluations)
    	return R_FAILURE;
    

    // PRE-BREEDING EXCHANGING
    statistics.prePreBreedingExchangeStatistics(this);
    population = exchanger.preBreedingExchangePopulation(this);
    statistics.postPreBreedingExchangeStatistics(this);

    String exchangerWantsToShutdown = exchanger.runComplete(this);
    if (exchangerWantsToShutdown!=null)
        { 
        output.message(exchangerWantsToShutdown);
        return R_SUCCESS;
        }

    // BREEDING
    statistics.preBreedingStatistics(this);
    population = breeder.breedPopulation(this);
    statistics.postBreedingStatistics(this);
        
    // POST-BREEDING EXCHANGING
    statistics.prePostBreedingExchangeStatistics(this);
    population = exchanger.postBreedingExchangePopulation(this); //Doesn't do anything
    statistics.postPostBreedingExchangeStatistics(this);

    // INCREMENT GENERATION AND CHECKPOINT
    generation++;
    if (checkpoint && generation%checkpointModulo == 0) 
        {
        output.message("Checkpointing");
        statistics.preCheckpointStatistics(this);
        Checkpoint.setCheckpoint(this);
        statistics.postCheckpointStatistics(this);
        }

    return R_NOTDONE;
    }

/**
 * @param result
 */
public void finish(int result) 
    {
    //Output.message("Finishing");
    /* finish up -- we completed. */
    statistics.finalStatistics(this,result);
    finisher.finishPopulation(this,result);
    exchanger.closeContacts(this,result);
    evaluator.closeContacts(this,result);
    }

}
