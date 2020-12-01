package es.ugr.sci2s.soccer.util.cloud.gce;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.jclouds.compute.options.TemplateOptions.Builder.runScript;
import static org.jclouds.compute.predicates.NodePredicates.inGroup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Security;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Module;

import es.ugr.sci2s.soccer.util.cloud.Node;
import util.io.ConfigFileReader;

/**
 * Wrappes the interaction with the google cloud engine.
 * @author imoya
 *
 */
public class GCEWrapper {
	
	private static ComputeService computeService = null;

	private final static String CONFIG ="CloudSettings.properties";
	
	private static String JSON_CREDENTIALS = "ZioCalibrate-7ef30b8f5503.json";
	private static String GROUP_NAME = "calibrator-group";
	
	private static String CALIBRATOR_IMAGE = "https://www.googleapis.com/compute/v1/projects/zio-calibrate/global/images/custom-image-calibrator";
	private static String SLAVE_HW = "https://www.googleapis.com/compute/v1/projects/zio-calibrate/zones/europe-west1-c/machineTypes/n1-highcpu-2";
	
	private static String LOCATION = "europe-west1-c";
	
	/**
	 * Initializes the compute engine service using given identity and credentials.
	 * @param identity the authorized user identity.
	 * @param credential the authorized user credentials.
	 * @return the initialized compute engine service.
	 */
	private final static ComputeService getComputeService(
			String identity, String credential
		) {
		
		Properties properties = new Properties();

		String endpoint = "oauth.endpoint";

		// set oauth endpoint property if set in system property
		String oAuthEndpoint = System.getProperty(endpoint);
		if (oAuthEndpoint != null) {
			properties.setProperty(endpoint, oAuthEndpoint);
		}
		// example of injecting a ssh implementation
		Iterable<Module> modules = ImmutableSet.<Module> of(
				new SshjSshClientModule(), new SLF4JLoggingModule(),
				new EnterpriseConfigurationModule());

		ContextBuilder builder = 
				ContextBuilder.newBuilder("google-compute-engine")
					.credentials(identity, credential)
						.modules(modules)
						.overrides(properties);

		System.out.printf(">> initializing %s%n", builder.getApiMetadata());

		return builder.buildView(ComputeServiceContext.class).getComputeService();
	}
	
	private final static void readConfigFile() {
		ConfigFileReader config = new ConfigFileReader();
		config.readConfigFile(new File(findFilePath(CONFIG)));
		
		JSON_CREDENTIALS = config.getParameterString("JSON_CREDENTIALS");
		
		GROUP_NAME = config.getParameterString("GROUP_NAME");
		
		CALIBRATOR_IMAGE = config.getParameterString("CALIBRATOR_IMAGE");
		SLAVE_HW = config.getParameterString("SLAVE_HW");
		
		LOCATION = config.getParameterString("LOCATION");
	}
	
	/**
	 * If no compute service has been initialized yet, retrieves the authorized
	 * user credentials and creates one.
	 * @throws IOException
	 * 				if the json credentials couldn't be loaded.
	 */
	private final static void initializeComputeService() throws IOException {
		//Read configuration parameters
		readConfigFile();
		
		// Find the json configuration file
		String credentialsPath = findFilePath(JSON_CREDENTIALS);
//		URL credentialsURL = GCEWrapper.class.getClassLoader().getResource(JSON_CREDENTIALS);

		// Retrieve credentials
		String fileContents = Files.toString(new File(credentialsPath), UTF_8);
		Supplier<org.jclouds.domain.Credentials> credentialSupplier = new GoogleCredentialsFromJson(fileContents);

		String credential = credentialSupplier.get().credential;
		String identity = credentialSupplier.get().identity;

		//Initializes de GCE service
		computeService = getComputeService(identity, credential);
	}
	
	/**
	 * Searchs for the JSON credentials path.
	 * @return the JSON credentials path.
	 */
	private final static String findFilePath(String fileName) {
		//Once the servlet is deployed in Tomcat, the file needs to be 
		//found this way.
		URL credentialsURL = GCEWrapper.class.getClassLoader().getResource(fileName);
		
		if(credentialsURL==null) {
			//If the file is looked for outside Tomcat, the relative path is enough.
			return fileName;
		} else {
			return credentialsURL.getPath();
		}
	}

	/**
	 * Creates a new node in the google compute engine environment and returns
	 * the public ip of the created machine.
	 * 
	 * @return the public ip of the created machine.
	 * @throws IOException
	 *             if the json credentials couldn't be loaded.
	 * @throws RunNodesException
	 *             if exceptions are thrown during node creation.
	 */
	public final static Node createNode() throws IOException, RunNodesException {
		//Checks wherever the compute service is enabled.
		if(computeService==null) {
			initializeComputeService();
		}

		TemplateBuilder templateBuilder = computeService.templateBuilder();
		// Customize template machine
		templateBuilder.locationId(LOCATION);
		templateBuilder.imageId(CALIBRATOR_IMAGE);
		templateBuilder.hardwareId(SLAVE_HW);

		// Set the access credentials
		Statement bootInstructions = AdminAccess.standard();

		// runScript runs commands as root
		templateBuilder.options(runScript(bootInstructions));

		Template template = templateBuilder.build();

		// Ensure the BC provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		//Create the node.
		NodeMetadata node = getOnlyElement(
				computeService.createNodesInGroup(GROUP_NAME, 1, template));
//		String nodeIp = getOnlyElement(node.getPublicAddresses());
		String nodeIp = getOnlyElement(node.getPrivateAddresses());
		
		Node newNode = new Node(node.getId(), nodeIp);
		
		System.out.printf("** Created node %s: with public ip: %s%n", newNode.id, nodeIp);

		return newNode;
	}
	
	public final static void destroyNode(String id) throws IOException {
		//Checks wherever the compute service is enabled.
		if(computeService==null) {
			initializeComputeService();
		}
		computeService.destroyNode(id);
	}
	
	/**
	 * Destroy every created node in the group pool.
	 * @throws IOException
	 * 				if the json credentials couldn't be loaded.
	 */
	public final static void shutdownAllNodes() throws IOException {
		//Checks wherever the compute service is enabled.
		if(computeService==null) {
			initializeComputeService();
		}
		// Destroy every node in the group pool
		System.out.printf("** Destroying nodes in group %s%n", GROUP_NAME);
		Set<? extends NodeMetadata> destroyed = 
				computeService.destroyNodesMatching(inGroup(GROUP_NAME));
		System.out.printf("<< destroyed nodes %s%n", destroyed);
	}
	
	/**
	 * Closes the connection to the compute serve if it is still running.
	 */
	public final static void closeConnection() {
		if(computeService!=null) {
			computeService.getContext().close();
		}
	}
}
