package org.neo4j.examples.server.unmanaged;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.Pair;
 
public class Query {
	private List<MyNode> queryNodes;
	private String query;
	private Iterable<RelationshipType> allRelationTypes;
	private Map<Integer, MyNode> nodeMap = new HashMap<Integer, MyNode>();
	private Map<Pair<Integer,Integer>, MyRelation> relationMap = new HashMap<>();
	
	/**
	 * Constructor of the class
	 */
	
	public Query(List<MyNode> queryNodes, String query, Iterable<RelationshipType> allRelationTypes) {
		this.queryNodes = queryNodes;
		this.query = query;
		this.allRelationTypes = allRelationTypes;
	}
	
	/**
	 * Returns the nodes of the query graph 
	 */

	public List<MyNode> getQueryNodes() {
		return queryNodes;
	}
	
	/**
	 * Returns the query graph node with the id given in function parameter
	 */
	
	public MyNode getNodeById(int id) {
		return nodeMap.get(id);
	}
	
	/**
	 * Returns the query graph relation whose end nodes' ids are given as function parameters
	 */
	
	public MyRelation getRelationByNodeIds(int startNodeId, int endNodeId) {
		return relationMap.get(Pair.of(startNodeId, endNodeId));
	}
	
	/**
	 * Takes the query graph as String input in the BFS Coding format
	 * Analyzes the edges, creates the nodes and relationships
	 * *******************************************
	 * Example Input (BFS Coding of Query Graph):
	 * <(0,1,KISI,ESI,OUTGOING,KISI)(0,1,KISI,ESI,INCOMING,KISI)(0,2,KISI,ANNESI,OUTGOING,KISI)(0,3,KISI,ANNESI,OUTGOING,KISI)
	 *  (0,4,KISI,ANNESI,OUTGOING,KISI)(0,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)(1,2,KISI,BABASI,OUTGOING,KISI)
	 *  (1,3,KISI,BABASI,OUTGOING,KISI)(1,4,KISI,BABASI,OUTGOING,KISI)(1,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)
	 *  (2,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)(3,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)(4,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)>
	 *  ******************************************
	 *  The above query can be given as input by copying the below line to the browser:
	 *  http://192.168.200.141:7474/examples/unmanaged/BasicMethod/(0,1,KISI,ESI,OUTGOING,KISI)(0,1,KISI,ESI,INCOMING,KISI)(0,2,KISI,ANNESI,OUTGOING,KISI)(0,3,KISI,ANNESI,OUTGOING,KISI)(0,4,KISI,ANNESI,OUTGOING,KISI)(0,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)(1,2,KISI,BABASI,OUTGOING,KISI)(1,3,KISI,BABASI,OUTGOING,KISI)(1,4,KISI,BABASI,OUTGOING,KISI)(1,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)(2,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)(3,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)(4,5,KISI,SON_ADRESI,INCOMING,BAGIMSIZBOLUM)
	 */
	
	public void extractQueryItems() {
		String[] edges = query.split("\\)");
		String[] edgeItems = new String[6];
		
		for(int i=0; i<edges.length; i++) {
			edges[i] = edges[i].substring(1);
			edgeItems = edges[i].split(",");
			int node1_id = Integer.parseInt(edgeItems[0]);
			int node2_id = Integer.parseInt(edgeItems[1]);
			
			if (queryNodes.size() < node1_id+1 )
				defineNode(edgeItems[2]);
			if (queryNodes.size() < node2_id+1 )
				defineNode(edgeItems[5]);
			
			MyNode node1  = queryNodes.get( node1_id );
			MyNode node2 = queryNodes.get( node2_id );
			relateNodes(node1, node2, edgeItems[3], edgeItems[4], i);
		}
	}
	
	/**
	 * Defines a new node with the given qualifications (labels and properties)
	 * Puts it into nodeMap with respect to id
	 * Adds it to queryNodes list
	 */
	
	public void defineNode(String qualifications) {
		MyNode tempNode = new MyNode();
		int count = qualifications.split("&").length;
		String[] nodeItems = new String[count];
		nodeItems = qualifications.split("&");
		for (int i=1; i < count; i++) {
			String[] property = nodeItems[i].split("=");
			tempNode.setProperty(property[0], property[1]);
		}
		tempNode.addLabel(DynamicLabel.label(nodeItems[0]));
		tempNode.setId(queryNodes.size());
		nodeMap.put(queryNodes.size(), tempNode);
		queryNodes.add(tempNode);
	}
	
	/**
	 * Defines the neighborhood between node1 and node2 if not defined before
	 * Adds the new relation to relation list of each node
	 */
	
	public void relateNodes(MyNode node1, MyNode node2, String qualifications, String direction, int relationId) {
		if (node1.isNeighborTo(node2.getId()) == false) {
			node1.addNeighbor(node2.getId());
			node2.addNeighbor(node1.getId());
		}

		if (direction.equals("INCOMING")) {
			MyNode tempNode = node1; 
			node1 = node2;
			node2 = tempNode;
		}

		MyRelation newRelation = defineRelation(qualifications, relationId, node1.getId(), node2.getId());
		node1.addRelation(newRelation, Direction.OUTGOING);
		node2.addRelation(newRelation, Direction.INCOMING);
	}
	
