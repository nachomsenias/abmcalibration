package es.ugr.sci2s.soccer.test;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.jclouds.compute.options.TemplateOptions.Builder.runScript;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.Module;


public class TestJClouds {

	public static final Map<String, ApiMetadata> allApis = Maps.uniqueIndex(Apis.viewableAs(ComputeServiceContext.class),
        Apis.idFunction());

	public static final Map<String, ProviderMetadata> appProviders = Maps.uniqueIndex(Providers.viewableAs(ComputeServiceContext.class),
        Providers.idFunction());
	
	public static final Set<String> allKeys = ImmutableSet.copyOf(Iterables.concat(appProviders.keySet(), allApis.keySet()));
	
	public static void main(String args[]) {
		String json = "test/gpc/Zio Calibrate-7ef30b8f5503.json";
		String fileContents;
		String groupName = "mytest-group-1";
		try {
			String provider="google-compute-engine";
			// note that you can check if a provider is present ahead of time
//		    checkArgument(contains(allKeys, provider), "provider %s not in supported list: %s", provider, allKeys);
			
			fileContents = Files.toString(new File(json), UTF_8);
			Supplier<org.jclouds.domain.Credentials> credentialSupplier 
				= new GoogleCredentialsFromJson(fileContents);
	        String credential = credentialSupplier.get().credential;
	        
	        
	        String identity = credentialSupplier.get().identity;
	        //service id? 7ef30b8f55033907eb9840cc8edcc74de23424dc
	        
	        ComputeService compute = initComputeService(provider, identity, credential);
	        
	        //Add node
	        System.out.printf(">> adding node to group %s%n", groupName);

            // Default template chooses the smallest size on an operating system
            // that tested to work with java, which tends to be Ubuntu or CentOS
            TemplateBuilder templateBuilder = compute.templateBuilder();
            //Change the location
            templateBuilder.locationId("europe-west1-c");
//            templateBuilder.imageId("https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/backports-debian-7-wheezy-v20150325");
            templateBuilder.imageId("https://www.googleapis.com/compute/v1/projects/zio-calibrate/global/images/custom-image-calibrator");
            //projects/zio-calibrate/global/images/custom-image-calibrator
//            templateBuilder.osNameMatches("ubuntu");


            // note this will create a user with the same name as you on the
            // node. ex. you can connect via ssh publicip
            Statement bootInstructions = AdminAccess.standard();

            // to run commands as root, we use the runScript option in the template.
            templateBuilder.options(runScript(bootInstructions));

            Template template = templateBuilder.build();
            
            //Ensure the BC provider
            Security.addProvider(
            		new org.bouncycastle.jce.provider.BouncyCastleProvider());

            NodeMetadata node = getOnlyElement(compute.createNodesInGroup(groupName, 1, template));
            System.out.printf("<< node %s: %s%n", node.getId(),
                  concat(node.getPrivateAddresses(), node.getPublicAddresses()));
	        
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RunNodesException e) {
			System.err.println("error adding node to group " + groupName + ": " 
					+ e.getMessage());
			e.printStackTrace();
		}
        System.exit(0);
	}
	
//	private static LoginCredentials getLoginForCommandExecution() {
//      try {
//        String user = System.getProperty("user.name");
//        String privateKey = Files.toString(
//            new File(System.getProperty("user.home") + "/.ssh/id_rsa"), UTF_8);
//        return LoginCredentials.builder().
//            user(user).privateKey(privateKey).build();
//      } catch (Exception e) {
//         System.err.println("error reading ssh key " + e.getMessage());
//         System.exit(1);
//         return null;
//      }
//	}
	
	private static ComputeService initComputeService(String provider, String identity, String credential) {

	  // example of specific properties, in this case optimizing image list to
	  // only amazon supplied
	  Properties properties = new Properties();
//	  properties.setProperty(PROPERTY_EC2_AMI_QUERY, "owner-id=137112412989;state=available;image-type=machine");
//	  properties.setProperty(PROPERTY_EC2_CC_AMI_QUERY, "");
//	  long scriptTimeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES);
//	  properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");
	
	  String endpoint = "oauth.endpoint";

	  // set oauth endpoint property if set in system property
	  String oAuthEndpoint = System.getProperty(endpoint);
	  if (oAuthEndpoint != null) {
	     properties.setProperty(endpoint, oAuthEndpoint);
	  }
	
	  // example of injecting a ssh implementation
	  Iterable<Module> modules = ImmutableSet.<Module> of(
	        new SshjSshClientModule(),
	        new SLF4JLoggingModule(),
	        new EnterpriseConfigurationModule());
	  
	  ContextBuilder builder = ContextBuilder.newBuilder(provider)
	                                         .credentials(identity, credential)
	                                         .modules(modules)
	                                         .overrides(properties);
	
	  System.out.printf(">> initializing %s%n", builder.getApiMetadata());
	
	      return builder.buildView(ComputeServiceContext.class).getComputeService();
	   }
}
