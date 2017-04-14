package com.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZloiY on 05.04.17.
 */
@Path("/patterndao")
public class PatternDAO extends Application {
    private Logger logger;
    private DataSource mySQLDataSource;
    private Connection connection;
    private Gson gson;
    private Type myType;

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
        logger.log(Level.INFO, "Connect to database");
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPatternByName(@QueryParam("pattern")String pattern){
        String json = "{"+pattern+"}";
        PatternModel patternModel = gson.fromJson(json,PatternModel.class);
        logger.log(Level.INFO, "GET request from client "+patternModel.toString());
        try{
            Statement statement = connection.createStatement();
            SQLSearchRequestConfigurator sqlSearchRequestConfigurator = new SQLSearchRequestConfigurator(patternModel);
            ResultSet resultSet = statement.executeQuery(sqlSearchRequestConfigurator.getSearchRequest());
            List<PatternModel> allPatterns = createLists(resultSet);
            statement.close();
            myType = new TypeToken<List<PatternModel>>() {}.getType();
            connection.close();
            return  gson.toJson(allPatterns, myType);
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get pattern by name");
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
        logger.log(Level.INFO, "PUT request from client "+newPattern.toString());
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
            connection.close();
            return ResponseCreator.success(getHeaderVersion(), newPattern.toString());
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot update pattern");
            e.printStackTrace();
            return ResponseCreator.error(500, 500, getHeaderVersion());
        }
    }

    @GET
    @Path("/pattern/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPatternById(@PathParam("id") String patternId){
        logger.log(Level.INFO,"GET request from client /pattern/"+patternId);
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
                connection.close();
                return gson.toJson(patternModel);
            }
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get pattern by id");
            e.printStackTrace();
        }
        return null;
    }

    @DELETE
    @Path("/{id}")
    public Response deletePattern(@PathParam("id") String id){
        logger.log(Level.INFO, "DELETE request from client id "+id);
        try{
            Statement statement = connection.createStatement();
            statement.execute("delete from patterns where pattern_id ="+id);
            statement.close();
            connection.close();
            return ResponseCreator.success(getHeaderVersion(),id);
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot delete pattern");
            e.printStackTrace();
            return ResponseCreator.error(500, 500, getHeaderVersion());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPattern(String jsonStr){
        PatternModel patternModel = gson.fromJson(jsonStr, PatternModel.class);
        logger.log(Level.INFO,"POST request from client "+patternModel.toString());
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
            connection.close();
            return ResponseCreator.success(getHeaderVersion(),jsonStr);
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot apply pattern" + e.getMessage());
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
            logger.log(Level.INFO,"GET pattern " + pattern.getName());
        }
        resultSet.close();
        return returnList;
    }

    private String getHeaderVersion(){
        return "some header";
    }
}