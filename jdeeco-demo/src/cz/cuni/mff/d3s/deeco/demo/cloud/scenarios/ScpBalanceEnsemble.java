package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
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

/**
 * The Deployment Ensemble for the Deployment Scenario (DS).
 * 
 * For a given number of application instances, the ensemble
 * takes all possible scp components which are available for these instances and requires scp components
 * to be forming a graph of lowest latencies among all the possible combinations.
 * It then attributes appcomponent-scpcomponent pairs such that each app component gets deployed.
 * 
 * @author Julien Malvot
 * 
 */
public class ScpBalanceEnsemble extends Ensemble {

	private static final long serialVersionUID = 1L;

	@Membership
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cId,
			@In("coord.balancer.isAppDeployed") Boolean isDeployed,
			@In("coord.balancer.scpIds") List<String> scpIds,
			// ScpComponent
			@In("member.id") String scpId,
			@In("member.onAppIds") List<String> onAppIds
			) {
		// if the application is deployed and the member is not the app balancer coordinator
		if (isDeployed != null && isDeployed && scpIds != null && scpIds.contains(scpId)){
			return true;
		}
		return false;
	}

	// to expand to different cdScpInstanceIds in case of high candidate range
	@KnowledgeExchange
	@PeriodicScheduling(3000)
	public static void map(
			// AppComponent coordinator knowledge
			@In("coord.balancer.appIds") List<String> appIds,
			@In("coord.balancer.scpIds") List<String> scpIds,
			// ScpComponent
			@In("member.id") String scpId,
			@InOut("member.onAppIds") OutWrapper<List<String>> onAppIds) {
		// the app id at index i is deployed with the scp id at index i
		int index = scpIds.indexOf(scpId);
		String id = appIds.get(index);
		if (onAppIds.value.contains(id))
			onAppIds.value.add(id);
		
		System.out.println("The scp component "+ scpId +  
							" is running " + id);
	}
}
