package calibration.ecj.multimodal.nichepso;

import ec.* ;
import ec.vector.*;
import ec.util.*;
import java.util.* ;
import java.io.*;

/*
 * Particle.java
 * Created: Thu May  2 17:09:40 EDT 2013
 */

/**
 * Particle is a DoubleVectorIndividual with additional statistical information
 * necessary to perform Particle Swarm Optimization.  Specifically, it has a 
 * VELOCITY, a NEIGHBORHOOD of indexes of individuals, a NEIGHBORHOOD BEST genome
 * and fitness, and a PERSONAL BEST genome and fitness.  These elements, plus the
 * GLOBAL BEST genome and fitness found in PSOBreeder, are used to collectively
 * update the particle's location in space.
 *
 * <p> Particle updates its location in two steps.  First, it gathers current
 * neighborhood and personal best statistics via the update(...) method.  Then
 * it updates the particle's velocity and location (genome) according to these
 * statistics in the tweak(...) method.  Notice that neither of these methods is
 * the defaultMutate(...) method used in DoubleVectorIndividual: this means that
 * in *theory* you could rig up Particles to also be mutated if you thought that
 * was a good reason.
 * 
 * <p> Many of the parameters passed into the tweak(...) method are based on
 * weights determined by the PSOBreeder.
 *
 * @author Khaled Ahsan Talukder
 */


