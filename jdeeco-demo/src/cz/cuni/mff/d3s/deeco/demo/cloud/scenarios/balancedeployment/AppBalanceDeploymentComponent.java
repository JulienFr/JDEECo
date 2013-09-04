package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.AppBalancer;
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
public class AppBalanceDeploymentComponent extends Component {
	
	public final static long serialVersionUID = 1L;

	/** id of the scp component running the component */
	public String onScpId;
	/** id of the machine whom the component belongs */
	public String machineId;
	/** balance role of the component for managing the other application instances */
	public AppBalancer balancer;
	
	/** constructor for the Application Component
	 * @param id
	 */
	public AppBalanceDeploymentComponent(String id) {
		this.id = id;
		this.onScpId = null;
		this.machineId = null;
		this.balancer = null;
	}
	
	/** constructor for the Application Component
	 * with a supplied balancer
	 * @param id
	 */
	public AppBalanceDeploymentComponent(String id, AppBalancer balancer) {
		this.id = id;
		this.onScpId = null;
		this.machineId = null;
		this.balancer = balancer;
	}
	
	@Process
	@PeriodicScheduling(6000)
	public static void process(
			@In("id") String id,
			@Out("onScpId") OutWrapper<String> onScpId,
			@InOut("balancer") OutWrapper<AppBalancer> balancer) {
		// if scp nodes have been selected
		if (balancer.value != null && balancer.value.isAppDeployed != null && !balancer.value.isAppDeployed && !balancer.value.scpIds.isEmpty()){
			//String appComponentIds = id;
			//String scpComponentIds = balancer.value.scpIds.get(0);
			// all AppComponents are now deployed by ScpComponents
			onScpId.value = balancer.value.scpIds.get(0);
			balancer.value.isAppDeployed = true;
			/*msScpOnAppIds.value.get(0).add(cAppId);
			// linkage
			for (int i = 0; i < balancer.value.appIds.size(); i++){
				balancer.value.appOnScpIds.set(i, balancer.value.scpIds.get(i+1));
				// app component id registration into the scp component
				// regarding of the first assigned id for the coordinator
				balancer.value.scpOnAppIds.add(balancer.value.appIds.get(i));
				
				appComponentIds += " " + balancer.value.appIds.get(i);
				scpComponentIds += " " + balancer.value.scpIds.get(i+1);
			}
			balancer.value.isAppDeployed = true;
			System.out.println("coordinator="+id+ 
								"   AppComponents=" + appComponentIds + 
								"   ScpComponents=" +scpComponentIds);*/
			
			System.out.println("App coordinator "+ id + " deployed on " + onScpId.value);
		}
	}
	
}
