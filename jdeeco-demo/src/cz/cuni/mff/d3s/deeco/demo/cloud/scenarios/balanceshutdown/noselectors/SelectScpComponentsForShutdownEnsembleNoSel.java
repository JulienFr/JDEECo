package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown.noselectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Selector;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.LogHelper;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ScpLatencyData;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.SelectScpComponentsForDeploymentEnsemble;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class SelectScpComponentsForShutdownEnsembleNoSel extends Ensemble {
	
	private static final long serialVersionUID = 1L;

	@Membership
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cAppId,
			@In("coord.balancer.appIds") List<String> appIds,
			@In("coord.balancer.isAppDeployed") Boolean isDeployed,
			// ScpComponent members (cores is a knowledge that can catch all scp nodes)
			@In("memberset.cores") List<List<Integer>> buCores
			) {
		return (isDeployed != null && !isDeployed);
	}

	@KnowledgeExchange
	@PeriodicScheduling(3000)
	public static void map(
			// AppComponent coordinator (1)
			@In("coord.id") String appId,
			@Out("coord.balancer.scpIds") OutWrapper<List<String>> scpIds,
			@Out("coord.balancer.buIds") OutWrapper<List<String>> buIds,
			@Out("coord.balancer.scpLatencies") OutWrapper<List<Map<String, ScpLatencyData>>> scpLatencies,
			@Out("coord.balancer.scpCores") OutWrapper<List<List<Integer>>> scpCores,
			// ScpComponent members (n)
			@In("memberset.id") List<String> msScpIds,
			@In("memberset.latencies") List<Map<String, ScpLatencyData>> msScpLatencies,
			@In("memberset.cores") List<List<Integer>> msScpCores
			) {
		// retains all the scp ids into the balancer
		scpIds.value = msScpIds;
		buIds.value = msScpIds;
		scpLatencies.value = msScpLatencies;
		scpCores.value = msScpCores;
		
		System.out.println("The application (balancer) coordinator "+appId +  
				" has formed an ensemble with the ScpComponents " +LogHelper.getIdsString(msScpIds));
	}
}
