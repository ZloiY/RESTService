package com.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Главный класс сервиса отвечающий на запросы клиента.
 * Поддерживает запросы POST,GET,PUT,DELETE.
 */
@Path("/patterns")
public class PatternDAO extends Application {
    /**
     * Логгер
     */
    private Logger logger;
    /**
     * Используемая база данных
     */
    private DataSource mySQLDataSource;
    /**
     * Соединение с базой данных
     */
    private Connection connection;
    /**
     * Служит для преобразования объекта в JSON и обратно
     */
    private Gson gson;
    /**
     * Токен служащий для преобразования списков в JSON
     */
    private Type myType;

    /**
     * Подключаем логер, полустанавливаем соединение с базой данных.
     */
    public PatternDAO() {
        logger = LogManager.getLogger("MyLogger");
        gson = new Gson();
        try {
            mySQLDataSource = (DataSource) new InitialContext().lookup("jdbc/MySQLDataSource");
        } catch (NamingException e) {
            logger.log(Level.ERROR, "Cannot get DataSource");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
        }
        try {
            connection = mySQLDataSource.getConnection("user", "user");
        }catch(SQLException e){
            logger.log(Level.ERROR, "Cannot connect to DB");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
        }
        logger.log(Level.INFO, "Connect to database");
    }

    /**
     * Выполняется на получение запроса GET от клиента.
     * Возвращает список паттернов согласно полученному имени.
     * @param name Имя по которому ищется паттерн
     * @return Возвращает все совподения согласно модели поиска в формате JSON
     */
    @GET
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPattern(@PathParam("name")String name){
        PatternModel patternModel = new PatternModel();
        patternModel.setName(name);
        logger.log(Level.INFO, "GET request from client name: "+name);
        try{
            Statement statement = connection.createStatement();
            SQLSearchRequestConfigurator sqlSearchRequestConfigurator = new SQLSearchRequestConfigurator(patternModel);
            ResultSet resultSet = statement.executeQuery(sqlSearchRequestConfigurator.getSearchRequest());
            List<PatternModel> allPatterns = createLists(resultSet);
            statement.close();
            myType = new TypeToken<List<PatternModel>>() {}.getType();
            connection.close();
            return  Response.status(200).entity(gson.toJson(allPatterns, myType)).type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get pattern by name");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
        return null;
    }

    /**
     * Выполняется наполучение запроса GET от клиента.
     * Возвращает список патернов согласно полученным имени и группе.
     * @param group группа по которой идйт поиск
     * @param name имя по которому идёт поиск
     * @return возвращает все совпадения согласно модели поиска
     */
    @GET
    @Path("/group/{group}/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatternGroupName(@PathParam("group")int group, @PathParam("name")String name){
        PatternModel patternModel = new PatternModel();
        patternModel.setName(name);
        patternModel.setGroup(group);
        logger.log(Level.INFO, "GET request from client name: "+name+", client group: "+group);
        try{
            Statement statement = connection.createStatement();
            SQLSearchRequestConfigurator sqlSearchRequestConfigurator = new SQLSearchRequestConfigurator(patternModel);
            ResultSet resultSet = statement.executeQuery(sqlSearchRequestConfigurator.getSearchRequest());
            List<PatternModel> allPatterns = createLists(resultSet);
            statement.close();
            myType = new TypeToken<List<PatternModel>>() {}.getType();
            connection.close();
            return  Response.status(200).entity(gson.toJson(allPatterns, myType)).type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get pattern by name and group");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
        return null;
    }

    /**
     * Выполняется на получение запроса GET от клиента.
     * Возвращает список паттернов согласно полученному имени.
     * @param group группа по которой идёт поиск паттернов
     * @return возвращает вск совпадения согласно модели поиска в формате JSON
     */
    @GET
    @Path("/group/{group}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatternGroup(@PathParam("group")int group){
        PatternModel patternModel = new PatternModel();
        patternModel.setGroup(group);
        logger.log(Level.INFO, "GET request from client group: "+group);
        try{
            throw new SQLException();
//            Statement statement = connection.createStatement();
//            SQLSearchRequestConfigurator sqlSearchRequestConfigurator = new SQLSearchRequestConfigurator(patternModel);
//            ResultSet resultSet = statement.executeQuery(sqlSearchRequestConfigurator.getSearchRequest());
//            List<PatternModel> allPatterns = createLists(resultSet);
//            statement.close();
//            myType = new TypeToken<List<PatternModel>>() {}.getType();
//            connection.close();
//            return  Response.status(200).entity(gson.toJson(allPatterns, myType)).type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get pattern by group");
            logger.log(Level.ERROR,  Arrays.toString(e.getStackTrace()));
            return Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

    /**
     * Выполняется при получении запроса PUT от клиента.
     * Заменяет текущий паттерн на новый.
     * @param jsonStr список из двух паттернов, 1 - старый паттерн, 2- новый паттерн
     * @return Возвращает код 200 если операция выполнена и 500 если операция не выполнена
     */
    @PUT
    @Path("pattern/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePattern(@PathParam("id") int replaceID, String jsonStr){
        PatternModel newPattern =  gson.fromJson(jsonStr, PatternModel.class);
        newPattern.setId(replaceID);
        PatternModel oldPattern = new PatternModel();
        oldPattern.setId(replaceID);
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
            return Response.status(200).entity(newPattern.toString() + " Successfully updated").type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot update pattern");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            return Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

    /**
     * Возвращает паттерн по его id
     * @param patternId id паттерна
     * @return модель паттерна согласно его id
     */
    @GET
    @Path("pattern/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatternById(@PathParam("id") String patternId){
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
                return Response.status(200).entity(gson.toJson(patternModel)).type(MediaType.TEXT_PLAIN_TYPE).build();
            }
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get pattern by id");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            return Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
        return Response.status(205).build();
    }

    /**
     * Выполняется при получении запроса DELETE от клиента.
     * Удаляет паттерн из базы данных.
     * @param id id паттерна в базе данных
     * @return код 200 если операция проведена успешно, 500 если произошла ошибка
     */
    @DELETE
    @Path("pattern/{id}")
    public Response deletePattern(@PathParam("id") String id){
        logger.log(Level.INFO, "DELETE request from client id "+id);
        try{
            Statement statement = connection.createStatement();
            statement.execute("delete from patterns where pattern_id ="+id);
            statement.close();
            connection.close();
            return Response.status(200).entity(id + " this pattern deleted").type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot delete pattern");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            return Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

    /**
     * Выполняется при получении запроса POST от клиента.
     * Добавляет новый паттерн в базу данных.
     * @param jsonStr паттерн для добавления
     * @return код 200 еслі операція проведена успешно, 500 если произошла ошибка.
     */
    @POST
    @Path("/pattern")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addPattern(String jsonStr){
        PatternModel patternModel = gson.fromJson(jsonStr, PatternModel.class);
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
            patternModel.setId(lastId());
            connection.close();
            logger.log(Level.INFO,"POST request from client "+patternModel.toString());
            return Response.status(200).entity(patternModel.toString()+" added").type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot apply pattern");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            return Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }
    /**
     * Используется в getPattern() для формирования списка паттернов из найденных данных в базе данных.
     * @param resultSet данные из базы данных
     * @return список найденных паттернов
     * @throws SQLException выбрасывается при сбоях вработе с базой данных
     */
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

    private int lastId(){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT pattern_id FROM patterns ORDER BY pattern_id DESC LIMIT 1");
            if (resultSet.next()) {
                return resultSet.getInt("pattern_id");
            }else{
                return 0;
            }
        }catch (SQLException e){
            logger.log(Level.ERROR, "Cannot get last id");
            logger.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            return 0;
        }
    }
}