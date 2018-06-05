package Baber;

public class PV {

    public static void P(Task task){
        synchronized (task) {
            task.number--;
            if (task.number < 0) {
                try {
                    task.wait();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void V(Task task){
        synchronized (task){
            task.number++;
            if(task.number<=0){
                task.notify();
            }
        }
    }

}
