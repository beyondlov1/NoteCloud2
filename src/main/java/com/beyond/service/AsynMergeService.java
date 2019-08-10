package com.beyond.service;

import com.beyond.ApplicationContext;
import com.beyond.f.F;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class AsynMergeService {

    private MergeService mergeService;

    private Timer timer;

    public AsynMergeService(ApplicationContext context){
        this.mergeService = new MergeService(F.DEFAULT_LOCAL_PATH,
                F.DEFAULT_REMOTE_PATH,
                F.DEFAULT_TMP_PATH,context);
    }

    public void startSynchronize() {
        this.timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    F.logger.info("synchronize begin");
                    mergeService.handle();
                    F.logger.info("synchronize end");
                }catch (Exception e){
                    F.logger.info("merge fail",e);
                }

            }
        };
        timer.schedule(timerTask, 0, F.SYNC_PERIOD);
    }

    public void stopSynchronize() {
        if (timer!=null){
            timer.cancel();
        }
    }

    public synchronized void synchronizeImmediately(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mergeService.handle();
            }
        });
        thread.start();
    }

    protected void onSuccess(){
    }

    protected void onFail(){

    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public MergeService getMergeService() {
        return mergeService;
    }

    public void setMergeService(MergeService mergeService) {
        this.mergeService = mergeService;
    }

}
