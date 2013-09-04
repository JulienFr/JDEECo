package cz.cuni.mff.d3s.deeco.demo.cloud.scenarios;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.Knowledge;

public class AppBalancer extends Knowledge {
	
	public List<String> appIds;
	public List<String> appOnScpIds;
	
	public List<String> scpIds;
	public List<String> scpOnAppIds;
	
	public Boolean isAppDeployed;
	
	public AppBalancer() {
		this.isAppDeployed = null;
	}
	
	public AppBalancer(List<String> appIds) {
		this.appIds = appIds;
		this.appOnScpIds = new ArrayList<String> ();
		this.scpIds = new ArrayList<String> ();
		this.scpOnAppIds = new ArrayList<String> ();
		this.isAppDeployed = false;
	}
}
