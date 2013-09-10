package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown.noselectors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.LogHelper;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ScpLatencyData;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.Snapshot;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.AppDeploymentBalancer;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.noselectors.AppDeploymentBalancerNoSel;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown.AppShutdownBalancer;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown.NoBackupNodeFound;

/**
 * 
 * @author Julien Malvot
 *
 */
public class AppShutdownBalancerNoSel extends AppShutdownBalancer {

	/** list of scp latencies */
	public List<Map<String, ScpLatencyData>> scpLatencies;
	/** list of scp cores */
	public List<List<Integer>> scpCores;
	/**
	 * Default constructor.
	 * Must exist for being well-stored in the repository
	 */
	public AppShutdownBalancerNoSel() {
		super();
	}
	/**
	 * Constructor with an initial set of application parts which the balancer
	 * will have to deploy. This must be called by the application part responsible for the full deployment.
	 *
	 * @param appIds
	 */
	public AppShutdownBalancerNoSel(List<String> appIds) {
		super(appIds);
		scpLatencies = new ArrayList<Map<String, ScpLatencyData>>();
		scpCores = new ArrayList<List<Integer>> ();
	}
	
	public void deploy() {
		initializeDeployment();
		// selects the scp nodes
		scpIds = AppDeploymentBalancerNoSel.selectScpNodes(appIds, scpIds, getScpSelectLatenciesFromSLA());
		// selects the bu nodes and their roles
		buIds = buSelection();
		// deploys from the selected nodes
		try {
			deployResponsabilities();
		} catch (NoBackupNodeFound e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() throws NoBackupNodeFound{
		super.shutdown();
	}
	
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
	
	private List<ScpLatencyData> getScpSelectLatenciesFromSLA(){
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

	private List<String> buSelection(){
		int range = appIds.size();
		// select backup nodes which are not already selected for any running purpose
		Integer buIndex = 0;
		List<String> bus = new ArrayList<String> ();
		while (bus.size() < range && buIndex < buIds.size()){
			String buId = buIds.get(buIndex);
			Integer scpBuIndex = scpIds.indexOf(buId);
			// the backup node must respect the SLA (2x(f>=2Ghz) cpu cores)
			if (scpIds.indexOf(scpBuIndex) == -1 && respectSLAScpCores(scpCores.get(buIndex))){
				bus.add(buId);
			}
			buIndex++;
		}
		return bus;
	}
}
