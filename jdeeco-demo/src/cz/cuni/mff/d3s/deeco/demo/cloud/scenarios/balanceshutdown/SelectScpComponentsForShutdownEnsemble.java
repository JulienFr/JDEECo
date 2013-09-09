package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Selector;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ScpLatencyData;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.SelectScpComponentsForDeploymentEnsemble;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class SelectScpComponentsForShutdownEnsemble extends Ensemble {
	
	private static final long serialVersionUID = 1L;

	// returns true if the cores respect the SLA prescribed for the cpu cores
	protected static Boolean respectSLAScpCores(List<Integer> cores){
		Integer core2GhzCount = 0;
		for (int j = 0; j < cores.size(); j++){
			if (cores.get(j) >= 2000)
				core2GhzCount++;
		}
		// if at least two-gigahertz frequency cores
		return (core2GhzCount >= 2);
	}
	
	private static List<ScpLatencyData> scpSelectLatenciesFromSLA(List<Map<String, ScpLatencyData>> scpLatencies, List<List<Integer>> scpCores){
		// transforming the List<Map> data structure into a List data structure
		List<ScpLatencyData> mLatencies = new ArrayList<ScpLatencyData> ();
		for (int i = 0; i < scpLatencies.size(); i++){
			List<Integer> cores = scpCores.get(i);
			// if at least two-gigahertz frequency cores
			if (respectSLAScpCores(cores)){
				// TODO: can factor this code which exactly the same here
				// as in the FilterBalanceDeploymentEnsemble
				// get the ids which the scp is linked to
				Map<String,ScpLatencyData> map = scpLatencies.get(i);
				Object[] toIdSet = scpLatencies.get(i).keySet().toArray();
				// iterate over all the link destinations
				for (int j = 0; j < toIdSet.length; j++){
					// get the latency
					ScpLatencyData latencyData = map.get((Object)toIdSet[j]);
					// if the latency respects the Service Level Agreement max latency of the source
					// do not add a latency data which is already existing in the list
					if (latencyData.cache <= 50 && !mLatencies.contains(latencyData)){
						// add to the data structure
						mLatencies.add(latencyData);
					}
				}
			}
		}
		return mLatencies;
	}

	private static void buSelection(List<Boolean> buSelectors, List<String> buIds, List<List<Integer>> cores, List<Boolean> scpSelectors, List<String> scpIds, int range){
		// set all selections to false
		for (int i = 0; i < buSelectors.size(); i++)
			buSelectors.set(i, false);
		// select backup nodes which are not already selected for any running purpose
		Integer buSelectCount = 0;
		Integer buIndex = 0;
		while (buSelectCount < range && buIndex < buIds.size()){
			Integer scpBuIndex = scpIds.indexOf(buIds.get(buIndex));
			// the backup node must respect the SLA (2x(f>=2Ghz) cpu cores)
			if (!scpSelectors.get(scpBuIndex) && respectSLAScpCores(cores.get(buIndex))){
				buSelectCount++;
				buSelectors.set(buIndex, true);
			}
			buIndex++;
		}
	}

	@Membership
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cAppId,
			@In("coord.balancer.appIds") List<String> appIds,
			@In("coord.balancer.isAppDeployed") Boolean isDeployed,
			// ScpComponent members
			@Selector("scp") List<Boolean> scpSelectors,
			@In("members.scp.id") List<String> scpIds,
			@In("members.scp.latencies") List<Map<String, ScpLatencyData>> scpLatencies,
			@In("members.scp.cores") List<List<Integer>> scpCores,
			// ScpComponent backup members
			@Selector("bu") List<Boolean> buSelectors,
			@In("members.bu.id") List<String> buIds,
			@In("members.bu.cores") List<List<Integer>> buCores
			) {
		if (isDeployed != null && !isDeployed){
			// select the latencies based on the SLA
			List<ScpLatencyData> slaSelectedLatencies = scpSelectLatenciesFromSLA(scpLatencies, scpCores);
			// scp selection
			SelectScpComponentsForDeploymentEnsemble.scpSelection(scpSelectors, scpIds, slaSelectedLatencies, appIds.size());
			// bu selection (backup components)
			buSelection(buSelectors, buIds, buCores, scpSelectors, scpIds, appIds.size());
			// accept the selected backup and scp nodes
			return true;
		}
		return false;
	}

	@KnowledgeExchange
	@PeriodicScheduling(3000)
	public static void map(
			// AppComponent coordinator (1)
			@In("coord.id") String appId,
			@Out("coord.balancer.scpIds") OutWrapper<List<String>> scpIds,
			@Out("coord.balancer.buIds") OutWrapper<List<String>> buIds,
			// ScpComponent members (n)
			@In("members.scp.id") List<String> msScpIds,
			// ScpComponent backup members (n)
			@In("members.bu.id") List<String> msBuIds) {
		// retains all the scp ids into the balancer
		scpIds.value = msScpIds;
		buIds.value = msBuIds;
		
		String scpComponentIds = msScpIds.get(0);
		String buComponentIds = msBuIds.get(0);
		
		// info printing
		for (int i = 1; i < msScpIds.size(); i++){
			scpComponentIds += " " + msScpIds.get(i);
		}
		for (int i = 1; i < msBuIds.size(); i++){
			buComponentIds += " " + msBuIds.get(i);
		}
		System.out.println("Balancer "+appId +  
							" with the scp nodes " +scpComponentIds +
							" and the backup nodes " +buComponentIds);
	}
}
