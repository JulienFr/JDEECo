package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balanceshutdown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.Snapshot;
import cz.cuni.mff.d3s.deeco.demo.cloud.scenarios.balancedeployment.AppDeploymentBalancer;

/**
 * 
 * @author Julien Malvot
 *
 */
public class AppShutdownBalancer extends AppDeploymentBalancer {
	/** List of ids of backup-scp nodes that can backup application parts */
	public List<String> buIds;
	/**
	 * List of ids of the backup-scp nodes per application parts
	 * which gets populated when the whole application gets deployed
	 * via the balancer  
	 */
	public List<String> appBuIds;
	/** snapshots of the backed-up application nodes */
	public List<Snapshot> buSnapshots;
	/**
	 * List of ids of the scp nodes (running the application parts) per backup-scp nodes (monitoring)
	 * which gets populated when the whole application gets deployed
	 * via the balancer  
	 */
	public List<List<String>> buScpIds;
	
	/**
	 * Default constructor.
	 * Must exist for being well-stored in the repository
	 */
	public AppShutdownBalancer() {
		this.isAppDeployed = null;
	}
	/**
	 * Constructor with an initial set of application parts which the balancer
	 * will have to deploy. This must be called by the application part responsible for the full deployment.
	 *
	 * @param appIds
	 */
	public AppShutdownBalancer(List<String> appIds) {
		super(appIds);
		this.buIds = new ArrayList<String> ();
		this.appBuIds = new ArrayList<String> ();
		this.buSnapshots = new ArrayList<Snapshot> ();
		this.buScpIds = new ArrayList<List<String>> ();
	}
	
