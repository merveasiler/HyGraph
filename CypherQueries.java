package org.neo4j.examples.server.unmanaged;

import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.graphdb.ExecutionPlanDescription;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

@Path("/Cypher")
public class CypherQueries {

	private final GraphDatabaseService database;
	private MeasureSourceConsumption measuringTool = new MeasureSourceConsumption();
	String response = "";
	String[] queries = new String[11];
	
	/**
	 * Connects to the database
	 */
	
	public CypherQueries(@Context GraphDatabaseService database) {
		this.database = database;
		
		// N�FUS VER�TABANI //
//		queries[1] = "MATCH (baba:`K���`) - [r1:`E��`] -> (anne:`K���`) <- [r2:`ANNES�`] - (cocuk1:`K���`) - [r3:BABASI] -> (baba) <- [r4:BABASI] - (cocuk2:`K���`) - [r5:`ANNES�`] -> (anne) <- [r6:`ANNES�`] - (cocuk3:`K���`) - [r7:BABASI] -> (baba) - [r8:`YERLE��M_YER�`] -> (adres:`BA�IMSIZ_B�L�M`) <- [r9:`YERLE��M_YER�`] - (anne), (cocuk1) - [r10:`YERLE��M_YER�`] -> (adres) <- [r11:`YERLE��M_YER�`] - (cocuk2), (adres) <- [r12:`YERLE��M_YER�`] - (cocuk3) WHERE NOT (anne) IN [baba,cocuk1,cocuk2,cocuk3] AND NOT (baba) IN [cocuk1,cocuk2,cocuk3] AND NOT (cocuk1) IN [cocuk2,cocuk3] AND NOT (cocuk2) IN [cocuk3] RETURN *";
//		queries[2] = "MATCH (baba:`K���`) - [r1:`E��`] -> (anne:`K���`) - [r2:`YERLE��M_YER�`] -> (adres:`BA�IMSIZ_B�L�M`) <- [r3:`YERLE��M_YER�`] - (baba) <- [r4:BABASI] - (cocuk:`K���`) - [r5:`ANNES�`] -> (anne), (cocuk) - [r6:`YERLE��M_YER�`] -> (adres) <- [r7:`YERLE��M_YER�`] - (gelin:`K���`) <- [r8:`E��`] - (cocuk) WHERE NOT (baba) IN [anne,cocuk,gelin] AND NOT (anne) IN [cocuk,gelin] AND NOT (cocuk) IN [gelin] RETURN *";
//		queries[3] = "MATCH (dede:`K���`) <- [r1:BABASI] - (anne1:`K���`) <- [r2:`ANNES�`] - (cocuk1:`K���`) - [r3:`E��`] -> (cocuk2:`K���`) - [r4:`ANNES�`] -> (anne2:`K���`) - [r5:BABASI] -> (dede) WHERE NOT (dede) IN [anne1,anne2,cocuk1,cocuk2] AND NOT (anne1) IN [anne2,cocuk1,cocuk2] AND NOT (anne2) IN [cocuk1,cocuk2] AND NOT (cocuk1) IN [cocuk2] RETURN *";
//		queries[4] = "MATCH (nine:`K���`) <- [r1:`ANNES�`] - (baba1:`K���`) <- [r2:BABASI] - (cocuk1:`K���`) - [r3:`YERLE��M_YER�`] -> (adres:`BA�IMSIZ_B�L�M`) <- [r4:`YERLE��M_YER�`] - (cocuk2:`K���`) - [r5:BABASI] -> (baba2:`K���`) - [r6:`ANNES�`] -> (nine) WHERE NOT (nine) IN [baba1,baba2,cocuk1,cocuk2] AND NOT (baba1) IN [baba2,cocuk1,cocuk2] AND NOT (baba2) IN [cocuk1,cocuk2] AND NOT (cocuk1) IN [cocuk2] RETURN *";
//		queries[5] = "MATCH (baba:`K���`) - [r1:`E��`] -> (es2:`K���`) <- [r2:`ANNES�`] - (cocuk2:`K���`) - [r3:BABASI] -> (baba) - [r4:`ESK� E��`] -> (es1:`K���`) <- [r5:`ANNES�`] - (cocuk1:`K���`) - [r6:BABASI] -> (baba) - [r7:`YERLE��M_YER�`] -> (adres:`BA�IMSIZ_B�L�M`) <- [r8:`YERLE��M_YER�`] - (es2), (cocuk1) - [r9:`YERLE��M_YER�`] -> (adres) <- [r10:`YERLE��M_YER�`] - (cocuk2), (es1) - [r11:`YERLE��M_YER�`] -> (baskaadres:`BA�IMSIZ_B�L�M`) WHERE NOT (baba) IN [es1,es2,cocuk1,cocuk2] AND NOT (es1) IN [es2,cocuk1,cocuk2] AND NOT (es2) IN [cocuk1,cocuk2] AND NOT (cocuk1) IN [cocuk2] AND NOT (adres) IN [baskaadres] RETURN *";
//		queries[6] = "MATCH (anne:`K���`) <- [r1:`ESK� E��`] - (baba:`K���`) - [r2:`YERLE��M_YER�`] -> (baskaadres:`BA�IMSIZ_B�L�M`), (anne) <- [r3:`ANNES�`] - (cocuk:`K���`) - [r4:BABASI] -> (baba), (cocuk) - [r5:`YERLE��M_YER�`] -> (adres:`BA�IMSIZ_B�L�M`) <- [r6:`YERLE��M_YER�`] - (anne) - [r7:`ANNES�`] -> (nine:`K���`) <- [r8:`E��`] - (dede:`K���`) <- [r9:BABASI] - (anne), (nine) - [r10:`YERLE��M_YER�`] -> (adres) <- [r11:`YERLE��M_YER�`] - (dede) WHERE NOT (anne) IN [baba,cocuk,nine,dede] AND NOT (baba) IN [cocuk,nine,dede] AND NOT (cocuk) IN [nine,dede] AND NOT (nine) IN [dede] AND NOT (adres) IN [baskaadres] RETURN *";
//		queries[7] = "MATCH (hane1:PRM_HANE) <- [r1:`B�REY`] - (es1:`K���`) - [r2:`E��`] -> (es2:`K���`) - [r3:`B�REY`] -> (hane2:PRM_HANE) WHERE NOT (es1) IN [es2] AND NOT (hane1) IN [hane2] RETURN * ";
//		queries[8] = "MATCH (bina:`B�NA`) <- [r1:`B�NASI`] - (adres1:`BA�IMSIZ_B�L�M`) <- [r2:`YERLE��M_YER�`] - (nine:`K���`) <- [r3:`E��`] - (dede:`K���`) - [r4:`YERLE��M_YER�`] -> (adres1), (bina) <- [r5:`B�NASI`] - (adres2:`BA�IMSIZ_B�L�M`) <- [r6:`YERLE��M_YER�`] - (erkekcocuk:`K���`) - [r7:`E��`] -> (gelin:`K���`) - [r8:`YERLE��M_YER�`] -> (adres2) <- [r9:`YERLE��M_YER�`] - (torun1:`K���`) - [r10:BABASI] -> (erkekcocuk) <- [r11:BABASI] - (torun2:`K���`) - [r12:`ANNES�`] -> (gelin) <- [r13:`ANNES�`] - (torun1), (torun2) - [r14:`YERLE��M_YER�`] -> (adres2), (bina) <- [r15:`B�NASI`] - (adres3:`BA�IMSIZ_B�L�M`) <- [r16:`YERLE��M_YER�`] - (kizcocuk:`K���`) <- [r17:`E��`] - (damat:`K���`) - [r18:`YERLE��M_YER�`] -> (adres3) <- [r19:`YERLE��M_YER�`] - (torun3:`K���`) - [r20:BABASI] -> (damat), (torun3) - [r21:`ANNES�`] -> (kizcocuk) - [r22:`ANNES�`] -> (nine) <- [r23:`ANNES�`] - (erkekcocuk) - [r24:BABASI] -> (dede) <- [r25:BABASI] - (kizcocuk) WHERE NOT (adres1) IN [adres2,adres3] AND NOT (adres2) IN [adres3] AND NOT (nine) IN [dede,erkekcocuk,gelin,kizcocuk,damat,torun1,torun2,torun3] AND NOT (dede) IN [erkekcocuk,gelin,kizcocuk,damat,torun1,torun2,torun3] AND NOT (erkekcocuk) IN [gelin,kizcocuk,damat,torun1,torun2,torun3] AND NOT (gelin) IN [kizcocuk,damat,torun1,torun2,torun3] AND NOT (kizcocuk) IN [damat,torun1,torun2,torun3] AND NOT (damat) IN [torun1,torun2,torun3] AND NOT (torun1) IN [torun2,torun3] AND NOT (torun2) IN [torun3] RETURN *";
//		queries[9] = "MATCH (person1:`K���`) - [r1:BABASI] -> (person2:`K���`) - [r2:BABASI] -> (person3:`K���`) - [r3:BABASI] -> (person4:`K���`) - [r4:BABASI] -> (person5:`K���`) - [r5:BABASI] -> (person6:`K���`) - [r6:BABASI] -> (person7:`K���`) - [r7:BABASI] -> (person8:`K���`) WHERE NOT (person1) IN [person2,person3,person4,person5,person6,person7,person8] AND NOT (person2) IN [person3,person4,person5,person6,person7,person8] AND NOT (person3) IN [person4,person5,person6,person7,person8] AND NOT (person4) IN [person5,person6,person7,person8] AND NOT (person5) IN [person6,person7,person8] AND NOT (person6) IN [person7,person8] AND NOT (person7) IN [person8] RETURN *";
//		queries[10] = "MATCH (baba:`K���`) <- [r1:BABASI] - (ikiz1:`K���`) - [r2:`ANNES�`] -> (anne:`K���`) <- [r3:`ANNES�`] - (ikiz2:`K���`) - [r4:BABASI] -> (baba) - [r5:`YERLE��M_YER�`] -> (adres3:`BA�IMSIZ_B�L�M`) <- [r6:`YERLE��M_YER�`] - (anne) <- [r7:`E��`] - (baba), (ikiz1) - [r8:`DO�UM_TAR�H�`] -> (gun:`G�N`) <- [r9:`DO�UM_TAR�H�`] - (ikiz2) - [r10:`YERLE��M_YER�`] -> (adres2:`BA�IMSIZ_B�L�M`) - [r11:`B�NASI`] -> (bina1:`B�NA`) <- [r12:`B�NASI`] - (adres1:`BA�IMSIZ_B�L�M`) <- [r13:`YERLE��M_YER�`] - (ikiz1), (adres3) - [r14:`B�NASI`] -> (bina2:`B�NA`) WHERE NOT (baba) IN [anne,ikiz1,ikiz2] AND NOT (anne) IN [ikiz1, ikiz2] AND NOT (ikiz1) IN [ikiz2] AND NOT (adres1) IN [adres2,adres3] AND NOT (adres2) IN [adres3] AND NOT (bina1) IN [bina2] RETURN *";
		
		// BANKA VER�TABANI //
//		queries[1] = "MATCH (musteri:Musteri) - [r1:`ADRES�`] -> (adres:Adres) - [r2:`A_�L�ES�`] -> (ilce:Ilce) <- [r3:`�_�L�ES�`] - (sube:Sube) <- [r4:`H_�UBES�`] - (hesap:Hesap) <- [r5:HESABI] - (musteri) RETURN *";
//		queries[2] = "MATCH (musteri:Musteri) - [r1:HESABI] -> (hesap1:Hesap) - [r2:`H_�UBES�`] -> (sube1:Sube) <- [r3:`H_�UBES�`] - (hesap2:Hesap) <- [r4:HESABI] - (musteri) - [r5:HESABI] -> (hesap3:Hesap) - [r6:`H_�UBES�`] -> (sube2:Sube) WHERE NOT (hesap1) IN [hesap2,hesap3] AND NOT (hesap2) IN [hesap3] AND NOT (sube1) IN  [sube2] RETURN *";
//		queries[3] = "MATCH (musteri:Musteri) - [r1:HESABI] -> (hesap:Hesap) - [r2:`H_�UBES�`] -> (sube:Sube) <- [r3:`K_�UBES�`] - (kredi:Kredi) <- [r4:`KRED�S�`] - (musteri) RETURN *";
//		queries[4] = "MATCH (musteri1:Musteri) - [r1:HESABI] -> (hesap1:Hesap) - [r2:`H_�UBES�`] -> (sube1:Sube) <- [r3:`K_�UBES�`] - (kredi2:Kredi) <- [r4:`KRED�S�`] - (musteri2:Musteri) - [r5:HESABI] -> (hesap2:Hesap) - [r6:`H_�UBES�`] -> (sube2:Sube) <- [r7:`K_�UBES�`] - (kredi3:Kredi) <- [r8:`KRED�S�`] - (musteri3:Musteri) - [r9:HESABI] -> (hesap3:Hesap) - [r10:`H_�UBES�`] -> (sube3:Sube) <- [r11:`K_�UBES�`] - (kredi1:Kredi) <- [r12:`KRED�S�`] - (musteri1) WHERE NOT (musteri1) IN [musteri2,musteri3] AND NOT (musteri2) IN [musteri3] AND NOT (hesap1) IN [hesap2,hesap3] AND NOT (hesap2) IN [hesap3] AND NOT (kredi1) IN [kredi2,kredi3] AND NOT (kredi2) IN [kredi3] AND NOT (sube1) IN [sube2,sube3] AND NOT (sube2) IN [sube3] RETURN *";
//		queries[5] = "MATCH (kart:KrediKarti) <- [r1:`KRED�_KARTI`] - (musteri1:Musteri) - [r2:`ADRES�`] -> (adres1:Adres) - [r3:`A_�L�ES�`] -> (ilce:Ilce) <- [r4:`A_�L�ES�`] - (adres2:Adres) <- [r5:`ADRES�`] - (musteri2:Musteri) - [r6:HESABI] -> (hesap2:Hesap) - [r7:`H_�UBES�`] -> (sube:Sube) <- [r8:`H_�UBES�`] - (hesap1:Hesap) <- [r9:HESABI] - (musteri1) WHERE NOT (musteri1) IN [musteri2] AND NOT (hesap1) IN [hesap2] AND NOT (adres1) IN [adres2] RETURN *";
		
		// WORLD-CUP VER�TABANI //
		queries[1] = "MATCH (country1:Country) - [rel1:NAMED_SQUAD] -> (squad1:Squad) <- [rel2:IN_SQUAD] - (player:Player) - [rel3:IN_SQUAD] -> (squad2:Squad) <- [rel4:NAMED_SQUAD] - (country2:Country) WHERE NOT (country1) IN [country2] AND NOT (squad1) IN [squad2] RETURN *";
		queries[2] = "MATCH (player:Player) - [rel1:STARTED] -> (p1:Performance) - [rel2:IN_MATCH] -> (match:Match) <- [rel3:IN_MATCH] - (p2:Performance) <- [rel4:SUBSTITUTE] - (player:Player) WHERE NOT (p1) IN [p2] RETURN *";
		queries[3] = "MATCH (cup1:WorldCup) - [rel1:CONTAINS_MATCH] -> (match1:Match) - [rel2:PLAYED_AT_TIME] -> (Time) <- [rel3:PLAYED_AT_TIME] - (match2:Match) <- [rel4:PLAYED_IN] - (country1:Country) - [rel5:PLAYED_IN] -> (match1) <- [rel6:PLAYED_IN] - (country2:Country) - [rel7:PLAYED_IN] -> (match2) <- [rel8:CONTAINS_MATCH] - (cup2:WorldCup) WHERE NOT (match1) IN [match2] AND NOT (cup1) IN [cup2] AND NOT (country1) IN [country2] RETURN *";
		queries[4] = "MATCH (match1:Match) - [rel1:HOME_TEAM] -> (country1:Country) <- [rel2:AWAY_TEAM] - (match2:Match) - [rel3:HOME_TEAM] -> (country2:Country) <- [rel4:AWAY_TEAM] - (match1) <- [rel5:CONTAINS_MATCH] - (cup:WorldCup) - [rel6:CONTAINS_MATCH] -> (match2) <- [rel7:IN_MATCH] - (p2:Performance) -[rel8:SCORED_GOAL] -> (goal2:Goal), (match1) <- [rel9:IN_MATCH] - (p1:Performance) - [rel10:SCORED_GOAL] -> (goal1:Goal), (p1) <- [rel11:STARTED] - (player:Player) - [rel12:STARTED] -> (p2) WHERE NOT (country1) IN [country2] AND NOT (match1) IN [match2] AND NOT (p1) IN [p2] AND NOT (goal1) IN [goal2] RETURN *";
		queries[5] = "MATCH (player:Player) - [rel1:STARTED] -> (p1:Performance) - [rel2:IN_MATCH] -> (match1:Match) <- [rel3:CONTAINS_MATCH] - (cup1:WorldCup), (player) - [rel4:STARTED] -> (p2:Performance) - [rel5:IN_MATCH] -> (match2:Match) <- [rel6:CONTAINS_MATCH] - (cup2:WorldCup), (player) - [rel7:STARTED] -> (p3:Performance) - [rel8:IN_MATCH] -> (match3:Match) <- [rel9:CONTAINS_MATCH] - (cup3:WorldCup) WHERE NOT (p1) IN [p2,p3] AND NOT (p2) IN [p3] AND NOT (match1) IN [match2,match3] AND NOT (match2) IN [match3] AND NOT (cup1) IN [cup2,cup3] AND NOT (cup2) IN [cup3] RETURN *";
	}
	
