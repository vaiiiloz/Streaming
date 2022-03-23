package Thread;

import com.google.protobuf.InvalidProtocolBufferException;
import config.AppfileConfig;
import config.SpringContext;
import entity.*;
import inference.GRPCInferenceServiceGrpc;
import inference.GrpcService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bytedeco.javacv.Frame;
import utils.ConvertByteToUINT8Thread;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamMuxerDetectThread implements Runnable {
    private ConcurrentHashMap<String, AiService> allAIServiceMap;
    private boolean running = true;
    private boolean initName;

    private int countSingle;
    private List<String> deviceIds = new ArrayList<>();
    private Supplier<Stream<AiService>> aiserviceStream;
    private volatile String nameDetectThred;
    private Object lockRemove = new Object();
    private Frame backupFrame;
    private List<String> missingFrameDeviceIds = new ArrayList<>();
    private int batchSize;
    private String fakeId = "fakeID";

    private static int totalFrame = 0;
    private static final int THRESHOLD_FRAME = 100;
    long TotalTime = 0;
    long GrpcTime = 0;
    long DetectTime = 0;

    private int preview_width;
    private int preview_height;

    private String host;
    private int port;
    private String modelName;
    private int modelVersion;
    private int modelInputWidth;
    private int modelInputHeight;
    private int ch;
    private boolean isGetModelInfor;
    private GRPCInferenceServiceGrpc.GRPCInferenceServiceBlockingStub blockingStub = null;
    private List<Frame> lsitFrames = new ArrayList<>();
    AppfileConfig appfileConfig;
    private Logger LOGGER = LogManager.getLogger(StreamMuxerDetectThread.class);

    public StreamMuxerDetectThread(List<String> deviceIds, ConcurrentHashMap<String, AiService> allAIServiceMap) {
        this.deviceIds.addAll(deviceIds);
        appfileConfig = SpringContext.context.getBean("appfileConfig", AppfileConfig.class);
        this.allAIServiceMap = allAIServiceMap;
        this.batchSize = appfileConfig.batch;
        this.preview_width = appfileConfig.preview_width;
        this.preview_height = appfileConfig.preview_height;
        setName();

        //create Triton Client
        initTritonClient(appfileConfig.host, appfileConfig.port,
                appfileConfig.modelName, appfileConfig.modelVersion,
                appfileConfig.preview_width, appfileConfig.preview_height,
                appfileConfig.isGetModelInfo);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean initTritonClient(String host, int port, String modelName, int modelVersion, int preview_width, int preview_height, boolean isGetModelInfo) {
        //Create gRPC stub for communicating with server
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        //Proto part
        blockingStub = GRPCInferenceServiceGrpc.newBlockingStub(channel);

        //check server live status
        GrpcService.ServerLiveRequest serverLiveRequest = GrpcService.ServerLiveRequest.getDefaultInstance();
        GrpcService.ServerLiveResponse serverLiveResponse = blockingStub.serverLive(serverLiveRequest);
        if (!serverLiveResponse.getLive()) {
            LOGGER.error("Server is not live");
            return false;
        }
        //check server ready status
        GrpcService.ServerReadyRequest serverReadyRequest = GrpcService.ServerReadyRequest.getDefaultInstance();
        GrpcService.ServerReadyResponse serverReadyResponse = blockingStub.serverReady(serverReadyRequest);
        if (!serverReadyResponse.getReady()) {
            LOGGER.error("Server is not ready");
            return false;
        }
        //check model ready status
        GrpcService.ModelReadyRequest.Builder modelReadyBuilder = GrpcService.ModelReadyRequest.newBuilder();
        modelReadyBuilder.setName(modelName);
        GrpcService.ModelReadyRequest modelReadyRequest = modelReadyBuilder.build();
        GrpcService.ModelReadyResponse modelReadyResponse = blockingStub.modelReady(modelReadyRequest);
        if (!modelReadyResponse.getReady()) {
            LOGGER.error("Model is not ready");
            return false;
        }
        //Get model info
        if (isGetModelInfo) {
            GrpcService.ModelMetadataRequest.Builder modelMetaBuilder = GrpcService.ModelMetadataRequest.newBuilder();
            modelMetaBuilder.setName(modelName);
            GrpcService.ModelMetadataRequest modelMetadataRequest = modelMetaBuilder.build();


            try {
                GrpcService.ModelMetadataResponse modelMetadataResponse = blockingStub.modelMetadata(modelMetadataRequest);
//                System.out.println(modelMetadataResponse.toString());
            } catch (Exception e) {
                LOGGER.error("Wrong info");
                return false;
            }
        }
        //Get configuration
        GrpcService.ModelConfigRequest.Builder modelConfigBuilder = GrpcService.ModelConfigRequest.newBuilder();
        modelConfigBuilder.setName(modelName);
        GrpcService.ModelConfigRequest modelConfigRequest = modelConfigBuilder.build();

        try {
            GrpcService.ModelConfigResponse modelConfigResponse = blockingStub.modelConfig(modelConfigRequest);
        } catch (Exception e) {
            LOGGER.error("Wrong config");
            return false;
        }
        return true;
    }

    public void DetectFrocss() throws InterruptedException, InvalidProtocolBufferException {
        synchronized (this.lockRemove) {
            if (aiserviceStream.get().allMatch((e -> e.getRtspCaptureThread().getmFrameBuffer().isEmpty()))) {
                return;
            }

            //create backupFrame
            if (backupFrame == null) {
                Optional<AiService> aiServiceEntry = aiserviceStream.get()
                        .filter(e -> !e.getRtspCaptureThread().getmFrameBuffer().isEmpty())
                        .filter(e -> {
                            try {

                                return !(((Frame) e.getRtspCaptureThread().getmFrameBuffer().getLast()).image == null);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            return false;
                        }).findFirst();
                if (aiServiceEntry.isPresent()) {
                    backupFrame = aiServiceEntry.get().getRtspCaptureThread().getmFrameBuffer().pop();
                } else {
                    aiserviceStream.get().forEach(e -> {
                        try {
                            e.getRtspCaptureThread().getmFrameBuffer().pop();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                    return;
                }
            }

            missingFrameDeviceIds.clear();

            //triton stream
            //Get image from Capture Thread
            List<Pair<String, Frame>> images = aiserviceStream.get()
                    .filter(e -> !e.getRtspCaptureThread().getmFrameBuffer().isEmpty())
                    .map(e -> {
                        try {
//                            System.out.println(e.getRtspCaptureThread().getmFrameBuffer().size());
                            return new Pair<String, Frame>(e.getDeviceId(), e.getRtspCaptureThread().getmFrameBuffer().pop());
                        } catch (InterruptedException ex) {

                            ex.printStackTrace();
                        }
                        return new Pair<String, Frame>(e.getDeviceId(), new Frame());
                    }).filter(e -> !(e.getValue().image == null)).collect(Collectors.toList());


            //Add fake frame to the missing batch size
            int i = 1;
            while (images.size() < batchSize) {
//                System.out.println(backupFrame.image.length);
                String deviceIdFake = "deviceFakeFake" + i;
                images.add(new Pair(deviceIdFake, backupFrame));
                missingFrameDeviceIds.add(deviceIdFake);
                i++;
            }

            //push batch to tritonserver
            List<PeopleBox> people = peopleDetectTriton(images);


            if (people == null) {
                return;
            }
//            //add frame
//            images.stream().filter(e -> !missingFrameDeviceIds.contains(e.getKey()))
//                    .filter(e -> !(e.getValue().image == null))
//                    .forEach(e ->{
//                try {
//
//                    allAIServiceMap.get(e.getKey()).getRtspStreamThread().getmFrameBuffer().push(e.getValue());
//                }catch (InterruptedException exception){
//                    exception.printStackTrace();
//                }
//            });

            //add bbox
            people.stream().filter(e -> !missingFrameDeviceIds.contains(e.getDeviceID()))
                    .forEach(e -> {
                        try {
//                            System.out.println(e.getbBoxes().size());
                            allAIServiceMap.get(e.getDeviceID()).getRtspStreamThread().getmFaceBuffer().push(e);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
        }
    }

    public List<PeopleBox> peopleDetectTriton(List<Pair<String, Frame>> images) {
        int[] inputData = new int[0];
        try {

            long now = System.currentTimeMillis();
            long converInputStart = System.currentTimeMillis();
            List<String> listDeviceId = new ArrayList<>();
            List<Integer> listInputSize = new ArrayList<>();

            List<TritonInputData> listInputData = new ArrayList<>();
            List<Thread> convertWorkers = new ArrayList<>();
            CountDownLatch countDownLatch = new CountDownLatch(images.size());

            //create converting workers to convert frame to uint8 array
            for (int i = 0; i < images.size(); i++) {
                Pair<String, Frame> input = images.get(i);
                String deviceId = input.getKey();
                listDeviceId.add(deviceId);
                Frame image = input.getValue();
                Thread convertThread = new Thread(new ConvertByteToUINT8Thread(image, deviceId,
                        listInputData, listInputSize, countDownLatch));
                convertWorkers.add(convertThread);
            }

            //Start all workers
            try {
                convertWorkers.forEach(Thread::start);
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
//
            if ((listInputData.size() == images.size()) && (listInputData.size() > 0)) {
                try {
                    // find the max input size
                    int maxInputSize = Collections.max(listInputSize);
                    //padding images
                    for (int i = 0; i < listInputData.size(); i++) {
                        TritonInputData tritonInputData = listInputData.get(i);
                        int[] image = tritonInputData.getData();
//                while (image.length<maxInputSize){
//                    int[] fillerArray = new int[maxInputSize-image.length];
//                    image = ArrayUtils.addAll(image, fillerArray);
//                }
                        inputData = ArrayUtils.addAll(inputData, Arrays.copyOf(image, maxInputSize));
                    }
                    long convertInputTime = System.currentTimeMillis() - converInputStart;
                    long buildInferReqStart = System.currentTimeMillis();

                    GrpcService.InferTensorContents.Builder inferTensorBuilder = GrpcService.InferTensorContents.newBuilder();
                    //Raw format
                    Integer[] IntegerArray = ArrayUtils.toObject(inputData);

                    inferTensorBuilder.addAllUintContents(Arrays.asList(IntegerArray));


                    //Build input
                    GrpcService.ModelInferRequest.InferInputTensor.Builder inferInputBuilder = GrpcService.ModelInferRequest.InferInputTensor.newBuilder();
                    inferInputBuilder.setName("INPUT");
                    inferInputBuilder.setDatatype("UINT8");
                    inferInputBuilder.addShape(images.size());
                    inferInputBuilder.addShape(inputData.length / images.size());
                    inferInputBuilder.setContents(inferTensorBuilder);

                    //create output
                    GrpcService.ModelInferRequest.InferRequestedOutputTensor.Builder output0Builder = GrpcService.ModelInferRequest.InferRequestedOutputTensor.newBuilder();
                    output0Builder.setName("OUTPUT_0");

                    GrpcService.ModelInferRequest.InferRequestedOutputTensor.Builder output1Builder = GrpcService.ModelInferRequest.InferRequestedOutputTensor.newBuilder();
                    output1Builder.setName("OUTPUT_1");

                    //create inference
                    GrpcService.ModelInferRequest.Builder modelInferBuilder = GrpcService.ModelInferRequest.newBuilder();
                    modelInferBuilder.setModelName(appfileConfig.modelName);
                    modelInferBuilder.addInputs(inferInputBuilder);
                    modelInferBuilder.addOutputs(output0Builder);
                    modelInferBuilder.addOutputs(output1Builder);

                    //push inference
                    long requestStart = System.currentTimeMillis();


                    GrpcService.ModelInferResponse response = this.blockingStub.modelInfer(modelInferBuilder.build());


                    long inferenceTime = System.currentTimeMillis() - requestStart;

                    List<TritonDetectedResults> tritonPostResult = new ArrayList<>();
                    switch (appfileConfig.modelType) {
                        case "scrfd": {
                            tritonPostResult = TritonDataProcessing.peoplePostProcessScrfd(response, deviceIds);
                            break;
                        }

                        case "rapid": {
                            tritonPostResult = TritonDataProcessing.peoplePostProcessRapid(response, deviceIds);
                            break;
                        }

                    }


                    return tritonPostResult.stream().map(e -> new PeopleBox(e.getDeviceId(), now, e.getListBBoxes())).collect(Collectors.toList());
                } catch (Exception e) {
                    LOGGER.error("Triton Server is not available ");
                    e.printStackTrace();
                    return null;
                }
            } else {

                return null;
            }
        } catch (Exception e) {


            e.printStackTrace();
            return null;
        }

    }

    public void setName() {
        aiserviceStream = () -> allAIServiceMap.entrySet().stream()
                .filter(e -> deviceIds.contains(e.getValue().getDeviceId()))
                .map(e -> e.getValue());
        nameDetectThred = String.join(",", deviceIds);
    }

    public int getBatchSize() {
        return deviceIds.size();
    }

    public void removeDevice(String deviceId) {
        synchronized (this.lockRemove) {
            deviceIds.remove(deviceId);
            setName();
        }
    }

    public void addDevide(String deviceId) {
        synchronized (this.lockRemove) {
            deviceIds.add(deviceId);
            setName();
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        setName();
        while (running) {
            try {
                DetectFrocss();
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {

                e.printStackTrace();
            } catch (Exception ee) {
                ee.printStackTrace();
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
