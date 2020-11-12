package calibration.ecj.multimodal.nmmso;

import ec.* ;
import ec.vector.*;
import ec.util.*;
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


public class NMMSOParticle extends DoubleVectorIndividual
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
    // history locations public double[][] historyGenome = null; or ¿ArrayList?

    // the best genome and fitness members of my neighborhood ever achieved
    public double[] gbestGenome = null;
    public Fitness gbestFitness = null;

    // the best genome and fitness *I* personally ever achieved
    public double[] personalBestGenome = null;
    public Fitness personalBestFitness = null;
 
    public int hashCode()
        {
        int hash = super.hashCode();
        // no need to change anything I think
        return hash;
        }


    public boolean equals(Object ind)
        {
        if (!super.equals(ind)) return false;
        NMMSOParticle i = (NMMSOParticle) ind;

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
        if ((gbestGenome == null && i.gbestGenome != null) ||
            (gbestGenome != null && i.gbestGenome == null))
            return false;
                        
        if (gbestGenome != null)
            {
            if (gbestGenome.length != i.gbestGenome.length)
                return false;
            for (int j = 0; j < gbestGenome.length; j++)
                if (gbestGenome[j] != i.gbestGenome[j])
                    return false;
            }
                        
        if ((gbestFitness == null && i.gbestFitness != null) ||
            (gbestFitness != null && i.gbestFitness == null))
            return false;
                        
        if (gbestFitness != null)
            {
            if (!gbestFitness.equals(i.gbestFitness))
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
        popindex=-1;
        for(int x=0; x < genome.length; x++)
        	velocity[x]=((FloatVectorSpecies) this.species).minGene(x)+state.random[0].nextDouble()*(((FloatVectorSpecies) this.species).maxGene(x)-((FloatVectorSpecies) this.species).minGene(x));
        personalBestGenome=velocity.clone();
        }
        

    public Object clone()
        {
        NMMSOParticle myobj = (NMMSOParticle) (super.clone());
        myobj.popindex=-1;
         // must clone the velocity and neighborhood pattern if they exist
        if (velocity != null) velocity = (double[])(velocity.clone());
        return myobj;
        }
    

    
    public void update(final EvolutionState state, int myindex)
        {
        // update personal best
        if (personalBestFitness == null || fitness.betterThan(personalBestFitness))
            {
            personalBestFitness = (Fitness)(fitness.clone());
            personalBestGenome = (double[])(genome.clone());
            }       
        // identify neighborhood best
        gbestFitness = fitness;  // initially me
        gbestGenome = genome;
 
        if(popindex!=myindex && popindex!=-1)
        	state.output.error("Particle index mismatch. Was the pop sorted?: "+popindex+" "+myindex);
        popindex=myindex;
        // clone neighborhood best
        gbestFitness = (Fitness)(gbestFitness.clone());
        gbestGenome = (double[])(gbestGenome.clone());
        }
     
   
    
	public void setBestNeighbor(EvolutionState state,int subpop,int bestssind) {
		gbestFitness = (Fitness)(state.population.subpops[subpop].individuals[bestssind].fitness).clone();
		gbestGenome= ((NMMSOParticle)state.population.subpops[subpop].individuals[bestssind]).genome.clone();
	}
	
    // velocityCoeff:       cognitive/confidence coefficient for the velocity
    // personalCoeff:       cognitive/confidence coefficient for self
    // informantCoeff:      cognitive/confidence coefficient for informants/neighbours -> gbest
 	public void tweak(EvolutionState state, double velocityCoeff, double personalCoeff, double informantCoeff, int thread)
        {
        for(int x = 0 ; x < genomeLength() ; x++)
            {
            double xCurrent = genome[x];
            double xPersonal = personalBestGenome[x];
            double xBest = gbestGenome[x];
            int retries=0;
            do {	
                double beta = state.random[thread].nextDouble() * personalCoeff ;
                double gamma = state.random[thread].nextDouble() * informantCoeff ;
            	double newVelocity = (velocityCoeff * velocity[x]) + (beta * (xPersonal - xCurrent)) + (gamma * (xBest - xCurrent)) ;
            	velocity[x] = newVelocity ;
            	genome[x] += newVelocity ;
            	retries++;
            }while(retries<5 &&(genome[x]<((FloatVectorSpecies)this.species).minGene(x) || genome[x]>((FloatVectorSpecies)this.species).maxGene(x)));
            if(genome[x]<((FloatVectorSpecies)this.species).minGene(x)) 
            	genome[x]=((FloatVectorSpecies)this.species).minGene(x); 
            if(genome[x]>((FloatVectorSpecies)this.species).maxGene(x)) 
            	genome[x]=((FloatVectorSpecies)this.species).maxGene(x); 
            }
                
        evaluated = false ;        
        }
        
        /*
 	function [x_c, x_d] = UNI(x1,x2)
 			% simulated binary crossover
 			l = length(x1);
 			x_c =x1;
 			x_d = x2;
 			r = find(rand(l,1)>0.5);
 			if isempty(r)==1 % ensure at least one swapped
 			    r = randperm(l);
 			    r=r(1);
 			end
 			x_c(r) = x2(r);
 			x_d(r) = x1(r);
*/
 	public void uniformCrossover(EvolutionState state, NMMSOParticle other, int thread) {
 		for(int x=0;x<this.genomeLength();x++)
 			if(state.random[thread].nextBoolean())
 				this.genome[x]=other.genome[x];
 	}
 	
    /// The following methods handle modifying the auxillary data when the
    /// genome is messed around with.
    
    void resetAuxillaryInformation(EvolutionState state, int thread)
        {
        //neighborhood = null;
        gbestGenome = null;
        gbestFitness = null;
        personalBestGenome = null;
        personalBestFitness = null;
      //  for(int i = 0; i < velocity.length; i++)
       //     velocity[i] = 0.0;
        for(int x=0; x < genome.length; x++) 
        	velocity[x]=state.random[thread].nextDouble()*(((FloatVectorSpecies) this.species).maxGene(x)-((FloatVectorSpecies) this.species).minGene(x))+((FloatVectorSpecies) this.species).minGene(x);
         
        }
        
    public void fullreset(EvolutionState state, int thread)
    {
    this.reset(state, thread);
    for(int x=0; x < genome.length; x++) 
    	genome[x]=state.random[thread].nextDouble()*(((FloatVectorSpecies) this.species).maxGene(x)-((FloatVectorSpecies) this.species).minGene(x))+((FloatVectorSpecies) this.species).minGene(x);

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
       
  	@Override
  	//TODO: Override calculating an euclidean/Hamming distance
    public double distanceTo(Individual otherInd)
        { 
        if (!(otherInd instanceof DoubleVectorIndividual)) 
            return Double.NEGATIVE_INFINITY; // will return infinity!
        DoubleVectorIndividual other = (DoubleVectorIndividual) otherInd;
        double[] otherGenome = other.genome;
        double sumSquaredDistance =0.0;
        for(int i=0; i < other.genomeLength(); i++)
            {
            double dist = otherGenome[i]- this.genome[i];
            //Normalize distance
            dist/=(((FloatVectorSpecies)this.species).maxGene(i)-((FloatVectorSpecies)this.species).minGene(i));
            sumSquaredDistance += dist*dist;
            }
        sumSquaredDistance /= this.genomeLength(); //Normalize Distance per dimension
        return StrictMath.sqrt(sumSquaredDistance);
        }
    
    /**Auxiliary methods from PSOParticle**/
    /// gunk for reading and writing, but trying to preserve some of the 
    /// auxillary information
        
    StringBuilder encodeAuxillary()
        {
        StringBuilder s = new StringBuilder();
        s.append(AUXILLARY_PREAMBLE);
        s.append(Code.encode(true));
        //s.append(Code.encode(neighborhood!=null));
        s.append(Code.encode(gbestGenome != null));
        s.append(Code.encode(gbestFitness != null));
        s.append(Code.encode(personalBestGenome != null));
        s.append(Code.encode(personalBestFitness != null));
        s.append("\n");
        
        // velocity
        s.append(Code.encode(velocity.length));
        for(int i = 0; i < velocity.length; i++)
            s.append(Code.encode(velocity[i]));
        s.append("\n");
        
     

        // neighborhood best
        if (gbestGenome != null)
            {
            s.append(Code.encode(gbestGenome.length));
            for(int i = 0; i < gbestGenome.length; i++)
                s.append(Code.encode(gbestGenome[i]));
            s.append("\n");
            }

        if (gbestFitness != null)
            s.append(gbestFitness.fitnessToString());

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
            gbestGenome = new double[(int)(d.l)];
            for(int i = 0; i < gbestGenome.length; i++)
                {
                Code.decode(d);
                if (d.type != DecodeReturn.T_DOUBLE)
                    state.output.fatal("Neighborhood-Best genome not long enough");
                gbestGenome[i] = d.d;
                }
            }
        else gbestGenome = null;

        if (nbf)
            {
            // here we don't know what kind of fitness it is.  So we'll do our best and guess
            // that it's the same fitness as our own Particle 
            gbestFitness = (Fitness)(fitness.clone());
            gbestFitness.readFitness(state, reader);
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


        if (gbestGenome != null)
            {
            dataOutput.writeBoolean(true);
            dataOutput.writeInt(gbestGenome.length);
            for(int i = 0; i < gbestGenome.length; i++)
                dataOutput.writeDouble(gbestGenome[i]);
            }
        else dataOutput.writeBoolean(false);


        if (gbestFitness != null)
            {
            dataOutput.writeBoolean(true);
            gbestFitness.writeFitness(state, dataOutput);
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
            gbestGenome = new double[dataInput.readInt()];
            for(int i = 0; i < gbestGenome.length; i++)
                gbestGenome[i] = dataInput.readDouble();
            }
        else gbestGenome = null;

        if (dataInput.readBoolean())
            {
            gbestFitness = (Fitness)(fitness.clone());
            gbestFitness.readFitness(state, dataInput);
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
