package DSCoinPackage;

import java.util.*;
import HelperClasses.*;

public class Members {

	public String UID;
	public List<Pair<String, TransactionBlock>> mycoins;
	public Transaction[] in_process_trans;

	public void initiateCoinsend(String destUID, DSCoin_Honest DSobj) {
		Pair<String, TransactionBlock> temp = mycoins.remove(0);
		Transaction tobj = new Transaction();
		tobj.coinID = temp.get_first();
		tobj.coinsrc_block = temp.get_second();
		tobj.Source = this;
		for (int i = 0; i < DSobj.memberlist.length; i++)
			if (DSobj.memberlist[i].UID.equals(destUID)) {
				tobj.Destination = DSobj.memberlist[i];
				break;
			}
		for (int i = 0; i < in_process_trans.length; i++)
			if (in_process_trans[i] == null) {
				in_process_trans[i] = tobj;
				break;
			}
		DSobj.pendingTransactions.AddTransactions(tobj);
	}

	public Pair<List<Pair<String, String>>, List<Pair<String, String>>> finalizeCoinsend(Transaction tobj,DSCoin_Honest DSObj) throws MissingTransactionException {
		TransactionBlock temp = DSObj.bChain.lastBlock;
		int k = 0;
		List<TransactionBlock> Blocks = new ArrayList<TransactionBlock>();
		int ff = 0;
		//to escape java compire flagging infinite loop
		boolean True = true;
		while (True) {
			Blocks.add(0, temp);
			for (k = 0; k < temp.trarray.length; k++) {
				if (temp.trarray[k].Source != null)
					if (temp.trarray[k].coinID.equals(tobj.coinID) && temp.trarray[k].Source.equals(tobj.Source)
							&& temp.trarray[k].Destination.equals(tobj.Destination)) {
						ff = 1;
						break;
					}
			}
			if (ff == 1)
				break;
			temp = temp.previous;
			if (temp == null)
				throw new MissingTransactionException();
		}
		int height = 1;
		int tempss = temp.trarray.length;
		while (tempss > 1) {
			tempss /= 2;
			height += 1;
		}
		int left = 0;
		int right = temp.trarray.length - 1;
		TreeNode curr = temp.Tree.rootnode;
		for (int j = 0; j < height - 1; j++) {
			if (k > (left + right) / 2) {
				curr = curr.right;
				left = (left + right) / 2 + 1;
			} else {
				curr = curr.left;
				right = (right + left) / 2;
			}
		}
		List<Pair<String, String>> Path = new ArrayList<Pair<String, String>>();
		while (curr != temp.Tree.rootnode) {
			Path.add(new Pair<String, String>(curr.parent.left.val, curr.parent.right.val));
			curr = curr.parent;
		}
		Path.add(new Pair<String, String>(curr.val, null));
		List<Pair<String, String>> Path2 = new ArrayList<Pair<String, String>>();
		if (temp.previous == null) {
			Path2.add(new Pair<String, String>(BlockChain_Honest.start_string, null));
			Path2.add(new Pair<String, String>(temp.dgst,
					BlockChain_Honest.start_string + "#" + temp.trsummary + "#" + temp.nonce));
		} else {
			Path2.add(new Pair<String, String>(temp.previous.dgst, null));
			Path2.add(
					new Pair<String, String>(temp.dgst, temp.previous.dgst + "#" + temp.trsummary + "#" + temp.nonce));
		}
		for (int i = 1; i < Blocks.size(); i++)
			Path2.add(new Pair<String, String>(Blocks.get(i).dgst,
					Blocks.get(i).previous.dgst + "#" + Blocks.get(i).trsummary + "#" + Blocks.get(i).nonce));
		int index = 0;
		for (int i = 0; i < in_process_trans.length && (in_process_trans[i] != null); i++) {
			if (in_process_trans[i].equals(tobj))
				break;
			else
				index++;
		}
		for (int i = index; i < in_process_trans.length - 1; i++)
			in_process_trans[i] = in_process_trans[i + 1];
		in_process_trans[in_process_trans.length - 1] = null;
		if (Long.parseLong(tobj.Destination.mycoins.get(0).get_first()) > Long.parseLong(tobj.coinID))
			tobj.Destination.mycoins.add(0, new Pair<String, TransactionBlock>(tobj.coinID, temp));
		else if (Long.parseLong(tobj.Destination.mycoins.get(tobj.Destination.mycoins.size() - 1).get_first()) < Long
				.parseLong(tobj.coinID))
			tobj.Destination.mycoins.add(new Pair<String, TransactionBlock>(tobj.coinID, temp));
		else {
			for (int i = 0; i < tobj.Destination.mycoins.size() - 1; i++) {
				if ((Long.parseLong(tobj.Destination.mycoins.get(i).get_first()) < Long.parseLong(tobj.coinID)) && (Long
						.parseLong(tobj.Destination.mycoins.get(i + 1).get_first()) > Long.parseLong(tobj.coinID))) {
					tobj.Destination.mycoins.add(i + 1, new Pair<String, TransactionBlock>(tobj.coinID, temp));
					break;
				}
			}
		}
		return new Pair<List<Pair<String, String>>, List<Pair<String, String>>>(Path, Path2);
	}

