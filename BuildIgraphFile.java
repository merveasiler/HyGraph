package org.neo4j.examples.server.unmanaged;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

public class BuildIgraphFile {

	private Map<RelationshipType, Integer> relationTypesMap = new HashMap<RelationshipType, Integer>();
	private Map<Label, Integer> labelsMap = new HashMap<Label, Integer>();
	String response;
	
	public BuildIgraphFile() {
		
	}
	
	public boolean writeFile(GraphDatabaseService database) {
		boolean isSuccessful = true;
		
		// Create File
		File file = new File("nufus.igraph");
	    if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    // Read db
		try {
			FileWriter fileWriter = new FileWriter(file, false);
			BufferedWriter bWriter = new BufferedWriter(fileWriter);
			
			try ( Transaction tx = database.beginTx() ) {
				Iterable<RelationshipType> relationTypes = GlobalGraphOperations.at(database).getAllRelationshipTypes();
				Iterable<Label> labels = GlobalGraphOperations.at(database).getAllLabels();
				int index = 0, number_of_rtypes;
				for (Iterator<RelationshipType> i = relationTypes.iterator(); i.hasNext(); ) {
					relationTypesMap.put(i.next(), index);
					index++;
				}
				number_of_rtypes = index;
				index = 0;
				for (Iterator<Label> i = labels.iterator(); i.hasNext(); ) {
					labelsMap.put(i.next(), index);
					index++;
				}
				Long number_of_nodes = (long) 70500000;
				// Write vertices 
				for (Long node_id = (long) 0; node_id < number_of_nodes; node_id++) {
					try {
						Node node = database.getNodeById(node_id);
						Label nodeLabel = node.getLabels().iterator().next();
						int nodeLabelId = labelsMap.get(nodeLabel);
						bWriter.write("v " + node_id + " " + nodeLabelId + "\n");
					
					}
					catch (Exception e) {
					}
					bWriter.flush();
				}
				// Write edges
				for (Long node_id = (long) 0; node_id < number_of_nodes; node_id++) {
					try {
						Node node = database.getNodeById(node_id);
						List<Long> tempOtherNodeIds = new ArrayList<Long>();
						List<Integer> tempTypeIds = new ArrayList<Integer>();
						Iterable<Relationship> outgoingRelations = node.getRelationships(Direction.OUTGOING);
						for (Iterator<Relationship> r = outgoingRelations.iterator(); r.hasNext(); ) {
							Relationship relation = r.next();
							Long other_node_id = relation.getEndNode().getId();
							if (other_node_id >= node_id) {
								RelationshipType type = relation.getType();
								int typeId = relationTypesMap.get(type);
								tempOtherNodeIds.add(other_node_id);
								tempTypeIds.add(typeId);
							}
						}
						Iterable<Relationship> incomingRelations = node.getRelationships(Direction.INCOMING);
						for (Iterator<Relationship> r = incomingRelations.iterator(); r.hasNext(); ) {
							Relationship relation = r.next();
							Long other_node_id = relation.getStartNode().getId();
							if (other_node_id >= node_id) {
								RelationshipType type = relation.getType();
								int typeId = relationTypesMap.get(type) + number_of_rtypes;
								tempOtherNodeIds.add(other_node_id);
								tempTypeIds.add(typeId);
							}
						}
						// Ordering
						if (tempOtherNodeIds.isEmpty() == false) {
							List<Long> orderedOtherNodeIds = new ArrayList<Long>();
							List<Integer> orderedTypeIds = new ArrayList<Integer>();
							orderedOtherNodeIds.add(tempOtherNodeIds.get(0));
							orderedTypeIds.add(tempTypeIds.get(0));
							for (int i=1; i < tempOtherNodeIds.size(); i++) {
								Long other_node_id = tempOtherNodeIds.get(i);
								int typeId = tempTypeIds.get(i);
								boolean not_broken = true;
								for (int j=0; j < orderedOtherNodeIds.size(); j++) {
									Long currentNodeId = orderedOtherNodeIds.get(j);
									if (other_node_id < currentNodeId) {
										orderedOtherNodeIds.add(j, other_node_id);
										orderedTypeIds.add(j, typeId);
										not_broken = false;
										break;
									}
								}
								if (not_broken) {
									orderedOtherNodeIds.add(other_node_id);
									orderedTypeIds.add(typeId);
								}
							}
							for (int i=0; i < orderedOtherNodeIds.size(); i++) {
								Long other_node_id = orderedOtherNodeIds.get(i);
								int typeId = orderedTypeIds.get(i);
								bWriter.write("e " + node_id + " " + other_node_id + " " + typeId + "\n");
							}
							orderedOtherNodeIds.clear();
							orderedTypeIds.clear();
							tempOtherNodeIds.clear();
							tempTypeIds.clear();
							orderedOtherNodeIds = null;
							orderedTypeIds = null;
							tempOtherNodeIds = null;
							tempTypeIds = null;
						}
						bWriter.flush();
					}
					catch (Exception e) {
					}
				}
			} 
			catch (Exception e) {
			    	response += e.getMessage();   	
			}
			
			bWriter.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return isSuccessful;
	}
}
