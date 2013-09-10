package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.noselectors;

import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

/**
 * The User Application (App) Component for the Deployment Scenario (DS).
 * 
 * The component needs be deployed onto a Scp component via the DeployDSEnsemble.
 * The isDeployed flag carries the deployment status of the application component.
 * whereas the onScpId string equals the id of the scp component which has deployed the app component.
 * 
 * @author Julien Malvot
 * 
 */
public class AppBalanceDeploymentComponentNoSel extends Component {
	
	public final static long serialVersionUID = 1L;
	/** balance role of the component for managing the other application instances */
	public AppDeploymentBalancerNoSel balancer;
	
	/** constructor for the Application Component
	 * @param id
	 */
	public AppBalanceDeploymentComponentNoSel(String id) {
		this.id = id;
		this.balancer = null;
	}
	
	/** constructor for the Application Component
	 * with a supplied balancer
	 * @param id
	 */
	public AppBalanceDeploymentComponentNoSel(String id, AppDeploymentBalancerNoSel balancer) {
		this.id = id;
		this.balancer = balancer;
	}
	
	@Process
	@PeriodicScheduling(6000)
	public static void deploy(
			@InOut("balancer") OutWrapper<AppDeploymentBalancerNoSel> balancer) {
		// if scp nodes have been selected
		if (balancer.value != null && balancer.value.isAppDeployed != null && !balancer.value.isAppDeployed && !balancer.value.scpIds.isEmpty()){
			balancer.value.deploy();
		}
	}
	
}
