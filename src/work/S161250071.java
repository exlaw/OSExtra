package work;

import bottom.BottomMonitor;
import bottom.BottomService;
import bottom.Task;
import main.Schedule;

import java.io.IOException;

/**
 *
 * 注意：请将此类名改为 S+你的学号   eg: S161250001
 * 提交时只用提交此类和说明文档
 *
 * 在实现过程中不得声明新的存储空间（不得使用new关键字，反射和java集合类）
 * 所有新声明的类变量必须为final类型
 * 不得创建新的辅助类
 *
 * 可以生成局部变量
 * 可以实现新的私有函数
 *
 * 可用接口说明:
 *
 * 获得当前的时间片
 * int getTimeTick()
 *
 * 获得cpu数目
 * int getCpuNumber()
 *
 * 对自由内存的读操作  offset 为索引偏移量， 返回位置为offset中存储的byte值
 * byte readFreeMemory(int offset)
 *
 * 对自由内存的写操作  offset 为索引偏移量， 将x写入位置为offset的内存中
 * void writeFreeMemory(int offset, byte x)
 *
 */

/**
 *  实现思路： 是一个局部变量随便使用，但是全局存储只能用内存的系统
 *  如果确保内存够用的话，就可以直接用实例中的内存管理方式，但是不用每次去存储，这样可以节约写的次数
 *
 */


public class S161250071 extends Schedule{
    /**
     *   内存里存进程的到达时间，需要的总cpu数，剩余时间和需要的资源数量后面跟需要的资源
     *   resource 也要存在里面
     *
     *   从18*1024开始存储上一个存到的地址，128*4的系统资源表 总进程数+  进程表位置表pid×4的位置存
     */
    public static final int arriveTime_pos = 0;
    public static final int all_cpu_time = 4;
    public static final  int left_cpu_time = 8;
    public static final int resource_length = 12;
    public static final int resource_pos = 16;

    public static final int last_index = 14*1024;
    public static final int task_number = last_index+4;
    public static final int resource_begin = task_number+4;
    public static final int process_list = resource_begin + 4*128;
    public static final int pcb_begin = resource_begin+2*1024;
    public static final int free_array1_length = pcb_begin+2*2014;
    public static final int free_array1 = free_array1_length+4;
    public static final int free_array2_length = free_array1+512;
    public static final int free_array2 = free_array2_length+4;


    /**
     *
     */
    public static final int first_level = 1;
    public static final int second_level = 2;
    public static final int third_level = 5;
    public static final int forth_level = 9;
    @Override
    public void ProcessSchedule(Task[] arrivedTask, int[] cpuOperate) {
        if(arrivedTask != null && arrivedTask.length != 0){
            for(Task task : arrivedTask){
                recordTask(task, getTimeTick());
            }
        }
        /**
         *   resource 数组不能使用，那么只能存到内存中，记录每个内存变量使用的情况，只要存储一个int型的变量就可以
         */
        clearResource();
        int cpuLength = cpuOperate.length;
        int task_length = readInteger(task_number);
//        System.out.println("打印内存");
//        print();
//        System.out.println("------------------");
        /**
         *  多级反馈调度算法，三个进程队列，每个是1,2,5，7,9,1
         */
        randomSort();
        task_length = readInteger(free_array1_length);
        for(int i = 0 ;i<task_length&&cpuLength>0;i++){
            int taskID = readInteger(free_array1+i*4);
            if(!inFinished(taskID)) {
                if (useResource(taskID)) {
                    cpuOperate[--cpuLength] = taskID;
                    countDown(taskID);
                }
            }
        }
    }

