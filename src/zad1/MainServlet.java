package zad1;

import java.io.IOException;
import java.util.*;
import java.sql.*;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	public String protocol = "jdbc:derby:";
	public Connection con;
	
	public void init() throws ServletException {
	  try {
		  Class.forName(driver).newInstance();
		  con = DriverManager.getConnection(protocol + "ksidb;create=false");
	  } catch (Exception exc) {
        throw new ServletException(exc.getStackTrace().toString(), exc);
	  }
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		out.append("{");
		try{
			String query_string = "select * from POZYCJE, AUTOR, WYDAWCA where POZYCJE.AUTID = AUTOR.AUTID and WYDAWCA.WYDID = POZYCJE.WYDID";
			Iterator it = request.getParameterMap().entrySet().iterator();
			String int_value, string_value, double_value;
			while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        string_value = ((String[])pair.getValue())[0].toString().toLowerCase();
		        switch (pair.getKey().toString()){
	        		case "isbn":
	        			// UWAGA! TEN KOD JEST PODADTNY NA SQL INJECTION
	        			// ATTENTION! THIS CODE IS VULNERABLE FOR SQLINJECTION
	        			query_string = query_string + " and lower(POZYCJE.ISBN) LIKE '%"+string_value.toLowerCase()+"%'";
	        			break;
		        	case "tytul":
		        		query_string = query_string + " and POZYCJE.TYTUL LIKE '%"+string_value+"%'";
		        		break;
		        	case "rok":
		        		int_value = Integer.parseInt(string_value) + "";
		        		query_string = query_string + " and POZYCJE.ROK = " + int_value;
		        		break;
		        	case "cena":
		        		double_value = Double.parseDouble(string_value) + "";
		        		query_string = query_string + " and POZYCJE.CENA = "+double_value;
		        		break;
		        	case "autor_id":
		        		int_value = Integer.parseInt(string_value) + "";
		        		query_string = query_string + " and POZYCJE.AUTID = " + int_value;
		        		break;
		        	case "autor_name":
		        		query_string = query_string + " and lower(AUTOR.NAME) LIKE '%"+string_value.toLowerCase()+"%'";
		        		break;
		        	case "wydawnictwo_id":
		        		int_value = Integer.parseInt(string_value) + "";
		        		query_string = query_string + " and POZYCJE.WYDID = " + int_value;
		        		break;
		        	case "wydawnictwo_name":
		        		query_string = query_string + " and lower(WYDAWCA.NAME) LIKE '%"+string_value.toLowerCase()+"%'";
		        		break;
		        }
		        it.remove();
		    }
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = stmt.executeQuery(query_string);
			
//ISBN         |AUTID      |TYTUL                                               |WYDID      |ROK        |CENA           |AUTID      |NAME              |WYDID      |NAME
//83-7197-252-0|1          |3D Studio MAX 3. Doskona│oťŠ i precyzja-suplement   |1          |2001       |41.0           |1          |Miller P. (red.)  
			int rowcount = 0;
			if (rs.last()) {
			  rowcount = rs.getRow();
			  rs.beforeFirst();
			}
			out.append("\"results\": [");
			while (rs.next()) {
				out.append("{");
				out.append(String.format("\"isbn\": \"%s\",", rs.getString(1)));
				out.append(String.format("\"tytul\": \"%s\",", rs.getString(3)));
				out.append(String.format("\"rok\": \"%s\",", rs.getString(5)));
				out.append(String.format("\"cena\": \"%s\",", rs.getString(6)));
				
				out.append(String.format("\"autor_id\": \"%s\",", rs.getString(2)));
				out.append(String.format("\"autor_name\": \"%s\",", rs.getString(8)));
				
				out.append(String.format("\"wydawnictwo_id\": \"%s\",", rs.getString(4)));
				out.append(String.format("\"wydawnictwo_name\": \"%s\"", rs.getString(10)));
				
				
				out.append("}");
				if( rowcount != rs.getRow() ){
					out.append(",");
				}
		    }
		    rs.close();
		    stmt.close();
		    out.append("]");
		}catch(Exception ex){
			out.append(String.format("\"error\": \"%s\"", ex.toString().replace('"', '\'')));
			ex.printStackTrace();
		}finally{
			out.append("}");	
		}
		
	}

}