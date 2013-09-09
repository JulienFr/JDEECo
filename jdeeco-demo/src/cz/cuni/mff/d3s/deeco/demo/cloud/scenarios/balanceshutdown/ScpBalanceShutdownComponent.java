package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.annotations.TriggerOnChange;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ENetworkId;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.Snapshot;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.deployment.ScpDeploymentComponent;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

/**
 * The Science Cloud Platform (Scp) Component for the Shutdown Scenario (SS)
 * 
 * A scp ss component can play different roles here :
 * <ul>
 * <li>It can monitor other scp components listed in the moScpIds list.</li>
 * <li>It can backup app components listed in the buAppIds and taking their respective snapshot into the buSnapshots list.</li>
 * <li>It can process app components listed in the superclass ScpDSComponent from the deployment scenario.</li>
 * </ul>
 * 
 * The specification stipulates a SLA on the cores with a minimum number of two, and frequencies high or equal to 2 Ghz.
 * This is interpreted into the cores list.
 * 
 * @author Julien Malvot
 * 
 */
public class ScpBalanceShutdownComponent extends ScpDeploymentComponent {

	public final static long serialVersionUID = 1L;

	/** list of cores with the frequency in MHz */
	public List<Integer> cores;
	
	/**
	 * constructor with input network id parameter
	 * 
	 * @param networkId
	 *            the network id of the Scp component among the cloud including
	 *            different networks
	 * @see EScenarioNetworkId
	 */
	public ScpBalanceShutdownComponent(String id, ENetworkId networkId, List<Integer> cores) {
		super(id, networkId);
		
		this.cores = cores;
	}	
}
