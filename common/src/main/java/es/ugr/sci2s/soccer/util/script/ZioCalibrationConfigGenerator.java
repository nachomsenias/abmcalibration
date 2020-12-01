package es.ugr.sci2s.soccer.util.script;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.CalibrationConfig;
import es.ugr.sci2s.soccer.beans.SimulationConfig;

import calibration.CalibrationConsole;
import calibration.CalibrationController;
import model.ModelDefinition;
import util.StringBean;
import util.io.CSVFileUtils;
import util.statistics.Statistics.TimePeriod;

public class ZioCalibrationConfigGenerator {

	public static void main(String args[]) {
		
		if(args.length!=2 && args.length!=3) {
			throw new IllegalArgumentException(
					"This generator uses two parameters: a calibration "
					+ "properties file and new json file name."
						);
		}
		
		//Load calibration parameters using default file structure
		CalibrationController controller 
			= CalibrationConsole.getCalibrationController(args[0]);
		
		CalibrationConfig config = new CalibrationConfig();
		config.setTargetSales(controller.getTask()
				.getHistoryManager().getSalesAggregatedHistory());
		config.setHistorySalesPeriod(TimePeriod.QUARTERLY.toString());
		config.setTotalSalesWeight(1.0);
		
		SimulationConfig simConfig = new SimulationConfig();
		simConfig.setnMC(30);
		ModelDefinition md = controller.getTask().getModelDefinition();
		simConfig.loadFromModelDefinition(md);
		config.setSimConfig(simConfig);
		
		
		StringBean[] parameterArray = 
				controller.getTask().getCalibrationParametersManager()
					.getUnconvertedParameters();
		config.setCalibrationModelParameters(parameterArray);
		
		List<StringBean> stringList = new ArrayList<StringBean>(Arrays.asList(parameterArray));
		
		//Driver 1 para el segmento 0
		StringBean bean = new StringBean("SegmentDrivers_0_1", "0.05,1.0,0.05");
		stringList.add(bean);
		//Driver 0 para todos los segmentos
		stringList.add(new StringBean("SegmentDrivers_0", "0.05,1.0,0.05"));
		
		config.setCalibrationModelParameters(stringList.toArray(parameterArray));
		
		config.setId(31416);
		
		String jsonFileName = args[1];
		
		if(args.length==3) {
			//Awareness File expected.
			String awarenessFile = args[2];
			try {
				double[][] awareness = 
						CSVFileUtils.readDoubleTwoDimArrayFromCSV(awarenessFile);
				
				config.setTargetAwareness(awareness);
				config.setTargetAwarenessPeriod("QUARTERLY");
				
				config.setAwarenessWeight(0.5);
				config.setSalesWeight(0.5);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Gson gson = new Gson();
		String translated = gson.toJson(config, CalibrationConfig.class);
		
		try {
			FileWriter fw = new FileWriter(jsonFileName);
			fw.write(translated);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
