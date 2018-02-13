package de.htw_berlin.f4.uranus.akma.services;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.bbn.parliament.jena.joseki.client.RemoteModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Elias Kechter s0555064
 *
 */
@Path("/ParTI")
public class ParTIService {

	private static final String TURTLE = "TURTLE";

	private static final String PARLIAMENT_BULK_HTTP_PORT = "http://localhost:8080/parliament/bulk";
	private static final String PARLIAMENT_SPARQL_HTTP_PORT = "http://localhost:8080/parliament/sparql";

	private static final String MISSING_SPARQL_SELECT_QUERY = "Missing SPARQL Select-Query";
	private static final String BAD_SPARQL_SELECT_QUERY = "Bad SPARQL Select-Query";

	private static final String MISSING_INSERT_STATEMENTS = "Missing Insert Statements";
	private static final String BAD_INSERT_STATEMENTS = "Bad Insert Statements";

	private static final String MISSING_SPARQL_UPDATE_QUERY = "Missing SPARQL Update-Query";
	private static final String BAD_SPAQRL_UPDATE_QUERY = "Bad SPAQRL Update-Query";

	private static final String MISSING_DELETE_STATEMENTS = "Missing Delete Statements";
	private static final String BAD_DELETE_STATEMENTS = "Bad Delete Statements";

	/**
	 * @param selectQuery
	 * @return
	 */
	@POST
	@Path("/select")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response select(String selectQuery) {

		if (selectQuery == null)
			return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_SPARQL_SELECT_QUERY).build();

		RemoteModel rmParliament = new RemoteModel(PARLIAMENT_SPARQL_HTTP_PORT, PARLIAMENT_BULK_HTTP_PORT);

		try {
			String message = getSelectMessageHTML(rmParliament, selectQuery);
			return Response.status(Response.Status.OK).entity(message).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(BAD_SPARQL_SELECT_QUERY).build();
		}
	}

	/**
	 * @param insertStatements
	 * @return
	 */
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response insert(String insertStatements) {

		if (insertStatements == null)
			return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_INSERT_STATEMENTS).build();

		RemoteModel rmParliament = new RemoteModel(PARLIAMENT_SPARQL_HTTP_PORT, PARLIAMENT_BULK_HTTP_PORT);
		try {
			rmParliament.insertStatements(insertStatements, TURTLE, null, false);
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(BAD_INSERT_STATEMENTS).build();
		}
	}

	/**
	 * @param updateQuery
	 * @return
	 */
	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response update(String updateQuery) {

		if (updateQuery == null)
			return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_SPARQL_UPDATE_QUERY).build();

		RemoteModel rmParliament = new RemoteModel(PARLIAMENT_SPARQL_HTTP_PORT, PARLIAMENT_BULK_HTTP_PORT);
		try {
			rmParliament.updateQuery(updateQuery);
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(BAD_SPAQRL_UPDATE_QUERY).build();
		}
	}

	/**
	 * @param deleteStatements
	 * @return
	 */
	@DELETE
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response delete(String deleteStatements) {

		if (deleteStatements == null)
			return Response.status(Response.Status.BAD_REQUEST).entity(MISSING_DELETE_STATEMENTS).build();

		RemoteModel rmParliament = new RemoteModel(PARLIAMENT_SPARQL_HTTP_PORT, PARLIAMENT_BULK_HTTP_PORT);
		try {
			rmParliament.deleteStatements(deleteStatements, TURTLE);
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(BAD_DELETE_STATEMENTS).build();
		}
	}
	
	
	/**
	 * @param rmParliament
	 * @param selectQuery
	 * @return
	 * @throws IOException
	 */
	String getSelectMessageHTML(RemoteModel rmParliament, String selectQuery) throws IOException {
		ResultSet rs = rmParliament.selectQuery(selectQuery);
		List<String> resultVars = rs.getResultVars();
		System.out.println(resultVars);
		int rvLength = resultVars.size();
		String message = "<!DOCTYPE html>\n<html>\n<head>\n<!-- HTML Codes by Quackit.com -->\n<title>\n"
				+ "Songs</title>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
				+ "<style>\ntable.GeneratedTable {\nwidth: 100%;\nbackground-color: #ffffff;\n"
				+ "border-collapse: collapse;\nborder-width: 2px;\nborder-color: #ffcc00;\n"
				+ "border-style: solid;\ncolor: #000000;\n}\n\n"
				+ "table.GeneratedTable td, table.GeneratedTable th {\nborder-width: 2px;\n"
				+ "border-color: #ffcc00;\nborder-style: solid;\npadding: 3px;\n}\n\n"
				+ "table.GeneratedTable thead {\nbackground-color: #ffcc00;\n" + "}\n</style>\n"
				+ "</head>\n<body>\n<table class=\"GeneratedTable\">\n<thead>\n<tr>\n"
				+ (rvLength >= 3? String.format("<th>%s</th>\n<th>%s</th>\n<th>%s</th>\n", 
						resultVars.get(0),
						resultVars.get(1), 
						resultVars.get(2))
				:  rvLength >= 2? String.format("<th>%s</th>\n<th>%s</th>\n", 
						resultVars.get(0),
						resultVars.get(1))
				:  rvLength >= 1? String.format("<th>%s</th>\n",
						resultVars.get(0)): "")
				+ "</tr>\n</thead>\n<tbody>";

		while (rs.hasNext()) {
			QuerySolution temp = rs.next();
			if (rvLength >= 3) {
				message += String.format("<tr>\n<td>%s</td>\n<td>%s</td>\n<td>%s</td>\n</tr>", 
						temp.get(resultVars.get(0)), 
						temp.get(resultVars.get(1)), 
						temp.get(resultVars.get(2)));
			} else if (rvLength >= 2) {
				message += String.format("<tr>\n<td>%s</td>\n<td>%s</td>\n</tr>", 
						temp.get(resultVars.get(0)), 
						temp.get(resultVars.get(1)));
			} else if (rvLength >= 1) {
				message += String.format("<tr>\n<td>%s</td>\n</tr>", 
						temp.get(resultVars.get(0)));
			}
		}
		message += "</tbody>\n</table>\n</body>\n</html>";
		return message;
	}
}
