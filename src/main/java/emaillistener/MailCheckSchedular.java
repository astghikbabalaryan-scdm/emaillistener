package emaillistener;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class MailCheckSchedular {
  public static final String TIME = "0 0/1 * * * ?";
  public static final String JOB_NAME = "CheckEmail";
  public static final String TRIGGER_NAME = "EmailReceiver";
  
  public static void main(String[] args) throws Exception {
    JobDetail job = JobBuilder.newJob(EmailRecieve.class).withIdentity(JOB_NAME).build();

    Trigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME)
        .withSchedule(CronScheduleBuilder.cronSchedule(TIME)).build();
    Scheduler scheduler = new StdSchedulerFactory().getScheduler();
    scheduler.start();
    scheduler.scheduleJob(job, trigger);
  }
}
