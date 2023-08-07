package bguspl.set.ex;

import bguspl.set.Env;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.lang.*;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    /**
     * queue for all players that claim for set-in order
     */
    private BlockingQueue<Integer> claimSet;

    /***
     * static object for locking mechanism
     */
    private static Object lock = new Object();

    //added
    public final Object dealerLock = new Object();
    public boolean cardsOnTable = false;

    private Thread[] playerThreads;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
//        deck = IntStream.range(0, 13).boxed().collect(Collectors.toList());
        claimSet = new LinkedBlockingDeque<>(players.length);

        playerThreads = new Thread[players.length];
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
        createPlayersThread();// creating for each player a thread
        while (!shouldFinish()) {

            placeAllCardsOnTable();
//            placeCardsOnTable();
            System.out.println("Before Notify all player");
//            notityAllPlayersThreads();// notify all players that the table is rady for finding set
            setPlayersFlagFalse();
            System.out.println("After!!!!! notify");
            timerLoop();
            updateTimerDisplay(true);
            removeAllCardsFromTable();
            System.out.println("All players sleep");

        }
        announceWinners();
        env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
    }


    /**
     * func for creating players threads
     */
    private void createPlayersThread()
    {
        for (int i = 0; i < playerThreads.length; i++) {
            Thread playerThread = new Thread(players[i]);
            playerThreads[i] = playerThread;
            playerThreads[i].start();
        }

    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        reshuffleTime = env.config.turnTimeoutMillis + System.currentTimeMillis(); //time to reshuffle the deck

        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            checkSets();
        }

        setPlayersFlagTrue();

    }
    private void setPlayersFlagFalse()
    {

        table.flag = false;
    }
    private void setPlayersFlagTrue()
    {
        table.flag=true;
    }

    public BlockingQueue<Integer> getClaimSet(){
        return claimSet;
    }

    public void checkSets()
    {
        if (!claimSet.isEmpty()) //means there is an id of player that claim for set
        {
            int id = 0;
            synchronized (this) {
                if (checkForLegalSet())//legal set;
                {

                    id = claimSet.element();
                    players[id].point = true;//adding point to player + 1 second freeze
                    removeCardsFromTable();
                    placeCardsOnTable();
                    updateTimerDisplay(true);

                } else //illegal set
                {
                    try {
                        id = claimSet.take();
                        players[id].penalty = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                synchronized (players[id].lock) {
                    players[id].lock.notify();
                }
            }
        }
    }





    /**
     * boolean function that checks for legal set of the top id from claimSet queue
     * @return
     */

    private boolean checkForLegalSet() {
        int id = claimSet.element();
        int[] cards = table.getPlayerTokenCards(id);
        boolean isSet = env.util.testSet(cards);
        return isSet;
    }
    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        //1 - deck is empty
        //2 - no more set- both the deck and the table
        List<int[]> sets ;
        sets = new LinkedList<>();
        sets = env.util.findSets(deck, 1);
        int size = sets.size();
        if (deck.isEmpty() || size == 0) {
            terminate = true;


            for (int i = 0; i < players.length; i++) {
                synchronized (players[i].lock){
                    players[i].lock.notifyAll();
                }
                players[i].terminate();
            }
            synchronized (dealerLock) {
                dealerLock.notifyAll();
            }
        }
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement
        try {
            System.out.println("RemoveCards from table startedd");
            int id = claimSet.take();//the player id we should remove set's card from table
            int[] setSlot = table.playerSetSlot(id); //gets the players slots with the tokens
            for (int i = 0; i < setSlot.length; i++) {
                int index = setSlot[i];
                table.slotToCard[index] = null;
                table.removeTokenFromSlot(index);
                env.ui.removeCard(index);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void placeAllCardsOnTable() {
        // TODO implement


        if (deck.size() > 0)
        {
            for (int i = 0; i < env.config.tableSize; i++) {
                if (deck.isEmpty()) //if no more card on the deck
                {
                    break;
                } else if (table.slotToCard[i] == null) // card per slot (if any)
                {
                    boolean flag = false; //check if the random number is at the deck
                    //generate a random number
                    int result = getRandomNumber(0,deck.size());

                    table.placeCard(deck.get(result), i);
                    deck.remove(result);

                }
            }

        }
        cardsOnTable = true;
        // Notify all waiting player threads
        synchronized (dealerLock) {
            dealerLock.notifyAll();
        }
    }
    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    public void placeCardsOnTable() {
        // TODO implement


        if (deck.size() > 0)
        {
            for (int i = 0; i < env.config.tableSize; i++) {
                if (deck.isEmpty()) //if no more card on the deck
                {
                    break;
                } else if (table.slotToCard[i] == null) // card per slot (if any)
                {
                    boolean flag = false; //check if the random number is at the deck
                    //generate a random number
                    int result = getRandomNumber(0,deck.size());

                    table.placeCard(deck.get(result), i);
                    deck.remove(result);
                }
            }

        }
    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * method for update the claimSet queue
     *
     * @param id
     * @throws InterruptedException
     */
    protected synchronized void addClaimSet(int id) {
        try {
            claimSet.put(id);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */

    private void sleepUntilWokenOrTimeout() {
        // TODO implement
        synchronized (lock) {
            try {
                lock.wait(10);

            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay ( boolean reset){
        // TODO implement
        if (!reset) {
            if (reshuffleTime - System.currentTimeMillis() <= 5000) {
                env.ui.setCountdown(reshuffleTime - System.currentTimeMillis(), true);
            } else
                env.ui.setCountdown(reshuffleTime - System.currentTimeMillis(), false);
        }
        else {
            reshuffleTime = env.config.turnTimeoutMillis + System.currentTimeMillis();
            env.ui.setCountdown(env.config.turnTimeoutMillis, false);
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    public void removeAllCardsFromTable () {
        // TODO implement
        cardsOnTable = false;
        while (!claimSet.isEmpty()) {
            checkSets();
        }
        // place the thread on waiting status until the table will be ready
        //handling removing the card and re insert them to the deck
        for (int i = 0; i < env.config.tableSize; i++) {
            if (table.slotToCard[i] != null) {
                int card = table.slotToCard[i];
                table.removeCard(i);
                deck.add(card);
            }
            table.removeAllTokensFromTable();



            terminate();//call terminate function

        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners () {
        // TODO implement
        LinkedList<Integer> res = new LinkedList<>();
        int maxScoreId = 0;
        res.add(maxScoreId);
        for (int i = 1; i < players.length; i++) {
            if (players[maxScoreId].score() < players[i].score()) {
                maxScoreId = i;
                LinkedList<Integer> temp = new LinkedList<>();
                temp.add(i);
                res = temp;
            }
            else if(players[maxScoreId].score() == players[i].score() )
            {
                res.add(i);
            }
        }
        int [] players = new int[res.size()];
        for (int i = 0; i < players.length; i++) {
            players[i] = res.get(i);
        }

        env.ui.announceWinner(players);


    }

}