	/**
	 * Creates a new relation with the given qualifications (relationType, startNode, endNode)
	 * Puts it into relationMap with respect to its start and end node ids
	 */
	
	public MyRelation defineRelation(String qualifications, int relationId, int startNodeId, int endNodeId) {
		int count = qualifications.split("&").length;
		String[] relationItems = new String[count];
		relationItems = qualifications.split("&");
		String relationTypeName = relationItems[0];
		RelationshipType relationType = null;
		
		for (Iterator<RelationshipType> j = allRelationTypes.iterator(); j.hasNext(); ) {
			RelationshipType type = j.next();
			if (type.name().equals(relationTypeName)) {
				relationType = type;
				break;
			}
		}
		MyRelation newRelation = new MyRelation(relationType, relationId, startNodeId, endNodeId);
		for (int i=1; i < count; i++) {
			String[] property = relationItems[i].split("=");
			newRelation.setProperty(property[0], property[1]);
		}
		relationMap.put(Pair.of(startNodeId, endNodeId), newRelation);
		return newRelation;
	}
	
	/**
	 * This function prints all features (labels and properties) of each node and each relation 
	 * to understand whether the query extraction was done successfully, or not
	 */
	
	String testExtraction() {
		String response = "";
		for (int i=0; i < queryNodes.size(); i++) {
			response += "*** Query Node - " + i + " ***\n";
			MyNode queryNode = queryNodes.get(i);
			for (Iterator<Label> j = queryNode.getLabels().iterator(); j.hasNext(); ) {
				Label label = j.next();
				response += "Label: " + label.toString() + "\n";
			}
			response += testProperties( queryNode.getPropertyMap() );
			for (Iterator<MyRelation> r = queryNode.getRelations(Direction.OUTGOING).iterator(); r.hasNext(); ) {
				MyRelation relation = r.next();
				response += "Relation: " + relation.getType().name().toString() + " from " + relation.getStartNodeId() + " to " + relation.getEndNodeId() + "\n";
				response += testProperties( relation.getPropertyMap() );
			}
			for (Iterator<MyRelation> r = queryNode.getRelations(Direction.INCOMING).iterator(); r.hasNext(); ) {
				MyRelation relation = r.next();
				response += "Relation: " + relation.getType().name().toString() + " from " + relation.getStartNodeId() + " to " + relation.getEndNodeId() + "\n";
				response += testProperties( relation.getPropertyMap() );
			}
		}
		return response;
	}
	
	/**
	 * This function prints all properties in key+value format of any node or relation whose 
	 * property map is given in the function parameter
	 */
	
	String testProperties(Map <String, String> properties) {
		String propertyText = "";
		for (Iterator<String> p = properties.keySet().iterator(); p.hasNext(); ) {
			String key = p.next();
			propertyText += "Property: " + key + " = " + properties.get(key) + "\n";
		}
		return propertyText;
	}
	
	/**
	 * This function returns the branches of the DFS Tree of depth (given in the second parameter) starting 
	 * from startNode (given in the first parameter) as a List of paths where each represented by a List of 
	 * node_ids on the path
	 */
	
	public List<List<Integer>> dfsPaths(MyNode startNode, int depth, List<Integer> ancestors) {
		List<List<Integer>> resultList = new ArrayList<List<Integer>>();
		List<Integer> visitedNodeIds = new ArrayList<Integer>(); // Holds the visited nodes at the current depth
		List<Integer> branch = new ArrayList<Integer>();
		int lastIndex = ancestors.size();
		int startNodeId = startNode.getId();
		ancestors.add(startNodeId);
		for (Iterator<Integer> i = ancestors.iterator(); i.hasNext(); )
			branch.add(i.next());
		if (depth == 0 || startNode.getDegree() == 0) {
			resultList.add(branch);
			ancestors.remove(lastIndex);
			return resultList;
		}
		
		for (Iterator<MyRelation> i = startNode.getRelations(Direction.OUTGOING).iterator(); i.hasNext(); ) {
				MyRelation relation = i.next();
				int node_id = relation.getTheOtherNodeId(startNodeId);
				if (ancestors.contains(node_id) == false && visitedNodeIds.contains(node_id) == false) {
					visitedNodeIds.add(node_id);
					MyNode node = nodeMap.get(node_id);
					resultList.addAll( dfsPaths(node, depth-1, ancestors) );
				}
		}
		for (Iterator<MyRelation> i = startNode.getRelations(Direction.INCOMING).iterator(); i.hasNext(); ) {
			MyRelation relation = i.next();
			int node_id = relation.getTheOtherNodeId(startNodeId);
			if (ancestors.contains(node_id) == false && visitedNodeIds.contains(node_id) == false) {
				visitedNodeIds.add(node_id);
				MyNode node = nodeMap.get(node_id);
				resultList.addAll( dfsPaths(node, depth-1, ancestors) );
			}
		}
		if (visitedNodeIds.isEmpty())
			resultList.add(branch);
		ancestors.remove(lastIndex);
		visitedNodeIds.clear();
		return resultList;
	}
}
