package org.neo4j.examples.server.unmanaged;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

public class NECTree {

	private NECNode rootNode;
	private List<MyNode> queryNodes;
	private Query queryGraph;
	private int number_of_NECNodes = 0;
	private Map<Integer, NECNode> queryNodeNECNodeMap;
	
	/**
	 * Constructor of the Class
	 */
	
	public NECTree(List<MyNode> queryNodes, MyNode startNode, Query queryGraph) {
		this.queryGraph = queryGraph;
		this.queryNodes = queryNodes;
		rootNode = new NECNode(number_of_NECNodes, startNode.getLabels());
		rootNode.addMember(startNode);
		number_of_NECNodes++;
		queryNodeNECNodeMap = new HashMap<Integer, NECNode>();
		queryNodeNECNodeMap.put(startNode.getId(), rootNode);
	}
	
	/**
	 * This function rewrites the query graph from the beginning into a tree consisting of NEC nodes 
	 * of query nodes. It returns the root node of the NEC tree.
	 */
	
	public NECNode RewriteToNECTree() {
		List<Integer> visitedQueryNodeIds = new ArrayList<Integer>();
		List<NECNode> currentNECNodes = new ArrayList<NECNode>();
		List<NECNode> childNECNodes = new ArrayList<NECNode>();
		
		visitedQueryNodeIds.add(rootNode.getNECMembers().get(0).getId());
		childNECNodes.add(rootNode);

		while (childNECNodes.size() > 0) {
			currentNECNodes.addAll(childNECNodes);
			childNECNodes.clear();
			for(Iterator<NECNode> i = currentNECNodes.iterator(); i.hasNext(); ) {
				NECNode necNode = i.next();
				List<MyNode> necMembers = necNode.getNECMembers();
				List<Integer> neighborSet = new ArrayList<Integer>();
				for (Iterator<MyNode> j = necMembers.iterator(); j.hasNext(); )
					takeUnion(j.next().getNeighbors(), neighborSet);
				takeUnvisitedsAndMark(neighborSet, visitedQueryNodeIds);
				if (neighborSet.size() > 0) {
					List<List<MyNode>> groups_wrt_label = groupByLabel(neighborSet);
					for (Iterator<List<MyNode>> k = groups_wrt_label.iterator(); k.hasNext(); ) {
						List<List<MyNode>> listOfNECs = new ArrayList<List<MyNode>>();
						findNEC(k.next(), listOfNECs);
						createNECs(listOfNECs, necNode, childNECNodes);
					}
				}
			}
			currentNECNodes.clear();
		}
		visitedQueryNodeIds.clear();
		return rootNode;
	}
	
	/**
	 * This function takes a group of query nodes of the same label, determines the NEC which they belong to 
	 * by analyzing their neighborhood with their relationships, and puts them into corresponding classes, 
	 * and the result list of NECs is assigned to listOfNECs.
	 */
	
	public void findNEC(List<MyNode> group, List<List<MyNode>> listOfNECs) {
		List<MyNode> sampleGroup = new ArrayList<MyNode>(); // This is a specific NEC
		List<MyNode> tempGroup = new ArrayList<MyNode>();	// This group can be in the same NEC, but different than sampleGroup 
		MyNode sampleNode = group.get(0);
		List<Integer> sampleNodeNeighborIds = sampleNode.getNeighbors();
		for (int i=1; i<group.size(); i++) {
			MyNode queryNode = group.get(i);
			List<Integer> queryNodeNeighborIds = queryNode.getNeighbors();
			if (sampleNodeNeighborIds.size() == queryNodeNeighborIds.size()) {
				if ( sampleNodeNeighborIds.containsAll(queryNodeNeighborIds) )
					sampleGroup.add(queryNode);
				else {	// Do they form a clique?
					if (doTheyFormClique(sampleNode, queryNode))
						sampleGroup.add(queryNode);
					else
						tempGroup.add(queryNode);
				}
			}
			else
				tempGroup.add(queryNode);
		}
		group.clear();
		if (tempGroup.size() > 0) {
			findNEC(tempGroup, listOfNECs);
			tempGroup.clear();
		}
		tempGroup = filterNECOfSampleGroup(sampleGroup, sampleNode, sampleNodeNeighborIds);
		listOfNECs.add(sampleGroup);
		if (tempGroup.size() > 0) {
			findNEC(tempGroup, listOfNECs);
			tempGroup.clear();
		}
	}
	
