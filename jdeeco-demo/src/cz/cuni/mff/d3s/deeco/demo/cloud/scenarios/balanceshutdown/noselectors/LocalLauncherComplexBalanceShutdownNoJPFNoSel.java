package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown.noselectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ENetworkId;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.LatencyGenerator;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.shutdown.ScpShutdownComponent;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.RepositoryKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.local.LocalKnowledgeRepository;
import cz.cuni.mff.d3s.deeco.provider.DEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.runtime.Runtime;
import cz.cuni.mff.d3s.deeco.scheduling.MultithreadedScheduler;
import cz.cuni.mff.d3s.deeco.scheduling.Scheduler;

/**
 * 
 * @author Julien Malvot
 *
 */
public class LocalLauncherComplexBalanceShutdownNoJPFNoSel {
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		KnowledgeManager km = new RepositoryKnowledgeManager(
				new LocalKnowledgeRepository());
		Scheduler scheduler = new MultithreadedScheduler();
		Runtime rt = new Runtime(km, scheduler);
		List<Class<?>> ensembles = Arrays.asList(new Class<?>[] { 
				// responsible for selecting the scp nodes and backup using selectors
				SelectScpComponentsForShutdownEnsembleNoSel.class
		});
		
		List<Component> scpComponents = new ArrayList<Component>(Arrays.asList(
				// 3 SCPis at the LMU Munich
				new ScpShutdownComponent("LMU1", ENetworkId.LMU_MUNICH, Arrays.asList(2400, 2400)),
				new ScpShutdownComponent("LMU2", ENetworkId.LMU_MUNICH, Arrays.asList(2200, 2200)),
				new ScpShutdownComponent("LMU3", ENetworkId.LMU_MUNICH, Arrays.asList(1600, 1600)),
				new ScpShutdownComponent("LMU4", ENetworkId.LMU_MUNICH, Arrays.asList(2200, 2200)),
				new ScpShutdownComponent("LMU5", ENetworkId.LMU_MUNICH, Arrays.asList(2100, 2000)),
				new ScpShutdownComponent("LMU6", ENetworkId.LMU_MUNICH, Arrays.asList(2200, 2200)),
				new ScpShutdownComponent("LMU7", ENetworkId.LMU_MUNICH, Arrays.asList(2100, 2000)),
				// 3 SCPis at the IMT Lucca
				new ScpShutdownComponent("IMT1", ENetworkId.IMT_LUCCA, Arrays.asList(2400, 2400)),
				new ScpShutdownComponent("IMT2", ENetworkId.IMT_LUCCA, Arrays.asList(2200, 2200)),
				new ScpShutdownComponent("IMT3", ENetworkId.IMT_LUCCA, Arrays.asList(1600, 1600)),
				new ScpShutdownComponent("IMT4", ENetworkId.IMT_LUCCA, Arrays.asList(2000, 2500)),
				new ScpShutdownComponent("IMT5", ENetworkId.IMT_LUCCA, Arrays.asList(2000, 2000)),
				new ScpShutdownComponent("IMT6", ENetworkId.IMT_LUCCA, Arrays.asList(2300, 2200)),
				new ScpShutdownComponent("IMT7", ENetworkId.IMT_LUCCA, Arrays.asList(2200, 2100)),
				// 1 SCPi in the English Garden (mobile device)
				new ScpShutdownComponent("EGM1", ENetworkId.EN_GARDEN, Arrays.asList(800)))
		);
		// list of all components which are part of the system
		List<Component> cloudComponents = new ArrayList<Component>(
				scpComponents);
		// Singleton Instance experiencing high load with a machine running on IMT Lucca
		cloudComponents.add(new AppBalanceShutdownComponentNoSel("APP1", new AppShutdownBalancerNoSel(Arrays.asList("APP1","APP2","APP3"))));
		cloudComponents.add(new AppBalanceShutdownComponentNoSel("APP2"));
		cloudComponents.add(new AppBalanceShutdownComponentNoSel("APP3"));
		// generate the latencies on the scp components
		LatencyGenerator.generate(scpComponents, true);
		// initialize the DEECo with input initialized components
		DEECoObjectProvider dop = new DEECoObjectProvider();
		dop.addEnsembles(ensembles);
		dop.addInitialKnowledge(cloudComponents);
		rt.registerComponentsAndEnsembles(dop);
		
		rt.startRuntime();
	}
}
