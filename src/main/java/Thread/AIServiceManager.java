package Thread;


import Mongo.MongoHandler;
import com.google.common.collect.Lists;
import config.AppfileConfig;
import config.SpringContext;
import config.ThreadPoolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Component("aiServiceManagement")
public class AIServiceManager {
    private static ConcurrentHashMap<String, AiService> allAIServiceMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, StreamMuxerDetectThread> deviceMapGrpcThread = new ConcurrentHashMap<>();
    private static List<StreamMuxerDetectThread> streamMuxerDetectThreadList = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(AIServiceManager.class);

    private MongoHandler mongoHandler;
    @Autowired
    private ThreadPoolFactory threadPoolFactory;

    @Autowired
    private AppfileConfig appfileConfig;

    public AIServiceManager() {

        appfileConfig = SpringContext.context.getBean("appfileConfig",AppfileConfig.class);
//        System.out.println(appfileConfig.mongouser);
        mongoHandler = new MongoHandler(appfileConfig.mongouser, appfileConfig.pass, appfileConfig.database);
    }

    public void startAll(){
        threadPoolFactory = new ThreadPoolFactory(appfileConfig);
        HashMap<String, String> device = new HashMap<String, String>();
        device.put("deviceA", appfileConfig.rtsp);
        device.put("deviceB", "rtsp://admin:12345678a@@192.168.0.3:8554/fhd");
        device.put("deviceC", "rtsp://admin:12345678a@@192.168.0.62:8554/fhd");
        device.put("deviceD", "rtsp://admin:12345678a@@192.168.0.247:554/fhd");

        device.entrySet().stream().forEach(d ->{
            String directoryName = String.format("%s/%s",appfileConfig.output_folder,d.getKey());
            File directory = new File(directoryName);
            if (!directory.exists()){

                directory.mkdirs();
            }else{
//                try {
//                    FileUtils.deleteDirectory(directory);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                directory.mkdirs();
            }
        });
        device.entrySet().forEach(e -> {
            createAiService(e.getValue(), e.getKey());
        });
        List<String> deviceIds = device.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
        System.out.println("Begin");
        Lists.partition(deviceIds, appfileConfig.batch)
                .stream()
                .forEach(subDeviceIds ->{
                    addGrpcDetectThread(subDeviceIds);
                });
        return;
    }

    public boolean createAiService(String rtsp, String deviceId){
        AiService aiService = new AiService(rtsp,
                appfileConfig.preview_width,
                appfileConfig.preview_height,
                appfileConfig.frameRate,
                deviceId,
                appfileConfig.isStreaming,
                appfileConfig.frameBufferMaxSize,
                appfileConfig.uiBufferSize,
                mongoHandler);
        startAiService(deviceId, aiService);
        return true;
    }

    public synchronized void startAiService(String deviceId, AiService aiService){
        allAIServiceMap.putIfAbsent(deviceId, aiService);
        aiService.startAll(threadPoolFactory.getCachedExecutor());
    }

    public AiService getAIService(String deviceId){
        return allAIServiceMap.get(deviceId);
    }

    public boolean isRunning(String deviceId){
        return allAIServiceMap.containsKey(deviceId);
    }

    public synchronized static void stopAIService(String deviceId){
        AiService aiService = allAIServiceMap.get(deviceId);
        if (aiService == null){
            return;
        }

        aiService.stopAll();
        allAIServiceMap.remove(deviceId);
        removeGrpcDetectThread(deviceId);
    }

    public static void removeGrpcDetectThread(String deviceId){
        if (deviceMapGrpcThread.containsKey(deviceId)){
            if (deviceMapGrpcThread.get(deviceId).getBatchSize()<=1){
                deviceMapGrpcThread.get(deviceId).stop();
                streamMuxerDetectThreadList.remove(deviceMapGrpcThread.get(deviceId));
            }else{
                deviceMapGrpcThread.get(deviceId).removeDevice(deviceId);
            }
            deviceMapGrpcThread.remove(deviceId);
        }
    }

    public static ConcurrentHashMap<String, AiService> getAllAIServiceMap() {
        return allAIServiceMap;
    }

    public static void setAllAIServiceMap(ConcurrentHashMap<String, AiService> allAIServiceMap) {
        AIServiceManager.allAIServiceMap = allAIServiceMap;
    }

    public synchronized void addGrpcDetect(String deviceId, StreamMuxerDetectThread streamMuxerDetectThread){
        deviceMapGrpcThread.put(deviceId, streamMuxerDetectThread);
    }

    public synchronized void addGrpcDetectThread(List<String> subDeviceIds){
        StreamMuxerDetectThread streamMuxerDetectThread = new StreamMuxerDetectThread(subDeviceIds, allAIServiceMap);
        threadPoolFactory.getCachedExecutor().execute(streamMuxerDetectThread);
        subDeviceIds.stream().forEach(e ->{
            addGrpcDetect(e, streamMuxerDetectThread);
        });

        streamMuxerDetectThreadList.add(streamMuxerDetectThread);
    }

    public synchronized void addCameraToGrocThread(String deviceId){
        if (!deviceMapGrpcThread.containsKey(deviceId)){
            Optional<StreamMuxerDetectThread> minGrpc = streamMuxerDetectThreadList.stream().min(Comparator.comparing(StreamMuxerDetectThread::getBatchSize));
            if (minGrpc.isPresent() && minGrpc.get().getBatchSize()<appfileConfig.batch){
                minGrpc.get().addDevide(deviceId);
                addGrpcDetect(deviceId,minGrpc.get());
            }else{
                addGrpcDetectThread(Arrays.asList(deviceId));
            }
        }
    }
}

