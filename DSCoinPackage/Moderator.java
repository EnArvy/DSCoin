package DSCoinPackage;

import HelperClasses.*;
import java.util.*;

public class Moderator {

  public void initializeDSCoin(DSCoin_Honest DSObj, int coinCount) {
    int coinid = 100000;
    List<Transaction> myList = new ArrayList<Transaction>();
    Members Mod = new Members();
    Mod.UID = "Moderator";
    for (int i = 0; i < coinCount / DSObj.memberlist.length; i++) {
      for (int j = 0; j < DSObj.memberlist.length; j++) {
        DSObj.memberlist[j].mycoins.add(new Pair<String, TransactionBlock>(Integer.toString(coinid), null));
        Transaction temp = new Transaction();
        temp.coinID = Integer.toString(coinid);
        temp.Destination = DSObj.memberlist[j];
        temp.Source = Mod;
        temp.coinsrc_block = null;
        myList.add(temp);
        coinid++;
      }
    }
    coinid--;
    DSObj.latestCoinID = Integer.toString(coinid);
    while (!myList.isEmpty()) {
      Transaction[] templist = new Transaction[DSObj.bChain.tr_count];
      for (int i = 0; i < DSObj.bChain.tr_count; i++) {
        templist[i] = myList.remove(0);
      }
      TransactionBlock tempBlock = new TransactionBlock(templist);
      for (int l = 0; l < tempBlock.trarray.length; l++) {
        for (int h = 0; h < tempBlock.trarray[l].Destination.mycoins.size(); h++) {
          if (tempBlock.trarray[l].Destination.mycoins.get(h).first.equals(tempBlock.trarray[l].coinID)) {
            tempBlock.trarray[l].Destination.mycoins.get(h).second = tempBlock;
          }
        }
      }
      DSObj.bChain.InsertBlock_Honest(tempBlock);
    }
  }

  public void initializeDSCoin(DSCoin_Malicious DSObj, int coinCount) {
    int coinid = 100000;
    List<Transaction> myList = new ArrayList<Transaction>();
    Members Mod = new Members();
    Mod.UID = "Moderator";
    for (; coinid < coinCount + 100000; coinid++) {
      DSObj.memberlist[(coinid - 100000) % DSObj.memberlist.length].mycoins
          .add(new Pair<String, TransactionBlock>(Integer.toString(coinid), null));
      Transaction temp = new Transaction();
      temp.coinID = Integer.toString(coinid);
      temp.Destination = DSObj.memberlist[(coinid - 100000) % DSObj.memberlist.length];
      temp.Source = Mod;
      temp.coinsrc_block = null;
      myList.add(temp);
    }
    coinid--;
    DSObj.latestCoinID = Integer.toString(coinid);
    while (!myList.isEmpty()) {
      Transaction[] templist = new Transaction[DSObj.bChain.tr_count];
      for (int i = 0; i < DSObj.bChain.tr_count; i++)
        templist[i] = myList.remove(0);

      TransactionBlock tempBlock = new TransactionBlock(templist);
      for (int l = 0; l < tempBlock.trarray.length; l++) {
        for (int h = 0; h < tempBlock.trarray[l].Destination.mycoins.size(); h++) {
          if (tempBlock.trarray[l].Destination.mycoins.get(h).first.equals(tempBlock.trarray[l].coinID)) {
            tempBlock.trarray[l].Destination.mycoins.get(h).second = tempBlock;
          }
        }
      }
      DSObj.bChain.InsertBlock_Malicious(tempBlock);
    }
  }
}
