package com.thorton.grant.uspto.prototypewebapp.config.bootstrap;

import com.thorton.grant.uspto.prototypewebapp.factories.ServiceBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.TimerTask;


@Component
@PropertySource("classpath:server-host-AWS-prod1.properties")

public class FilingStatusUpdateTask extends TimerTask {



    private  final ServiceBeanFactory serviceBeanFactory;

    public FilingStatusUpdateTask(ServiceBeanFactory serviceBeanFactory) {
        this.serviceBeanFactory = serviceBeanFactory;

        System.out.println("task consturctor called.");
    }

    // read properties from property file
    // for duration intervals

    // create variables




    // note. these numbers will be in days
    @Value("${uspto.blackoutPeriod}")
    private long blackOutPeriodDuration = 5*60*1000;  // 5 mins

    @Value("${uspto.officeaction1}")
    private long firstOfficeActionDuration = 5*60*1000;;  // 5 mins


    @Value("${uspto.officeaction2b}")
    private long durationToRevivieWithoutClaim = 2*60*1000;; // 2 minsh

    @Value("${uspto.officeaction2}")
    private long durationToReviveWithClaim = 5*60*1000;;   // 5 mins



    @PostConstruct
    public void init() {

        System.out.println("================== " + "post construct check" + "================== ");


        System.out.println("================== " + blackOutPeriodDuration + "================== ");

        System.out.println("================== " + firstOfficeActionDuration + "================== ");

        System.out.println("================== " + durationToReviveWithClaim + "================== ");
    }



    // add date fields to the filings object

    @Override
    public void run() {



        System.out.println("run scheduled jobs.");


        checkBlackOutPeriod();
        checkOfficeActionPeriod1();
        checkOfficeActionPeriod2();



    }



    // check submitted date + interval
    private void checkBlackOutPeriod(){


        // loop through all filings
        System.out.println("checking black out period status for filings : "+blackOutPeriodDuration);


    }



    // check submitted date + interval + office action interval
    private void checkOfficeActionPeriod1(){

        System.out.println("checking Office Action period 1 status for filings : "+firstOfficeActionDuration);

    }


    private void checkOfficeActionPeriod2(){
        System.out.println("checking Office Action period 2 status for filings : "+durationToReviveWithClaim);
    }


    public long getBlackOutPeriodDuration() {
        return blackOutPeriodDuration;
    }

    public void setBlackOutPeriodDuration(long blackOutPeriodDuration) {
        this.blackOutPeriodDuration = blackOutPeriodDuration;
    }

    public long getFirstOfficeActionDuration() {
        return firstOfficeActionDuration;
    }

    public void setFirstOfficeActionDuration(long firstOfficeActionDuration) {
        this.firstOfficeActionDuration = firstOfficeActionDuration;
    }

    public long getDurationToRevivieWithoutClaim() {
        return durationToRevivieWithoutClaim;
    }

    public void setDurationToRevivieWithoutClaim(long durationToRevivieWithoutClaim) {
        this.durationToRevivieWithoutClaim = durationToRevivieWithoutClaim;
    }

    public long getDurationToReviveWithClaim() {
        return durationToReviveWithClaim;
    }

    public void setDurationToReviveWithClaim(long durationToReviveWithClaim) {
        this.durationToReviveWithClaim = durationToReviveWithClaim;
    }
}
