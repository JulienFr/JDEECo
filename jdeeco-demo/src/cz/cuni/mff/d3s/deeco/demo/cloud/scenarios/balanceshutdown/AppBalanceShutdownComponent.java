package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.Snapshot;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.AppDeploymentBalancer;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment.AppDeploymentComponent;
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
public class AppBalanceShutdownComponent extends Component {
	
	public final static long serialVersionUID = 1L;
	/** balance role of the component for managing the other application instances */
	public AppShutdownBalancer balancer;
	/** component state snapshot which can be provided to a backup node */
	public Snapshot snapshot;
	
	/** constructor for the Application Component
	 * @param id
	 */
	public AppBalanceShutdownComponent(String id) {
		this.id = id;
		this.snapshot = null;
	}
	
	/** constructor for the Application Component
	 * with a supplied balancer
	 * @param id
	 * @param balancer The balancing role of the application component. There should be one per working set of application components.
	 */
	public AppBalanceShutdownComponent(String id, AppShutdownBalancer balancer) {
		this.id = id;
		this.balancer = balancer;
		this.snapshot = null;
	}
	
	@Process
	@PeriodicScheduling(1000)
	public static void takeSnapshot(
			@In("id") String id,
			@InOut("snapshot") OutWrapper<Snapshot> snapshot) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		try {
			w.writeBytes(id);
			w.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		snapshot.value = new Snapshot((snapshot.value == null ? 0 : snapshot.value.timestamp+1), baos.toByteArray());
		System.out.println(id + " takes a new snapshot");
	}
	
	@Process
	@PeriodicScheduling(2000)
	public static void deploy(
			@InOut("balancer") OutWrapper<AppShutdownBalancer> balancer) {
		// if scp nodes have been selected
		if (balancer.value != null && balancer.value.isAppDeployed != null && !balancer.value.isAppDeployed && !balancer.value.scpIds.isEmpty()){
			balancer.value.deploy();
		}
	}
	
	@Process
	@PeriodicScheduling(5000)
	public static void shutdown(@InOut("balancer") OutWrapper<AppShutdownBalancer> balancer){
		if (balancer.value != null && balancer.value.isAppDeployed != null && balancer.value.isAppDeployed){
			balancer.value.shutdown();
		}
	}
}
