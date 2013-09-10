package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.Knowledge;

public class AppDeploymentBalancer extends Knowledge {
	
	/** list of application parts */
	public List<String> appIds;
	/** 
	 *  List of scp nodes ordered by best latency 
	 *  from an application part at the same index,
	 *  unless the number of application parts is higher 
	 *  than the number of scp nodes
	 */
	public List<String> scpIds;
	/**
	 * List of ids of the scp nodes per application parts
	 * which gets populated when the whole application gets deployed
	 * via the balancer
	 */
	public List<List<String>> scpAppIds;
	/** flag indicating that all the application parts have been deployed on scp nodes */
	public Boolean isAppDeployed;
	
	/**
	 * Default constructor.
	 * Must exist for being well-stored in the repository
	 */
	public AppDeploymentBalancer() {
		this.isAppDeployed = null;
	}
	/**
	 * Constructor with an initial set of application parts which the balancer
	 * will have to deploy. This must be called by the application part responsible for the full deployment.
	 *
	 * @param appIds
	 */
	public AppDeploymentBalancer(List<String> appIds) {
		this.appIds = appIds;
		this.scpIds = new ArrayList<String> ();
		this.scpAppIds = new ArrayList<List<String>> ();
		this.isAppDeployed = false;
	}
	
	public void deploy(){
		// all AppComponents are now deployed by ScpComponents
		isAppDeployed = true;
		Integer appSize = appIds.size();
		Integer scpSize = scpIds.size();
		// populates initial lists in the scpAppIds
		for (int i = 0; i < scpSize; i++)
			scpAppIds.add(new ArrayList<String> ());
		// distributes the application parts on the scp nodes
		for (int i = 0; i < appSize; i++){
			String appId = appIds.get(i);
			Integer scpIndex = i;
			// more scp nodes than application parts, add one app part per node
			if (appSize > scpSize){
				scpIndex %= scpSize;
			}
			scpAppIds.get(scpIndex).add(appId);
			// print info
			System.out.println("Application part "+ appId + " deployed on " + scpIds.get(scpIndex));	
		}
	}
}
