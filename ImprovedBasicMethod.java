package org.neo4j.examples.server.unmanaged;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.Pair;
import org.neo4j.tooling.GlobalGraphOperations;

@Path("/ImprovedBasicMethod")
public class ImprovedBasicMethod {
	
	private final GraphDatabaseService database;
	private Query queryGraph;
	private NECTree necTree;
	private NECNode rootOfNECTree;
	private FilterCandidates filteringTool;
	private DetermineMatchOrder orderingTool;
	private Iterable<RelationshipType> allRelationTypes;
	private List<MyNode> queryNodes = new ArrayList<MyNode>();
	private Map<Integer, Long> matchedCoupleNodeIds = new HashMap<Integer, Long>();
	private Map<Integer, Long> matchedCoupleRelationIds = new HashMap<Integer, Long>();
	private Stack<Integer> notCheckedQueryNodeIds = new Stack<Integer>();
	private List<Pair<List<Long>, List<Long>>> matchedSubgraphs= new ArrayList<Pair<List<Long>, List<Long>>>();
	// Temporary Variables
	int matchCount = 0;
	long previousNodeId = (long) 0;
	int uniqueFamilyCount = 0;
	int matchCountPerFamily = 1;
	Long startTime, endTime, timeDifference;
	Long totalProcessTime = (long) 0;
	Long filterForInitialQueryNodeTotalTime = (long) 0;
	Long prepareCandidateRelationsTotalTime = (long) 0;
	Long relationPermutationCheckTotalTime = (long) 0;
	String response = "";
	
	/**
	 * Connects to the database
	 */
	