    public void sort(){
        /**
         *  写一个冒泡排序
         */
        int length = readInteger(task_number);
        writeInteger(free_array1_length,0);
        for(int i = 0;i<length; i++){
            int taskID = readInteger(process_list+i*4);
            if(!inFinished(taskID)){
                writeInteger(free_array1+readInteger(free_array1_length)*4,taskID);
                writeInteger(free_array1_length,readInteger(free_array1_length)+1);
            }
        }

        length = readInteger(free_array1_length);
        for(int i = 0;i<length-1;i++){
            for(int j = 0;j<length-i-1;j++){
                int taskID = readInteger(free_array1+j*4);
                int taskID1 = readInteger(free_array1+(j+1)*4);
                if(!judgeHRRF(taskID,taskID1)){
                    writeInteger(free_array1+j*4,taskID1);
                    writeInteger(free_array1+(j+1)*4,taskID);
                }
            }
        }
        //对结果加一个随机化 递减顺序分配彩票
        for(int i = 0;i<length-2;i++) {
            int b = (int)(Math.random()*((double)(length-1-i)*(length-i)/2));
            b = getX(b);
            //然后交换b 和 i
            int taskID = readInteger(free_array1+i*4);
            int taskID1 = readInteger(free_array1+b*4);

            writeInteger(free_array1+i*4,taskID1);
            writeInteger(free_array1+b*4,taskID);
        }

    }
    public int getX(int b){
        for(int i =0;i<1000;i++){
            if((i-1)*(i-1)<=b&&i*i>=b){
                return i;
            }
        }
        return -1;
    }

    public void randomSort(){
        int length = readInteger(task_number);
        writeInteger(free_array1_length,0);
        for(int i = 0;i<length; i++){
            int taskID = readInteger(process_list+i*4);
            if(!inFinished(taskID)){
                writeInteger(free_array1+readInteger(free_array1_length)*4,taskID);
                writeInteger(free_array1_length,readInteger(free_array1_length)+1);
            }
        }
        length = readInteger(free_array1_length);
        for(int i = 0;i<length-2;i++) {
            int b = (int)(Math.random()*(length-1-i)+i);
            //然后交换b 和 i
            int taskID = readInteger(free_array1+i*4);
            int taskID1 = readInteger(free_array1+b*4);

            writeInteger(free_array1+i*4,taskID1);
            writeInteger(free_array1+b*4,taskID);
        }

    }


    public boolean judgeHRRF(int tid1,int tid2){
        int begin1 = getBeginIndex(tid1);
        double hrrf1 = ((double)getTimeTick() - (double)readInteger(begin1+arriveTime_pos))/(readInteger(begin1+left_cpu_time));
        int begin2 = getBeginIndex(tid2);
        double hrrf2 = ((double)getTimeTick() - (double)readInteger(begin2+arriveTime_pos))/(readInteger(begin2+left_cpu_time));
        return hrrf1>=hrrf2;
    }

    public boolean judgeLeast(int tid1,int tid2){
        int begin1 = getBeginIndex(tid1);
        int left1 = readInteger(begin1+left_cpu_time);
        int begin2 = getBeginIndex(tid2);
        int left2 = readInteger(begin2+left_cpu_time);
        return left1<=left2;
    }

    public boolean judgeAllTime(int tid1,int tid2){
        int begin1 = getBeginIndex(tid1);
        int left1 = readInteger(begin1+all_cpu_time);
        int begin2 = getBeginIndex(tid2);
        int left2 = readInteger(begin2+all_cpu_time);
        return left1<left2;
    }


    /**
     *  遍历打印内存中所有进程的方法
     */
    public void print(){
        int length = readInteger(task_number);
        System.out.println("长度是 "+length);
        for(int i =0;i<length;i++){
            System.out.print(readInteger(process_list+i*4)+" ");
        }
        System.out.println();
        for(int i = 0;i<length;i++){
            int taskID = readInteger(process_list+i*4);
            int begin = getBeginIndex(taskID);
            System.out.println(taskID+" "+readInteger(begin+all_cpu_time)+" "+
            readInteger(begin+arriveTime_pos)+" "+readInteger(begin+left_cpu_time));
        }
    }



    public void countDown(int tID){
        int begin = getBeginIndex(tID);
        writeInteger(begin+left_cpu_time,readInteger(begin+left_cpu_time)-1);
    }
    public boolean inFinished(int tID){
        int begin = getBeginIndex(tID);
        int left = readInteger(begin+left_cpu_time);
        if(left==0){
            return true;
        }
        else{
            return false;
        }
    }

    public int getBeginIndex(int tID){
        return readInteger(pcb_begin+tID*4);
    }

    /**
     *  全部置0 的操作
     */
    public void clearResource(){
        for(int i =0;i<129;i++){
            int condition = readInteger(resource_begin+i);
            if(condition != 0){
                writeInteger(resource_begin+i,0);
            }
        }
    }

