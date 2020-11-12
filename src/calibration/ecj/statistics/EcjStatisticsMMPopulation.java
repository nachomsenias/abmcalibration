package calibration.ecj.statistics;
import ec.*;
import ec.simple.SimpleFitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleStatistics;
import ec.steadystate.*;
import ec.util.*;
import ec.vector.DoubleVectorIndividual;
import ec.vector.FloatVectorSpecies;
import ec.vector.IntegerVectorIndividual;
import ec.vector.VectorIndividual;

import java.io.*;


/**
 * This class is used to store information about the current status of the population. 
 * It prints the genome of the current individuals, along with its fitness value in 
 *  different logs:
 * 	The whole population sorted
 * 	Those individuals above the average fitness
 * 	Those individuals above the average fitness and a minimum threshold 
 *  	distance with the rest of the individuals as the average distance in the
 *      population
 *  Individuals representative of a determined number of Niches defined in the 
 *  population through a clearing procedure with a distance radius
 *  
 * In the ecj structure, this object depends on stat.chlid.x
 * 
 * This class takes four new parameters from the ecj configuration file
 * 		activate: Indicates weather or not the user wants to take snapshots
 * 		generations: Number of generations between each snapshot
 * 		distance: Indicates if we want to export the distance. This
 * 			increases the runtime.
 * 		out: Name of the file to which export the data
 *  
 * @author ebermejo
 *
 */
