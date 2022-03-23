package config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("file:InferenceProp.properties")
@Component
@Qualifier("appfileConfig")
public class AppfileConfig {

    @Value("${rtsps}")
    public String[] rtsps;

    @Value("${width}")
    public int preview_width;

    @Value("${height}")
    public int preview_height;

    @Value("${frameRate}")
    public int frameRate;

    @Value("${host}")
    public String host;

    @Value("${port}")
    public int port;

    @Value("${modelName}")
    public String modelName;

    @Value("${modelVersion}")
    public int modelVersion;

    @Value("${isGetModelInfo}")
    public boolean isGetModelInfo;

    @Value("${output_folder}")
    public String output_folder;

    @Value("${batch}")
    public int batch;

    @Value("${box_collection}")
    public String box_collection;

    @Value("${threadpool.prefix}")
    public String threadPoolPrefix;

    @Value("${application.type}")
    public String applicationType;

    @Value("${is.streaming}")
    public Boolean isStreaming;

    @Value("${frame.buffer.max.size}")
    public int frameBufferMaxSize;

    @Value("${ui.buffer.size}")
    public int uiBufferSize;

    @Value("${threadpool.fix.num}")
    public int threadPoolFixedNum;

    @Value("${multiplier}")
    public float multiplier;

    @Value("${waittime}")
    public int waittime;

    @Value("${mongoAddress}")
    public String mongoAddress;

    @Value("${mongoPort}")
    public int mongoPort;

    @Value("${database}")
    public String database;

    @Value("${modelType}")
    public String modelType;

    @Value("${mongouser}")
    public String mongouser;

    @Value("${pass}")
    public String pass;

    @Value("${cephAccessKey}")
    public String cephAccessKey;

    @Value("${cephPrivateKey}")
    public String cephPrivateKey;

    @Value("${cephHostname}")
    public String cephHostname;

    @Value("${cephBucket}")
    public String cephBuket;

    @Value("${cephFolder}")
    public String cephFolder;

    @Value("${background_collection}")
    public String background_collection;

    @Value("${cephBackgroundType}")
    public String cephBackgroundType;

    @Value("${missinglogtime}")
    public int missinglogtime;

    @Value("${isRecord}")
    public boolean isRecord;

    @Value("${isSave}")
    public boolean isSave;

    @Value("${everfocus.UserName}")
    public String everfocus_userName;

    @Value("${everfocus.Password}")
    public String everfocus_password;

    @Value("${everfocus.realm}")
    public String everfocus_realm;

    @Value("${everfocus.nonce_count}")
    public String everfocus_nonce_count;

    @Value("${NVR.LOGIN.URI}")
    public String NVR_LOGIN_URI;

    @Value("${NVR.CAMERALIST.URI}")
    public String NVR_CAMERALIST_URI;

    @Value("${NVR.CAMERADETAIL.URI}")
    public String NVR_CAMERADETAIL_URI;

    @Value("${EVERFOCUS.LOGIN.METHOD}")
    public String EVERFOCUS_LOGIN_METHOD;

    @Value("${EVERFOCUS.LOGIN.DIGEST_URI}")
    public String EVERFOCUS_LOGIN_DIGEST_URI;

    @Value("${NVR.CAMERASTATUS.URI}")
    public String NVR_CAMERASTATUS_URI;

}
