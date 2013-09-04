package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment.ScpDSComponent;
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
public class LocalLauncherComplexBDSNoJPF {

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
				new ScpDSComponent("LMU1", ENetworkId.LMU_MUNICH),
				new ScpDSComponent("LMU2", ENetworkId.LMU_MUNICH),
				new ScpDSComponent("LMU3", ENetworkId.LMU_MUNICH),
				new ScpDSComponent("LMU4", ENetworkId.LMU_MUNICH),
				new ScpDSComponent("LMU5", ENetworkId.LMU_MUNICH),
				new ScpDSComponent("LMU6", ENetworkId.LMU_MUNICH),
				new ScpDSComponent("LMU7", ENetworkId.LMU_MUNICH),
				// 3 SCPis at the IMT Lucca
				new ScpDSComponent("IMT1", ENetworkId.IMT_LUCCA),
				new ScpDSComponent("IMT2", ENetworkId.IMT_LUCCA),
				new ScpDSComponent("IMT3", ENetworkId.IMT_LUCCA),
				new ScpDSComponent("IMT4", ENetworkId.IMT_LUCCA),
				new ScpDSComponent("IMT5", ENetworkId.IMT_LUCCA),
				new ScpDSComponent("IMT6", ENetworkId.IMT_LUCCA),
				new ScpDSComponent("IMT7", ENetworkId.IMT_LUCCA),
				// 
				new ScpDSComponent("EGM1", ENetworkId.EN_GARDEN),
				new ScpDSComponent("EGM2", ENetworkId.EN_GARDEN),
				new ScpDSComponent("EGM3", ENetworkId.EN_GARDEN))
		);
		// list of all components which are part of the system
		List<Component> cloudComponents = new ArrayList<Component>(scpComponents);	
		// 2 Application Instances to be deployed in the cloud
		cloudComponents.add(new AppBDSComponent("APP1"));
		cloudComponents.add(new AppBDSComponent("APP2"));
		cloudComponents.add(new AppBDSComponent("APP3"));
		cloudComponents.add(new AppBDSComponent("APP4"));
		// generate the latencies on the scp components
		LatencyGenerator.generate(scpComponents, true);
		// initialize the DEECo with input initialized components
		DEECoObjectProvider dop = new DEECoObjectProvider();
		dop.addEnsemble(ScpBalanceEnsemble.class);
		dop.addInitialKnowledge(cloudComponents);
		rt.registerComponentsAndEnsembles(dop);
	
		rt.startRuntime();
	}
}