public class NicheParticle extends DoubleVectorIndividual
    {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String AUXILLARY_PREAMBLE = "Auxillary: ";
    //my index within the population;    
    public int popindex;
    // my velocity
    public double[] velocity ;

    // the best genome and fitness members of my neighborhood ever achieved
    public double[] neighborhoodBestGenome = null;
    public Fitness neighborhoodBestFitness = null;

    // the best genome and fitness *I* personally ever achieved
    public double[] personalBestGenome = null;
    public Fitness personalBestFitness = null;

//    public ArrayList<Double> fitarchive;
    public Queue<Double> fitarchive;
    public double stdev;
    public double rho;
    public double increaserho=2.0;
    public double decreaserho=0.5;
    
    public int hashCode()
        {
        int hash = super.hashCode();
        // no need to change anything I think
        return hash;
        }


    public boolean equals(Object ind)
        {
        if (!super.equals(ind)) return false;
        NicheParticle i = (NicheParticle) ind;

        if ((velocity == null && i.velocity != null) ||
            (velocity != null && i.velocity == null))
            return false;
                        
        if (velocity != null)
            {
            if (velocity.length != i.velocity.length)
                return false;
            for (int j = 0; j < velocity.length; j++)
                if (velocity[j] != i.velocity[j])
                    return false;
            }
        /*                
        if ((neighborhood == null && i.neighborhood != null) ||
            (neighborhood != null && i.neighborhood == null))
            return false;
                        
        if (neighborhood != null)
            {
            if (neighborhood.length != i.neighborhood.length)
                return false;
            for (int j = 0; j < neighborhood.length; j++)
                if (neighborhood[j] != i.neighborhood[j])
                    return false;
            }
        */
        if ((neighborhoodBestGenome == null && i.neighborhoodBestGenome != null) ||
            (neighborhoodBestGenome != null && i.neighborhoodBestGenome == null))
            return false;
                        
        if (neighborhoodBestGenome != null)
            {
            if (neighborhoodBestGenome.length != i.neighborhoodBestGenome.length)
                return false;
            for (int j = 0; j < neighborhoodBestGenome.length; j++)
                if (neighborhoodBestGenome[j] != i.neighborhoodBestGenome[j])
                    return false;
            }
                        
        if ((neighborhoodBestFitness == null && i.neighborhoodBestFitness != null) ||
            (neighborhoodBestFitness != null && i.neighborhoodBestFitness == null))
            return false;
                        
        if (neighborhoodBestFitness != null)
            {
            if (!neighborhoodBestFitness.equals(i.neighborhoodBestFitness))
                return false;
            }

        if ((personalBestGenome == null && i.personalBestGenome != null) ||
            (personalBestGenome != null && i.personalBestGenome == null))
            return false;
                        
        if (personalBestGenome != null)
            {
            if (personalBestGenome.length != i.personalBestGenome.length)
                return false;
            for (int j = 0; j < personalBestGenome.length; j++)
                if (personalBestGenome[j] != i.personalBestGenome[j])
                    return false;
            }

        if ((personalBestFitness == null && i.personalBestFitness != null) ||
            (personalBestFitness != null && i.personalBestFitness == null))
            return false;
                        
        if (personalBestFitness != null)
            {
            if (!personalBestFitness.equals(i.personalBestFitness))
                return false;
            }

        return true;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
    	super.setup(state, base);
        velocity = new double[genome.length] ;
        fitarchive = new LinkedList<Double>();
        popindex=-1;
        for(int x=0; x < genome.length; x++)
        	velocity[x]=((FloatVectorSpecies) this.species).minGene(x)+state.random[0].nextDouble()*(((FloatVectorSpecies) this.species).maxGene(x)-((FloatVectorSpecies) this.species).minGene(x));
        personalBestGenome=velocity.clone();
        }
        

    public Object clone()
        {
        NicheParticle myobj = (NicheParticle) (super.clone());
        myobj.fitarchive=new LinkedList<>(fitarchive);
        // must clone the velocity and neighborhood pattern if they exist
        if (velocity != null) velocity = (double[])(velocity.clone());
        return myobj;
        }
    
    @Override
    public double distanceTo(Individual otherInd){
    	if (!(otherInd instanceof DoubleVectorIndividual)) 
            return Double.NEGATIVE_INFINITY; // will return infinity!
        DoubleVectorIndividual other = (DoubleVectorIndividual) otherInd;
        double[] otherGenome = other.genome;
        double sumSquaredDistance =0.0;
        for(int i=0; i < other.genomeLength(); i++)
            {
            double dist = this.genome[i] - otherGenome[i]; 
            sumSquaredDistance += dist*dist;
            }
        return Math.abs(StrictMath.sqrt(sumSquaredDistance));
    }
    
  /*  public double normdistanceTo(Individual otherInd){
    	if (!(otherInd instanceof DoubleVectorIndividual)) 
            return Double.NEGATIVE_INFINITY; // will return infinity!
        DoubleVectorIndividual other = (DoubleVectorIndividual) otherInd;
        double[] otherGenome = other.genome;
        double sumSquaredDistance =0.0;
        for(int i=0; i < other.genomeLength(); i++)
            {
            double dist = this.genome[i] - otherGenome[i];
            //Normalize distance
            dist/=(((FloatVectorSpecies)this.species).maxGene(i)-((FloatVectorSpecies)this.species).minGene(i));
            sumSquaredDistance += dist*dist; 
            }
        return Math.abs(StrictMath.sqrt(sumSquaredDistance));
    }*/
    
    
    //After evaluation we call for an update on fitnessarchive, fitnessstdev,personalbest
    public void updateArchive() {
    	//insert new fitness into archive queue - limited to a maximum of 3 iterations
    	while (fitarchive.size()>=3) fitarchive.poll();
    	fitarchive.add(this.fitness.fitness());
    	//i
    	if (fitarchive.size()<3) this.stdev=Double.POSITIVE_INFINITY;
    	else {
    		//TODO: CHECK VALID Compute stdev of fitarchive
    		updatestdev();
    	}	
    }
    
    public void updatestdev() {
    	double mean = 0;
    	double ssum = 0;
    	for(int i=0;i<fitarchive.size();i++) {
    		double fit=fitarchive.poll();
    		mean+=fit;
    		fitarchive.add(fit);
    	}
    	mean=mean/fitarchive.size();
    	for(int i=0;i<fitarchive.size();i++) {
    		double fit=fitarchive.poll();
    		ssum+=Math.pow(fit-mean, 2);
    		fitarchive.add(fit);
    	}
    	this.stdev=Math.sqrt(ssum/fitarchive.size());
    	
    }
    
    
    //TODO: check valid
   /* public void setNeighborhood(int me, ArrayList<Integer> neigh) {
    	//Reset previous neighborhood just in case
    	neighborhood =null;
    	neighborhoodBestGenome = null;
        neighborhoodBestFitness = null;
        //Clone items from subswarm list
    	neighborhood =  new int[neigh.size()-1];
    	int counter = 0;
    	for (Integer n : neigh) 
    		if(n!=me) {
    			neighborhood[counter]=neigh.get(n).intValue();
    			counter++;
    		}
    }
    */
    
    //TODO: update properly and check if we should call updateArchive()
    public void update(final EvolutionState state, int subpop, int myindex, int thread)
        {
        // update personal best
        if (personalBestFitness == null || fitness.betterThan(personalBestFitness))
            {
            personalBestFitness = (Fitness)(fitness.clone());
            personalBestGenome = (double[])(genome.clone());
            }      
        //update fitness archive and std value
        this.updateArchive();
        // identify neighborhood best
        neighborhoodBestFitness = fitness;  // initially me
        neighborhoodBestGenome = genome;
       /* if(neighborhood!=null)
	    for(int i = 0 ; i < neighborhood.length ; i++)
	            {
	            int ind = neighborhood[i] ;
	            if (state.population.subpops[subpop].individuals[ind].fitness.betterThan(fitness))
	                {
	                neighborhoodBestFitness = state.population.subpops[subpop].individuals[ind].fitness;
	                neighborhoodBestGenome = ((DoubleVectorIndividual)(state.population.subpops[subpop].individuals[ind])).genome;
	                }
	            }*/
        if(popindex!=myindex && popindex!=-1)
        	state.output.error("Particle index mismatch. Was the pop sorted?"+popindex+" "+myindex);
        popindex=myindex;
        // clone neighborhood best
        neighborhoodBestFitness = (Fitness)(neighborhoodBestFitness.clone());
        neighborhoodBestGenome = (double[])(neighborhoodBestGenome.clone());
        }
     
    public void tweakGC(EvolutionState state,double velocityCoeff, double rhoCoeff, int thread){
    	for(int x = 0 ; x < genomeLength() ; x++){
    		double xCurrent = genome[x] ;
        	double xPersonal = personalBestGenome[x];
         	double newVelocity =  -xCurrent+xPersonal+rhoCoeff*(1.0-2.0*state.random[thread].nextDouble())+(velocityCoeff * velocity[x]);
            velocity[x] = newVelocity; 
            genome[x] += newVelocity ;
            if(genome[x]<((FloatVectorSpecies)this.species).minGene(x))
            	genome[x]=((FloatVectorSpecies)this.species).minGene(x);
            if(genome[x]>((FloatVectorSpecies)this.species).maxGene(x))
            	genome[x]=((FloatVectorSpecies)this.species).maxGene(x);
    	}
        evaluated = false ;    
    }
    
	public void setBestNeighbor(EvolutionState state,int subpop,int bestssind) {
		neighborhoodBestFitness = (Fitness)(state.population.subpops[subpop].individuals[bestssind].fitness).clone();
		neighborhoodBestGenome= ((NicheParticle)state.population.subpops[subpop].individuals[bestssind]).genome.clone();
	}
	
    // velocityCoeff:       cognitive/confidence coefficient for the velocity
    // personalCoeff:       cognitive/confidence coefficient for self
    // informantCoeff:      cognitive/confidence coefficient for informants/neighbours
    // globalCoeff:         cognitive/confidence coefficient for global best, this is not done in the standard PSO
	public void tweak(
        EvolutionState state,  double[] globalBest,
        double velocityCoeff, double personalCoeff, 
        double informantCoeff, double globalCoeff, 
        int thread)
        {
        for(int x = 0 ; x < genomeLength() ; x++)
            {
            double xCurrent = genome[x];
            double xPersonal = personalBestGenome[x];
            double xNeighbour = neighborhoodBestGenome[x];
            double xGlobal = globalBest[x];
            double beta = state.random[thread].nextDouble() * personalCoeff ;
            double gamma = state.random[thread].nextDouble() * informantCoeff ;
            double delta = state.random[thread].nextDouble() * globalCoeff ;

            double newVelocity = (velocityCoeff * velocity[x]) + (beta * (xPersonal - xCurrent)) + (gamma * (xNeighbour - xCurrent)) + (delta * (xGlobal - xCurrent)) ;
            velocity[x] = newVelocity ;
            genome[x] += newVelocity ;
            if(genome[x]<((FloatVectorSpecies)this.species).minGene(x)) 
            	genome[x]=((FloatVectorSpecies)this.species).minGene(x); 
            if(genome[x]>((FloatVectorSpecies)this.species).maxGene(x)) 
            	genome[x]=((FloatVectorSpecies)this.species).maxGene(x); 
            }
                
        evaluated = false ;        
        }
        
        
        
    /// The following methods handle modifying the auxillary data when the
    /// genome is messed around with.
    
    void resetAuxillaryInformation(EvolutionState state, int thread)
        {
        //neighborhood = null;
        neighborhoodBestGenome = null;
        neighborhoodBestFitness = null;
        personalBestGenome = null;
        personalBestFitness = null;
      //  for(int i = 0; i < velocity.length; i++)
       //     velocity[i] = 0.0;
        for(int x=0; x < genome.length; x++)
        	velocity[x]=state.random[thread].nextDouble()*(((FloatVectorSpecies) this.species).maxGene(x)-((FloatVectorSpecies) this.species).minGene(x));
        
        }
        
    public void reset(EvolutionState state, int thread)
        {
        super.reset(state, thread);
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
        resetAuxillaryInformation(state,thread);
        }
    
    // This would be exceptionally weird to use in a PSO context, but for
    // consistency's sake...
    public void setGenomeLength(int len)
        {
        super.setGenomeLength(len);
        
        // we always reset regardless of whether the length is the same
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
      //  resetAuxillaryInformation(state);
        }
        
    // This would be exceptionally weird to use in a PSO context, but for
    // consistency's sake...
    public void setGenome(Object gen)
        {
        super.setGenome(gen);
        
        // we always reset regardless of whether the length is the same
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
      //  resetAuxillaryInformation(state);
        }

    // This would be exceptionally weird to use in a PSO context, but for
    // consistency's sake...
    public void join(Object[] pieces)
        {
        super.join(pieces);
        
        // we always reset regardless of whether the length is the same
        if (genome.length != velocity.length)
            velocity = new double[genome.length];
      //  resetAuxillaryInformation();
        }
        
        
    
    
    /// gunk for reading and writing, but trying to preserve some of the 
    /// auxillary information
        
    StringBuilder encodeAuxillary()
        {
        StringBuilder s = new StringBuilder();
        s.append(AUXILLARY_PREAMBLE);
        s.append(Code.encode(true));
        //s.append(Code.encode(neighborhood!=null));
        s.append(Code.encode(neighborhoodBestGenome != null));
        s.append(Code.encode(neighborhoodBestFitness != null));
        s.append(Code.encode(personalBestGenome != null));
        s.append(Code.encode(personalBestFitness != null));
        s.append("\n");
        
        // velocity
        s.append(Code.encode(velocity.length));
        for(int i = 0; i < velocity.length; i++)
            s.append(Code.encode(velocity[i]));
        s.append("\n");
        
        // neighborhood 
        /*if (neighborhood != null)
            {
            s.append(Code.encode(neighborhood.length));
            for(int i = 0; i < neighborhood.length; i++)
                s.append(Code.encode(neighborhood[i]));
            s.append("\n");
            }*/

        // neighborhood best
        if (neighborhoodBestGenome != null)
            {
            s.append(Code.encode(neighborhoodBestGenome.length));
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                s.append(Code.encode(neighborhoodBestGenome[i]));
            s.append("\n");
            }

        if (neighborhoodBestFitness != null)
            s.append(neighborhoodBestFitness.fitnessToString());

        // personal     best
        if (personalBestGenome != null)
            {
            s.append(Code.encode(personalBestGenome.length));
            for(int i = 0; i < personalBestGenome.length; i++)
                s.append(Code.encode(personalBestGenome[i]));
            s.append("\n");
            }

        if (personalBestFitness != null)
            s.append(personalBestFitness.fitnessToString());
        s.append("\n");
                
        return s;
        }
    
    public void printIndividual(final EvolutionState state, final int log)
        {
        super.printIndividual(state, log);
        state.output.println(encodeAuxillary().toString(), log);
        }

    public void printIndividual(final EvolutionState state, final PrintWriter writer)
        {
        super.printIndividual(state, writer);
        writer.println(encodeAuxillary().toString());
        }

    public void readIndividual(final EvolutionState state, 
        final LineNumberReader reader)
        throws IOException
        {
        super.readIndividual(state, reader);
        
        // Next, read auxillary header.
        DecodeReturn d = new DecodeReturn(Code.readStringWithPreamble(AUXILLARY_PREAMBLE, state, reader));
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean v = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
    
        boolean nb = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean nbf = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean pb = (d.l != 0);
        Code.decode(d);
        if (d.type != DecodeReturn.T_BOOLEAN)
            state.output.fatal("Line " + d.lineNumber + " should have six boolean values but seems to have fewer.");
        boolean pbf = (d.l != 0);

        // Next, read auxillary arrays.
        if (v)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Velocity length missing.");
            velocity = new double[(int)(d.l)];
            for(int i = 0; i < velocity.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_DOUBLE)
                    state.output.fatal("Velocity information not long enough");
                velocity[i] = d.d;
                }
            }
        else velocity = new double[genome.length];

        /* if (n)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Neighborhood length missing.");
            neighborhood = new int[(int)(d.l)];
            for(int i = 0; i < neighborhood.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_INT)
                    state.output.fatal("Neighborhood information not long enough");
                neighborhood[i] = (int)(d.l);
                }
            }
        else neighborhood = null;*/

        if (nb)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Neighborhood-Best length missing.");
            neighborhoodBestGenome = new double[(int)(d.l)];
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_DOUBLE)
                    state.output.fatal("Neighborhood-Best genome not long enough");
                neighborhoodBestGenome[i] = d.d;
                }
            }
        else neighborhoodBestGenome = null;

        if (nbf)
            {
            // here we don't know what kind of fitness it is.  So we'll do our best and guess
            // that it's the same fitness as our own Particle 
            neighborhoodBestFitness = (Fitness)(fitness.clone());
            neighborhoodBestFitness.readFitness(state, reader);
            }

        if (pb)
            {
            String s = reader.readLine();
            d = new DecodeReturn(s);
            Code.decode(d);
            if (d.type != DecodeReturn.T_INT)
                state.output.fatal("Personal-Best length missing.");
            personalBestGenome = new double[(int)(d.l)];
            for(int i = 0; i < personalBestGenome.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_DOUBLE)
                    state.output.fatal("Personal-Best genome not long enough");
                personalBestGenome[i] = d.d;
                }
            }
        else personalBestGenome = null;

        if (pbf)
            {
            // here we don't know what kind of fitness it is.  So we'll do our best and guess
            // that it's the same fitness as our own Particle 
            personalBestFitness = (Fitness)(fitness.clone());
            personalBestFitness.readFitness(state, reader);
            }
        }

    public void writeIndividual(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
        {
        super.writeIndividual(state, dataOutput);
        
        if (velocity != null)  // it's always non-null
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(velocity.length);
            for(int i = 0; i < velocity.length; i++)
                dataOutput.writeDouble(velocity[i]);
            }
        else dataOutput.writeBoolean(false);  // this will never happen
 
 
        /*if (neighborhood != null)
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(neighborhood.length);
            for(int i = 0; i < neighborhood.length; i++)
                dataOutput.writeDouble(neighborhood[i]);
            }
        else dataOutput.writeBoolean(false);*/


        if (neighborhoodBestGenome != null)
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(neighborhoodBestGenome.length);
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                dataOutput.writeDouble(neighborhoodBestGenome[i]);
            }
        else dataOutput.writeBoolean(false);


        if (neighborhoodBestFitness != null)
            {
            dataOutput.writeBoolean(true);
            neighborhoodBestFitness.writeFitness(state, dataOutput);
            }
        else dataOutput.writeBoolean(false);


        if (personalBestGenome != null)  // it's always non-null
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(personalBestGenome.length);
            for(int i = 0; i < personalBestGenome.length; i++)
                dataOutput.writeDouble(personalBestGenome[i]);
            }
        else dataOutput.writeBoolean(false);


        if (personalBestFitness != null)
            {
            dataOutput.writeBoolean(true);
            personalBestFitness.writeFitness(state, dataOutput);
            }
        else dataOutput.writeBoolean(false);
        }

    public void readIndividual(final EvolutionState state,
        final DataInput dataInput) throws IOException
        {
        super.readIndividual(state, dataInput);
        
        // Next, read auxillary arrays.
        if (dataInput.readBoolean())
            {
            velocity = new double[dataInput.readInt()];
            for(int i = 0; i < velocity.length; i++)
                velocity[i] = dataInput.readDouble();
            }
        else velocity = new double[genome.length];

      /*  if (dataInput.readBoolean())
            {
            neighborhood = new int[dataInput.readInt()];
            for(int i = 0; i < neighborhood.length; i++)
                neighborhood[i] = dataInput.readInt();
            }
        else neighborhood = null;*/
        
        if (dataInput.readBoolean())
            {
            neighborhoodBestGenome = new double[dataInput.readInt()];
            for(int i = 0; i < neighborhoodBestGenome.length; i++)
                neighborhoodBestGenome[i] = dataInput.readDouble();
            }
        else neighborhoodBestGenome = null;

        if (dataInput.readBoolean())
            {
            neighborhoodBestFitness = (Fitness)(fitness.clone());
            neighborhoodBestFitness.readFitness(state, dataInput);
            }

        if (dataInput.readBoolean())
            {
            personalBestGenome = new double[dataInput.readInt()];
            for(int i = 0; i < personalBestGenome.length; i++)
                personalBestGenome[i] = dataInput.readDouble();
            }
        else personalBestGenome = null;

        if (dataInput.readBoolean())
            {
            personalBestFitness = (Fitness)(fitness.clone());
            personalBestFitness.readFitness(state, dataInput);
            }
        }




    }