	/**
	 * This function executes Cypher queries
	 */
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{query}")
	public Response executeQuery( @PathParam("query") String queryID) {
		
		List<Map<String, Object>> sonuc = new ArrayList<Map<String, Object>>();
		Long startCPUTime = (long) 0, startTime = (long) 0, timeDifference = (long) 0;
		Long totalDbHits = (long) 0;
		int cpuCount = 0;
		
		try ( Transaction tx = database.beginTx() )
		{
			String cypherQuery = queries[ Integer.parseInt(queryID) ];
			cpuCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
			startCPUTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
			startTime = System.nanoTime();
			Result result = database.execute(cypherQuery);
			while ( result.hasNext() )
				sonuc.add( result.next() );
			Long endTime = System.nanoTime();
	    	timeDifference = (endTime - startTime) / 1000000;
	    	//ExecutionPlanDescription plan = result.getExecutionPlanDescription();
	    	//totalDbHits = profileChildPlans(plan);
			//result.resultAsString();
		}
		catch (Exception e) {
			e.getMessage();
		}

    	response += "Number of matches: " + sonuc.size() + "\n";
    	response += "Total Memory Consumption: \n" + measuringTool.measureMemoryConsumption(Runtime.getRuntime()) + "\n";
    	response += "Total CPU Consumption: " + measuringTool.measureCPUConsumption(startCPUTime, startTime, cpuCount) + "%" + "\n";
    	response += "Cypher Query Execution Time: " + timeDifference + " ms" + "\n";
    	//response += "Total number of DB Hits: " + totalDbHits + "\n";
    	
		return Response
				.status(Status.OK)
				.entity(("!!!HERE THE RESULTS ARE:" + "\n" + response).getBytes(Charset.forName("UTF-8"))).build();
	}
	
	/**
	 * This function profiles the children of query execution plan, and so it counts 
	 * the total number of db hits for each child.
	 */
	
	public Long profileChildPlans(ExecutionPlanDescription plan) {
		Long totalDbHits = plan.getProfilerStatistics().getDbHits();
		response += totalDbHits + "\n"; 
		for (Iterator<ExecutionPlanDescription> planIterator = plan.getChildren().iterator(); planIterator.hasNext(); ) {
			response += "mmmm\n";
			ExecutionPlanDescription childPlan = planIterator.next();
			totalDbHits += profileChildPlans(childPlan);
    	}
		return totalDbHits;
	}
	
	
}
