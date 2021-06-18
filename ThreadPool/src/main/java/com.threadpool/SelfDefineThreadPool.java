package com.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 自定义线程池的实现
 *
 * 1、任务队列，存放提交的任务
 * 2、线程队列，执行任务的线程
 * 3、线程处理任务队列中的任务，队列为空，则阻塞线程池中的线程，新增任务则唤醒，
 * 4、拒绝策列，当任务队列满的时候（或者到达阈值）则启用拒绝策略。
 *
 * 5 todo 扩容缩容线程队列中的线程 （也就是线程中的 maxPoolSize参数），当任务多的时候预设线程不够时就在创建一批， 任务减少后 则销毁超出的线程
 *
 * @Author: tangwq
 */
public class SelfDefineThreadPool extends Thread{

    private int currentSize;

    //默认值
    private static final int DEFAULT_SIZE = 4;

    //默认最大任务数
    private  static final int DEFAULT_TASK_QUEUE_SIZE = 30;

    // 最大任务数
    private int maxTaskQueueSize;

    //任务队列
    private static final LinkedList<Runnable> TASKQUEUE= new LinkedList<>();

    //线程队列
    private static final List<WorkerThread> THREADQUEUE =  new ArrayList<>();

    // 线程池是否已关闭
    private boolean isDistory = false;

    // 默认拒绝策略。 直接用lamada表达式 写一个函数即可 （实际上这个函数就是拒绝策略接口DisCardPolicy中方法的实现。）。
    private static DisCardPolicy DEFAULT_POLICY = ()->{
      throw new RuntimeException("任务超出预期！");
    };

    private DisCardPolicy disCardPolicy;

    // 线程状态枚举 空闲、阻塞、任务中、死亡
    private enum WorkerThreadState{
        FREE,BLOCK,RUNNING,DEAD
    }

    /**
     * 默认构造函数
     */
    public SelfDefineThreadPool(){
        this.currentSize = DEFAULT_SIZE;
        this.disCardPolicy = DEFAULT_POLICY;
        this.maxTaskQueueSize = DEFAULT_TASK_QUEUE_SIZE;
    }

    /**
     * 含参的构造函数
     * @param poolSize 线程池大小
     * @param disCardPolicy 拒绝策略
     */
    public SelfDefineThreadPool(int poolSize, DisCardPolicy disCardPolicy, int maxQueueSize){
        currentSize = poolSize;
        this.disCardPolicy = disCardPolicy;
        this.maxTaskQueueSize = maxQueueSize;
    }


    public void initThreadPool(){
        for (int i = 0; i < currentSize; i++) {
            createThread();
        }
    }

    /**
     * 创建线程，并加入到线程队列中
     */
    public void createThread(){
        WorkerThread workerThread = new WorkerThread();
        workerThread.start(); //启动线程 start之后 会自动调用run方法
        // 加入到线程队列中
        THREADQUEUE.add(workerThread);
    }

    public void shutdown() throws InterruptedException {
        // 优雅关闭，等一段时间再关闭
        while(!TASKQUEUE.isEmpty()){
            Thread.sleep(500);
        }
        // 关闭线程，并发下需要对线程队列加锁，线程安全
        synchronized (THREADQUEUE){
            int threadSize = THREADQUEUE.size();
            while(threadSize>0){
                for(WorkerThread workerThread:THREADQUEUE){ //
                    if(workerThread.state== WorkerThreadState.BLOCK){ //如果线程的状态是阻塞状态
                        workerThread.interrupt(); // 中断该线程（从阻塞状态跳出）（会执行中断异常。run方法里面catch的异常）
                        workerThread.close(); // 关闭
                        --threadSize;
                    }else if(workerThread.state== WorkerThreadState.FREE){ //线程空闲时 直接关闭
                        workerThread.close(); // 关闭
                        --threadSize;
                    }else{ //线程在跑任务时
                        Thread.sleep(500);
                    }
                }
            }
        }

        isDistory=true;
        System.out.println("线程池已关闭");
    }

    /**
     * 提交任务到任务队列
     */
    public void submit(Runnable task){
        synchronized (TASKQUEUE){ //并发下 也需要加锁去操作
            // 判断任务队列中的任务数是否超出阈值, 超出则拒绝
            if(TASKQUEUE.size()>maxTaskQueueSize){
                this.disCardPolicy.discard(); //执行拒绝策略
            }
            TASKQUEUE.addLast(task);

            //唤醒所有阻塞的线程 ， 可以去执行队列中的任务
            TASKQUEUE.notifyAll();
        }

    }

    /**
     * 拒绝策略接口
     */
    public interface DisCardPolicy{
        void discard();
    }

    /**
     * 扩容方法
     * 将扩容和缩容写在线程的run方法里面，
     * 相当于开启一个线程实时监控任务的变化 达到动态扩容
     */
    @Override
    public void run() {
        while(!isDistory){
            System.out.println( );
        }
    }

    /**
             * 定义工作线程
             */
    class WorkerThread extends Thread{
        //线程状态
        WorkerThreadState state;

        @Override
        public void run() {
            OUTER:
            while(state!= WorkerThreadState.DEAD){ //线程没有死亡 则一直可以运行任务
                Runnable task;
                synchronized (TASKQUEUE){  // 并发下，Linklist不是线程安全的，所以要加锁
                    while(TASKQUEUE.isEmpty()){ //如果任务队列是空的，则线程wait 等待被唤醒
                        try {
                            this.state = WorkerThreadState.BLOCK;
                            TASKQUEUE.wait(); // 使该线程进入等待状态。 注意 wait方法是让调用这个方法所在的线程 wait 不是 TASKQUEUE去wait。
                        } catch (InterruptedException e) { //中断异常
                            //e.printStackTrace(); // 没必要打印
                            break OUTER; //跳出循环到OUTER处，直接跳到最外面
                        }
                    }

                    // 唤醒后就代表 有任务可以执行了
                    task = TASKQUEUE.remove(); //取出该线程并从队伍中移除
                }
                // 注意 执行任务的部分不需要同步， 他们是并发执行的
                if(task!=null){
                    this.state = WorkerThreadState.RUNNING; // 设置状态为运行中
                    task.run(); // 运行任务
                    this.state = WorkerThreadState.FREE; // 设置状态为空闲
                }
            }

        }

                /**
                 *关闭线程 （这里目前是逻辑关闭，将状态置为DEAD）
                 */
        public void close(){
            this.state = WorkerThreadState.DEAD;
        }
    }



    public static void main(String[] args) throws InterruptedException {
        SelfDefineThreadPool selfDefineThreadPool = new SelfDefineThreadPool();
        selfDefineThreadPool.initThreadPool();


        IntStream.rangeClosed(0,10).forEach(i->{
            //提交任务到线程池
            selfDefineThreadPool.submit(()->{
                try {
                    Thread.sleep(1000);
                    System.out.println(">>>>>>>>>>>>>>>>当前线程："+Thread.currentThread()+"处理任务"+i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


        });
        System.out.println("1111111");
        Thread.sleep(10000);
        selfDefineThreadPool.shutdown();
    }

}
