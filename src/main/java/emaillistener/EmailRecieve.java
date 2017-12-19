package emaillistener;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;
import org.apache.commons.io.FilenameUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class EmailRecieve implements Job {
  private List<String> requiredEmails = new ArrayList<String>();

  public static final String KEY_IMAP_HOST = "mail.imap.host.imap";
  public static final String KEY_USERNAME = "mail.username";
  public static final String KEY_PASSWORD = "mail.password";
  public static final String KEY_PROTOCOL = "mail.store.protocol";

  public static final String CONFIG_FILE = "mail.config.properties";

  public static final String SUBJECT = "test";
  public static final String MULTIPART_MIXED = "multipart/MIXED";
  public static final String DIRECTORY = "/home/astghikbabalaryan/temp/";

  public EmailRecieve() {
    requiredEmails.add("gayane.dzvakeryan@deloitte-audit-analytics.com");
    requiredEmails.add("astghik.babalaryan@deloitte-audit-analytics.com");
    requiredEmails.add("mariam.avetisyan@deloitte-audit-analytics.com");
  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    getMail();
  }

  public void getMail() {
    InputStream inputStream = null;
    try {
      Properties props = new Properties();
      inputStream = EmailRecieve.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
      props.load(inputStream);
      String hostImap = props.getProperty(KEY_IMAP_HOST);
      String username = props.getProperty(KEY_USERNAME);
      String password = props.getProperty(KEY_PASSWORD);
      String protocol = props.getProperty(KEY_PROTOCOL);

      Session session = Session.getDefaultInstance(props);
      Store store = session.getStore(protocol);
      store.connect(hostImap, username, password);

      Folder folder = store.getFolder("Inbox");
      folder.open(Folder.READ_WRITE);
      Message unreadEmails[] = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

      for (int i = 0; i < unreadEmails.length; i++) {
        Message msg = unreadEmails[i];
        if (msg.getSubject().contains(SUBJECT) && msg.getContentType().contains(MULTIPART_MIXED)) {
          Address[] from = msg.getFrom();
          String email = ((InternetAddress) from[0]).getAddress();
          if (requiredEmails.contains(email)) {
            Multipart multi = ((Multipart) msg.getContent());
            saveParts(multi, DIRECTORY);
          }
        } else {
          System.out.println("No matching emails");
        }
      }
      folder.close(false);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeStream(inputStream);
    }
  }

  public void saveParts(Multipart content, String directory) {
    InputStream in = null;
    OutputStream out = null;
    try {
      for (int i = 0; i < content.getCount(); i++) {
        MimeBodyPart part = (MimeBodyPart) content.getBodyPart(i);
        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
          String attName = FilenameUtils.removeExtension(part.getFileName());
          String extenstion = FilenameUtils.getExtension(part.getFileName());
          String filename = directory + attName + "_"
              + String.valueOf(System.currentTimeMillis()) + "." + extenstion;
          in = part.getInputStream();
          File file = new File(filename);
          out = new FileOutputStream(file);
          int read = 0;
          while ((read = in.read()) != -1) {
            out.write(read);
          }
          String awsFileName = attName + "_" + String.valueOf(System.currentTimeMillis()) 
              + "." + extenstion;
          UploadFileToS3.saveFileS3(awsFileName, file);
          file.delete();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeStream(in);
      closeStream(out);
    }
  }

  private void closeStream(Closeable stream) {
    try {
      if(stream != null) {
        stream.close();
      }
    } catch (IOException e) {
      System.out.println("Failed to close resource");
    }
  }
}