public class EcjStatisticsMMPopulation extends Statistics implements SteadyStateStatisticsForm
    {
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;	
    
    /**
	 * Number of evaluations between snapshots
	 */
    public static final String P_SNAP_INTERVAL = "generations";
    public int generations;
    
    
    /**
	 * Number of evaluations between snapshots
	 */
    public static final String P_DISTANCE = "distance";
    public boolean showDistance;
    
    /**
	 * Output files
	 */
    public static final String P_LOG_POP = "out-pop";
    public int logPop = -1;
    public static final String P_LOG_FIT = "out-fitness";
    public int logFitness = -1;  
    public static final String P_LOG_DIS = "out-distance";
    public int logDistance = -1;  
    public static final String P_LOG_NIC = "out-niches";
    public int logNiches = -1;  
    
    public static final String P_RADIUS= "radius";
    public static final String P_K = "niches";
    public static final String P_DO_FINAL = "do-final";
    
    boolean doFinal;
    double radius;
    int niches;
 	//=========================================================================
	//		CONSTRUCTORS / INITIALIZERS
	//=========================================================================
	/**
	 * Initializing function for ECJ objects. Its structure is fixed
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 * @param base ECJ object that contains the root for parameter names
	 */
    public void setup(final EvolutionState state, final Parameter base) {
        // Call the parent constructor
        super.setup(state,base);
        
        doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL),null,true);
		// Read the number of generations between snapshots (1 by default)
		generations = state.parameters.getIntWithDefault(base.push(P_SNAP_INTERVAL), null, 1);
		if(generations <= 0) {
			state.output.fatal("The interval between snapshots must be a value "
					+ "greater than 0", null);
		}
		
		 // Reads the path of the log file and open it for writting
        File logFile = state.parameters.getFile(base.push(P_LOG_POP), null);
		try {
			if(logFile != null) {
				logPop = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		logFile = state.parameters.getFile(base.push(P_LOG_FIT), null);
		try {
			if(logFile != null) {
				logFitness = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		logFile = state.parameters.getFile(base.push(P_LOG_DIS), null);
		try {
			if(logFile != null) {
				logDistance = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		logFile = state.parameters.getFile(base.push(P_LOG_NIC), null);
		try {
			if(logFile != null) {
				logNiches = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
	 
	    radius = state.parameters.getDouble(base.push(P_RADIUS), null);
		if(radius <= 0) {
			state.output.fatal("Niche radius value not initialized", null);
		}
		niches = state.parameters.getInt(base.push(P_K), null);
		if(niches <= 0) {
			state.output.fatal("Niche parameter was not initialized", null);
		}
			 
		// Reads the parameter indicating if the distance has to be computed.
		// The parameter takes false as default value
		showDistance = state.parameters.getBoolean(
				base.push(P_DISTANCE), null, false);
    }
    	
    /**
	 * Set the log for storing the data
	 * 
	 * @param state ECJ object containing the state of the process.
	 * @param logName File path for the log. 
	 */

	public void setLogs(final EvolutionState state, final String logPop,
			final String logFitness, final String logDistance, final String logNiches) {
		
		if(this.logPop > 0)
			state.output.removeLog(this.logPop);
		if(this.logFitness > 0)
			state.output.removeLog(this.logFitness);
		if(this.logDistance > 0)
			state.output.removeLog(this.logDistance);
		if(this.logNiches > 0)
			state.output.removeLog(this.logNiches);
		
		File logFile = new File(logPop);
		try {
			if(logFile != null) {
				this.logPop = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		
		logFile = new File(logFitness);
		try {
			if(logFile != null) {
				this.logFitness = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		
		logFile = new File(logDistance);
		try {
			if(logFile != null) {
				this.logDistance = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
		
		logFile = new File(logNiches);
		try {
			if(logFile != null) {
				this.logNiches = state.output.addLog(logFile, false);
			}
		} catch (IOException e) {
			state.output.fatal("The log File for Ecj cannot be created");
		}
	}
    	
	
	//=========================================================================
	//		METHODS
	//=========================================================================
	
	private void sortPop(EvolutionState state, int subpop) {
		java.util.Arrays.sort(state.population.subpops[subpop].individuals,
		          new java.util.Comparator<Object>(){
		              public int compare(Object o1, Object o2) {
		                  Individual a = (Individual) o1;
		                  Individual b = (Individual) o2;
		                  if (a.fitness.betterThan(b.fitness))         return -1; //better is maximum value
		                  if (b.fitness.betterThan(a.fitness))         return 1;
		                  return 0;
		              }
		          });
		//remove worst or uninitialized individuals
		int thresh=state.population.subpops[subpop].individuals.length-1;
    	int popSize = state.population.subpops[subpop].individuals.length;

		if(((Individual)(state.population.subpops[subpop].individuals[0])).fitness instanceof SimpleFitness) {
			Fitness worst=state.population.subpops[subpop].individuals[thresh].fitness;
			for(int i=state.population.subpops[subpop].individuals.length-1;i>=0;i--)
				if(state.population.subpops[subpop].individuals[i].fitness.equals(worst))
					thresh=i;
		}else {
			double worst=((MultiObjectiveFitness)((Individual)state.population.subpops[subpop].individuals[thresh]).fitness).getObjective(0);
			for(int i=state.population.subpops[subpop].individuals.length-1;i>=0;i--)
				if(((MultiObjectiveFitness)((Individual)state.population.subpops[subpop].individuals[i]).fitness).getObjective(0)==worst)
					thresh=i;
		}
		if(thresh<popSize/3) thresh=(int)popSize/3;

		state.population.subpops[subpop].individuals=java.util.Arrays.copyOf(state.population.subpops[subpop].individuals, thresh);
	}
    
    
    int clearFitness(final EvolutionState s,final int subpopulation,final int thread) {
    	int nwins, nniches=0;
    	//we iterate in order (pop was sorted previously
    	s.output.message("We are clearing the population before statistic output for niches with "+this.niches+"solutions with radius "+this.radius);
    
    	int length=s.population.subpops[subpopulation].individuals.length;
    	for (int i=0; i<length;i++) {
	    	double ifit=((Individual)(s.population.subpops[subpopulation].individuals[i])).fitness.fitness();
	    	if(ifit>0) {
	    		nwins=1;
	        	nniches++;
	 	    	for (int j=i+1; j<length;j++) {
	 	    		double jfit=((Individual)(s.population.subpops[subpopulation].individuals[j])).fitness.fitness();
			    	if(jfit>0){
			    		
		 	    		double distance=distance_Individuals((VectorIndividual)s.population.subpops[subpopulation].individuals[i],(VectorIndividual)s.population.subpops[subpopulation].individuals[j])/((VectorIndividual)s.population.subpops[subpopulation].individuals[i]).genomeLength();
 
		 	    		if(distance<this.radius) {
 				    		if(nwins<this.niches)
				    			nwins+=1;
				    		else
				    			if(((Individual)(s.population.subpops[subpopulation].individuals[j])).fitness instanceof SimpleFitness)
				    				((SimpleFitness)((Individual)s.population.subpops[subpopulation].individuals[j]).fitness).setFitness(s, Double.NEGATIVE_INFINITY, false);
				    			else 
				    				((MultiObjectiveFitness)((Individual)s.population.subpops[subpopulation].individuals[j]).fitness).setObjectives(s, new double [] {-1000,1.0});
 			    		}
			    	}
			    }
	    	}
    	}
    	return nniches;
    }
    private double getDistAll(final EvolutionState state, final Individual ind, final Subpopulation mypop) {
        int popsize = mypop.individuals.length;
        double retval = 0;
        for (int t1 = 0; t1 < popsize; t1++) 
        	if(ind.species instanceof FloatVectorSpecies)
        		retval += distance_Individuals((DoubleVectorIndividual)ind,(DoubleVectorIndividual)mypop.individuals[t1]);
        	else
        		retval += distance_Individuals((IntegerVectorIndividual)ind,(IntegerVectorIndividual)mypop.individuals[t1]);
         return retval/popsize;
    }
    
    private double distance_Individuals(VectorIndividual ind, VectorIndividual ind1) {
        double result = 0; 
        for (int i = 0; i < ind.genomeLength(); i++) {
        	if(ind.species instanceof FloatVectorSpecies)
        		result += Math.pow(((DoubleVectorIndividual)ind).genome[i] - ((DoubleVectorIndividual)ind1).genome[i], 2);
        	else
        		result += Math.pow(((IntegerVectorIndividual)ind).genome[i] - ((IntegerVectorIndividual)ind1).genome[i], 2);
        }
        return Math.sqrt(result);
    }
    
    /**
	 * This function stores in the file the best and average fitness
	 * in the population.
	 * 
	 * @param state ECJ object that contains all the information of the algorithm.
	 */
    public void finalStatistics(final EvolutionState state, final int result)
    {
    	super.finalStatistics(state,result);
		// 	be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		
		// Do not generate a snapshot before initializing the population
		if(state.generation == 0 || state.generation % generations != 0)
			return;
		
		// Loops over the population to compute the statistical values
		if (!(state.statistics instanceof SimpleStatistics))
			state.output.fatal("The obtained statistics is not in the right format");

		// Compute the average distance with respect to the best individual
		double fit=Double.NEGATIVE_INFINITY,fitmean,fitmedian;
 
		if (doFinal)
			for(int pop=0; pop < state.population.subpops.length; pop++) {
				Individual [] last=state.population.subpops[pop].individuals.clone();
            	sortPop(state, pop);
            	int popSize = state.population.subpops[pop].individuals.length;
            	state.output.message(""+popSize);
            	//Using Average value of fitness
			/*	VectorIndividual best=(VectorIndividual)state.population.subpops[pop].individuals[0];
				VectorIndividual worst=(VectorIndividual)state.population.subpops[pop].individuals[popSize-1];
				if (best.fitness instanceof SimpleFitness) 	fitmean=((worst.fitness.fitness()-best.fitness.fitness())/2)+best.fitness.fitness();
				else  fitmean=((((MultiObjectiveFitness)worst.fitness).getObjective(0)-((MultiObjectiveFitness)best.fitness).getObjective(0))/2)+((MultiObjectiveFitness)best.fitness).getObjective(0);
			*/	//Using Median value of fitness 
				VectorIndividual m1=(VectorIndividual)state.population.subpops[pop].individuals[(int)popSize/2];
				VectorIndividual m2=(VectorIndividual)state.population.subpops[pop].individuals[(int)popSize/2-1];
				if (m1.fitness instanceof SimpleFitness) fitmedian=(m1.fitness.fitness()+m2.fitness.fitness())/2;
				else fitmedian=(((MultiObjectiveFitness)m1.fitness).getObjective(0)+((MultiObjectiveFitness)m2.fitness).getObjective(0))/2;
				fitmean=fitmedian;
				
				state.output.message("Generating Fitness Report");
            	for(int i=0; i < popSize; i++) {
					VectorIndividual actual=(VectorIndividual)state.population.subpops[pop].individuals[i];
						if (actual.fitness instanceof SimpleFitness) 	fit=actual.fitness.fitness();
						else 			fit= ((MultiObjectiveFitness)actual.fitness).getObjective(0);	
						
						state.output.println(
							String.valueOf(i) +";"+
							String.valueOf(actual.genotypeToStringForHumans()) +","+
							String.valueOf(fit),logPop);
  					if(fit>=fitmean) {
						state.output.println(
								String.valueOf(i) +";"+
								String.valueOf(actual.genotypeToStringForHumans()) +","+
								String.valueOf(fit),logFitness);
						double avgdist=getaverageDist(state,actual,state.population.subpops[pop]);
						if(getDistAll(state, actual, state.population.subpops[pop])>avgdist) 
							state.output.println(
									String.valueOf(i) +";"+
									String.valueOf(actual.genotypeToStringForHumans()) +","+
									String.valueOf(fit),logDistance);
					}
				}
				state.output.message("Generating Clearing Report");
				// Compute the average distance with respect to the best individual
				this.clearFitness(state, pop, 0);
				popSize = state.population.subpops[pop].individuals.length;
				for(int i=0; i < popSize; i++) {
					VectorIndividual actual=(VectorIndividual)state.population.subpops[pop].individuals[i];
					if (actual.fitness instanceof SimpleFitness) {
						if(actual.fitness.fitness()!=Double.NEGATIVE_INFINITY)
							fit=actual.fitness.fitness();
					}else 
						if(((MultiObjectiveFitness)actual.fitness).getObjective(0)!=Double.NEGATIVE_INFINITY)
							fit= ((MultiObjectiveFitness)actual.fitness).getObjective(0);
					if(fit!=Double.NEGATIVE_INFINITY && fit!=0.0)
					state.output.println(
							String.valueOf(i) +";"+
							String.valueOf(actual.genotypeToStringForHumans()) +","+
							String.valueOf(fit),logNiches);
					
					
				}
				state.population.subpops[pop].individuals=last.clone(); //recover final population 
			}
    }
	
	
    private double getaverageDist(EvolutionState state, VectorIndividual ind, Subpopulation mypop) {
    	int popsize = mypop.individuals.length;
        double min,max;
        min=Double.POSITIVE_INFINITY;
        max=Double.NEGATIVE_INFINITY;
        for (int t1 = 0; t1 < popsize; t1++) {
            double curr=distance_Individuals((VectorIndividual)ind,(VectorIndividual)mypop.individuals[t1])/ind.genomeLength();
            if(curr>max)       	max=curr;
            if(curr<min)		min=curr;
        }
    	return (max-min)/2;
	}
 
		
    }
	
	