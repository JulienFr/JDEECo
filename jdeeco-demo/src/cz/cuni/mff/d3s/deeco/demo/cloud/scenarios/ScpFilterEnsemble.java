package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Selector;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment.ScpDSComponentOSLatencyData;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment.ScpDSComponentOSLatencyDataComparator;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.shutdown.DeploySSEnsemble;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class ScpFilterEnsemble extends Ensemble {
	
	private static List<ScpDSComponentOSLatencyData> scpSelectLatenciesFromSLA(List<Map<String, ScpDSComponentOSLatencyData>> scpLatencies){
		// transforming the List<Map> data structure into a List data structure
		List<ScpDSComponentOSLatencyData> mLatencies = new ArrayList<ScpDSComponentOSLatencyData> ();
		for (int i = 0; i < scpLatencies.size(); i++){
			// get the ids which the scp is linked to
			Map<String,ScpDSComponentOSLatencyData> map = scpLatencies.get(i);
			Object[] toIdSet = scpLatencies.get(i).keySet().toArray();
			// iterate over all the link destinations
			for (int j = 0; j < toIdSet.length; j++){
				// get the latency
				ScpDSComponentOSLatencyData latencyData = map.get((Object)toIdSet[j]);
				// if the latency respects the Service Level Agreement max latency of the source
				// do not add a latency data which is already existing in the list
				if (latencyData.cache <= 50 && !mLatencies.contains(latencyData)){
					// add to the data structure
					mLatencies.add(latencyData);
				}
			}
		}
		return mLatencies;
	}
	
	/**
	 * function reused for selecting the scp in the DeploySSEnsemble
	 * @see DeploySSEnsemble
	 * @param selectors
	 * @param scpIds
	 * @param scpSLASelectedLatencies the input list of ScpDSComponentOSLatencyData based on the SLA contract
	 * @param range
	 */
	public static void scpSelection(List<Boolean> selectors, List<String> scpIds, List<ScpDSComponentOSLatencyData> scpSLASelectedLatencies, int range){
		List<String> mLinkedIds = new ArrayList<String> ();
		// set all selectors to false
		for (int i = 0; i < selectors.size(); i++)
			selectors.set(i, false);
		// sort all the list of links by order of latency
		Collections.sort(scpSLASelectedLatencies, new ScpDSComponentOSLatencyDataComparator());
		// reuse the initial implemented algorithm
		Integer indexer = -1; // the first required id to be explored for starting the exploration
		// while the linkedIds set is not well-sized or the algorithm runs out of possibilities
		while (mLinkedIds.size() < range && (range+indexer) <= scpSLASelectedLatencies.size()){
			mLinkedIds.clear();
			Integer firstAddIndex = 0;
			for (int i = 0; i < scpSLASelectedLatencies.size(); i++){
				ScpDSComponentOSLatencyData latencyData = (ScpDSComponentOSLatencyData) scpSLASelectedLatencies.get(i);
				// if the latencyData respects the maximum sla latency
				if (i > indexer){
					// first add into the linkage group
					if (mLinkedIds.size() == 0){
						// the starting index of the add is remembered as a bottom limit to be reached for a new search
						firstAddIndex = i;
						// add the two ids of the latencyData
						mLinkedIds.add(latencyData.id1);
						mLinkedIds.add(latencyData.id2);
					}else if (mLinkedIds.size() < range){
						String fId = latencyData.id1;
						String tId = latencyData.id2;
						// if exclusively one or the other ids is part of the covered latencyData
						if ((mLinkedIds.contains(fId) && !mLinkedIds.contains(tId))
								|| (!mLinkedIds.contains(fId) && mLinkedIds.contains(tId))){
							// we add the uncovered to the linkage group
							if (!mLinkedIds.contains(fId))
								mLinkedIds.add(fId);
							else
								mLinkedIds.add(tId);
						}
					}else{
						break;
					}
				}
			}
			// if we did not get enough ids into the interconnection, 
			// then we start the new iteration from the first found index
			if (mLinkedIds.size() < range)
				indexer = firstAddIndex;
		}
		// setting up the list of booleans
		for (int i = 0; i < mLinkedIds.size(); i++){
			int index = scpIds.indexOf(mLinkedIds.get(i));
			selectors.set(index, true);
		}
	}
	
	@Membership
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cAppId,
			@In("coord.balancer.appIds") List<String> appIds,
			@In("coord.balancer.isAppDeployed") Boolean isDeployed,
			// ScpComponent members
			@Selector("Scp") List<Boolean> msScpSelectors,
			@In("members.Scp.id") List<String> msScpIds,
			@In("members.Scp.latencies") List<Map<String, ScpDSComponentOSLatencyData>> scpLatencies
			) {
		if (isDeployed != null && !isDeployed){
			// select the latencies based on the SLA
			List<ScpDSComponentOSLatencyData> slaSelectedLatencies = scpSelectLatenciesFromSLA(scpLatencies);
			// scp selection
			scpSelection(msScpSelectors, msScpIds, slaSelectedLatencies, appIds.size());
			// here we go
			return true;
		}
		return false;
	}

	// to expand to different cdScpInstanceIds in case of high candidate range
	@KnowledgeExchange
	@PeriodicScheduling(3000)
	public static void map(
			// AppComponent coordinator (1)
			@In("coord.id") String appId,
			@Out("coord.balancer.scpIds") OutWrapper<List<String>> scpIds,
			// ScpComponent members (n)
			@In("members.Scp.id") List<String> msScpIds) {
		// retains all the scp ids into the balancer
		scpIds.value = msScpIds;
		
		String scpComponentIds = msScpIds.get(0);
		// linkage
		for (int i = 1; i < msScpIds.size(); i++){
			scpComponentIds += " " + msScpIds.get(i);
		}
		System.out.println("The application (balancer) coordinator "+appId +  
							" has formed an ensemble with the ScpComponents " +scpComponentIds);
	}
}
