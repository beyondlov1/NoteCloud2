package com.beyond.service;

import com.beyond.f.F;

import java.util.Timer;
import java.util.TimerTask;

public class SyncService {

    private MergeService mergeService;

    private Timer timer;

    private Thread thread;

    public SyncService(){
        this.mergeService = new MergeService(F.DEFAULT_LOCAL_PATH,
                F.DEFAULT_REMOTE_PATH,
                F.DEFAULT_TMP_PATH);
        this.timer = new Timer();
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mergeService.handle();
            }
        });
    }

    public void startSynchronize() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    mergeService.handle();
                }catch (Exception e){
                    F.logger.info(e.getMessage());
                }
            }
        };
        timer.schedule(timerTask, 0, F.SYNC_PERIOD);
    }

    public void stopSynchronize() {
        timer.cancel();
    }

    public void synchronizeImmediately(){
        thread.start();
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
