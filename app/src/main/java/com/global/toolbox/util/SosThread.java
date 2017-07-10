package com.global.toolbox.util;


/**
 * 控制sos灯光
 */
public class SosThread extends Thread {

    public void stopThread() {
        running = false;
    }

    private boolean running = true;

    private IFlashControl mFlashControl;

    public SosThread(IFlashControl flashControl) {
        super(SosThread.class.getSimpleName());
        this.setDaemon(true);
        mFlashControl = flashControl;
    }

    @Override
    public void run() {
        super.run();
        int time = 0;
        try {
            // 灭1300、亮200、灭200、亮200、灭200、亮200、灭500、亮400、灭200、亮400、灭200、亮400、灭500、亮200、灭200、亮200、灭200、亮200、 （MS）循环
            while (running) {
                time = time % 18;
                if ((time % 2) == 0) {
                    if (mFlashControl != null) mFlashControl.closeFlash();
                } else {
                    if (mFlashControl != null) mFlashControl.openFlash();
                }
                if (time == 0) {
                    sleep(1300);
                } else if (time == 6 || time == 12) {
                    sleep(500);
                } else if (time == 7 || time == 9 || time == 11) {
                    sleep(400);
                } else {
                    sleep(200);
                }
                time ++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