	/**
	 * This function creates corresponding NEC nodes to groups of query nodes in listOfNECs. 
	 * Then, it places the created NEC nodes into childNECNodes.
	 */
	
	public void createNECs(List<List<MyNode>> listOfNECs, NECNode parentNode, List<NECNode> childNECNodes) {
		for (int i=0; i<listOfNECs.size(); i++) {
			List<MyNode> necMembers = listOfNECs.get(i);
			MyNode sampleNode = necMembers.get(0);
			// Create NEC node
			NECNode necNode = new NECNode(number_of_NECNodes, sampleNode.getLabels());
			// Add members
			for (int j=0; j<necMembers.size(); j++) {
				MyNode queryNode = necMembers.get(j);
				necNode.addMember(queryNode);
				queryNodeNECNodeMap.put(queryNode.getId(), necNode);
			}
			// Set parent
			necNode.setParent(parentNode);
			// Relate with parent
			MyNode sampleParentNode = parentNode.getNECMembers().get(0); 
			MyRelation outgoingRelation = queryGraph.getRelationByNodeIds(sampleNode.getId(), sampleParentNode.getId());
			MyRelation incomingRelation = queryGraph.getRelationByNodeIds(sampleParentNode.getId(), sampleNode.getId());
			necNode.setRelations(outgoingRelation, incomingRelation);
			// Add as a child to parent
			parentNode.addChild(necNode);
			number_of_NECNodes++;
			childNECNodes.add(necNode);
		}
	}
	
	/**
	 * This function checks for the existence of a clique between two nodes given in the argument
	 */
	
	public boolean doTheyFormClique(MyNode node1, MyNode node2) {
		MyRelation relation1 = queryGraph.getRelationByNodeIds(node1.getId(), node2.getId());
		MyRelation relation2 = queryGraph.getRelationByNodeIds(node2.getId(), node1.getId());
		if (relation1 == null || relation2 == null)
			return false;
		else if (relation1.getType() == relation2.getType())
			return true;
		else
			return false;
	}
	
	/**
	 * This function checks the query nodes in the group which are the candidates for belonging to the 
	 * same NEC with sampleNode. If a query node belongs to the NEC of sampleNode, then it is conserved 
	 * in the group, otherwise it is put into another group called tempGroup. At the end, the group is 
	 * consisted of only the nodes belonging to the NEC of sampleNode and the filtered ones are returned 
	 * as a list.
	 */
	
	public List<MyNode> filterNECOfSampleGroup(List<MyNode> group, MyNode sampleNode, List<Integer> sampleNodeNeighborIds) {
		List<MyNode> nec = new ArrayList<MyNode>();
		List<MyNode> tempGroup = new ArrayList<MyNode>();
		List<MyRelation> outgoingRelations = sampleNode.getRelations(Direction.OUTGOING);
		List<MyRelation> incomingRelations = sampleNode.getRelations(Direction.INCOMING);
		nec.add(sampleNode);
		for (int i=0; i<group.size(); i++) {
			MyNode queryNode = group.get(i);
			if (haveTheSameNeighborhood(outgoingRelations, queryNode, Direction.OUTGOING)) {
				if (haveTheSameNeighborhood(incomingRelations, queryNode, Direction.INCOMING)) {
					nec.add(queryNode);
					continue;
				}
			}
			tempGroup.add(queryNode);
		}
		group.clear();
		group.addAll(nec);
		nec.clear();
		return tempGroup;
	}
	
