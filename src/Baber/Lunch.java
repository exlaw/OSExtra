package Baber;

public class Lunch{
    public static int waitings = 0;
    public static int chairs = 4;
    public static Task babers = new Task(0),customers = new Task(0),mutex = new Task(1);

    class Baber implements Runnable {
        @Override
        public void run() {
            while(true){
                PV.P(customers);
                PV.P(mutex);
                Lunch.waitings--;
                PV.V(babers);
                PV.V(mutex);
                System.out.println(Thread.currentThread().getName());
            }
        }
    }

    class Customer implements Runnable{
        @Override
        public void run() {
            PV.P(mutex);
            if(waitings<chairs){
                waitings++;
                PV.V(customers);
                PV.V(mutex);
                PV.P(babers);
                System.out.println(Thread.currentThread().getName());
            }else{
                PV.V(mutex);
                System.out.println("顾客离开了");
            }

        }
    }

    public void lunch(){
        new Thread(new Baber(),"baber").start();
        for(int i = 0;i<100;i++){
            new Thread(new Customer(),"customer "+i).start();
        }
    }

    public static void main(String[] args){
        new Lunch().lunch();
    }
}


