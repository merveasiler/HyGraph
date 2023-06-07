package org.neo4j.examples.server.unmanaged;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

public class MyRelation {

		/**
		 * The direction of this relation is from the node whose id is startNodeId to the node whose id is endNodeId
		 */
		
		private RelationshipType type;
		private int id;
		private int startNodeId;
		private int endNodeId;
		private Map<String, String> propertyMap = null;
		
		/**
		 * Class Constructor 
		 */
		
		public MyRelation(RelationshipType type, int id, int startNodeId, int endNodeId) {
			
			this.type = type;
			this.id = id;
			this.startNodeId = startNodeId;
			this.endNodeId = endNodeId;
			this.propertyMap = new HashMap<String, String>();
		}
		
		/** 
		 * This function returns the relationship type of MyRelation object
		 */
		
		public RelationshipType getType() {
			return type;
		}

		/** 
		 * This function returns the id of MyRelation object
		 */
		
		public int getId() {
			return id;
		}
		
		/**
		 * This function returns the direction of this relation with respect to the nodeId given in function parameter
		 */
		
		public Direction getDirection(int nodeId) {
			if (nodeId == startNodeId)
				return Direction.OUTGOING;
			else
				return Direction.INCOMING;
		}
		
		/** 
		 * This function returns the id of the start node of MyRelation object
		 */
		
		public int getStartNodeId() {
			return startNodeId;
		}
		
		/** 
		 * This function returns the id of the end node of MyRelation object
		 */
		
		public int getEndNodeId() {
			return endNodeId;
		}
		
		/**
		 * This function returns the id of the other node when one of the nodes of 
		 * MyRelation object is given in the input parameter
		 */
		
		public int getTheOtherNodeId(int node_id) {
			if (node_id == startNodeId)
				return endNodeId;
			else
				return startNodeId;
		}

		// Properties
		
		/**
		 * This function sets a property value with the specified key to MyRelation object
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
		 * This function returns the whole property key-value map of MyRelation object
		 */
		public Map<String, String> getPropertyMap() {
			return propertyMap;
		}
		
		/**
		 * This function check whether MyRelation object contains any property
		 */
		public boolean hasAnyProperty() {
			if (propertyMap.isEmpty())
				return false;
			return true;
		}
		
		/**
		 * This function checks whether MyRelation object has the property given in function parameter
		 */
		public boolean hasProperty(String key) {
			if (propertyMap.containsKey(key))
				return true;
			return false;
		}
		
}
