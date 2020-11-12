/**
 * 
 */
package calibration.ecj.CoralReefOptimizer;



import ec.*;
import ec.util.*;
import java.util.*;
/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class CoralSubPopulation extends Subpopulation {
	/**
	 * Default serial version
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//=========================================================================
	//		FIELDS
	//=========================================================================
	/*List of reef occupation- assigns the individual number of the population*/
	public int[] cro_ReefO;
	/*Array*/
	public ArrayList<Double> cro_ReefFit;
	public ArrayList<Integer> cro_EmptyIndvs;
	public float cro_r0;
	public int next_index;
	
	public static final String P_CRO_R0 ="cro_r0"; // Parameter to control initial reef occupation
		

	//=========================================================================
	//		METHODS
	//=========================================================================
	/*
	 * Sort the empty individuals array
	 */
	public void sortEmptyIndexes(){
		Collections.sort(this.cro_EmptyIndvs);
	}
	
	/* 
	 * Initialize Reef lists
	 */
	public void buildReefFit() {
		int pos=-1; 
		for(int i=0;i<individuals.length;i++)
			cro_ReefFit.set(i,0.0);

		for(int i=0;i<individuals.length;i++){
			if(individuals[i] instanceof IntegerCoral)
				pos=((IntegerCoral)individuals[i]).getPosition();
			else if(individuals[i] instanceof DoubleCoral)
				pos=((DoubleCoral)individuals[i]).getPosition();
			if(pos!=-1){
				cro_ReefFit.set(pos,individuals[i].fitness.fitness());
			}
		}
	
	}
	
	/*
	 * Sort Reef fitness (For depredation)
	 */
	public Integer[] sortReefFit(){
		Integer [] indexes =new Integer[cro_ReefFit.size()];

		for (int n=0; n< cro_ReefFit.size();n++){
			indexes[n]=n;
		}

		Arrays.sort(indexes, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		        return Double.compare(cro_ReefFit.get(o1), cro_ReefFit.get(o2));
		    }
		});
		return indexes;
	}
	
	@Override
	public void setup(EvolutionState state, Parameter base) {
		Parameter def = defaultBase();
		// TODO Auto-generated method stub
		super.setup(state, base);
		this.cro_r0=state.parameters.getFloat(base.push(P_CRO_R0), def.push(P_CRO_R0),0.6);
		this.cro_ReefO=new int[individuals.length];
		this.cro_ReefFit=new ArrayList<Double>(individuals.length);
		this.cro_EmptyIndvs=new ArrayList<Integer>(individuals.length);
		for (int i=0;i<individuals.length;i++){
			this.cro_ReefO[i]=-1;
			this.cro_ReefFit.add(0.0);
		}
	}

	@Override
	/*
	 * Initialize the population. Mark cro_r0 individuals as valid. Rest as invalid with no position in the reef
	 */
	public void populate(EvolutionState state, int thread) {
        int start = 0;                         //where to start filling new individuals in -- may get modified if we read some individuals in
        
        // should we load individuals from a file? -- duplicates are permitted
       /* if (loadInds)
            {
            InputStream stream = state.parameters.getResource(file,null);
            if (stream == null)
                state.output.fatal("Could not load subpopulation from file", file);
            
            try { readSubpopulation(state, new LineNumberReader(new InputStreamReader(stream))); }
            catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + state.parameters.getString(file, null) + ".  The IOException was: \n" + e,
                    file, null); }
            
            if (len < individuals.length)
                {
                state.output.message("Old subpopulation was of size " + len + ", expanding to size " + individuals.length);
                return;
                }
            else if (len > individuals.length)   // the population was shrunk, there's more space yet
                {
                // What do we do with the remainder?
                if (extraBehavior == TRUNCATE)
                    {
                    state.output.message("Old subpopulation was of size " + len + ", truncating to size " + individuals.length);
                    return;  // we're done
                    }
                else if (extraBehavior == WRAP)
                    {
                    state.output.message("Only " + individuals.length + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled with copies of the read-in individuals.");
                        
                    Individual[] oldInds = individuals;
                    individuals = new Individual[len];
                    System.arraycopy(oldInds, 0, individuals, 0, oldInds.length);
                    start = oldInds.length;
                                
                    int count = 0;
                    for(int i = start; i < individuals.length; i++)
                        {
                        individuals[i] = (Individual)(individuals[count].clone());
                        if (++count >= start) count = 0;
                        }
                    return;
                    }
                else // if (extraBehavior == FILL)
                    {
                    state.output.message("Only " + individuals.length + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled using randomly generated individuals.");
                        
                    Individual[] oldInds = individuals;
                    individuals = new Individual[len];
                    System.arraycopy(oldInds, 0, individuals, 0, oldInds.length);
                    start = oldInds.length;
                    // now go on to fill the rest below...
                    }                       
                }
            else // exactly right number, we're dont
                {
                return;
                }
            }*/
        state.output.message("Populating Reef");
        int stop=(int) Math.round(individuals.length*this.cro_r0);
        int grid;
        // populating the remainder with random individuals
        HashMap<Individual,Individual> h = null;
        if (numDuplicateRetries >= 1)
            h = new HashMap<Individual, Individual>((stop - start) / 2);  // seems reasonable

        for(int x=start;x<stop;x++) 
            {
            for(int tries=0; 
                tries <= /* Yes, I see that*/ numDuplicateRetries; 
                tries++)
                {
                individuals[x] = species.newIndividual(state, thread);
                grid=-1;
                //Loop reef occupation array to set the new individual in an empty location
                
                while (grid<0 || grid>=individuals.length){
                	grid=state.random[thread].nextInt(individuals.length);
//                	if(l>0 && l<individuals.length-1)
	                	if(this.cro_ReefO[grid]!=-1)          		grid=-1;
                }
                //Set the coral in the reef at position l and mark the occupation array of l to individual x
                if(individuals[x] instanceof IntegerCoral)
                	((IntegerCoral) individuals[x]).setPosition(grid);
                else if(individuals[x] instanceof DoubleCoral)
                	((DoubleCoral) individuals[x]).setPosition(grid);
                this.cro_ReefO[grid]= x;
                this.cro_ReefFit.set(grid,individuals[x].fitness.fitness());
                if (numDuplicateRetries >= 1)
                    {
                    // check for duplicates
                    Object o = h.get(individuals[x]);
                    if (o == null) // found nothing, we're safe
                        // hash it and go
                        {
                        h.put(individuals[x],individuals[x]);
                        break;
                        }
                    }
                }  // oh well, we tried to cut down the duplicates
            }
        next_index=stop;
        //TODO: check this works with null elements or replace them with random and no position
        for(int x=stop; x<individuals.length;x++){
        	individuals[x]=species.newIndividual(state, thread);
        	//individuals[x].evaluated=true;
        	if(individuals[x] instanceof IntegerCoral)
            	((IntegerCoral) individuals[x]).setPosition(-1);
            else if(individuals[x] instanceof DoubleCoral)
            	((DoubleCoral) individuals[x]).setPosition(-1);
        	this.cro_EmptyIndvs.add(x);;
        }
        state.output.message("Reef is alive");
	}	
}
