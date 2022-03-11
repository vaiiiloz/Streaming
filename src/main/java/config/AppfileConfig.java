package config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("file:InferenceProp.properties")
@Component
@Qualifier("appfileConfig")
public class AppfileConfig {

    @Value("${rtsp}")
    public String rtsp;

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

    @Value("${collection}")
    public String collection;

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

//    @Value("${isRecord}")
//    public boolean isRecord;

//    @Value("${isSave}")
//    public boolean isSave;

}
