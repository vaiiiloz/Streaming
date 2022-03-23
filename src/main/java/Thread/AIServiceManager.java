package Thread;


import Mongo.MongoHandler;
import com.github.chengtengfei.onvif.model.OnvifDeviceInfo;
import com.github.chengtengfei.onvif.service.OnvifService;
import com.google.common.collect.Lists;
import config.Constants;
import config.ThreadPoolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.forceDelete;


@Component("aiServiceManagement")
public class AIServiceManager {
    private static ConcurrentHashMap<String, AiService> allAIServiceMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, StreamMuxerDetectThread> deviceMapGrpcThread = new ConcurrentHashMap<>();
    private static List<StreamMuxerDetectThread> streamMuxerDetectThreadList = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(AIServiceManager.class);

    private MongoHandler mongoHandler;
    @Autowired
    private ThreadPoolFactory threadPoolFactory;


    public AIServiceManager() {
        //        System.out.println(appfileConfig.mongouser);
        mongoHandler = new MongoHandler(Constants.MONGO_USER, Constants.MONGO_PASS, Constants.DATABASE);

    }

    /**
     * Create StreamMuxerDetectThread and start all
     */
    public void startAll() throws IOException, InterruptedException {
        threadPoolFactory = new ThreadPoolFactory();
        HashMap<String, String> device = new HashMap<>();

        //add device to rtsp
        //connect to NVR_API
        //login
        NVR_API.getInstance().Login();
        //get Camera list
        List<JSONObject> cameraStatus = NVR_API.getInstance().getCameraSTATUS();



        cameraStatus.stream().filter(Status -> Status.getBoolean("OnLine")).forEach(Status -> {
            JSONObject camera = null;
            try {
                camera = NVR_API.getInstance().getCameraDetail(Status.getInt("ChannelID"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //get camera protocol
            String protocol = camera.get("protocol").toString();
            String userName = camera.getString("UserName");
            String password = camera.getString("Password");
            String IPAddress = camera.getString("IPAddress");
            String deviceName = camera.get("DeviceName").toString();
            switch (protocol.toUpperCase()) {
                case "RTSP": {
                    //get paramar
                    String mainUri = camera.getString("MainUri").split("rtsp://")[1];
                    String rtsp = String.format("rtsp://%s:%s@%s", userName, password, mainUri);
                    //case rtsp then put in to map
                    device.put(deviceName, rtsp);
                    break;
                }
                case "ONVIF": {
                    //initilize onvif
                    try {
                    OnvifDeviceInfo onvifDeviceInfo = new OnvifDeviceInfo();
                    onvifDeviceInfo.setIp(IPAddress);
                    onvifDeviceInfo.setUsername(userName);
                    onvifDeviceInfo.setPassword(password);


//                    SingleIPCDiscovery.fillOnvifAddress(onvifDeviceInfo);

                    //get rtsp
                    String rtsp = OnvifService.getVideoInfo(onvifDeviceInfo).get(0).getVideoInfo().getStreamUri();

                    device.put(deviceName, rtsp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default: {
                    System.out.println("Invalid protocol");
                }
            }
        });

        //Create save folder
        if (Constants.IS_SAVE){
            device.entrySet().stream().forEach(d -> {
                String directoryName = String.format("%s/%s", Constants.output_folder, d.getKey());
                File directory = new File(directoryName);
                if (!directory.exists()) {

                    directory.mkdirs();
                } else {
                    try {
                        File[] allContents = directory.listFiles();
                        for (File file : allContents) {

                            forceDelete(file);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        device.entrySet().forEach(e -> {
            createAiService(e.getValue(), e.getKey());
        });
        List<String> deviceIds = device.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
        System.out.println("Begin");

        //Group AIService into batchsize and start the thread
        Lists.partition(deviceIds, Constants.BATCH)
                .stream()
                .forEach(subDeviceIds -> {
                    addGrpcDetectThread(subDeviceIds);
                });
        return;
    }

    /**
     * Construct and start an AIService
     *
     * @param rtsp
     * @param deviceId
     * @return
     */
    public boolean createAiService(String rtsp, String deviceId) {
        AiService aiService = new AiService(rtsp,
                Constants.PREVIEW_WIDTH,
                Constants.PREVIEW_HEIGHT,
                Constants.FRAMERATE,
                deviceId,
                Constants.IS_STREAMING,
                Constants.FRAME_BUFFER_MAX_SIZE,
                Constants.UI_BUFFER_SIZE,
                mongoHandler);
        startAiService(deviceId, aiService);
        return true;
    }

    public synchronized void startAiService(String deviceId, AiService aiService) {
        allAIServiceMap.putIfAbsent(deviceId, aiService);
        aiService.startAll(threadPoolFactory.getCachedExecutor());
    }

    public AiService getAIService(String deviceId) {
        return allAIServiceMap.get(deviceId);
    }

    public boolean isRunning(String deviceId) {
        return allAIServiceMap.containsKey(deviceId);
    }

    public synchronized static void stopAIService(String deviceId) {
        AiService aiService = allAIServiceMap.get(deviceId);
        if (aiService == null) {
            return;
        }

        aiService.stopAll();
        allAIServiceMap.remove(deviceId);
        removeGrpcDetectThread(deviceId);
    }

    public static void removeGrpcDetectThread(String deviceId) {
        if (deviceMapGrpcThread.containsKey(deviceId)) {
            if (deviceMapGrpcThread.get(deviceId).getBatchSize() <= 1) {
                deviceMapGrpcThread.get(deviceId).stop();
                streamMuxerDetectThreadList.remove(deviceMapGrpcThread.get(deviceId));
            } else {
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

    public synchronized void addGrpcDetect(String deviceId, StreamMuxerDetectThread streamMuxerDetectThread) {
        deviceMapGrpcThread.put(deviceId, streamMuxerDetectThread);
    }

    public synchronized void addGrpcDetectThread(List<String> subDeviceIds) {
        StreamMuxerDetectThread streamMuxerDetectThread = new StreamMuxerDetectThread(subDeviceIds, allAIServiceMap);
        threadPoolFactory.getCachedExecutor().execute(streamMuxerDetectThread);
        subDeviceIds.stream().forEach(e -> {
            addGrpcDetect(e, streamMuxerDetectThread);
        });

        streamMuxerDetectThreadList.add(streamMuxerDetectThread);
    }

    public synchronized void addCameraToGrocThread(String deviceId) {
        if (!deviceMapGrpcThread.containsKey(deviceId)) {
            Optional<StreamMuxerDetectThread> minGrpc = streamMuxerDetectThreadList.stream().min(Comparator.comparing(StreamMuxerDetectThread::getBatchSize));
            if (minGrpc.isPresent() && minGrpc.get().getBatchSize() < Constants.BATCH) {
                minGrpc.get().addDevide(deviceId);
                addGrpcDetect(deviceId, minGrpc.get());
            } else {
                addGrpcDetectThread(Arrays.asList(deviceId));
            }
        }
    }
}

