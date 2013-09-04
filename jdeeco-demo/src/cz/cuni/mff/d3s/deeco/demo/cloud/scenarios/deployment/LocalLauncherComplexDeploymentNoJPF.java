package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ENetworkId;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.LatencyGenerator;
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
public class LocalLauncherComplexDeploymentNoJPF {

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
				// 3 SCPis at the IMT Lucca
				new ScpDeploymentComponent("IMT1", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT2", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT3", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT4", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT5", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT6", ENetworkId.IMT_LUCCA),
				new ScpDeploymentComponent("IMT7", ENetworkId.IMT_LUCCA),
				// 
				new ScpDeploymentComponent("EGM1", ENetworkId.EN_GARDEN),
				new ScpDeploymentComponent("EGM2", ENetworkId.EN_GARDEN),
				new ScpDeploymentComponent("EGM3", ENetworkId.EN_GARDEN))
		);
		// list of all components which are part of the system
		List<Component> cloudComponents = new ArrayList<Component>(scpComponents);	
		// 2 Application Instances to be deployed in the cloud
		cloudComponents.add(new AppDeploymentComponent("APP1"));
		cloudComponents.add(new AppDeploymentComponent("APP2"));
		cloudComponents.add(new AppDeploymentComponent("APP3"));
		cloudComponents.add(new AppDeploymentComponent("APP4"));
		// generate the latencies on the scp components
		LatencyGenerator.generate(scpComponents, true);
		// initialize the DEECo with input initialized components
		DEECoObjectProvider dop = new DEECoObjectProvider();
		dop.addEnsemble(DeploymentEnsemble.class);
		dop.addInitialKnowledge(cloudComponents);
		rt.registerComponentsAndEnsembles(dop);
	
		rt.startRuntime();
	}
}
