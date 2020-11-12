package test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.decisionmaking.DecisionMaking;
import util.random.Randomizer;
import util.random.RandomizerFactory;

public class DecisionMakingSelectHeuristic {
	private static final int ITERATIONS = 1000;
	private static final int NR_OF_HEURISTICS = 4;
	private static final String FILE_NAME_EXP 
	= "../experiments/decisionMaking/heuristicSelectionExperiment.txt";
	private static final String[] HEURISTIC_NAMES 
	= {"utility_maximization", "majority_rule", "elimination_by_aspects", "satisficing"};	
	private int[][] results;
		
	// ########################################################################
	// Constructors
	// ######################################################################## 	

	public DecisionMakingSelectHeuristic() {
		this.results = null;
	}
	
	// ########################################################################	
	// Methods/Functions 	
	// ########################################################################

	private static Collection<Object[]> involvedEmotionalData() {
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] data;
		data = new Object[] {new double[]{1.0, 0, 0, 0}}; // involved, nonemotional (UMAX)
		list.add(data);
		data = new Object[] {new double[]{0, 1.0, 0, 0}}; // involved, emotional (MRULE)
		list.add(data);
		data = new Object[] {new double[]{0, 0, 1.0, 0}}; // noninvolved, nonemotional (EBA)
		list.add(data);
		data = new Object[] {new double[]{0, 0, 0, 1.0}}; // noninvolved, emotional (SAT)
		list.add(data);
		data = new Object[] {new double[]{0.25, 0.25, 0.25, 0.25}}; // equal for all heuristics
		list.add(data);
		data = new Object[] {new double[]{0.5, 0.5, 0, 0}};
		list.add(data);	
		data = new Object[] {new double[]{0.5, 0, 0.5, 0}};
		list.add(data);		
		data = new Object[] {new double[]{0.5, 0, 0, 0.5}};
		list.add(data);
		data = new Object[] {new double[]{0.25, 0.75, 0, 0}};
		list.add(data);	
		data = new Object[] {new double[]{0.25, 0, 0.75, 0}};
		list.add(data);		
		data = new Object[] {new double[]{0.25, 0, 0, 0.75}};
		list.add(data);
		
		return list;
	}
	
	private void run() {
		List<Object[]> listInput;
		Randomizer r = RandomizerFactory.createDefaultRandomizer();
		double[] heuristicSelectionProb = new double[NR_OF_HEURISTICS];
		int res;
		int nrBrands = 1;
		int nrAttributes = 1;
		double[][] drivers = new double[1][1];
		
		drivers[0][0] = 1.0;
		
		System.out.println("Running heuristic selection...");
		
		listInput = (ArrayList<Object[]>) involvedEmotionalData();
		results = new int[NR_OF_HEURISTICS][listInput.size()];
		
		for(int i=0; i<ITERATIONS; i++) {
			for(int j=0; j<listInput.size(); j++) {
				r.setSeed(i);
				heuristicSelectionProb = (double[]) listInput.get(j)[0];
				
				DecisionMaking dm = new DecisionMaking(
						r, drivers, heuristicSelectionProb[0], 
						heuristicSelectionProb[1], heuristicSelectionProb[2], 
						heuristicSelectionProb[3], nrAttributes, nrBrands);
				res = dm.selectHeuristic();
				
				this.results[res][j]++;
			}
		}
		for(int j=0; j<listInput.size(); j++) {
			System.out.println("Inputs: " + j);
			for(int i=0; i<NR_OF_HEURISTICS; i++) {
				System.out.print(results[i][j] + " ");
			}
			System.out.println();			
		}
		this.writeToFile();
	}

	//----------------------------- I/O methods -----------------------------//
	
	private void writeToFile() {
		PrintStream streamFile;
		List<Object[]> listInput;
		
		listInput = (ArrayList<Object[]>) involvedEmotionalData();		
		
		try {
			streamFile = new PrintStream(new FileOutputStream(FILE_NAME_EXP, false));	
			// First line
			String aux = "Heuristic drivers ";
			streamFile.println(aux);			
			// Second line
			aux = "";
			for(int i=0; i<NR_OF_HEURISTICS; i++) {
				aux += " ";
			}
			aux += " ";			
			for(int i=0; i<NR_OF_HEURISTICS; i++) {
				aux += HEURISTIC_NAMES[i] + " ";
			}
			streamFile.println(aux);

			for(int j=0; j<listInput.size(); j++) {
				// Input
				for(int i=0; i<NR_OF_HEURISTICS; i++) {
					String inAux = Double.toString(((double[]) listInput.get(j)[0])[i]);
					streamFile.print(inAux+ " ");
				}
				streamFile.print(" ");
				// Results			
				for(int i=0; i<NR_OF_HEURISTICS; i++) {
					streamFile.print(results[i][j] + " ");
				}
				streamFile.println();		
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		DecisionMakingSelectHeuristic expDMselectHeuristic = new DecisionMakingSelectHeuristic();
		expDMselectHeuristic.run();
	}
}