	/**
	 * This function checks the neighborhood of queryNode. If the queryNode has the same type of relations in
	 * the same direction with the nodes which are the neighbors of the node connected to relations given in 
	 * the function argument, then it return true; else it returns false.
	 */
	
	public boolean haveTheSameNeighborhood(List<MyRelation> relations, MyNode queryNode, Direction direction) {
		for (int i=0; i<relations.size(); i++) {
			MyRelation relation1 = relations.get(i);
			MyRelation relation2 = null;
			if (direction == Direction.OUTGOING)
				relation2 = queryGraph.getRelationByNodeIds(queryNode.getId(), relation1.getEndNodeId());
			else
				relation2 = queryGraph.getRelationByNodeIds(relation1.getStartNodeId(), queryNode.getId());
			if (relation2 == null || relation1.getType() != relation2.getType())
				return false;
		}
		return true;
	}
	
	/**
	 * This function takes a list of query nodes and groups them with respect to their label.
	 * It returns the list of groups of query nodes by labels.
	 */
	
	public List<List<MyNode>> groupByLabel(List<Integer> neighborSet) {
		List<List<MyNode>> groups_wrt_label = new ArrayList<List<MyNode>>();
		List<Label> observedLabels = new ArrayList<Label>();
		for(int i=0; i<neighborSet.size(); i++) {
			int node_id = neighborSet.get(i);
			MyNode queryNode = queryNodes.get(node_id);
			Label nodeLabel = queryNode.getLabels().get(0);
			if (observedLabels.contains(nodeLabel)) {
				int index = observedLabels.indexOf(nodeLabel);
				groups_wrt_label.get(index).add(queryNode);
			}
			else {
				observedLabels.add(nodeLabel);
				List<MyNode> newGroup = new ArrayList<MyNode>();
				newGroup.add(queryNode);
				groups_wrt_label.add(newGroup);
			}
		}
		observedLabels.clear();
		return groups_wrt_label;
	}
	
	/**
	 * This function takes the union of two sets, set1 and set2. It saves the union set as set2.
	 */
	
	public void takeUnion(List<Integer> set1, List<Integer> set2) {
		if (set2.size() == 0)
			set2.addAll(set1);
		else
			for (int i=0; i<set1.size(); i++) {
				int element = set1.get(i);
				if (set2.contains(element))
					continue;
				set2.add(element);
			}
	}
	
	/**
	 * This function filters the query node ids in neighbor set regarding whether they are visited or not. 
	 * It resets neighborSet, puts the ones which are not in the visitedQueryNodeIds list into neighborSet, 
	 * but before return, it marks those nodes as "visited" by putting them into visitedQueryNodeIds list.
	 */
	
	public void takeUnvisitedsAndMark(List<Integer> neighborSet, List<Integer> visitedQueryNodeIds) {
		List<Integer> unvisitedNeighborSet = new ArrayList<Integer>();
		for (int i=0; i<neighborSet.size(); i++) {
			int node_id = neighborSet.get(i);
			if (visitedQueryNodeIds.contains(node_id))
				continue;
			unvisitedNeighborSet.add(node_id);
			visitedQueryNodeIds.add(node_id);
		}
		neighborSet.clear();
		neighborSet.addAll(unvisitedNeighborSet);
		unvisitedNeighborSet.clear();
	}
	
	/**
	 * This function traverses the NEC tree in BFS order and prints the NEC node features in order to test
	 */
	
	public String testNECTree() {
		String response = "";
		Queue<NECNode> NECTreeNodes = new LinkedList<NECNode>();
		NECTreeNodes.add(rootNode);
		while(NECTreeNodes.size() > 0) {
			NECNode necNode = NECTreeNodes.poll();
			response += "/************************************************/\n";
			response += necNode.printIdentity();
			response += "/************************************************/\n";
			List<NECNode> childNECNodes = necNode.getChildren();
			if (childNECNodes != null)
				NECTreeNodes.addAll(childNECNodes);
		}
		return response;
	}
	
}
