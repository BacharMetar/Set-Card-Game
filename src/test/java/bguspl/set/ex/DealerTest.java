package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DealerTest {

    Table table;
    Dealer dealer;
    private Integer[] slotToCard;
    private Integer[] cardToSlot;
    private Player[] players;
    private Env env;

    @BeforeEach
    void setUp() {

        Properties properties = new Properties();
        properties.put("Rows", "2");
        properties.put("Columns", "2");
        properties.put("FeatureSize", "3");
        properties.put("FeatureCount", "4");
        properties.put("TableDelaySeconds", "0");
        properties.put("PlayerKeys1", "81,87,69,82");
        properties.put("PlayerKeys2", "85,73,79,80");
        TableTest.MockLogger logger = new TableTest.MockLogger();
        Config config = new Config(logger, properties);
        slotToCard = new Integer[config.tableSize];
        cardToSlot = new Integer[config.deckSize];
        players = new Player[config.players] ;
        Env env = new Env(logger, config, new TableTest.MockUserInterface(), new TableTest.MockUtil());
        table = new Table(env, slotToCard, cardToSlot);

        dealer = new Dealer(env,table,players);
    }


    private int fillSomeSlots() {
        slotToCard[1] = 3;
        slotToCard[2] = 5;
        cardToSlot[3] = 1;
        cardToSlot[5] = 2;

        return 2;
    }

    private void fillAllSlots() {
        for (int i = 0; i < slotToCard.length; ++i) {
            slotToCard[i] = i;
            cardToSlot[i] = i;
        }
    }


    @AfterEach
    void tearDown() {
    }

    @Test
    void run() {
    }

    @Test
    void checkSets() {
    }

    @Test
    void terminate() {
    }

    @Test
    void addClaimSet() {
    }

//@inv dealer.table !=null
//@pre exist slots with null
//@post not slot is null
    @Test
    void testPlaceCardsOnTable()
    {
        fillSomeSlots();
        dealer.placeCardsOnTable();
        boolean flag = false;
        for (int i = 0; i < slotToCard.length; i++) {
            if(slotToCard[i] == null)
            {
                flag = true;
                break;
            }
        }
        assertEquals(false,flag,"should be false");
    }

    //@inv dealer.getClaimSet().size() <=3
    //     dealer.getClaimSet().size() >=0
    //@pre dealer.getClaimSet() !=null
    //@post dealer.getClaimSet().size() = dealer.getClaimSet().size()
    //      dealer.getClaimSet().size() = dealer.getClaimSet().size() + 1

    @Test
    void testAddClaimSet(){
        if(dealer.getClaimSet().size()!= 3)
        {
            int size = dealer.getClaimSet().size();
            dealer.addClaimSet(0);
            assertEquals(dealer.getClaimSet().size(),size + 1 ,"size not the same");

        }
        else
        {

            dealer.addClaimSet(0);
            assertEquals(dealer.getClaimSet().size(),3  ,"size not the same");

        }
    }


}