	public ImprovedBasicMethod(@Context GraphDatabaseService database) {
		this.database = database;
		// No need to construct the inverse vertex label list cause we can reach it by GlobalGraphOperations...getAllNodesWithLabel() function
		// No need to construct the adjacency list cause we can reach it by Node...getRelationships() function
		try ( Transaction tx = database.beginTx() )
		{
			allRelationTypes = GlobalGraphOperations.at(database).getAllRelationshipTypes();
			tx.success();
			tx.close();
		}
		catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * This function executes Cypher queries for test purposes
	 */
	
	public void executeCypherQuery() {
		Result result = null;
		Long startTime = (long) 0;
		Long endTime = (long) 0;
		int count = 0;
		List<Map<String, Object>> sonuc = new ArrayList<Map<String, Object>>();
		response += "Cypher Query Execution Time:\n ";
		try ( Transaction tx = database.beginTx() )
		{
			String cypherQuery = "MATCH (adres:BAGIMSIZBOLUM) - [:SON_ADRESI] -> (kisi1:KISI), (adres) - [:SON_ADRESI] -> (kisi2:KISI), (adres) - [:SON_ADRESI] -> (kisi3:KISI), (adres) - [:SON_ADRESI] -> (kisi4:KISI) RETURN adres";
			startTime = System.nanoTime();
			result = database.execute(cypherQuery);
			while ( result.hasNext() ) {
				sonuc.add( result.next() );
				count++;
			}
			//result.resultAsString();
		}
		catch (Exception e) {
			e.getMessage();
		}
		endTime = System.nanoTime();
    	Long timeDifference1 = (endTime - startTime) / 1000000;
    	response += timeDifference1 + " ms   " + count + "\n";
    			
    	count = 0;
		startTime = System.nanoTime();
		for (Iterator<Map<String, Object>> i = sonuc.iterator(); i.hasNext(); ) {
			i.next();
			count++;
		}
		endTime = System.nanoTime();
	    Long timeDifference2 = (endTime - startTime) / 1000000;
	    response += timeDifference2 + " ms   " + count + "\n";
	}
	
	/**
	 * This function reads the input query from browser and analyzes the query graph
	 */
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{query}")
	public Response executeQuery( @PathParam("query") String query) {

		// Analyze Query
		queryGraph = new Query(queryNodes, query, allRelationTypes);
		queryGraph.extractQueryItems();
		necTree = new NECTree(queryNodes, queryNodes.get(0), queryGraph);
		rootOfNECTree = necTree.RewriteToNECTree();

		filteringTool = new FilterCandidates(database, queryGraph, null, queryNodes, null);
//		Long startTime = System.nanoTime();	// Measure Time
//		findRootCandidates();
//		Long endTime = System.nanoTime();	// Measure Time
//    	Long timeDifference = (endTime - startTime) / 1000000;	// Measure Time
//    	totalProcessTime += timeDifference;	// Measure Time
//		//printMatch(27386);
//		
//    	// Print Total Wasted Times Part By Part
//    	response += "Total Process Time: " + totalProcessTime + " ms" + "\n";
//    	response += "Total Time Wasted In Filtering For The Initial Query Node: " + filterForInitialQueryNodeTotalTime + " ms" + "\n";
//    	response += "Total Time Wasted In Preparation of Candidate Relation Set: " + prepareCandidateRelationsTotalTime + " ms" + "\n";
//    	response += "Total Time Wasted In Relationship Permutation Check: " + relationPermutationCheckTotalTime + " ms" + "\n";
//    	
		return Response
				.status(Status.OK)
				.entity(("!!!HERE THE RESULTS ARE:" + "\n" + uniqueFamilyCount + "\n" + response).getBytes(Charset
						.forName("UTF-8"))).build();
	}
	
	/**
	 * This function finds all the candidate nodes for the first query node (root node) by using the 
	 * first 2 filtering techniques used in GraphQL. Then it starts a search for each candidate to find 
	 * a subgraph isomorphism for the query graph.
	 */
	
	public void findRootCandidates() {
		
		MyNode rootNode = rootOfNECTree.getNECMembers().get(0);
		Iterator<Node> rootCandidatesIterator  = null;
		try ( Transaction tx = database.beginTx() ) {
			startTime = System.nanoTime();		// Measure Time
			rootCandidatesIterator = filteringTool.filterCandidatesByLabelAndProperty(rootNode, rootCandidatesIterator);
			rootCandidatesIterator = filteringTool.filterCandidatesByRelationships(rootNode, rootCandidatesIterator, Direction.OUTGOING);
			rootCandidatesIterator = filteringTool.filterCandidatesByRelationships(rootNode, rootCandidatesIterator, Direction.INCOMING);
			endTime = System.nanoTime();		// Measure Time
	    	timeDifference = (endTime - startTime) / 1000000;	// Measure Time
	    	filterForInitialQueryNodeTotalTime += timeDifference;	// Measure Time
	    	startTime = (long) 0;		// Measure Time
		} 
		catch (Exception e) {
		    	response += e.getMessage();
		}
		
		for ( ; rootCandidatesIterator.hasNext(); ) {
			Node rootCandidate = rootCandidatesIterator.next();
			matchedCoupleNodeIds.put(rootNode.getId(), rootCandidate.getId());
			try ( Transaction tx = database.beginTx() ) {
				startFromRoot(rootNode, rootCandidate);
			}
			catch (Exception e) {
			   	response += e.getMessage();
			}
			matchedCoupleNodeIds.remove(rootNode.getId());			
		}
	}
	
	/**
	 * This function finds candidate relations for relations of the node rootCandidate.
	 * Then it continues according to all possible permutations of the candidate relations.
	 * It returns true only for the case where it needs to continue matching with an other 
	 * not-checked node by popping one more item from notCheckedQueryNodeIds stack in 
	 * checkOtherMatches(). If it returns false, checkOtherMatches() function understands 
	 * that it must break while loop without checking notCheckedQueryNodeIds stack is empty or
	 * not.
	 */
	
	public boolean startFromRoot(MyNode rootNode, Node rootCandidate) {
		List<Integer> neighborIds = new ArrayList<Integer>();
		List<Integer> relationIdsSet = new ArrayList<Integer>();
		List<List<Relationship>> candidateRelationsSet = new ArrayList<List<Relationship>>();
		
		startTime = System.nanoTime();		// Measure Time	
		boolean isSuccessful = prepareCandidateRelations(Direction.OUTGOING, rootNode, rootCandidate, neighborIds, relationIdsSet, candidateRelationsSet);
		endTime = System.nanoTime();		// Measure Time
    	timeDifference = (endTime - startTime) / 1000000;	// Measure Time
    	prepareCandidateRelationsTotalTime += timeDifference;	// Measure Time
    	startTime = (long) 0;		// Measure Time
		if (isSuccessful == false)
			return false;
		
		startTime = System.nanoTime();		// Measure Time	
		isSuccessful = prepareCandidateRelations(Direction.INCOMING, rootNode, rootCandidate, neighborIds, relationIdsSet, candidateRelationsSet);
		endTime = System.nanoTime();		// Measure Time
    	timeDifference = (endTime - startTime) / 1000000;	// Measure Time
    	prepareCandidateRelationsTotalTime += timeDifference;	// Measure Time
    	startTime = (long) 0;		// Measure Time
		if (isSuccessful == false)
			return false;
		
		if (candidateRelationsSet.isEmpty())
			return true;
		checkEachPermutation(relationIdsSet, candidateRelationsSet, 0, rootCandidate, neighborIds);
		notCheckedQueryNodeIds.clear();
		return false;
	}
	
	/**
	 * This function finds the candidate relations for the query graph relations given in the 
	 * function parameter. It returns false if there is no candidate.
	 */
	
	public boolean prepareCandidateRelations(Direction direction, MyNode rootNode, Node rootCandidate, List<Integer> neighborIds, List<Integer> relationIdsSet, List<List<Relationship>> candidateRelationsSet) {
		List<MyRelation> relationList = rootNode.getRelations(direction);
		int rootNodeId = rootNode.getId();
		for (int i=0; i < relationList.size(); i++) {
			MyRelation relation = relationList.get(i);
			if ( matchedCoupleRelationIds.containsKey(relation.getId()) )
				continue; // This edge was already matched during the check of neighborId
			Iterable<Relationship> candidateRelations = rootCandidate.getRelationships(relation.getType(), direction);
			if (candidateRelations == null)
				return false;
			List<Relationship> candidateRelationList = new ArrayList<Relationship>();
			filteringTool.checkRelationProperties(relation, candidateRelations, candidateRelationList);
			if (candidateRelationList.isEmpty())
				return false;
			candidateRelationsSet.add(candidateRelationList);
			relationIdsSet.add(relation.getId());
			int neighborId = relation.getTheOtherNodeId(rootNodeId);
			neighborIds.add( neighborId );
		}
		return true;
	}
	

	
	/**
	 * This function creates all possible permutations of relations which are candidates for 
	 * the neighbor relations of root. For each possible permutation, it calls checkOtherMatches() 
	 * function in order to continue match operation of the not-matched (actually not-checked)
	 * nodes and relations. This function works with the principle of backtracking algorithm.
	 */
	
	public void checkEachPermutation(List<Integer> relationIdsSet, List<List<Relationship>> candidateRelationsSet, int index, Node rootCandidate, List<Integer> neighborIds) {
		int neighborId = neighborIds.get(index);
		MyNode neighborNode = queryNodes.get(neighborId);
		Set<String> neighborNodePropertyKeys = neighborNode.getPropertyMap().keySet();
		int relationId = relationIdsSet.get(index);
		List<Relationship> candidateRelations = candidateRelationsSet.get(index);
		for (Iterator<Relationship> r = candidateRelations.iterator(); r.hasNext(); ) {
			boolean isNodeDiscovered = false;
			Relationship candidateRelation = r.next();
			Node neighborCandidate = candidateRelation.getOtherNode(rootCandidate);
			if ( matchedCoupleNodeIds.containsKey(neighborId) ) {
				if ( matchedCoupleNodeIds.get(neighborId) != neighborCandidate.getId() )
					continue;
			}
			else if ( matchedCoupleNodeIds.containsValue(neighborCandidate.getId()) )
				continue;
			else if ( filteringTool.checkRestOfNodeProperties(neighborCandidate, neighborNode, neighborNodePropertyKeys.iterator(), false) == false )
				continue;
			else if ( filteringTool.checkRelationshipCount(neighborNode, neighborCandidate, Direction.OUTGOING) == false)
				continue;
			else if ( filteringTool.checkRelationshipCount(neighborNode, neighborCandidate, Direction.INCOMING) == false)
				continue;
			else {
				matchedCoupleNodeIds.put(neighborId, neighborCandidate.getId());
				notCheckedQueryNodeIds.push(neighborId);
				isNodeDiscovered = true;
			}
			matchedCoupleRelationIds.put(relationId, candidateRelation.getId());
			
			if ( index+1 == neighborIds.size() )
				checkOtherMatches();
			else
				checkEachPermutation(relationIdsSet, candidateRelationsSet, index+1, rootCandidate, neighborIds);
			// Remove the lastly added relationship and node to backtrack one step
			matchedCoupleRelationIds.remove(relationId);
			if (isNodeDiscovered) {
				matchedCoupleNodeIds.remove(neighborId);
				notCheckedQueryNodeIds.pop();
			}
		}
	}
	
	/**
	 * This function pops one not-checked node from stack and send it to startFromRoot()
	 * to continue matching from that node.
	 */
	
	public void checkOtherMatches() {
		Stack<Integer> tempNotCheckedQueryNodeIds = new Stack<Integer>();
		for (int i = 0; i < notCheckedQueryNodeIds.size(); i++)
			tempNotCheckedQueryNodeIds.push(notCheckedQueryNodeIds.get(i));
		boolean shouldContinue = true;
		while (notCheckedQueryNodeIds.isEmpty() == false) {
			int queryNodeId = notCheckedQueryNodeIds.pop();
			MyNode queryNode = queryNodes.get(queryNodeId);
			Long dbNodeId = matchedCoupleNodeIds.get(queryNodeId);
			Node dbNode = database.getNodeById(dbNodeId);
			shouldContinue = startFromRoot(queryNode, dbNode);
			if (shouldContinue == false)
				break;
		}
		savePermutation(shouldContinue, tempNotCheckedQueryNodeIds);
	}
	
	/**
	 * This function saves the just found match which is isomorphic to query graph.
	 * Also it brings the notCheckedQueryNodeIds stack to the old condition that is 
	 * the situation before calling the startFromRoot() inside checkOtherMatches().
	 * This is done in order to look for other possible matches rooting from the same 
	 * previously-matched partial subgraph structure.
	 */
	
	public void savePermutation(boolean isSuccessful, Stack<Integer> tempNotCheckedQueryNodeIds) {
		if (isSuccessful) {
			List<Long> dbNodeIds = new ArrayList<Long>();
			for (Iterator<Long> i = matchedCoupleNodeIds.values().iterator(); i.hasNext(); )
				dbNodeIds.add(i.next());
			List<Long> dbRelationIds = new ArrayList<Long>();
			for (Iterator<Long> i = matchedCoupleRelationIds.values().iterator(); i.hasNext(); )
				dbRelationIds.add(i.next());
			Pair<List<Long>, List<Long>> matchedInstance = Pair.of(dbNodeIds, dbRelationIds);
			matchedSubgraphs.add(matchedInstance);
			printResults();
		}
		notCheckedQueryNodeIds.clear();
		notCheckedQueryNodeIds = tempNotCheckedQueryNodeIds;
	}
	
	/**
	 * This function prints the matched instance whose rank is given as the function parameter.
	 */
	
	public void printMatch(int rank) {
		response += "********** MATCH NO - " + rank + ": **********" + "\n";
		Pair<List<Long>, List<Long>> matchedInstance = matchedSubgraphs.get(rank);
		List<Long> nodeIds = matchedInstance.first();
		List<Long> relationIds = matchedInstance.other();
		response += "Node Ids:    ";
		for (int i=0; i < nodeIds.size(); i++)
			response += nodeIds.get(i) + "  ";
		response += "\nRelation Ids: ";
		for (int i=0; i < relationIds.size(); i++)
			response += relationIds.get(i) + "  ";
		response += "\n****************************************\n";
	}
	
	/**
	 * This function prints the node ids and relationship ids to check whether the 
	 * match found is correct. It prints the content of matchedSubgraphs.
	 */
	
	public void printResults() {
		Long motherNodeId = matchedCoupleNodeIds.get(0);
		if (motherNodeId != previousNodeId) {
//			if (previousNodeId > 0) {
//				response += "********** " + matchCount + " **********" + "\n"; 
//				response += previousNodeId + " x " + matchCountPerFamily + "\n";
//				int lastMatchIndex = matchedSubgraphs.size() - 1;
//				Pair<List<Long>, List<Long>> matchedInstance = matchedSubgraphs.get(lastMatchIndex);
//				List<Long> nodeIds = matchedInstance.first();
//				List<Long> relationIds = matchedInstance.other();
//				response += "Node Ids:    ";
//				for (int i=0; i < nodeIds.size(); i++)
//					response += nodeIds.get(i) + "  ";
//				response += "\nRelation Ids: ";
//				for (int i=0; i < relationIds.size(); i++)
//					response += relationIds.get(i) + "  ";
//				response += "\n *************************** \n";
//			}
			previousNodeId = motherNodeId;
			uniqueFamilyCount++;
			matchCountPerFamily = 1;
		}
		else
			matchCountPerFamily++;
		matchCount++;
	}	
	
	/**
	 * This function prints the node ids and relationship ids to check whether the 
	 * match found is correct. It prints the content of matchedCoupleNodeIds and 
	 * matchedCoupleRelationIds.
	 */
	
	public void printResults2() {
		response += "******* " + matchCount + " ********\n";
		response += "MATCHED NODE IDS:\n";
		for (Iterator<Entry<Integer, Long>> k = matchedCoupleNodeIds.entrySet().iterator(); k.hasNext(); ) {
			Entry<Integer, Long> entry = k.next();
			response += "Key: " + entry.getKey() + " - Value: " + entry.getValue() + "\n";
		}
		response += "MATCHED RELATION IDS:\n";
		for (Iterator<Entry<Integer, Long>> k = matchedCoupleRelationIds.entrySet().iterator(); k.hasNext(); ) {
			Entry<Integer, Long> entry = k.next();
			response += "Key: " + entry.getKey() + " - Value: " + entry.getValue() + "\n";
		}
		response += "NOT CHECKED QUERY NODE IDS\n";
		for (int k=0; k < notCheckedQueryNodeIds.size(); k++)
			response += notCheckedQueryNodeIds.get(k) + "\n";
		response += "******************\n";
		matchCount++;
	}
	
	/**
	 * This function prints the results into a file
	 */
	
	public void printIntoFile() {
//		Writer writer = null;
//
//		try {
//		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\merve.asiler\\workspace\\unmanaged\\filename.txt"), "utf-8"));
//		    writer.write("something");
//		} catch (IOException ex) {
//		  // report
//		} finally {
//		   try {writer.close();} catch (Exception ex) {/*ignore*/}
//		}
		
		File file = new File("C:\\Users\\merve.asiler\\workspace\\unmanaged\\filename.txt");
        if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		try {
			FileWriter fileWriter = new FileWriter(file, false);
			BufferedWriter bWriter = new BufferedWriter(fileWriter);
			bWriter.write("Hello");
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

