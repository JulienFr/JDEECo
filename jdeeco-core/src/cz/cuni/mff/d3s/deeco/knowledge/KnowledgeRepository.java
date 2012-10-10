/*******************************************************************************
 * Copyright 2012 Charles University in Prague
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package cz.cuni.mff.d3s.deeco.knowledge;

import cz.cuni.mff.d3s.deeco.exceptions.KRExceptionAccessError;
import cz.cuni.mff.d3s.deeco.exceptions.KRExceptionUnavailableEntry;
import cz.cuni.mff.d3s.deeco.scheduling.IKnowledgeChangeListener;

/**
 * An abstract class specifing and defining basic operations on the knowledge
 * repository.
 * 
 * @author Michal Kit
 * 
 */
public abstract class KnowledgeRepository {

	/**
	 * Reads a single entry from the knowledge repository. This method is
	 * session oriented.
	 * 
	 * @param entryKey
	 *            key of the object in the knowledge repository
	 * @param session
	 *            a session object within which the operation should be
	 *            performed
	 * @return object in the knowledge repository
	 * @throws KRExceptionUnavailableEntry
	 *             thrown whenever the object for the specified
	 *             <code>entryKey</code> does not exists
	 * @throws KRExceptionAccessError
	 *             thrown whenever there is a knowledge repository access
	 *             problem
	 */
	public abstract Object [] get(String entryKey, ISession session)
			throws KRExceptionUnavailableEntry, KRExceptionAccessError;

	/**
	 * Inserts an object to the knowledge repository. This method is session
	 * oriented.
	 * 
	 * @param entryKey
	 *            key of the object in the knowledge repository
	 * @param value
	 *            inserted object
	 * @param session
	 *            a session object within which the operation should be
	 *            performed
	 * @throws KRExceptionAccessError
	 *             thrown whenever there is a knowledge repository access
	 *             problem
	 */
	public abstract void put(String entryKey, Object value, ISession session)
			throws KRExceptionAccessError;

	/**
	 * Withdraws an object from the knowledge repository. This method is session
	 * oriented.
	 * 
	 * @param entryKey
	 *            key of the object in the knowledge repository
	 * @param session
	 *            a session object within which the operation should be
	 *            performed
	 * @return object from the knowledge repository
	 * @throws KRExceptionAccessError
	 *             thrown whenever there is a knowledge repository access
	 *             problem
	 */
	public abstract Object [] take(String entryKey, ISession session)
			throws KRExceptionUnavailableEntry, KRExceptionAccessError;
	
	/**
	 * Register a listener that should be notified by the knowledge repository
	 * whenever a specified properties are changing.
	 * 
	 * @param listener listening object
	 */
	public abstract boolean registerListener(IKnowledgeChangeListener listener);
	
	/**
	 * Unregisters knowledge listener that has been registered earlier.
	 * 
	 * @param listener listening object
	 */
	public abstract boolean unregisterListener(IKnowledgeChangeListener listener);
	
	
	/**
	 * Switching listening on or off
	 * 
	 * @param on if true the listening is on otherwise its off.
	 */
	public abstract void switchListening(boolean on);
	
	
	/**
	 * Checks if current knowledge change listening is on or off.
	 * 
	 */
	public abstract boolean isTriggeringOn();

	/**
	 * Creates a session object, which can be used for all the operations on the
	 * knowledge repository.
	 * 
	 * @return session object
	 */
	public abstract ISession createSession();

	/**
	 * Reads a single entry from the knowledge repository.
	 * 
	 * @param entryKey
	 *            key of the object in the knowledge repository
	 * @return object in the knowledge repository
	 * @throws KRExceptionUnavailableEntry
	 *             thrown whenever the object for the specified
	 *             <code>entryKey</code> does not exists
	 * @throws KRExceptionAccessError
	 *             thrown whenever there is a knowledge repository access
	 *             problem
	 */
	public Object [] get(String entryKey) throws KRExceptionUnavailableEntry,
			KRExceptionAccessError {
		return get(entryKey, null);
	}

	/**
	 * Inserts an object to the knowledge repository.
	 * 
	 * @param entryKey
	 *            key of the object in the knowledge repository
	 * @param value
	 *            inserted object
	 * @throws KRExceptionAccessError
	 *             thrown whenever there is a knowledge repository access
	 *             problem
	 */
	public void put(String entryKey, Object value)
			throws KRExceptionAccessError {
		put(entryKey, value, null);
	}

	/**
	 * Withdraws an object from the knowledge repository.
	 * 
	 * @param entryKey
	 *            key of the object in the knowledge repository
	 * @return object from the knowledge repository
	 * @throws KRExceptionAccessError
	 *             thrown whenever there is a knowledge repository access
	 *             problem
	 */
	public Object [] take(String entryKey) throws KRExceptionUnavailableEntry, KRExceptionAccessError {
		return take(entryKey, null);
	}
}