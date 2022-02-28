package Mongo;


import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import config.AppfileConfig;
import config.SpringContext;
import entity.Coordinate;
import entity.Polygon;
import org.bson.Document;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import entity.BBox;
import entity.PeopleBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class MongoHandler {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> peopleBox;
    private String mongoClientURI;
    private String dbname;
    AppfileConfig appfileConfig;

    public MongoHandler(String mongoClientURI, String dbname) {
        this.dbname = dbname;
        this.mongoClientURI = mongoClientURI;
//        appfileConfig = SpringContext.context.getBean("appfileConfig",AppfileConfig.class);
        connectMongoDB();
    }

    public void connectMongoDB(){
        mongoClient = new MongoClient(new MongoClientURI(mongoClientURI));
        database = mongoClient.getDatabase(dbname);

    }

    public void detectChangeDB(){
        ChangeStreamIterable<Document> dbChange = database.watch(asList(Aggregates.match(Filters.in("operationType", asList("insert", "delete")))));
        MongoCursor<ChangeStreamDocument<Document>> cursorDB = dbChange.iterator();
        while (cursorDB.getServerCursor()!=null){
            if (cursorDB.hasNext()){
                System.out.println("cursor next "+cursorDB.tryNext());
                System.out.println("DB changed!");
            }
        }
    }

    public void detectChangePeopleBox(String collectionName){
        peopleBox = database.getCollection(collectionName);
        MongoCursor<ChangeStreamDocument<Document>> changeLines = peopleBox.watch(Arrays.asList(Aggregates.match(Filters.in("opetationType", asList("insert","delete"))))).iterator();
        while (changeLines.getServerCursor()!=null){
            if (changeLines.hasNext()){
                System.out.println("cursor next "+changeLines.tryNext());
                System.out.println("DB changed!");
            }
        }
    }

    public PeopleBox getBox(){
        peopleBox = database.getCollection(appfileConfig.collection);
        FindIterable<Document> findIterable = peopleBox.find();
        MongoCursor<Document> cursor = findIterable.iterator();
        if (cursor.hasNext()){
            Document document = cursor.next();
            String deviceId = document.get("DeviceID").toString();
            int hour = (int) (((Double) document.get("hour")).doubleValue());
            int time = (int) (((Double) document.get("time")).doubleValue());
            List<Document> bboxes = (List<Document>) document.get("points");
            List<BBox> boxes = bboxes.stream().map(convertDocumentToBbox()).collect(Collectors.toList());
            return new PeopleBox(deviceId, hour, time, boxes);
        }
        return null;
    }

    public PeopleBox getPolygon(){
        peopleBox = database.getCollection(appfileConfig.collection);
        FindIterable<Document> findIterable = peopleBox.find();
        MongoCursor<Document> cursor = findIterable.iterator();
        if (cursor.hasNext()){
            Document document = cursor.next();
            String deviceId = document.get("DeviceID").toString();
            int hour = (int) (((Double) document.get("hour")).doubleValue());
            int time = (int) (((Double) document.get("time")).doubleValue());
            List<Document> bboxes = (List<Document>) document.get("points");
            List<Polygon> polygons = bboxes.stream().map(convertDocumentToPolygon()).collect(Collectors.toList());
            return new PeopleBox(deviceId, hour, time, polygons);
        }
        return null;
    }

    public  void addPeople(PeopleBox peopleBox){
        Document document = new Document();
        document.append("DeviceID", peopleBox.getDeviceID());
        document.append("hour", peopleBox.getHour());
        document.append("time", peopleBox.getTime());
        document.append("points", peopleBox.getbBoxes().stream().map(convertToJson()).map(json -> Document.parse(json)).collect(Collectors.toList()));
//        MongoCollection<Document> collection = database.getCollection(appfileConfig.collection);
        MongoCollection<Document> collection = database.getCollection("test");

        collection.insertOne(document);
    }

    public void ResetAllLine(){
        peopleBox = database.getCollection(appfileConfig.collection);
        peopleBox.deleteMany(new BasicDBObject());
    }

    public void DeleteLine(String cloudId){
        peopleBox = database.getCollection(appfileConfig.collection);
        peopleBox.deleteOne(Filters.eq("ID", cloudId));
    }

    private <T> Function<T, String> convertToJson(){
        ObjectMapper mapper = new ObjectMapper();
        return object -> {
            try{
                return mapper.writeValueAsString(object);
            }catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        };
    }

    private Function<Document, BBox> convertDocumentToBbox(){
        return document -> {
            if (!document.containsKey("w")){
                return null;
            }
            int x1 = (int) document.get("x1");
            int y1 = (int) document.get("y1");
            int w = (int) document.get("w");
            int h = (int) document.get("h");
            double score = (double) document.get("score");
            return new BBox(x1, y1, w, h, score);
        };

    }

    private Function<Document, Polygon> convertDocumentToPolygon(){
        return document -> {
            if (!document.containsKey("level")){
                return null;
            }
            int level = (int) document.get("level");
            List<Coordinate> coordinates = (List<Coordinate>) document.get("coords");
            double score = (double) document.get("score");
            return new Polygon( coordinates,level, score);
        };

    }


}
