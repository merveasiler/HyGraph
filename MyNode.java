package org.neo4j.examples.server.unmanaged;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public class MyNode {
	
	private int id;
	private List<Label> labelList = null;
	private List<MyRelation> outgoingRelationList = null;
	private List<MyRelation> incomingRelationList = null;
	private List<Integer> neighborNodeIdList = null;
	private Map<String, String> propertyMap = null;
	
	/**
	 * Class Constructor 
	 */
	public MyNode() {
		labelList = new ArrayList<Label>();
		outgoingRelationList = new ArrayList<MyRelation>();
		incomingRelationList = new ArrayList<MyRelation>();
		neighborNodeIdList = new ArrayList<Integer>();
		propertyMap = new HashMap<String, String>();
	}
	
	// ID
	
	/** 
	 * This function puts the id of MyNode object with the given function argument 
	 */
	public boolean setId(int id) {
		this.id = id;
		return true;
	}
	
	/** 
	 * This function returns the id of MyNode object 
	 */
	public Integer getId() {
		return this.id;
	}
	
	// Labels
	
	/**
	 * This function adds a new label to MyNode object 
	 */
	public boolean addLabel(Label l) {
		labelList.add(l);
		return true;
	}
	
	/**
	 * This function returns all the labels of MyNode object 
	 */
	public List<Label> getLabels() {
		return labelList;
	}
	
	/** 
	 * This function checks whether MyNode object has the label given in function parameter
	 */
	public boolean hasLabel(Label l) {
		return labelList.contains(l);
	}

	// Properties
	
	/**
	 * This function sets a property value with the specified key to MyNode object
	 */
	public boolean setProperty(String key, String value) {
		propertyMap.put(key, value);
		return true;
	}
	
	/**
	 * This function returns the property value with the property key given in function parameter
	 */
	public String getProperty(String key) {
		return propertyMap.get(key);
	}
	
	/**
	 * This function returns the whole property key-value map of MyNode object
	 */
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	
	/**
	 * This function check whether MyNode object contains any property
	 */
	public boolean hasAnyProperty() {
		if (propertyMap.isEmpty())
			return false;
		return true;
	}
	
	/**
	 * This function checks whether MyNode object has the property given in function parameter
	 */
	public boolean hasProperty(String key) {
		if (propertyMap.containsKey(key))
			return true;
		return false;
	}
	
	// Neighbor Nodes
	
	/**
	 * This function adds id of  a just discovered neighbor node
	 */
	
	public boolean addNeighbor(int nodeId) {
		neighborNodeIdList.add(nodeId);
		return true;
	}
	
	/**
	 * This function checks whether the node whose id is given in function parameter is  
	 * neighbor of MyNode object, or not
	 */
	
	public boolean isNeighborTo(int nodeId) {
		if (neighborNodeIdList.contains(nodeId))
			return true;
		else
			return false;
	}
	
	/**
	 * This function returns the list which holds the id of the nodes that are neighbor to MyNode object
	 */
	
	public List<Integer> getNeighbors() {
		return neighborNodeIdList;
	}
	
	// Relationships
	
	/**
	 * This function adds a new relation with a direction 
	 */
	public boolean addRelation( MyRelation r, Direction d ) {
		if (d.equals(Direction.OUTGOING))
			outgoingRelationList.add(r);
		else
			incomingRelationList.add(r);
		return true;	
	}
	
	/**
	 * This function checks whether MyNode object has the relationship type in the direction 
	 * given in function parameter 
	 */
	public boolean hasRelation( RelationshipType type, Direction d) {
		List<MyRelation> directionalRelationListOfThisNode;
		if (d.equals(Direction.OUTGOING))
			directionalRelationListOfThisNode = outgoingRelationList;
		else
			directionalRelationListOfThisNode = incomingRelationList;
		
		for (int i=0; i<directionalRelationListOfThisNode.size(); i++)
			if ( directionalRelationListOfThisNode.get(i).getType().toString().equals(type.toString()) )
				return true;
		return false;
	}

	/**
	 * This function returns all the relations of MyNode object in the direction given in 
	 * function parameter
	 */
	public List<MyRelation> getRelations(Direction d) {
		if (d.equals(Direction.OUTGOING))
			return outgoingRelationList;
		else
			return incomingRelationList;
	}
	
	/**
	 * This function returns all the relations of MyNode object in the direction and relationship 
	 * type given in function parameter
	 */
	public List<MyRelation> getRelations(RelationshipType type, Direction d) {
		List<MyRelation> directionalRelationListOfThisNode;
		if (d.equals(Direction.OUTGOING))
			directionalRelationListOfThisNode = outgoingRelationList;
		else
			directionalRelationListOfThisNode = incomingRelationList;
		
		List<MyRelation> resultRelationList = new ArrayList<MyRelation>();
		MyRelation tempRelation;
		for (int i=0; i<directionalRelationListOfThisNode.size(); i++) {
			tempRelation = directionalRelationListOfThisNode.get(i);
			if ( tempRelation.getType().toString().equals(type.toString()) )
				resultRelationList.add( tempRelation);
		}
		return resultRelationList;
	}
	
	/**
	 * This function returns all different relationshipTypes in the direction given in the function 
	 * parameter together with their count. It returns a list whose elements are groups of 2 such 
	 * that the first object is RelationshipType, the second object is count of that type.
	 */
	public List<Object> getRelationTypesWithCount(Direction d) {
		List<MyRelation> directionalRelationListOfThisNode;
		if (d.equals(Direction.OUTGOING))
			directionalRelationListOfThisNode = outgoingRelationList;
		else
			directionalRelationListOfThisNode = incomingRelationList;
		
		List<Object> result_Type_Count_List = new ArrayList<Object>();
		List<RelationshipType> typeList = new ArrayList<RelationshipType>();
		MyRelation tempRelation;
		
		for (int i=0; i<directionalRelationListOfThisNode.size(); i++) {
			tempRelation = directionalRelationListOfThisNode.get(i);
			int ind = typeList.indexOf(tempRelation.getType());
			if ( ind >= 0 ) {
				ind = 2*ind+1;
				int count = (Integer)result_Type_Count_List.get(ind);
				result_Type_Count_List.set(ind, count+1);
			}
			else {
				typeList.add(tempRelation.getType());
				result_Type_Count_List.add(tempRelation.getType());
				result_Type_Count_List.add(1);
			}
		}
		return result_Type_Count_List;
	}
	
	// Degree
	
	/**
	 * This function returns the degree of MyNode object counting the relations in any direction 
	 */
	public int getDegree() {
		return outgoingRelationList.size() + incomingRelationList.size();
	}
	
	/**
	 * This function returns the degree of MyNode object counting the relations in the direction
	 * given in function parameter
	 */
	public int getDegree(Direction d) {
		if (d.equals(Direction.OUTGOING))
			return outgoingRelationList.size();
		else
			return incomingRelationList.size();
	}
		
}
