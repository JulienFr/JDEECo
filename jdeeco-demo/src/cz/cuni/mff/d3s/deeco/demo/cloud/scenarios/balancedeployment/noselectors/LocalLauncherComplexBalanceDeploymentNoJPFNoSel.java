package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.noselectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * The Science Cloud Platform (Scp) local launcher for the complex Deployment Scenario (DS).
 * 
 * The launcher is the same as the non-complex deployment scenario local launcher,
 * but with more scp nodes and app nodes to test the robustness of the ensemble.
 * 
 * @author Julien Malvot
 * 
 */
public class LocalLauncherComplexBalanceDeploymentNoJPFNoSel {

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
				new ScpDeploymentComponent("LMU4", ENetworkId.LMU_MUNICH),
				new ScpDeploymentComponent("LMU5", ENetworkId.LMU_MUNICH),
				new ScpDeploymentComponent("LMU6", ENetworkId.LMU_MUNICH),
				new ScpDeploymentComponent("LMU7", ENetworkId.LMU_MUNICH),
				new ScpDeploymentComponent("LMU8", ENetworkId.LMU_MUNICH),
				new ScpDeploymentComponent("LMU9", ENetworkId.LMU_MUNICH),
				// 3 SCPis at the IMT Lucca
				new ScpDeploymentComponent("IMT1", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT2", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT3", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT4", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT5", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT6", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT7", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT8", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT9", ENetworkId.IMT_LUCCA),
				// 
				new ScpDeploymentComponent("EGM1", ENetworkId.EN_GARDEN),
				new ScpDeploymentComponent("EGM2", ENetworkId.EN_GARDEN),
				new ScpDeploymentComponent("EGM3", ENetworkId.EN_GARDEN))
		);
		// list of all components which are part of the system
		List<Component> cloudComponents = new ArrayList<Component>(scpComponents);	
		// 2 Application Instances to be deployed in the cloud
		List<String> ids = Arrays.asList("APP1", "APP2", "APP3", "APP4");
		// injects the balancer role to the application 1 w.r.t. the other application component APP2
		cloudComponents.add(new AppBalanceDeploymentComponentNoSel("APP1", new AppDeploymentBalancerNoSel(ids)));
		cloudComponents.add(new AppBalanceDeploymentComponentNoSel("APP2"));
		cloudComponents.add(new AppBalanceDeploymentComponentNoSel("APP3"));
		cloudComponents.add(new AppBalanceDeploymentComponentNoSel("APP4"));
		// generate the latencies on the scp components
		LatencyGenerator.generate(scpComponents, true);
		// initialize the DEECo with input initialized components
		DEECoObjectProvider dop = new DEECoObjectProvider();
		dop.addEnsemble(SelectScpComponentsForDeploymentEnsembleNoSel.class);
		dop.addInitialKnowledge(cloudComponents);
		rt.registerComponentsAndEnsembles(dop);
	
		rt.startRuntime();
	}
}
