package config;



public class Constants {
    private static AppfileConfig appfileConfig = SpringContext.context.getBean("appfileConfig", AppfileConfig.class);

    public static int THREADSCHEDULED_BACKGROUND = 1000;

    public static int PREVIEW_WIDTH = appfileConfig.preview_width;

    public static int PREVIEW_HEIGHT = appfileConfig.preview_height;

    public static int FRAMERATE = appfileConfig.frameRate;

    public static String host = appfileConfig.host;

    public static int port = appfileConfig.port;

    public static String modelName = appfileConfig.modelName;

    public static int modelVersion = appfileConfig.modelVersion;

    public static boolean isGetModelInfo = appfileConfig.isGetModelInfo;

    public static String output_folder = appfileConfig.output_folder;

    public static int BATCH = appfileConfig.batch;

    public static String BOX_COLLECTION = appfileConfig.box_collection;

    public static String THREAD_POOL_PREFIX = appfileConfig.threadPoolPrefix;

    public static String APPLICATION_TYPE = appfileConfig.applicationType;

    public static Boolean IS_STREAMING = appfileConfig.isStreaming;

    public static int FRAME_BUFFER_MAX_SIZE = appfileConfig.frameBufferMaxSize;

    public static int UI_BUFFER_SIZE = appfileConfig.uiBufferSize;

    public static int THREAD_POOL_FIXED_NUM = appfileConfig.threadPoolFixedNum;

    public static int WAITTIME = appfileConfig.waittime;

    public static String MONGO_ADDRESS = appfileConfig.mongoAddress;

    public static int MONGO_PORT = appfileConfig.mongoPort;

    public static String DATABASE = appfileConfig.database;

    public static String ModelType = appfileConfig.modelType;

    public static String MONGO_USER = appfileConfig.mongouser;

    public static String MONGO_PASS = appfileConfig.pass;

    public static String CEPH_ACCESS_KEY = appfileConfig.cephAccessKey;

    public static String CEPH_PRIVATE_KEY = appfileConfig.cephPrivateKey;

    public static String CEPH_HOST_NAME = appfileConfig.cephHostname;

    public static String CEPH_BUCKET = appfileConfig.cephBuket;

    public static String CEPH_FOLDER = appfileConfig.cephFolder;

    public static String BACKGROUND_COLLECTION = appfileConfig.background_collection;

    public static String CEPH_BACKGROUND_TYPE = appfileConfig.cephBackgroundType;

    public static int MISSINGLOGTIME = appfileConfig.missinglogtime;

    public static boolean IS_RECORD = appfileConfig.isRecord;

    public static boolean IS_SAVE = appfileConfig.isSave;

    public static String EVERFOCUS_USERNAME = appfileConfig.everfocus_userName;

    public static String EVERFOCUS_PASSWORD = appfileConfig.everfocus_password;

    public static String EVERFOCUS_REALM = appfileConfig.everfocus_realm;

    public static String EVERFOCUS_NONCE_COUNT = appfileConfig.everfocus_nonce_count;

    public static String NVR_LOGIN_URI = appfileConfig.NVR_LOGIN_URI;

    public static String NVR_CAMERALIST_URI = appfileConfig.NVR_CAMERALIST_URI;

    public static String NVR_CAMERADETAIL_URI = appfileConfig.NVR_CAMERADETAIL_URI;

    public static String METHOD = appfileConfig.EVERFOCUS_LOGIN_METHOD;

    public static String DIGEST_URI = appfileConfig.EVERFOCUS_LOGIN_DIGEST_URI;

    public static String NVR_CAMERASTATUS_URI = appfileConfig.NVR_CAMERASTATUS_URI;
}
