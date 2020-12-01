package es.ugr.sci2s.soccer.workers;

import java.util.Map;

import es.ugr.sci2s.soccer.beans.ResultContainer;
import es.ugr.sci2s.soccer.beans.SimulationResult;

public class AnsweringWorker extends SimulationWorker {

	private String host;
	
	public AnsweringWorker(String config, ResultContainer result, 
			Map<Integer, ResultContainer> table, int id, String host) {
		super(config, result, table, id);
		this.host = host;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		super.run();
		
		String response;
		
		switch (executionType) {
		case SIMPLE:
			response = gson.toJson(result.getSimpleResult(), SimulationResult.class);
			break;
		case SENSITIVITY_ANALYSIS:
			response = gson.toJson(result.getSaResult(), SimulationResult[][].class);
			break;
		case TP_CONTRIBUTION:
			response = gson.toJson(result.getComparisonResult(), SimulationResult[].class);
			break;
		case COMPARISON:
			response = gson.toJson(result.getComparisonResult(), SimulationResult[].class);
			break;
		default:
			SimulationResult errorResult = new SimulationResult();
			errorResult.setErrorMessage("Invalid simulation type.");
			response = gson.toJson(errorResult, SimulationResult.class);
		}
		
		//XXX Repeat until X attempts.

		String urlEntry=referenceConfig.getResponseURL();
		String url = host.concat(urlEntry);
		
		//Send message
		
		// XXX This currently throws compilation errors due to 
		// org.apache.http.NameValuePair type

//		try {
////			Request.Post(url)
////			.bodyForm(Form.form()
////					.add("result",response).build())
////			.execute();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
