package emaillistener;

import java.io.File;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class UploadFileToS3 {  
  public static final String BUCKETNAME = "test-file-upl1";
  private static final String ACCESSKEYID ="AKIAJO7SCE5REZO3X2QA";
  private static final String ACCESSPASSWORD ="xmZL3iqAd6mwdAL2hrPvnOOZ5ye3ga3krxc1BHso";
  
  public static void saveFileS3(String filname, File file) {
    AWSCredentials credentials = new BasicAWSCredentials(ACCESSKEYID,ACCESSPASSWORD);
    AmazonS3 s3client = AmazonS3ClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(Regions.EU_CENTRAL_1)
        .build();
    s3client.putObject(new PutObjectRequest(BUCKETNAME, filname, file));
  }
}
