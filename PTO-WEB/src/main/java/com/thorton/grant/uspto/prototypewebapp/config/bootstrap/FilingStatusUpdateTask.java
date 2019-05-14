package com.thorton.grant.uspto.prototypewebapp.config.bootstrap;

import com.thorton.grant.uspto.prototypewebapp.factories.ServiceBeanFactory;

import java.util.TimerTask;

public class FilingStatusUpdateTask extends TimerTask {



     private  final ServiceBeanFactory serviceBeanFactory;

    public FilingStatusUpdateTask(ServiceBeanFactory serviceBeanFactory) {
        this.serviceBeanFactory = serviceBeanFactory;
    }

    // read properties from property file
    // for duration intervals

    // create variables

    private long blackOutPeriodDuration;  // 2 months


    private long firstOfficeActionDuration;  // 6 month



    private long durationToRevivieWithoutClaim; // 2 month

    private long durationToReviveWithClaim;   // 6 month







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
        System.out.println("checking black out period status for filings");


    }



    // check submitted date + interval + office action interval
    private void checkOfficeActionPeriod1(){

        System.out.println("checking Office Action period 1 status for filings");

    }


    private void checkOfficeActionPeriod2(){
        System.out.println("checking Office Action period 2 status for filings");
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
