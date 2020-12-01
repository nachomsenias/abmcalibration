package es.ugr.sci2s.soccer.util.script;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import util.io.CSVFileUtils;

public class RenameResults {

	public static void main(String[] args) throws IOException {
		String root = args[0];
		
//		String method = "IPOP-CMA-ES-HC-real";
		String method = "IPOP-CMA-ES-OPT-real";
		
//		String method = "CMA-ES-OPT-real";
//		String method = "LSHADE-real";
		
		for (int p=0; p<GenerateReports.problems.length; p++) {
			String folder = root+GenerateReports.problems[p]+"/"+method;
			
			File directory = new File(folder);
			if(directory.isDirectory()) {
				
				File[] files = directory.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.contains("_it") 
								&& name.endsWith(".json");
					}
				});
				
				for (File result : files) {
					String path = result.getAbsolutePath();
					
					if(path.charAt(path.length()-7) == '2') {
						String content = CSVFileUtils.readFile(path);
						
						StringBuffer sb = new StringBuffer(path);
						sb.setCharAt(path.length()-7, '1');
						
						CSVFileUtils.writeFile(sb.toString(), content);
						
						result.deleteOnExit();
						
						System.out.println("File "+result.getName()+" renamed into "+sb.toString());
					}
					
				}
				
				System.out.println("================================");
			}
		}
	}

}
