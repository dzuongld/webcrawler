import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import util.HTMLFilter;

public class myapp extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Request Parameters Example</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h3>Request Parameters Example</h3>");
		out.println("Parameters in this request:<br>");
		
		String firstName = request.getParameter("firstname");
		String lastName = request.getParameter("lastname");

		if (firstName != null || lastName != null) {
			out.println("First Name:");
			out.println(" = " + HTMLFilter.filter(firstName) +
						 "<br>");
			out.println("Last Name:");
			out.println(" = " + HTMLFilter.filter(lastName));
		} else {
			out.println("No Parameters, Please enter some");
		}

	    out.println("<P>");
		out.print("<form action=\"");
		out.print("myapp\" ");
		out.println("method=POST>");
		out.println("First Name:");
		out.println("<input type=text size=20 name=firstname>");
		out.println("<br>");
		out.println("Last Name:");
		out.println("<input type=text size=20 name=lastname>");
		out.println("<br>");
		out.println("<input type=submit>");
		out.println("</form>");
		out.println("</body>");
		out.println("</html>");
	}

    public void doPost(HttpServletRequest request, HttpServletResponse res) throws IOException, ServletException
	{
		doGet(request, res);
	}

}

