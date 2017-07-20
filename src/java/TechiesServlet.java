
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class TechiesServlet extends HttpServlet {
    public Statement stmt = null;
    public Connection con = null;
    public ResultSet rs = null;
    public URLConnection  fromGosMet = null;
    private String typeOfUser = null;
    
    

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

    }
    
    public TechiesServlet() throws ClassNotFoundException, SQLException, MalformedURLException, IOException
    {
        Class.forName("org.postgresql.Driver");
        con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pg","postgres", "474747");
        stmt = con.createStatement();

    }
    
  
    @Override
    public void init() throws ServletException {
        super.init();
    }
    
    
    protected JSONObject traslateRequest(String data, String tab) throws IOException, SQLException
    {
        JSONObject resultJson = new JSONObject();
        JSONArray ar = new JSONArray();
        JSONArray ar_name = new JSONArray();
        JSONArray ar_col = new JSONArray();
        
        
        try{  
        rs  = stmt.executeQuery(data); 
        }catch (SQLException a){
            JSONArray ar_err = new JSONArray();
            ar_err.add("Message: " + a.getMessage());
            resultJson.put("Error", ar_err);
            return resultJson;
        }
        
        
        int size_col = rs.getMetaData().getColumnCount();
        resultJson.put("size_width", size_col);
        
        
        int size_height = 0;
        while(rs.next())
        {
            JSONArray ar_inp = new JSONArray();
            for (int i=1;i<=rs.getMetaData().getColumnCount();i++)
                ar_inp.add(rs.getString(i));
            ar.add(ar_inp);
            size_height++;
        }
        resultJson.put("tables", ar);
        resultJson.put("size_height", size_height);
        System.out.println(resultJson.toString());
     
         
        BufferedReader reader = new BufferedReader(new FileReader("C:\\"+tab));
        String line;
        while((line = reader.readLine()) != null)
        {
            ar_name.add(line);
        }
        resultJson.put("Name", ar_name);
        
        line = "";
        
        System.out.println(resultJson.toString());
        reader = new BufferedReader(new FileReader("C:\\"+tab +"col"));
        while((line = reader.readLine()) != null)
        {
            ar_col.add(line);
        }
        resultJson.put("Column", ar_col);
        System.out.println(resultJson.toString());
        return resultJson;
    }
    
    
    private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }
     

    protected JSONObject getTabless(String tab, String size) throws SQLException, UnsupportedEncodingException, IOException
    {
        System.out.println(size);
        JSONObject resultJson = null;
        if (!"".equals(size))
            resultJson = traslateRequest("select * from "+ tab +" limit "+ size, tab);
        else
            resultJson = traslateRequest("select * from "+ tab, tab);
        
        if (resultJson.get("Type") == "Error")
            return resultJson;
       
        System.out.println(resultJson.toString());
        return resultJson;
       
      
    }
    
    
    protected String userLogin(String login, String pass) throws FileNotFoundException, IOException
    {
        
        System.out.println(login);
        if ("guest".equals(login))
        {
            typeOfUser = "guest";
            return typeOfUser;
        }
        else
        {
            BufferedReader reader = new BufferedReader(new FileReader("D:\\logins"));
            String line;

            while((line = reader.readLine()) != null)
            { 
                System.out.println(line);
                if(line.contains(login))
                {
                    if(line.contains(":"+pass))
                    {
                        if("root".equals(login))
                        {
                            return "root";
                        }
                        else
                            return "worker";
                    
                    }
                
                }
            }
            
        }
             
        return "error";
    }
    
    
    protected JSONObject getNameTabless() throws SQLException, ClassNotFoundException, IOException
    {

                JSONObject resultJson = new JSONObject();
                JSONArray ar = new JSONArray();
                JSONArray ar_rus = new JSONArray();
                String name_rus[] = {"Данные гм-10", "Коды работ гм-10", "цгмс", "Набл. по подгуппам", "Типы организаций", "Меджународное", "Территориал. Коды", "угмс"};
                rs = stmt.executeQuery("select tablename from pg_tables where schemaname='public'");
                int i=0;
                while(rs.next())              
                   {
                       ar.add(rs.getString(1));
                       ar_rus.add(name_rus[i]);
                       i++;
                   }
               
               resultJson.put("dataFin", ar);
               resultJson.put("dataFinRus", ar_rus);
               resultJson.put("len", i);

               return resultJson;
        
    }

    
    protected String saveTable(String TableName, Integer CountData, Integer SizeTable, String objCol_, String objData_, String objDataOld_) throws ParseException
    {
        System.out.println("Save_table");
        JSONParser parser = new JSONParser();
        Object objData = null;
        Object objCol = null;
        Object objDataOld = null;            
                
        objCol = parser.parse(objCol_);
        objData = parser.parse(objData_);
        objDataOld = parser.parse(objDataOld_);
                                              
        JSONArray jsonObjData =  (JSONArray) objData;
        JSONArray jsonObjCol =  (JSONArray) objCol;
        JSONArray jsonObjDataOld =  (JSONArray) objDataOld;
                
        System.out.println(jsonObjDataOld);
        System.out.println(jsonObjData);
                
        for (int i=0; i<CountData;i++)
        {
                    
            String sql = "update " + TableName + " set ";
            String t = "";
            JSONArray jsonObjData_come =(JSONArray) jsonObjData.get(i);
            JSONArray jsonObjDataOld_come = (JSONArray) jsonObjDataOld.get(i);
                    
            for(int j=0;j<SizeTable;j++)
            {
                if(j!=0)
                    t=", ";
                sql = sql + t + jsonObjCol.get(j) + " = '" + jsonObjData_come.get(j) + "'";
            }
            sql = sql + " where ";
            t = "";
                
            for(int j=0;j<SizeTable;j++)
            {
                if(j!=0)
                    t=" and ";
                sql = sql + t +jsonObjCol.get(j) + " = '" + jsonObjDataOld_come.get(j) + "'";
            }
                    
                    
            System.out.println(sql);
            
            try {
                stmt.execute(sql);
            } catch (SQLException ex) {      
                System.out.println("neok");
                return "Error: " + ex.getMessage();
            }
    
        }
        System.out.println("Ok");
       return "ok";              
    }
    
    
    protected void remRow(String RemData_, String ObjCol_, String Table_, String Size_) throws ParseException
    {
        System.out.println("Rem_row");
        JSONParser parser = new JSONParser();
        Object RemData = null;
        Object ObjCol = null;

        RemData = parser.parse(RemData_);
        ObjCol = parser.parse(ObjCol_);

        JSONArray jsonObjCol =  (JSONArray) ObjCol;
        JSONArray jsonObjData =  (JSONArray) RemData;

        String sql = "delete from " + Table_ + " where ";
        String t = "";
        System.out.println(sql);
        for(int i=0;i<Integer.parseInt(Size_);i++) 
        {
            if(i!=0)
                t="and ";
            sql = sql + t + jsonObjCol.get(i) + " = '" + jsonObjData.get(i) + "' ";
        }
        System.out.println(sql);
        try {
            stmt.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(TechiesServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    protected JSONArray listUsers() throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader("D:\\logins")); 
        JSONArray ar = new JSONArray();
        String line;
            while((line = reader.readLine()) != null)
            { 
                ar.add(line);
            }
        reader.close();
        return ar;
    }
    
    
    protected String addUser(String user, String pass) throws IOException
    {
        FileWriter wrt = new FileWriter(new File("D:\\logins"), true);
        CharSequence cq = user+":"+pass;
        wrt.append(cq).append('\n');
        wrt.flush();
        wrt.close();
        return "ok";
    }
    
    
    protected String delUser(String user) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader("D:\\logins")); 
        ArrayList lineLog = new ArrayList();
        String line;
        int i=0;
        while((line = reader.readLine()) != null)
        { 
            if((!line.contains(user)))
            {
                lineLog.add(line);
                i++;
            }
        }
        reader.close();
        
        FileWriter wrt = new FileWriter(new File("D:\\logins"));
        CharSequence cq;
        for (int j=0;j<i;j++)
        {
           cq = (CharSequence) lineLog.get(j);
           wrt.append(cq).append('\n');
        }
        wrt.close();
        return "ok";
    }
    
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String runned = request.getParameter("selectedItem");
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        switch(runned)
        {
              
            case "table":
            {
                
                try 
                {
                    out.print(getNameTabless());
                } 
                catch (SQLException | ClassNotFoundException ex) 
                {
                    Logger.getLogger(TechiesServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
                
                
            case "selectItem":
            {
                
                try 
                {
                    out.print(getTabless(request.getParameter("value"), request.getParameter("StringSet")));
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(TechiesServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            
                break;
            }
                
            
            case "login":
            {
                out.print(userLogin(request.getParameter("userLogin"), request.getParameter("passwdLogin")));
                break;
            }
            
            
            case "Save_table":
            {
                try {
                    out.print(saveTable(request.getParameter("table"), Integer.parseInt(request.getParameter("count_change")), Integer.parseInt(request.getParameter("size")), request.getParameter("column"),  request.getParameter("value"), request.getParameter("value_old")));              
                } catch (ParseException ex) {
                    Logger.getLogger(TechiesServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
                
            case "Rem_row":
            {
                try {
                    remRow(request.getParameter("value"), request.getParameter("column"), request.getParameter("table"), request.getParameter("size"));
                } catch (ParseException ex) {
                    Logger.getLogger(TechiesServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            case "listUers":
            {
                out.print(listUsers());
                break;
            }
            
            case "addUser":
            {
                out.print(addUser(request.getParameter("login"),request.getParameter("pass")));
                break;
            }
            
            case "delUser":
            {
                out.print(delUser(request.getParameter("login")));
                break;
            }
            
        }
    }

    
        

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        {
            
        }

    
    

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

