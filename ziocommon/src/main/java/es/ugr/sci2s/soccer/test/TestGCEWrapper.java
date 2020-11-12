package es.ugr.sci2s.soccer.test;

import java.io.IOException;

import es.ugr.sci2s.soccer.util.cloud.Node;
import es.ugr.sci2s.soccer.util.cloud.gce.GCEWrapper;
import org.jclouds.compute.RunNodesException;

public class TestGCEWrapper {

	private static final String ARGUMENTS_MESSAGE =
			"This class expects two arguments: {MODE} {ID}";
	
	private static final String UNRECOGNIZED_MESSAGE =
			"Unrecognized mode.";
	
	public static void main(String[] args) {
		
		if(args.length!=2) {
			throw new IllegalArgumentException(ARGUMENTS_MESSAGE);
		}
		
		String mode = args[0];
		String id = args[1];
		try {
			if(mode.equals("CREATE")) {
				Node node = GCEWrapper.createNode();
				System.out.println("Created node id: "+node.id);
			} else if (mode.equals("DELETE")) {
				GCEWrapper.destroyNode(id);
			} else {
				throw new IllegalArgumentException(UNRECOGNIZED_MESSAGE);
			}
			GCEWrapper.closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RunNodesException e) {
			e.printStackTrace();
		}
		
		System.out.println("Script ended.");
	}
}
