package com.databasepreservation.common.server;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.server.index.factory.SolrClientFactory;


@SuppressWarnings("serial")
public class BrowserServiceImpl extends HttpServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserServiceImpl.class);

  @Override
  public void init() throws ServletException {
    super.init();
    new Thread(SolrClientFactory::get).start();

//    SchedulerFactory sf = new StdSchedulerFactory();
//    try {
//      Scheduler s = sf.getScheduler();
//      // get current state
//      Set<JobKey> jobKeys = s.getJobKeys(GroupMatcher.anyGroup());
//      for(JobKey jk :  s.getJobKeys(GroupMatcher.anyGroup())) {
//        JobDetail jd = s.getJobDetail(jk);
//        for (Trigger trigger : s.getTriggersOfJob(jk)) {
//          Trigger.TriggerState triggerState = s.getTriggerState(trigger.getKey());
//          // triggerState == Trigger.TriggerState.BLOCKED;
//        }
//        JobExecutionContext c;
//        // TODO index
//      }
//      // TODO add listener to be updated
//    } catch (
//
//    SchedulerException e) {
//      e.printStackTrace();
//    }
//    // TODO init Quartz Scheduler
//    // TODO bind Quartz listener to update Solr status
  }

  /**
   * Called by the servlet container to indicate to a servlet that the servlet is
   * being taken out of service.
   */
  @Override
  public void destroy() {
    super.destroy();

    try {
      SolrClientFactory.get().getSolrClient().close();
    } catch (IOException e) {
      LOGGER.error("Stopping SolrClient", e);
    }

    // TODO stop Quartz Scheduler

  }
}
