// =================================================================                                                                   
// Copyright (C) 2011-2013 Pierre Lison (plison@ifi.uio.no)                                                                            
//                                                                                                                                     
// This library is free software; you can redistribute it and/or                                                                       
// modify it under the terms of the GNU Lesser General Public License                                                                  
// as published by the Free Software Foundation; either version 2.1 of                                                                 
// the License, or (at your option) any later version.                                                                                 
//                                                                                                                                     
// This library is distributed in the hope that it will be useful, but                                                                 
// WITHOUT ANY WARRANTY; without even the implied warranty of                                                                          
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU                                                                    
// Lesser General Public License for more details.                                                                                     
//                                                                                                                                     
// You should have received a copy of the GNU Lesser General Public                                                                    
// License along with this program; if not, write to the Free Software                                                                 
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA                                                                           
// 02111-1307, USA.                                                                                                                    
// =================================================================                                                                   

package opendial.inference.bn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import opendial.arch.DialException;
import opendial.utils.InferenceUtils;
import opendial.utils.Logger;

/**
 * 
 *
 * @author  Pierre Lison (plison@ifi.uio.no)
 * @version $Date::                      $
 *
 */
public class BNetwork {

	static Logger log = new Logger("BNetwork", Logger.Level.DEBUG);
	
	Map<String,BNode> nodes;
	
	public static final int MAX_LENGTH = 100;
	
	public static boolean autoCompletion = true;

	public BNetwork () {
		nodes = new HashMap<String, BNode>();
	}
	
	
	public BNode getNode(String nodeId) {
		return nodes.get(nodeId);
	}
	
	/**
	 * Also check for acyclicity, for consistency in BN links (inputs, outputs).
	 * 
	 * @param node
	 * @throws DialException
	 */
	public void addNode(BNode node) throws DialException {
		if (autoCompletion) {
			node.getDistribution().completeProbabilityTable();
		}
		
		if (!node.getDistribution().isWellFormed()) {
			throw new DialException("Probability table for node " + node.getId() + " is not well-formed");
		}
		nodes.put(node.getId(), node);
	}
	
	
	public List<BNode> getNodes() {
		return new ArrayList<BNode>(nodes.values());
	}
	
	
	/**
	 * TODO: Check for infinite loops!
	 * And 
	 * 
	 * @param node
	 * @return
	 */
	public List<BNode> getAncestors(BNode node, int max_length) {
		List<BNode> ancestors = new LinkedList<BNode>();
		
		if (max_length <= 0) {
			return ancestors;
		}
		for (BNode anc : node.getConditionalNodes()) {
			ancestors.add(anc);
			for (BNode anc2 : getAncestors(anc, max_length - 1)) {
				if (!ancestors.contains(anc2)) {
					ancestors.add(anc2);
				}
			}
		}
		return ancestors;
	}


	/**
	 * 
	 * @return
	 */
	public List<BNode> getSortedNodes() {
		List<BNode> endNodes = new LinkedList<BNode>();
		for (BNode n : nodes.values()) {
			if (n.getOutputNodes().isEmpty()) {
				endNodes.add(n);
			}
		}
		
		List<BNode> sortedNodes = new LinkedList<BNode>();
		sortedNodes.addAll(endNodes);
		for (BNode n : endNodes) {
			for (BNode anc2 : getAncestors(n, MAX_LENGTH))  {
				if (!sortedNodes.contains(anc2)) {
					sortedNodes.add(anc2);
				}
			}
		}
		
		return sortedNodes;
	}


}
