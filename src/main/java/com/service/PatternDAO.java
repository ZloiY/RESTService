package com.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZloiY on 05.04.17.
 */
@Path("/patterndao")
@ApplicationScoped
public class PatternDAO {
    private Logger logger;
    private DataSource mySQLDataSource;
    private Connection connection;
    private Gson gson;
    private Type myType;
    @Context
    private HttpHeaders requsetHeaders;

    public PatternDAO() {
        logger = LogManager.getLogger("MyLogger");
        gson = new Gson();
        try {
            mySQLDataSource = (DataSource) new InitialContext().lookup("jdbc/MySQLDataSource");
        } catch (NamingException e) {
            logger.log(Level.ERROR, "Cannot get DataSource");
            e.printStackTrace();
        }
        try {
            connection = mySQLDataSource.getConnection("user", "user");
        }catch(SQLException e){
            logger.log(Level.ERROR, "Cannot connect to DB");
            e.printStackTrace();
        }
    }
    @GET
    @Path("/group/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllPatterns(@PathParam("id") String groupId){
        PatternModel patternModel = new PatternModel();
        patternModel.setGroup(Integer.parseInt(groupId));
        try {
            Statement statement = connection.createStatement();
            SQLSearchRequestConfigurator sqlSearchRequestConfigurator = new SQLSearchRequestConfigurator(patternModel);
            ResultSet resultSet = statement.executeQuery(sqlSearchRequestConfigurator.getSearchRequest());
            List<PatternModel> allpatterns = new ArrayList<PatternModel>();
            allpatterns = createLists(resultSet);
            statement.close();
            myType = new TypeToken<List<PatternModel>>() {}.getType();
            return gson.toJson(allpatterns, myType);
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get all patterns");
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<PatternModel> createLists(ResultSet resultSet)throws SQLException{
        ArrayList<PatternModel> returnList = new ArrayList<PatternModel>();
        while (resultSet.next()) {
            PatternModel pattern = new PatternModel();
            if (resultSet.getBinaryStream(4) != null){
                Blob blob = resultSet.getBlob(4);
                ByteBuffer byteBuffer = ByteBuffer.wrap(blob.getBytes(1,(int)blob.length()));
                pattern.setImage(byteBuffer);
            }
            pattern.setId(resultSet.getInt(1));
            pattern.setName(resultSet.getString(2));
            pattern.setDescription(resultSet.getString(3));
            pattern.setGroup(resultSet.getInt(5));
            returnList.add(pattern);
            logger.log(Level.INFO,"Find pattern " + pattern.getName());
        }
        resultSet.close();
        return returnList;
    }

    @GET
    @Path("/pattern/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPatternById(@PathParam("id") String patternId){
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select pattern_id, pattern_name, pattern_description, pattern_schema, pattern_group from govno where pattern_id ="+patternId);
            if (resultSet.next()){
                PatternModel patternModel = new PatternModel();
                patternModel.setId(resultSet.getInt(1));
                patternModel.setName(resultSet.getString(2));
                patternModel.setDescription(resultSet.getString(3));
                patternModel.setGroup(resultSet.getInt(5));
                return gson.toJson(patternModel);
            }
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get pattern by id");
            e.printStackTrace();
        }
        return null;
    }
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void deletePattern(String jsonStr){
        logger.log(Level.INFO, "Delete request from client");
        logger.log(Level.INFO, "Pattern id =");
        PatternModel patternModel  = gson.fromJson(jsonStr, PatternModel.class);
        try{
            Statement statement = connection.createStatement();
            statement.execute("delete from govno where pattern_id ="+patternModel.getId());
            statement.close();
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot delete pattern");
            e.printStackTrace();
        }
    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPattern(String jsonStr){
        System.out.println(jsonStr);
        PatternModel patternModel = gson.fromJson(jsonStr, PatternModel.class);
        System.out.println(patternModel.toString());
        try{
                PreparedStatement statement = connection.prepareStatement("insert into govno(pattern_name, pattern_description, pattern_group) values(?,?,?)");
                statement.setString(1, patternModel.getName());
                statement.setString(2, patternModel.getDescription());
                statement.setInt(3, patternModel.getGroup());
                statement.execute();
                statement.close();
                return ResponseCreator.success(getHeaderVersion(),jsonStr);
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot apply pattern" + e.getMessage());
            e.printStackTrace();
            return ResponseCreator.error(500, 500, getHeaderVersion());
        }
    }

    private String getHeaderVersion(){
        return "some version";
    }

    private PatternModel assemblePttern(String paramsStr){
        PatternModel pattern = new PatternModel();
        if (paramsStr.contains("9")){
        String[] params = paramsStr.split("9");
        pattern.setName(params[0]);
        pattern.setDescription(params[1]);
        return pattern;}
        else{
         pattern.setGroup(Integer.parseInt(paramsStr));
         return pattern;
        }
    }
}
