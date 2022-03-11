import Thread.AIServiceManager;
import config.AppfileConfig;
import config.SpringContext;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.springframework.boot.SpringApplication;

import java.util.Properties;

public class main {

    private static Properties properties;
    public static void main(final String[] args){
        Loader.load(opencv_java.class);
//        BasicConfigurator.configure();
        SpringContext springContext = new SpringContext();
        springContext.setApplicationContext(SpringApplication.run(AppfileConfig.class,args));



        AIServiceManager aiServiceManager = new AIServiceManager();
        aiServiceManager.startAll();


        //get boxes from mongodb
//        MongoHandler mongoHandler = new MongoHandler("mongodb://localhost:27017", "Camera");
//        List<PeopleBox> boxes = mongoHandler.getBoxes();
//
//        //draw box to image
//        System.out.println(boxes.size());
//        boxes.forEach(e->{
//            System.out.println(e.toString());
//        });



































//        //get properties
//
//        File configFile = new File("InferenceProp.properties");
//        try {
//            FileReader reader = new FileReader(configFile);
//            properties = new Properties();
//            properties.load(reader);
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//        //init DataProcess
//
//        DataProcess dataProcess = new DataProcess(properties.getProperty("output_folder"));
//
//        BlockingBuffer queues = new BlockingBuffer(1000);
//
//        List<BBox> listBoxes = Collections.synchronizedList(new ArrayList<>());
//
//        GrabFrame grabFrame = new GrabFrame(properties.getProperty("rtsp"),
//                Integer.parseInt(properties.getProperty("height")),
//                Integer.parseInt(properties.getProperty("width")),
//                Integer.parseInt(properties.getProperty("frameRate")),
//                queues);
//
//        TritonClient tritonClient = new TritonClient(
//                properties.getProperty("host"),
//                Integer.parseInt(properties.getProperty("port")),
//                Integer.parseInt(properties.getProperty("width")),
//                Integer.parseInt(properties.getProperty("height")),
//                properties.getProperty("modelName"),
//                Boolean.parseBoolean(properties.getProperty("isGetModelInfo")),
//
//
//                properties.getProperty("mode"),
//                Integer.parseInt(properties.getProperty("batch")),
//                dataProcess,
//                queues
//
//        );
//        tritonClient.setPoints(listBoxes);
//
//        MongoDB task3 = new MongoDB(listBoxes);
//
//        Thread task1 = new Thread(grabFrame);
//        Thread task2 = new Thread(tritonClient);
//        try{
//            if (tritonClient.initTritonClient()){
//                task1.start();
//                task2.start();
//                task3.start();
//
//
//                task1.join();
//                task2.join();
//                task3.join();
//            }
//        }catch (InterruptedException e){
//            System.out.println("interupt");
//            e.printStackTrace();
//        }


    }
}
