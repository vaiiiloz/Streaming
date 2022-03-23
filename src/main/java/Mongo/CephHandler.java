package Mongo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.InputStream;

public class CephHandler {
    private String accessKey;
    private String secretKey;
    private String hostname;

    /**
     * Construct CephHandler with accesskey, secretKey, hostname
     * @param accessKey
     * @param secretKey
     * @param hostname
     */
    public CephHandler(String accessKey, String secretKey, String hostname) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.hostname = hostname;
    }

    /**
     * Save image to a ceph server
     * @param bucket_name
     * bucket of the save image
     * @param key
     * image path
     * @param type
     * image type (png, jpg, ...)
     * @param metalength
     * length of image
     * @param is
     * image in InputStream
     */
    public void addBackground(String bucket_name, String key, String type, int metalength, InputStream is) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(metalength);
        meta.setContentType("image/" + type);
        createConnect().putObject(new PutObjectRequest(bucket_name, key, is, meta).withCannedAcl(CannedAccessControlList.PublicRead));
    }

    /**
     * Connect to ceph server
     * @return ceph server connected
     */
    private AmazonS3 createConnect() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 conn = new AmazonS3Client(credentials);
        conn.setEndpoint(hostname);
        return conn;
    }
}
