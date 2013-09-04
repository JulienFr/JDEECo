package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios;

import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;

public class AppBalanceEnsemble extends Ensemble {

	private static final long serialVersionUID = 1L;

	@Membership
	public static Boolean membership(
			// AppComponent coordinator
			@In("coord.id") String cId,
			@In("coord.balancer.isAppDeployed") Boolean isDeployed,
			@In("coord.balancer.appIds") List<String> appIds,
			// AppComponent part
			@In("member.id") String appId,
			@In("member.onScpId") String onScpId
			) {
		// if the application is deployed and the member is not the app balancer coordinator
		if (isDeployed != null && isDeployed && !cId.equals(appId) && appIds != null && appIds.contains(appId)){
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
			// AppComponent part
			@In("member.id") String appId,
			@Out("member.onScpId") String onScpId) {
		// retains all the scp ids into the balancer
		int index = appIds.indexOf(appId);
		onScpId = scpIds.get(index);
		
		System.out.println("The application component "+ appId +  
							" is deployed on " + onScpId);
	}
}
