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



}