	public void deploy() {
		super.deploy();
		// populates initial lists in the buAppIds
		for (int i = 0; i < buIds.size(); i++){
			appBuIds.add("");
			buScpIds.add(new ArrayList<String> ());
		}
		// finds the initial scp ids to monitor
		List<String> scpIdsToMonitor = new ArrayList<String> ();
		for (int i = 0; i < scpAppIds.size(); i++){
			if (!scpAppIds.get(i).isEmpty())
				scpIdsToMonitor.add(scpIds.get(i));
		}
		// assign reponsabilities to components among Monitoring and Backing up
		try {
			assignResponsabilities(appIds, scpIdsToMonitor);
		} catch (BackupNodeNotFound e) {
			e.printStackTrace();
		} catch (MonitoringNodeNotFound e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown(){
		// shutdowns a scp node intentionally
		Random r = new Random();
		Integer shutdownScpIndex = r.nextInt(scpIds.size());
		// step 1 : removes the shutdown node related information
		String shutdownId = scpIds.get(shutdownScpIndex);
		scpIds.remove(shutdownId);
		System.out.println("Shutdown node " + shutdownId);
		// step 2 : removes the list of application parts running on the shutdown scp node
		List<String> appIdsToRunAndBackup = scpAppIds.get(shutdownScpIndex);
		scpAppIds.remove(appIdsToRunAndBackup);
		// gets the shutdown backup node if it exists, check if the node had this role
		Integer shutdownBuIndex = buIds.indexOf(shutdownId);
		List<String> scpIdsToMonitor = null;
		// step 3 : shutdowns the corresponding backup scp node if the scp node is also a backup scp node
		if (shutdownBuIndex >= 0){
			// step 3.1 : removes the shutdown id from the backup id lists
			buIds.remove(shutdownId);
			// step 3.2 : removes the list of scp nodes which were backed up by the shutdown backup scp node
			scpIdsToMonitor = buScpIds.get(shutdownBuIndex);
			buScpIds.remove(scpIdsToMonitor);
		}
		// step 4 : converts the backup responsability of each backup scp nodes into scp nodes running the respective application parts
		Iterator<String> itr = appIdsToRunAndBackup.iterator();
		while (itr.hasNext()){
			String appId = itr.next();
			// gets the application part index within the appIds list
			Integer appIndex = appIds.indexOf(appId);
			// gets the backup scp node of the application part
			String buId = appBuIds.get(appIndex);
			// step 4.1 : finds the corresponding scp node role
			Integer scpIndex = scpIds.indexOf(buId);
			// if not existing, adds it to the list of scps which are running applications
			if (scpIndex == -1){
				scpIds.add(buId);
				scpAppIds.add(new ArrayList<String>());
				scpIndex = scpIds.size()-1;
			}
			// step 4.2 : attaches the application part to the scp node (which was a backup scp node)
			scpAppIds.get(scpIndex).add(appId);
			
			System.out.println(scpIds.get(scpIndex) + " runs " + getIdsString(scpAppIds.get(scpIndex)));
		}
		// assign roles to the backup/monitor scp nodes, scp nodes
		try {
			assignResponsabilities(appIdsToRunAndBackup, scpIdsToMonitor);
		} catch (BackupNodeNotFound e) {
			e.printStackTrace();
		} catch (MonitoringNodeNotFound e) {
			e.printStackTrace();
		}
	}
	
	private boolean isBackupResponsabilityConflicting(String buId, String appId){
		Integer scpIndex = scpIds.indexOf(buId);
		return (scpIndex != -1 && scpAppIds.get(scpIndex).indexOf(appId) != -1);
	}
	
	private String getIdsString(List<String> ids){
		String str = "";
		for (String i : ids){
			str += i + (!i.equals(ids.get(ids.size()-1)) ? ", " : "");
		}
		return str;
	}
	
	private void assignResponsabilities(List<String> appIdsToBackup, List<String> scpIdsToMonitor) throws BackupNodeNotFound, MonitoringNodeNotFound{
		// step 5 of shutdown : finds backup nodes for the application parts
		Iterator<String> itr = appIdsToBackup.iterator();
		Integer buIndex = 0;
		while (itr.hasNext()){
			String appId = itr.next();
			Integer appIndex = appIds.indexOf(appId);
			// finds a backup node which would be not both running and backing up the same application part
			String buId = buIds.get(buIndex);
			Integer count = 0;
			while (count < buIds.size() && isBackupResponsabilityConflicting(buId, appId)){
				buId = buIds.get((buIndex+1 < buIds.size() ? ++buIndex : 0));
				count++;
			}
			// no backup node found for the given application id : hardly happens, unless the scps/apps ratio is very low
			if (count == buIds.size()){
				throw new BackupNodeNotFound();
			}
			// sets the backup scp node of the application part
			if (buId != null){
				appBuIds.set(appIndex, buId);
				
				List<String> buAppIds = new ArrayList<String> ();
				for (int i = 0; i < appBuIds.size(); i++){
					if (appBuIds.get(i).equals(buId)){
						buAppIds.add(appIds.get(i));
					}
				}
				System.out.println(buId + " backs up " + getIdsString(buAppIds));
			}
			// enables a rotation in the selection for balancing
			buIndex++;
		}
		// step 6 of shutdown : finds monitoring nodes for the scp nodes
		if (scpIdsToMonitor != null){
			itr = scpIdsToMonitor.iterator();
			buIndex = 0;
			// iterates over the scp ids which need each a monitoring scp node different than themselves
			while (itr.hasNext()){
				String scpId = itr.next();
				// finds a backup node which would be not both running and monitoring the same application part (=is not the same scp node)
				String buId = buIds.get(buIndex);
				Integer count = 0;
				while (count < buIds.size() && buId.equals(scpId)){
					buId = buIds.get((buIndex+1 < buIds.size() ? ++buIndex : 0));
					count++;
				}
				// no backup node found for the given application id : hardly happens, unless the scps/apps ratio is very low
				if (count == buIds.size()){
					throw new MonitoringNodeNotFound();
				}
				// sets the monitoring scp node of the scp node
				if (buId != null){
					buScpIds.get(buIndex).add(scpId);
					System.out.println(buId + " monitors " + getIdsString(buScpIds.get(buIndex)));
				}
				// enables a rotation in the selection for balancing
				buIndex++;
			}
		}
		
	}
}
