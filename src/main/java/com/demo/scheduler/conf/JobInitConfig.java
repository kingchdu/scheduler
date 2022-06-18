package com.demo.scheduler.conf;

import com.demo.scheduler.service.JobScheduler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class JobInitConfig implements InitializingBean, DisposableBean {

    private JobScheduler jobScheduler;

    @Override
    public void afterPropertiesSet() throws Exception {
        jobScheduler = new JobScheduler();
        jobScheduler.init();
    }

    @Override
    public void destroy() throws Exception {
        jobScheduler.destroy();
    }

}
