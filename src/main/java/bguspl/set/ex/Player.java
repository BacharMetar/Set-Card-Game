package bguspl.set.ex;

import bguspl.set.Env;

import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    private final Dealer dealer;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    public final boolean human;

    /**
     * check if thread should be asleep
     */
    private volatile boolean isSleep = false;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    /*
     *The max size of the queue
     */
    private final int MaxSize = 3;

    /***
     * static object for locking mechanism
     */
    public static Object lock = new Object();

    public static Object lockPlayer = new Object();

    protected BlockingQueue<Integer> queue;

    protected volatile boolean point;
    protected volatile  boolean penalty;

//    public  boolean flag = false;
    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.dealer = dealer;
        this.table = table;
        this.id = id;
        this.human = human;
        this.score = 0;
        queue = new LinkedBlockingDeque<>(3);

        point = false;
        penalty = false;
        terminate = false;

    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
        //run fr Ai
        if (!human) createArtificialIntelligence();
        //run for human player

        while (!terminate) {
// TODO implement main player loop
            //wait for dealer done placing cards on table
            synchronized (dealer.dealerLock) {
                while (!dealer.cardsOnTable && !terminate) {
                    try {
                        dealer.dealerLock.wait();
                    } catch (InterruptedException e) {
                        // Handle interrupted exception
                    }
                }
            }

//            if(!table.flag) {
                if(!queue.isEmpty()) {
                    try {

                        //take the slot where I should remove/add token
                        int slot = queue.take();
                        //check if I have already 3 tokens/a token on this slot
                        if (!table.hasToken(id, slot) && table.numberOfToken[id] < 3) {
                            table.placeToken(id, slot);
                            checkSet();
                        } else {
                            table.removeToken(id, slot);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                }
            }
        }


        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * function that call the dealer to check if the 3 tokens are set
     */
    private void checkSet() {

        if (table.numberOfToken[id] == 3) {
            isSleep = true;
            dealer.addClaimSet(id);

            synchronized (lock) {
                try {

//                        System.out.println("player " + id +" is before waitingg");
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (point || penalty) {
                if(point)
                {
                    point();
                    point = false;
                }
                else
                {
                    penalty();
                    penalty = false;
                }
            }
        }

    }


    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)

        aiThread = new Thread(() -> {
            env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                // TODO implement player key press simulator
                int generateSlot = getRandomNumber(0,env.config.tableSize);
                keyPressed(generateSlot);
            }

            env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        System.out.println("player "+ id + "Thread start");
        aiThread.start();
    }





    /**
     * generate random number
     * @param min
     * @param max
     * @return
     */
    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        terminate = true;
        playerThread.interrupt();
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public synchronized void keyPressed(int slot) {
        // TODO implement

        if (!isSleep) {
            try {
                if (queue.size() < 3)
                    queue.put(slot);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement
        if (!table.flag) {
            point = true;
            try {
                env.ui.setFreeze(id, env.config.pointFreezeMillis);
                Thread.sleep( env.config.pointFreezeMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            env.ui.setFreeze(id, 0);
            isSleep = false;

            int ignored = table.countCards(); // this part is just for demonstration in the unit tests
            env.ui.setScore(id, ++score); //was ++score
        }
    }
    /**
     * Penalize a player and perform other related actions.
     */

    public void penalty() {
        // TODO implement

        penalty = true;

        try {
//
                long timeInMili =env.config.penaltyFreezeMillis;
                env.ui.setFreeze(id, (timeInMili) * 1000 );
                for (int i = 0; i < env.config.penaltyFreezeMillis /1000; i++) {
                    env.ui.setFreeze(id, (timeInMili) - (i * 1000));
                    Thread.sleep(1000);
                }
                env.ui.setFreeze(id, 0);


        } catch(InterruptedException e){
            e.printStackTrace();
        }

        isSleep = false;

    }

    public int score() {
        return score;
    }

}
