package com.beyond.service;

import com.beyond.f.F;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class SyncService implements Observer{

    private MergeService mergeService;

    private Timer timer;

    public SyncService(){
        this.mergeService = new MergeService(F.DEFAULT_LOCAL_PATH,
                F.DEFAULT_REMOTE_PATH,
                F.DEFAULT_TMP_PATH);
        this.timer = new Timer();
    }

    public void startSynchronize() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                synchronize();
            }
        };
        timer.schedule(timerTask, 0, F.SYNC_PERIOD);
    }

    public void stopSynchronize() {
        timer.cancel();
    }

    public synchronized void synchronizeImmediately(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronize();
            }
        });
        thread.start();
    }

    private void synchronize() {
        try {
            mergeService.handle();
            onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            F.logger.info(e.getMessage());
            onFail();
        }
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

    @Override
    public void update(Observable o, Object arg) {
        synchronizeImmediately();
    }
}
