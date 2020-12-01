package es.ugr.sci2s.soccer.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import es.ugr.sci2s.soccer.beans.SimulationConfig;
import org.junit.Test;

import com.google.gson.Gson;

import model.ModelDefinition;
import model.ModelRunner;
import util.exception.sales.SalesScheduleError;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics.TimePeriod;

public class StatisticTest {
	
	private MonteCarloStatistics zioStats;
	
	private MonteCarloStatistics jsonStats;
	
	private TimePeriod period;
	
	public StatisticTest() throws IOException, SalesScheduleError {
		//Zio File
		String simpleZio = "test/ZioTest.zio";
		ModelDefinition mdZio = new ModelDefinition();
		File target = new File(simpleZio);
		mdZio.loadValuesFromFile(target);
		
		zioStats=ModelRunner.simulateModel(
				mdZio, 5, false, StatisticsRecordingBean.noStatsBean());
		
		//Read the JSON file
		String jsonFile = "test/ZioTest.json";
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		SimulationConfig givenConfig = gson.fromJson(config, SimulationConfig.class);
		
		period = givenConfig.getStatPeriod();
		
		ModelDefinition mdJson = givenConfig.getModelDefinition();
		
		jsonStats=ModelRunner.simulateModel(
				mdJson, 5, false, StatisticsRecordingBean.noStatsBean());
	}

	@Test
	public void equalSales() {
		boolean equals = false;
		
		double[][][] zioSales = zioStats.computeScaledSalesByBrandByStep(period);
		
		double[][][] jsonSales = jsonStats.computeScaledSalesByBrandByStep(period);
		
		equals = Arrays.deepEquals(zioSales, jsonSales);
		
		assertTrue(equals);
	}

}
