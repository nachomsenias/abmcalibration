package es.ugr.sci2s.soccer.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.ugr.sci2s.soccer.beans.SimulationConfig;
import model.Model;
import model.ModelBean;
import model.ModelBuilder;
import model.ModelDefinition;
import model.sales.SalesScheduler;
import model.touchpoints.TouchPointOwnedRegistry;
import model.touchpoints.TouchPointScheduler;
import test.ReachTestBean;
import util.exception.simulation.SimulationException;
import util.random.RandomizerUtils;

public class TestReachParameter {

	public class SimpleReachTestBean {
		private double[] actualRM;
		
		private boolean [] agentsReached;

		private double [][] reachEvolution;
		private double [][] actualRMByStep;
		
		public SimpleReachTestBean(ReachTestBean bean) {
			this.actualRM = bean.getActualRM();
			
			this.agentsReached = bean.getAgentsReached();
			
			this.reachEvolution = bean.getReachEvolution();
			this.actualRMByStep = bean.getActualRMByStep();
		}

		public double[] getActualRM() {
			return actualRM;
		}

		public void setActualRM(double[] actualRM) {
			this.actualRM = actualRM;
		}

		public boolean[] getAgentsReached() {
			return agentsReached;
		}

		public void setAgentsReached(boolean[] agentsReached) {
			this.agentsReached = agentsReached;
		}

		public double[][] getReachEvolution() {
			return reachEvolution;
		}

		public void setReachEvolution(double[][] reachEvolution) {
			this.reachEvolution = reachEvolution;
		}

		public double[][] getActualRMByStep() {
			return actualRMByStep;
		}

		public void setActualRMByStep(double[][] actualRMByStep) {
			this.actualRMByStep = actualRMByStep;
		}
	}
	
	
	public static void main(String[] args) throws IOException, SimulationException {
		if(args.length!=2) {
			throw new IllegalArgumentException("This test uses 2 argument.");
		}
		
		String jsonFile = args[0];
		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		//Get ModelDefinition
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		SimulationConfig givenConfig = gson.fromJson(config, SimulationConfig.class);
		
		ModelDefinition md = givenConfig.getModelDefinition();
		md.setDebug(true);
		
		ModelBuilder mb = md.createBuilder();
		ModelBean bean = mb.createBean();
		Model m = mb.build(bean, RandomizerUtils.PRIME_SEEDS[0]);
		
		SalesScheduler ss = m.getSalesScheduler();
		ss.enableTest();
		
		TouchPointOwnedRegistry tpor = m.getTPORegistry();

		TouchPointScheduler[] tps = new TouchPointScheduler[0];		
		tps = tpor.getSchedulers().toArray(tps);
		
		int numSchedulers = tps.length;
		
		String baseName = args[1];
		
		for (int i=0; i<numSchedulers; i++) {
			
			ReachTestBean rtb = tpor.getTest(i, 0);
			SimpleReachTestBean srtb = new TestReachParameter().new SimpleReachTestBean(rtb);
			String out = gson.toJson(srtb, SimpleReachTestBean.class);
			
			FileWriter fw = new FileWriter(baseName+"_"+i);
			fw.write(out);
			fw.close();
		}	
	}
}
