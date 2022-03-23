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
import config.Constants;
import entity.BBox;
import entity.Coordinate;
import entity.PeopleBox;
import entity.Polygon;
import org.bson.Document;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
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

    /**
     * Construct MongoHanlder with user, pass and database name
     * @param user
     * @param pass
     * @param dbname
     */
    public MongoHandler(String user, String pass, String dbname) {
        this.dbname = dbname;
        this.user = user;
        this.pass = pass;
        connectMongoDB();
    }

    /**
     * Connect to MongoDB
     */
    public void connectMongoDB(){
        MongoCredential credential = MongoCredential.createScramSha1Credential(user, "admin", pass.toCharArray());
        mongoClient = new MongoClient(new ServerAddress(Constants.MONGO_ADDRESS, Constants.MONGO_PORT), Arrays.asList(credential));
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


    /**
     * Add people box (list of Entity, time, deviceId) to DB
     * @param peopleBox
     * @param frameNum
     * @throws InterruptedException
     */
    public void addPeople(PeopleBox peopleBox, int frameNum) throws InterruptedException{
        if (peopleBox.getbBoxes().size()<=0){
            return;
        }
        Document document = new Document();
        document.append("DeviceID", peopleBox.getDeviceID());
        document.append("time", peopleBox.getDate());
        document.append("frameNum", frameNum);
        document.append("points", peopleBox.getbBoxes().stream().map(convertToJson()).map(json -> Document.parse(json)).collect(Collectors.toList()));

        MongoCollection<Document> collection = database.getCollection(Constants.BOX_COLLECTION);
        collection.insertOne(document);

        Thread.sleep(1);

    }

    /**
     * Add people box (list of Entity (x1, y1, x2, y2), time, deviceId) to DB
     * @param peopleBox
     * @throws InterruptedException
     */
    public void addPeople(PeopleBox peopleBox, String path) throws InterruptedException{
        if (peopleBox.getbBoxes().size()<=0){
            return;
        }
        Document document = new Document();
        document.append("camId", peopleBox.getDeviceID());
        document.append("time", peopleBox.getDate());
//        document.append("points", peopleBox.getbBoxes().stream().map(convertToJson()).map(json -> Document.parse(json)).collect(Collectors.toList()));
        document.append("boxes", peopleBox.getbBoxes().stream().map(box -> {
            List list = new ArrayList<>();
            //add box value
            list.add(box.getX1());
            list.add(box.getY1());
            list.add(box.getX2());
            list.add(box.getY2());
            list.add(null);
            list.add(Constants.CEPH_BUCKET);
            list.add(path);
            return list;
        }).collect(Collectors.toList()));

        MongoCollection<Document> collection = database.getCollection(Constants.BOX_COLLECTION);
        collection.insertOne(document);
        System.out.println("add "+ peopleBox.getDeviceID());
        Thread.sleep(1);

    }

    /**
     * Insert background path in ceph server into MongoDB
     * @param deviceID
     * @param path
     * @param hour
     * @param time
     * @throws InterruptedException
     */
    public void addBackground(String deviceID, String path, int hour, long time) throws InterruptedException{
        Document document = new Document();
        document.append("ID", hour);
        document.append("camId",deviceID);
        document.append("bucket", Constants.CEPH_BUCKET);
        document.append("path", path);
        document.append("time", time);

        //insert
        MongoCollection<Document> collection = database.getCollection(Constants.BACKGROUND_COLLECTION);
        collection.insertOne(document);
        Thread.sleep(1);
    }

    public void ResetAllLine(){
        peopleBox = database.getCollection(Constants.BOX_COLLECTION);
        peopleBox.deleteMany(new BasicDBObject());
    }

    public void DeleteLine(String cloudId){
        peopleBox = database.getCollection(Constants.BOX_COLLECTION);
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
