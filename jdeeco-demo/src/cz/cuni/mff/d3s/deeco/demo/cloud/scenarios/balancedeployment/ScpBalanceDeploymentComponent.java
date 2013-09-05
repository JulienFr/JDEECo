package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment;

import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ENetworkId;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.ScpLatencyData;
import cz.cuni.mff.d3s.deeco.knowledge.Component;

public class ScpBalanceDeploymentComponent extends Component {
		
	public final static long serialVersionUID = 1L;
	
	/** network id where the component is running */
	public ENetworkId networkId;
	/** os latency data with cached list of latencies between the component and other components */
	public Map<String, ScpLatencyData> latencies;
	
	/**
	 * constructor with input network id parameter
	 * @param networkId the network id of the Scp component among the cloud including different networks
	 * @see EScenarioNetworkId
	 */
	public ScpBalanceDeploymentComponent(String id, ENetworkId networkId) {
		this.id = id;
		this.networkId = networkId;
		this.latencies = new HashMap<String, ScpLatencyData>();
	}
}
