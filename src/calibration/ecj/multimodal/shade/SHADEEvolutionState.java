/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package calibration.ecj.multimodal.shade;
import ec.*;
import ec.util.Checkpoint;
import ec.util.Parameter;
import calibration.ecj.EcjModelEvaluation; 
public class SHADEEvolutionState extends EvolutionState
    {
	
	private static final long serialVersionUID = 1L;
	public Population newpopulation;
	int initial_pop_size;
	int missing;


	 public void startFresh() 
     {
     output.message("Setting up");
     setup(this,null);  // a garbage Parameter

     // POPULATION INITIALIZATION
     output.message("Initializing Generation 0");
     statistics.preInitializationStatistics(this);
     population = initializer.initialPopulation(this, 0); // unthreaded
     statistics.postInitializationStatistics(this);
     
     Parameter parameter=  new Parameter("pop.subpop.0.size");
     initial_pop_size=parameters.getInt(parameter, null,0);
     
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
         output.message("Generations will be " + numGenerations);
         }    
     
   
     // INITIALIZE CONTACTS -- done after initialization to allow
     // a hook for the user to do things in Initializer before
     // an attempt is made to connect to island models etc.
     exchanger.initializeContacts(this);
     evaluator.initializeContacts(this);
     }

    public int evolve()
        {
        if (generation > 0) 
            output.message("Generation " + generation);
        if (generation ==0) {
	        missing=0;
        	// EVALUATION
	        statistics.preEvaluationStatistics(this);
	        evaluator.evaluatePopulation(this);
	        statistics.postEvaluationStatistics(this);
        }
        // SHOULD WE QUIT?
        if (evaluator.runComplete(this) && quitOnRunComplete)
            {
            output.message("Found Ideal Individual");
            return R_SUCCESS;
            }

        // SHOULD WE QUIT?
//        if (generation == numGenerations)
//            {
//            return R_FAILURE;
//            }
 	       if( EcjModelEvaluation.getCurrentEvaluations() >= numEvaluations)
	       {
           return R_FAILURE;
           }

        // PRE-BREEDING EXCHANGING
	    ((SHADEExchanger)  exchanger).setcurrentEvaluation(EcjModelEvaluation.getCurrentEvaluations());
        statistics.prePreBreedingExchangeStatistics(this);
        population = exchanger.preBreedingExchangePopulation(this);
        statistics.postPreBreedingExchangeStatistics(this);

        String exchangerWantsToShutdown = exchanger.runComplete(this);
        if (exchangerWantsToShutdown!=null)
            { 
            output.message(exchangerWantsToShutdown);
            /*
             * Don't really know what to return here.  The only place I could
             * find where runComplete ever returns non-null is 
             * IslandExchange.  However, that can return non-null whether or
             * not the ideal individual was found (for example, if there was
             * a communication error with the server).
             * 
             * Since the original version of this code didn't care, and the
             * result was initialized to R_SUCCESS before the while loop, I'm 
             * just going to return R_SUCCESS here. 
             */
            
            return R_SUCCESS;
            }

        // BREEDING
        statistics.preBreedingStatistics(this);
        Population oldpopulation = this.population;
        
 
        population = breeder.breedPopulation(this);
        
      
        
        // EVALUATION
        statistics.preEvaluationStatistics(this);
        evaluator.evaluatePopulation(this);
        statistics.postEvaluationStatistics(this);
 
        
        newpopulation=population;
        this.population=oldpopulation;
        // POST-BREEDING EXCHANGING
        if(exchanger instanceof SHADEExchanger) {
	        statistics.prePostBreedingExchangeStatistics(this);
	        ((SHADEExchanger)exchanger).setChildrenPop(newpopulation);
	        population = exchanger.postBreedingExchangePopulation(this);
	        statistics.postPostBreedingExchangeStatistics(this);
        }else {
        	output.fatal("Wrong exchanger selected, Please fix param inputs with SHADEExchanger");
        }
        
        //Adjust the counter of generations although it is not needed as we control the evolution through number of evaluations
        if(population.subpops[0].individuals.length<initial_pop_size) {
        	missing+=initial_pop_size-population.subpops[0].individuals.length;
        	if(missing>=initial_pop_size) {
        		numGenerations++;
        		missing-=initial_pop_size;
        	}
        }
        
 

        
        // POST-BREEDING EXCHANGING - Here we call the corresponding local search method
        statistics.postBreedingStatistics(this);
        
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
