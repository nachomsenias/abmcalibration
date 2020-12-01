package es.ugr.sci2s.soccer.util.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import es.ugr.sci2s.soccer.beans.CalibrationResponse;

public class ResponseWrapper {

	public static void main(String[] args) throws IOException {
		String json = args[0];
		
//		String history1 = args[1];
//		String history2 = args[2];
		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(json));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		Gson gson = new Gson();
		
		CalibrationResponse resultConfig = gson.fromJson(config, CalibrationResponse.class);

		String output = gson.toJson(resultConfig, CalibrationResponse.class);
		
		//Export to File
//		FileWriter fw = new FileWriter(args[3]);
		FileWriter fw = new FileWriter(args[1]);
		fw.write(output);
		fw.close();
	}

}
