package Mongo;


import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import config.AppfileConfig;
import config.SpringContext;
import entity.BBox;
import entity.Coordinate;
import entity.PeopleBox;
import entity.Polygon;
import org.bson.Document;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

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
    private String user;
    private String pass;
    private String dbname;
    AppfileConfig appfileConfig;

    public MongoHandler(String user, String pass, String dbname) {
        this.dbname = dbname;
        this.user = user;
        this.pass = pass;
        appfileConfig = SpringContext.context.getBean("appfileConfig",AppfileConfig.class);
        connectMongoDB();
    }

    public void connectMongoDB(){
        MongoCredential credential = MongoCredential.createScramSha1Credential(user, "admin", pass.toCharArray());
        mongoClient = new MongoClient(new ServerAddress(appfileConfig.mongoAddress, appfileConfig.mongoPort), Arrays.asList(credential));

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

//    public List<PeopleBox> getBoxes(){
//        MongoCollection collection = database.getCollection(appfileConfig.collection);
//        List<PeopleBox> peopleBoxes = new ArrayList<>();
//        FindIterable<Document> findIterable = collection.find();
//        MongoCursor<Document> cursor = findIterable.iterator();
//        while (cursor.hasNext()){
//            Document document = cursor.next();
//            String deviceId = document.get("DeviceID").toString();
//            int hour =  Integer.parseInt(document.get("hour").toString());
//            int min = Integer.parseInt(document.get("min").toString());
//            int sec = Integer.parseInt(document.get("sec").toString());
//            List<Document> bboxes = (List<Document>) document.get("points");
//            List<BBox> boxes = bboxes.stream().map(convertDocumentToBbox()).collect(Collectors.toList());
//            peopleBoxes.add(new PeopleBox(deviceId, hour, min, sec, boxes));
//        }
//        collection.drop();
//        return peopleBoxes;
//    }

//    public PeopleBox getPolygon(){
//        peopleBox = database.getCollection(appfileConfig.collection);
//        FindIterable<Document> findIterable = peopleBox.find();
//        MongoCursor<Document> cursor = findIterable.iterator();
//        if (cursor.hasNext()){
//            Document document = cursor.next();
//            String deviceId = document.get("DeviceID").toString();
//            int hour = (int) (((Double) document.get("hour")).doubleValue());
//            int time = (int) (((Double) document.get("time")).doubleValue());
//            List<Document> bboxes = (List<Document>) document.get("points");
//            List<Polygon> polygons = bboxes.stream().map(convertDocumentToPolygon()).collect(Collectors.toList());
//            return new PeopleBox(deviceId, hour, time, polygons);
//        }
//        return null;
//    }

    public void addPeople(PeopleBox peopleBox, int frameNum) throws InterruptedException{
        if (peopleBox.getbBoxes().size()<=0){
            return;
        }
        Document document = new Document();
        document.append("DeviceID", peopleBox.getDeviceID());
        document.append("time", peopleBox.getDate());
        document.append("frameNum", frameNum);
        document.append("points", peopleBox.getbBoxes().stream().map(convertToJson()).map(json -> Document.parse(json)).collect(Collectors.toList()));

        MongoCollection<Document> collection = database.getCollection(appfileConfig.collection);
        collection.insertOne(document);

        Thread.sleep(1);

    }

    public void addPeople(PeopleBox peopleBox) throws InterruptedException{
        if (peopleBox.getbBoxes().size()<=0){
            return;
        }
        Document document = new Document();
        document.append("DeviceID", peopleBox.getDeviceID());
        document.append("time", peopleBox.getDate());
        document.append("points", peopleBox.getbBoxes().stream().map(convertToJson()).map(json -> Document.parse(json)).collect(Collectors.toList()));

        MongoCollection<Document> collection = database.getCollection(appfileConfig.collection);
        collection.insertOne(document);
        System.out.println("add "+ peopleBox.getDeviceID());
        Thread.sleep(1);

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
