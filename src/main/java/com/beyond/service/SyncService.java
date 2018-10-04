package com.beyond.service;

import com.beyond.f.F;

import java.util.Timer;
import java.util.TimerTask;

public class SyncService {

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
                mergeService.handle();
            }
        };
        timer.schedule(timerTask, 0, F.SYNC_PERIOD);
    }

    public void stopSynchronize() {
        timer.cancel();
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
