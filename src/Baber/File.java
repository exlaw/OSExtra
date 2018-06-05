package Baber;

import PVTest.WaitNotifyModel;

/**
 *  读者优先版本，只有读者的数量是0，才可以去写
 */
public class File {
    public static Task rmutex = new Task(1);
    public static Task wmutex = new Task(1);
    public static Task S = new Task(1);
    public static int readcount = 0;
    class Reader implements  Runnable{
        @Override
        public void run() {
            while(true) {
                PV.P(rmutex);
                if (readcount == 0) PV.P(wmutex);
                readcount++;
                System.out.println("start "+Thread.currentThread().getName());
                PV.V(rmutex);
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                PV.P(rmutex);
                readcount--;
                System.out.println("end "+Thread.currentThread().getName());
                if (readcount == 0) PV.V(wmutex);
                PV.V(rmutex);
            }
        }
    }
    class writer implements Runnable{

        @Override
        public void run() {
            while(true){
                PV.P(wmutex);
                System.out.println(Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                PV.V(wmutex);
            }
        }
    }

    public void lunch(){
        for(int i = 0;i<3;i++){
            new Thread(new writer(),"writer "+i).start();
        }

        for(int i = 0;i<1;i++){
            new Thread(new Reader(),"reader "+i).start();
        }
    }

    public static void main(String[] args){
        new File().lunch();
    }



}