    /**
     *  如果资源可用，就会直接全部使用
     */
    public Boolean useResource(int taskID){
        int length = readInteger(getBeginIndex(taskID)+resource_length);
        for(int i = 0;i<length;i++){
            int resource  = readInteger(getBeginIndex(taskID)+resource_pos+i*4);
            int condition = readInteger(resource_begin + resource*4);
            if(condition!=0){
                return false;
            }
        }
        for(int i = 0;i<length;i++){
            int resource  = readInteger(getBeginIndex(taskID)+resource_pos+i*4);
            writeInteger(resource_begin+resource*4,1);
        }
        return true;
    }

    private int readInteger(int beginIndex){
        int ans = 0;
        ans += (readFreeMemory(beginIndex)&0xff)<<24;
        ans += (readFreeMemory(beginIndex+1)&0xff)<<16;
        ans += (readFreeMemory(beginIndex+2)&0xff)<<8;
        ans += (readFreeMemory(beginIndex+3)&0xff);
        return ans;
    }

    private void writeInteger(int beginIndex, int value){
        writeFreeMemory(beginIndex+3, (byte) ((value&0x000000ff)));
        writeFreeMemory(beginIndex+2, (byte) ((value&0x0000ff00)>>8));
        writeFreeMemory(beginIndex+1, (byte) ((value&0x00ff0000)>>16));
        writeFreeMemory(beginIndex, (byte) ((value&0xff000000)>>24));
    }


    private void recordTask(Task task, int arrivedTime){
//        System.out.println("写入内存 "+task.tid);
        int newIndex = getNewIndex();
//        System.out.println(newIndex);
        writeInteger(newIndex+arrivedTime, arrivedTime);
        writeInteger(newIndex+all_cpu_time,task.cpuTime);
        writeInteger(newIndex+left_cpu_time, task.cpuTime);
        writeInteger(newIndex+resource_length,task.resource.length);
        for(int i =0; i< task.resource.length;i++){
            writeInteger(newIndex+resource_pos+i*4,task.resource[i]);
        }
        /**
         *  准备下一个存的时候内存的位置
         */
        writeInteger(last_index,newIndex+resource_pos+task.resource.length*4);
//        System.out.println(newIndex+resource_pos+task.resource.length*4);

        /**
         *  列表中加入
         */
        writeInteger(process_list+readInteger(task_number)*4,task.tid);

        /**
         *  更新内存的数量
         */
        writeInteger(task_number,readInteger(task_number)+1);
        /**
         *  pcb表中存入相关的位置
         */
        writeInteger(pcb_begin+task.tid*4,newIndex);
    }

    /**
     *  获取当前存储的开始位置
     */
    public int getNewIndex(){
        return readInteger(last_index);
    }


    /**
     * 执行主函数 用于debug
     * 里面的内容可随意修改
     * 你可以在这里进行对自己的策略进行测试，如果不喜欢这种测试方式，可以直接删除main函数
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // 定义cpu的数量
        int cpuNumber = 2;
        // 定义测试文件
        String filename = "src/testFile/textSample.txt";

        BottomMonitor bottomMonitor = new BottomMonitor(filename,cpuNumber);
        BottomService bottomService = new BottomService(bottomMonitor);
        Schedule schedule =  new S161250071();
        schedule.setBottomService(bottomService);

        //外部调用实现类
        for(int i = 0 ; i < 500 ; i++){
            Task[] tasks = bottomMonitor.getTaskArrived();
            int[] cpuOperate = new int[cpuNumber];

            // 结果返回给cpuOperate
            schedule.ProcessSchedule(tasks,cpuOperate);

            try {
                bottomService.runCpu(cpuOperate);
            } catch (Exception e) {
                System.out.println("Fail: "+e.getMessage());
                e.printStackTrace();
                return;
            }
            bottomMonitor.increment();
        }

        //打印统计结果
        bottomMonitor.printStatistics();
        System.out.println();

        //打印任务队列
        bottomMonitor.printTaskArrayLog();
        System.out.println();

        //打印cpu日志
        bottomMonitor.printCpuLog();


        if(!bottomMonitor.isAllTaskFinish()){
            System.out.println(" Fail: At least one task has not been completed! ");
        }
    }

}
