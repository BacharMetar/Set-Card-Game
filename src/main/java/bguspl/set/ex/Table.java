package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.UserInterfaceSwing;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)
    /**
     * an array that represent for each player which tokens he have
     */
    protected boolean[][] tokenToplayer;

    /**
     * an array of int that represent for each player hoe many tokens he have
     */
    protected int [] numberOfToken;

    public volatile boolean flag = false;

    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */

    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        tokenToplayer = new boolean[env.config.players][env.config.tableSize];
        numberOfToken = new int[env.config.players];
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        cardToSlot[card] = slot;
        slotToCard[slot] = card;

        // TODO implement
        env.ui.placeCard(card,slot);
    }

    /**
     * Removes a card from a grid slot on the table.
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        // TODO implement

        int card = slotToCard[slot];
        slotToCard[slot] = null;
        env.ui.removeCard(slot);



    }

    /**
     * Places a player token on a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {
        // TODO implement
      if(!flag)
      {if (slotToCard[slot] != null) {
//            System.out.println("player " + player + " place token");
          tokenToplayer[player][slot] = true;
          numberOfToken[player]++;
          env.ui.placeToken(player, slot);
      }
        }
    }

    /**
     * Removes a token of a player from a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        // TODO implement
        if(!flag) {
            if (tokenToplayer[player][slot] == true) {
                tokenToplayer[player][slot] = false;
                numberOfToken[player]--;
                env.ui.removeToken(player, slot);
                return true; //return true if player's token was succcessfuly removed
            }
        }
        return false;//return false if the slot was with non player's token
    }

    /**
     * return true iff the player has a token on the specific slot
     * @param playerId
     * @param slot
     * @return
     */
    public boolean hasToken(int playerId,int slot)
    {
        return tokenToplayer[playerId][slot];
    }
    public boolean declareSet(int playerId){
        return numberOfToken[playerId] == 3;

    }


    /**
     * return an array with the players slots with tokens
     * @param playerId
     * @return
     */
    protected int[] playerSetSlot(int playerId)
    {
        int [] result = new int[3];
        int i = 0;
        int index = 0;
        while (index < tokenToplayer[playerId].length)
        {
            if(tokenToplayer[playerId][index])
            {
                result[i] = index;
                i++;
            }
            index++;
        }

        return result;
    }

    /**
     * return an array of integer with the players card(where he put tokens)
     * @param id
     * @return
     */
    protected  int[] getPlayerTokenCards(int id)
    {
        int [] slots = playerSetSlot(id);
        int [] cards = new int[3];
        for (int i = 0; i < cards.length; i++) {
            int index = slots[i];
            if(slotToCard[index] != null) {
                cards[i] = slotToCard[index];
            }
        }

        return cards;

    }

    /**
     * removes the player's tokens from table
     * @param playerId
     */
    protected void removePlayerToken(int playerId)
    {
        int [] arr = playerSetSlot(playerId);
        for (int i = 0; i < arr.length; i++) {
            tokenToplayer[playerId][arr[i]] = false;
            env.ui.removeToken(playerId,arr[i]);
        }
        numberOfToken[playerId] = 0;

    }

    protected void removeSetPlayersToken(int [] slotsArray)
    {
        for (int i = 0; i < slotsArray.length; i++) {
            for (int j = 0; j < tokenToplayer.length; j++) {
                if (tokenToplayer[j][slotsArray[i]] = true) {
                    tokenToplayer[j][slotsArray[i]] = false;
                    numberOfToken[j] --;
                    env.ui.removeToken(j, slotsArray[i]);
                }
            }
        }
    }
    protected void removeTokenFromSlot(int slot)
    {
        for (int i = 0; i < env.config.players; i++) {
            if(tokenToplayer[i][slot])
            {
                tokenToplayer[i][slot] = false;
                numberOfToken[i] = numberOfToken[i] - 1;
            }
            env.ui.removeTokens(slot);
        }
    }

    protected void  removeAllTokensFromTable()
    {
        for (int i = 0; i < numberOfToken.length; i++) {
            numberOfToken[i] = 0;
        }
        for (int i = 0; i < tokenToplayer.length; i++) {
            for (int j = 0; j < tokenToplayer[i].length; j++) {
                tokenToplayer[i][j] = false;

            }

        }
        for (int i = 0; i < env.config.tableSize; i++) {
            env.ui.removeTokens(i);

        }

    }





}