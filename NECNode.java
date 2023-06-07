package org.neo4j.examples.server.unmanaged;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Label;

public class NECNode {
	private int id;
	private List<Label> labelList;
	private MyRelation relationToParent;
	private MyRelation relationFromParent;
	private NECNode parentNECNode;
	private List<NECNode> childNECNodes;
	private List<MyNode> necMembers;
	
	/**
	 * Class Constructor
	 */
	
	public NECNode(int id, List<Label> labelList) {
		this.id = id;
		this.labelList = labelList; 
		this.necMembers = new ArrayList<MyNode>();
		this.relationToParent = null;
		this.relationFromParent = null;
		this.parentNECNode = null;
		this.childNECNodes = null;
	}
	
	/**
	 * This function returns the id of the current NEC node.
	 */
	
	public Integer getId() {
		return this.id;
	}
	
	/**
	 * This function adds childNode as a child NEC node to the current NEC node.
	 */
	
	public void addChild(NECNode childNode) {
		if (childNECNodes == null)
			childNECNodes = new ArrayList<NECNode>();
		childNECNodes.add(childNode);
	}
	
	/**
	 * This function adds queryNode determined as a member of current NEC.
	 */
	
	public void addMember(MyNode queryNode) {
		necMembers.add(queryNode);
	}
	
	/**
	 * This function returns the list of child NEC nodes of the current NEC node.
	 */
	
	public List<NECNode> getChildren() {
		return childNECNodes;
	}
	
	/**
	 * This function returns the list of query nodes which are members of current NEC node.
	 */
	
	public List<MyNode> getNECMembers() {
		return necMembers;
	}
	
	/**
	 * This function sets the parent NEC node of the current NEC node.
	 */
	
	public void setParent(NECNode parentNode) {
		this.parentNECNode = parentNode;
	}
	
	/**
	 * This function sets the relations between the NEC node and its parent.
	 * If a relation is absent, then it sets it as null.
	 * Note that it sets the same relations defined for the connections between query nodes.
	 * Hence, relation.startNodeId() and relation.endNodeId() functions will return the corresponding 
	 * query node ids, not NEC node ids. This relations were set only to inform the relation type and 
	 * direction.
	 */
	
	public void setRelations(MyRelation outgoingRelation, MyRelation incomingRelation) {
		this.relationToParent = outgoingRelation;
		this.relationFromParent = incomingRelation;
	}
	
	/**
	 * This function saves all the identity information of the NEC node into a String, and returns it.
	 */
	
	public String printIdentity() {
		String information = "";
		information += "NEC Node: " + id + "\n";
		information += "label: " + labelList.get(0).toString() + "\n";
		if (parentNECNode != null)
			information += "Parent NEC Node: " + parentNECNode.getId() + "\n";
		if (relationToParent != null)
			information += "Relation going to parent: " + relationToParent.getType().name().toString() + "\n";
		if (relationFromParent != null)
			information += "Relation coming from parent: " + relationFromParent.getType().name().toString() + "\n";
		information += "NEC Member Query Node Ids are: ";
		for (int i=0; i<necMembers.size(); i++)
			information += necMembers.get(i).getId() + ", ";
		if (childNECNodes != null) {
			information += "\n" + "Child NEC Node Ids are: ";
			for (int i=0; i<childNECNodes.size(); i++)
				information += childNECNodes.get(i).getId() + ", ";
		}
		information += "\n";
		return information;
	}
}
