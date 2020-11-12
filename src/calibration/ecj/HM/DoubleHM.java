package calibration.ecj.HM;

import ec.*;
import ec.util.*;
import ec.vector.*;
/**
 * @author ebermejo
 * @email enric2186@gmail.com
 */
public class DoubleHM extends DoubleVectorIndividual {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;


	//=========================================================================
	//		OVERRIDE OPERATIONS
	//=========================================================================
	@Override
	/**
	 * Copy the coral position along with the individual
	 */
	public Object clone() {
		// TODO Auto-generated method stub
		DoubleHM obj=(DoubleHM) super.clone();
		return obj;
	}

	@Override
	public void reset(EvolutionState state, int thread) {
		// TODO Auto-generated method stub
		super.reset(state, thread);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);
	}

	@Override
	public void reset(EvolutionState state, int thread, int newSize) {
		// TODO Auto-generated method stub
		super.reset(state, thread, newSize);
	}

	public void onestep(EvolutionState state, int thread, int a, int b, double a_value,double b_value){
		this.genome[a]=a_value;
		this.genome[b]=b_value;
		this.evaluated=false;
		
	}
	

}
