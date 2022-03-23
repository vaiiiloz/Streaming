import Thread.AIServiceManager;
import config.AppfileConfig;
import config.SpringContext;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.util.Properties;
public class main {

    private static Properties properties;
    public static void main(final String[] args) throws IOException, InterruptedException {
        Loader.load(opencv_java.class);
        avutil.av_log_set_level(avutil.AV_LOG_QUIET);
//        BasicConfigurator.configure();
        SpringContext springContext = new SpringContext();
        springContext.setApplicationContext(SpringApplication.run(AppfileConfig.class,args));




        AIServiceManager aiServiceManager = new AIServiceManager();
        aiServiceManager.startAll();


    }
}
