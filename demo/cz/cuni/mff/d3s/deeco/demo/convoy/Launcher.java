package cz.cuni.mff.d3s.deeco.demo.convoy;

import java.rmi.RMISecurityManager;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.RepositoryKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.TSKnowledgeRepository;
import cz.cuni.mff.d3s.deeco.runtime.Runtime;

/**
 * Main class for launching the application.
 * 
 * @author Michal Kit
 * 
 */
public class Launcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Class[] classes = { ListComponent.class };
		KnowledgeManager km = new RepositoryKnowledgeManager(
				new TSKnowledgeRepository());
		Runtime runtime = new Runtime(classes, null, km);
	}

}
