package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.noselectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.LogHelper;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ScpLatencyData;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ScpLatencyDataComparator;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.AppDeploymentBalancer;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.shutdown.DeployShutdownEnsemble;

public class AppDeploymentBalancerNoSel extends AppDeploymentBalancer {

	/** list of scp latencies */
	public List<Map<String, ScpLatencyData>> scpLatencies;
	/**
	 * Default constructor.
	 * Must exist for being well-stored in the repository
	 */
	public AppDeploymentBalancerNoSel() {
		super();
	}
	/**
	 * Constructor with an initial set of application parts which the balancer
	 * will have to deploy. This must be called by the application part responsible for the full deployment.
	 *
	 * @param appIds
	 */
	public AppDeploymentBalancerNoSel(List<String> appIds) {
		super(appIds);
		scpLatencies = new ArrayList<Map<String, ScpLatencyData>>();
	}
	
	public void deploy(){
		// takes only the scp ids which respect the SLA and are well connected by best latencies
		scpIds = selectScpNodes(appIds, scpIds, getScpSelectLatenciesFromSLA());
		// all AppComponents are now deployed by ScpComponents
		super.deploy();
	}
	
	private List<ScpLatencyData> getScpSelectLatenciesFromSLA(){
		// transforming the List<Map> data structure into a List data structure
		List<ScpLatencyData> mLatencies = new ArrayList<ScpLatencyData> ();
		for (int i = 0; i < scpLatencies.size(); i++){
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
		return mLatencies;
	}
	
	/**
	 * function reused for selecting the scp in the DeploySSEnsemble
	 * @see DeployShutdownEnsemble
	 * @param selectors
	 * @param scpIds
	 * @param scpSLASelectedLatencies the input list of ScpDSComponentOSLatencyData based on the SLA contract
	 * @param range
	 */
	public static List<String> selectScpNodes(List<String> appIds, List<String> scpIds, List<ScpLatencyData> scpSelectLatenciesFromSLA){
		
		System.out.println("Balancer selects the scp nodes from the latencies");
		
		int range = appIds.size();
		// = getScpSelectLatenciesFromSLA();
		List<String> mLinkedIds = new ArrayList<String> ();
		// sort all the list of links by order of latency
		Collections.sort(scpSelectLatenciesFromSLA, new ScpLatencyDataComparator());
		// reuse the initial implemented algorithm
		Integer indexer = -1; // the first required id to be explored for starting the exploration
		// while the linkedIds set is not well-sized or the algorithm runs out of possibilities
		while (mLinkedIds.size() < range && (range+indexer) <= scpSelectLatenciesFromSLA.size()){
			mLinkedIds.clear();
			Integer firstAddIndex = 0;
			for (int i = 0; i < scpSelectLatenciesFromSLA.size(); i++){
				ScpLatencyData latencyData = (ScpLatencyData) scpSelectLatenciesFromSLA.get(i);
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
		System.out.println("Balancer keeps the scp nodes " + LogHelper.getIdsString(mLinkedIds));
		return mLinkedIds;
	}
}
