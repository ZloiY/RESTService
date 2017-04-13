package com.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by ZloiY on 05.04.17.
 */
@Path("/patterndao")
public class PatternDAO extends Application {
    //private Logger logger;
    private DataSource mySQLDataSource;
    private Connection connection;
    private Gson gson;
    private Type myType;
    @Context
    private HttpHeaders requsetHeaders;

    public PatternDAO() {
        //logger = LogManager.getLogger("MyLogger");
        Logger logger =Logger.getLogger("myApp");
//        Properties preferences = new Properties();
//        try {
//            FileInputStream configFile = new FileInputStream("src/main/logger.properties");
//            preferences.load(configFile);
//            LogManager.getLogManager().readConfiguration(configFile);
//        } catch (IOException ex)
//        {
//            System.out.println("WARNING: Could not open configuration file");
//            System.out.println("WARNING: Logging not configured (console output only)");
//        }
        ResourceConfig config = new ResourceConfig(PatternDAO.class);
        config.register(new LoggingFeature(logger, LoggingFeature.Verbosity.PAYLOAD_ANY));
        gson = new Gson();
        try {
            mySQLDataSource = (DataSource) new InitialContext().lookup("jdbc/MySQLDataSource");
        } catch (NamingException e) {
//            logger.log(Level.ERROR, "Cannot get DataSource");
            e.printStackTrace();
        }
        try {
            connection = mySQLDataSource.getConnection("user", "user");
        }catch(SQLException e){
//            logger.log(Level.ERROR, "Cannot connect to DB");
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
            List<PatternModel> allpatterns = createLists(resultSet);
            statement.close();
            myType = new TypeToken<List<PatternModel>>() {}.getType();
            return gson.toJson(allpatterns, myType);
        }catch (SQLException e){
//            logger.log(Level.ERROR, "Cannot get all patterns");
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPatternByName(@PathParam("name")String patternName){
        PatternModel patternModel = new PatternModel();
        patternModel.setName(patternName);
        try{
            Statement statement = connection.createStatement();
            SQLSearchRequestConfigurator sqlSearchRequestConfigurator = new SQLSearchRequestConfigurator(patternModel);
            ResultSet resultSet = statement.executeQuery(sqlSearchRequestConfigurator.getSearchRequest());
            List<PatternModel> allPatterns = createLists(resultSet);
            statement.close();
            myType = new TypeToken<List<PatternModel>>() {}.getType();
            return  gson.toJson(allPatterns, myType);
        }catch (SQLException e){
//            logger.log(Level.ERROR, "Cannot get pattern by name");
            e.printStackTrace();
        }
        return null;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePattern(String jsonStr){
        myType = new TypeToken<List<PatternModel>>() {}.getType();
        List<PatternModel> updateList = gson.fromJson(jsonStr, myType);
        PatternModel oldPattern = updateList.get(0);
        PatternModel newPattern = updateList.get(1);
        try{
            if (newPattern.getImage() != null) {
                PreparedStatement statement = connection.prepareStatement("update patterns set pattern_name=?,pattern_description=?,pattern_name=?,pattern_schema=?,pattern_group=? where pattern_id=?");
                statement.setInt(1, newPattern.getId());
                statement.setString(2, newPattern.getDescription());
                statement.setString(3, newPattern.getName());
                statement.setBytes(4, newPattern.getImage());
                statement.setInt(5,newPattern.getGroup());
                statement.setInt(6, oldPattern.getId());
                statement.execute();
            }else{
                PreparedStatement statement = connection.prepareStatement("update patterns set pattern_name=?,pattern_description=?,pattern_name=?,pattern_group=? where pattern_id=?");
                statement.setInt(1, newPattern.getId());
                statement.setString(2, newPattern.getDescription());
                statement.setString(3, newPattern.getName());
                statement.setInt(4,newPattern.getGroup());
                statement.setInt(5, oldPattern.getId());
                statement.execute();
            }
            return ResponseCreator.success(getHeaderVersion(), newPattern.toString());
        }catch (SQLException e){
//            logger.log(Level.ERROR, "Cannot update pattern");
            e.printStackTrace();
            return ResponseCreator.error(500, 500, getHeaderVersion());
        }
    }

    @GET
    @Path("/pattern/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPatternById(@PathParam("id") String patternId){
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select pattern_id, pattern_name, pattern_description, pattern_schema, pattern_group from patterns where pattern_id ="+patternId);
            if (resultSet.next()){
                PatternModel patternModel = new PatternModel();
                patternModel.setId(resultSet.getInt(1));
                patternModel.setName(resultSet.getString(2));
                patternModel.setDescription(resultSet.getString(3));
                if (resultSet.getBlob(4) != null) {
                    Blob blob = resultSet.getBlob(4);
                    patternModel.setImage(blob.getBytes(1,(int)blob.length()));
                }
                patternModel.setGroup(resultSet.getInt(5));
                return gson.toJson(patternModel);
            }
        }catch (SQLException e){
//            logger.log(Level.ERROR, "Cannot get pattern by id");
            e.printStackTrace();
        }
        return null;
    }

    @DELETE
    @Path("/{id}")
    public Response deletePattern(@PathParam("id") String id){
        //logger.log(Level.INFO, "Delete request from client");
        //logger.log(Level.INFO, "Pattern id ="+id);
        System.out.println(id);
        try{
            Statement statement = connection.createStatement();
            statement.execute("delete from patterns where pattern_id ="+id);
            statement.close();
            return ResponseCreator.success(getHeaderVersion(),id);
        }catch (SQLException e){
//            logger.log(Level.ERROR, "Cannot delete pattern");
            e.printStackTrace();
            return ResponseCreator.error(500, 500, getHeaderVersion());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPattern(String jsonStr){
        System.out.println(jsonStr);
        PatternModel patternModel = gson.fromJson(jsonStr, PatternModel.class);
        System.out.println(patternModel.toString());
        try{
            Statement statement = connection.createStatement();
            if (patternModel.getImage()!=null) {
                byte[] schemaBytes = patternModel.getImage();
                PreparedStatement preparedStatement = connection.prepareStatement("insert into patterns(pattern_description, pattern_name, pattern_schema, pattern_group) values(?,?,?,?)");
                preparedStatement.setString(1,patternModel.getDescription());
                preparedStatement.setString(2,patternModel.getName());
                preparedStatement.setBytes(3,schemaBytes);
                preparedStatement.setInt(4,patternModel.getGroup());
                preparedStatement.execute();
                preparedStatement.close();
            }else
                statement.execute("insert into patterns(pattern_description, pattern_name, pattern_group) values('" + patternModel.getDescription() + "','" + patternModel.getName() + "','"+patternModel.getGroup()+"')");
            statement.close();
            return ResponseCreator.success(getHeaderVersion(),jsonStr);
        }catch (SQLException e){
//            logger.log(Level.ERROR, "Cannot apply pattern" + e.getMessage());
            e.printStackTrace();
            return ResponseCreator.error(500, 500, getHeaderVersion());
        }
    }

    private ArrayList<PatternModel> createLists(ResultSet resultSet)throws SQLException{
        ArrayList<PatternModel> returnList = new ArrayList<PatternModel>();
        while (resultSet.next()) {
            PatternModel pattern = new PatternModel();
            if (resultSet.getBinaryStream(4) != null){
                Blob blob = resultSet.getBlob(4);
                pattern.setImage(blob.getBytes(1,(int)blob.length()));
            }
            pattern.setId(resultSet.getInt(1));
            pattern.setName(resultSet.getString(2));
            pattern.setDescription(resultSet.getString(3));
            pattern.setGroup(resultSet.getInt(5));
            returnList.add(pattern);
//            logger.log(Level.INFO,"Find pattern " + pattern.getName());
        }
        resultSet.close();
        return returnList;
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