	public void MineCoin(DSCoin_Honest DSObj) {
		Transaction[] myList = new Transaction[DSObj.bChain.tr_count];
		for (int i = 0; i < DSObj.bChain.tr_count - 1; i++) {
			Transaction temp = null;
			try {
				temp = DSObj.pendingTransactions.RemoveTransaction();
			} catch (EmptyQueueException E) {
			}
			int flag = 0;
			for (int j = 0; myList[j] != null; j++) {
				if (myList[j].coinID.equals(temp.coinID)) {
					flag = 1;
					i--;
					break;
				}
			}
			if ((flag != 1) && (!DSObj.bChain.lastBlock.checkTransaction(temp))) {
				i--;
				flag = 1;
			}
			if (flag != 1)
				myList[i] = temp;
		}
		Transaction minerRewardTransaction = new Transaction();
		minerRewardTransaction.coinID = Long.toString(Long.parseLong(DSObj.latestCoinID) + 1);
		minerRewardTransaction.Source = null;
		minerRewardTransaction.Destination = this;
		minerRewardTransaction.coinsrc_block = null;
		myList[DSObj.bChain.tr_count - 1] = minerRewardTransaction;
		TransactionBlock tB = new TransactionBlock(myList);
		DSObj.bChain.InsertBlock_Honest(tB);
		mycoins.add(new Pair<String, TransactionBlock>(minerRewardTransaction.coinID,minerRewardTransaction.coinsrc_block));
		DSObj.latestCoinID = minerRewardTransaction.coinID;
	}

	public void MineCoin(DSCoin_Malicious DSObj) {
		Transaction[] myList = new Transaction[DSObj.bChain.tr_count];
		TransactionBlock longestValidBlock = DSObj.bChain.FindLongestValidChain();
		for (int i = 0; i < DSObj.bChain.tr_count - 1; i++) {
			Transaction temp = null;
			;
			try {
				temp = DSObj.pendingTransactions.RemoveTransaction();
			} catch (EmptyQueueException E) {
			}
			int flag = 0;
			for (int j = 0; myList[j] != null; j++) {
				if (myList[j].coinID.equals(temp.coinID)) {
					i--;
					flag = 1;
					break;
				}
			}
			if (flag != 1) {
				if (!longestValidBlock.checkTransaction(temp)) {
					flag = 1;
					i--;
				}
			}
			if (flag != 1) {
				myList[i] = temp;
			}
		}
		Transaction minerRewardTransaction = new Transaction();
		minerRewardTransaction.coinID = Long.toString(Long.parseLong(DSObj.latestCoinID) + 1);
		minerRewardTransaction.Source = null;
		minerRewardTransaction.Destination = this;
		minerRewardTransaction.coinsrc_block = null;
		myList[DSObj.bChain.tr_count - 1] = minerRewardTransaction;
		TransactionBlock tB = new TransactionBlock(myList);
		DSObj.bChain.InsertBlock_Malicious(tB);
		mycoins.add(new Pair<String, TransactionBlock>(minerRewardTransaction.coinID,
				minerRewardTransaction.coinsrc_block));
		DSObj.latestCoinID = minerRewardTransaction.coinID;
	}
}
