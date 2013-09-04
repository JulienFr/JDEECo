package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.AppBalancer;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ENetworkId;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.LatencyGenerator;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment.ScpDeploymentComponent;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.RepositoryKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.local.LocalKnowledgeRepository;
import cz.cuni.mff.d3s.deeco.provider.DEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.runtime.Runtime;
import cz.cuni.mff.d3s.deeco.scheduling.MultithreadedScheduler;
import cz.cuni.mff.d3s.deeco.scheduling.Scheduler;

/**
 * The Science Cloud Platform (Scp) local launcher for the Deployment Scenario (DS).
 * 
 * @author Julien Malvot
 * 
 */
public class LocalLauncherBalanceDeploymentNoJPF {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		KnowledgeManager km = new RepositoryKnowledgeManager(
				new LocalKnowledgeRepository());
		Scheduler scheduler = new MultithreadedScheduler();
		Runtime rt = new Runtime(km, scheduler);
	
		List<Component> scpComponents = new ArrayList<Component>( 
			Arrays.asList(
				// 3 SCPis at the LMU Munich 
				new ScpDeploymentComponent("LMU1", ENetworkId.LMU_MUNICH),
				new ScpDeploymentComponent("LMU2", ENetworkId.LMU_MUNICH),
				new ScpDeploymentComponent("LMU3", ENetworkId.LMU_MUNICH),
				// 3 SCPis at the IMT Lucca
				new ScpDeploymentComponent("IMT1", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT2", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT3", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("EGM1", ENetworkId.EN_GARDEN))
		);
		// list of all components which are part of the system
		List<Component> cloudComponents = new ArrayList<Component>(scpComponents);	
		// 2 Application Instances to be deployed in the cloud
		List<String> ids = Arrays.asList("APP1", "APP2");
		// injects the balancer role to the application 1 w.r.t. the other application component APP2
		cloudComponents.add(new AppBalanceDeploymentComponent("APP1", new AppBalancer(ids)));
		cloudComponents.add(new AppBalanceDeploymentComponent("APP2"));
		// generate the latencies on the scp components
		LatencyGenerator.generate(scpComponents, true);	
		// initialize the DEECo with input initialized components
		DEECoObjectProvider dop = new DEECoObjectProvider();
		dop.addEnsemble(ScpBalanceDeploymentEnsemble.class);
		dop.addEnsemble(AppBalanceDeploymentEnsemble.class);
		dop.addEnsemble(FilterScpBalanceDeploymentEnsemble.class);
		dop.addInitialKnowledge(cloudComponents);
		rt.registerComponentsAndEnsembles(dop);
	
		rt.startRuntime();
	}
}
