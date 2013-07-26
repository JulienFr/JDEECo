package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.highload;

import java.util.List;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ENetworkId;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment.ScpDSComponent;

/**
 * 
 * @author Julien Malvot
 * 
 */
public class ScpHSComponent extends ScpDSComponent {

	public final static long serialVersionUID = 1L;

	/** the machine which the application is running on */
	public String machineId;
	/** load of the scp component provided by the underlying OS 
	 * and based on the number of ready threads at some period of time */
	public ScpHSComponentOSLoadData osLoadData;
	/**
	 * constructor with input network id parameter
	 * 
	 * @param networkId
	 *            the network id of the Scp component among the cloud including
	 *            different networks
	 * @see EScenarioNetworkId
	 */
	public ScpHSComponent(String id, ENetworkId networkId) {
		super(id, networkId);
		this.machineId = null;
		this.osLoadData = new ScpHSComponentOSLoadData(1.0);
	}
	
	public ScpHSComponent(String id, ENetworkId networkId, List<String> onAppIds) {
		super(id, networkId);
		this.machineId = null;
		this.osLoadData = new ScpHSComponentOSLoadData(1.0);
		this.onAppIds = onAppIds;
	}

	/*
	 * @Process
	 * 
	 * @PeriodicScheduling(6000) public static void process(@In("id") String id,
	 * @In("linkedScpInstanceIds") List<String> scpIds) { //loadRatio.value =
	 * (new Random()).nextFloat();
	 * 
	 * System.out.print(id + " linked with"); for (String str : scpIds){
	 * System.out.println(" " + str); } System.out.println(); //+
	 * Math.round(loadRatio.value * 100) + "%"); }
	 */
}
