package DSCoinPackage;

import HelperClasses.CRF;
import java.util.ArrayList;
import java.util.List;

public class BlockChain_Malicious {

	public int tr_count;
	public static final String start_string = "DSCoin";
	public TransactionBlock[] lastBlocksList;

	public static boolean checkTransactionBlock(TransactionBlock tB) {
		//if digest begins with 0000
		if (!(tB.dgst.substring(0, 4).equals("0000")))
			return false;
		CRF myCRF = new CRF(64);
		String temp;
		if (tB.previous == null) {
			temp = start_string;
		} else {
			temp = tB.previous.dgst;
		}
		//if dgst matches crf  of attributes
		if (!(tB.dgst.equals(myCRF.Fn(temp + "#" + tB.trsummary + "#" + tB.nonce)))) {
			return false;
		}
		temp = tB.trsummary;
		//if trsummary is computed correctly
		if (!(temp.equals(tB.Tree.Build(tB.trarray)))) {
			return false;
		}
		//if all transactions are valid
		for (int i = 0; i < tB.trarray.length; i++) {
			if (!checkTransactions(tB.trarray[i], tB)) {
				return false;
			}
		}
		return true;
	}

	public TransactionBlock FindLongestValidChain() {
		//list of lastblocks
		List<TransactionBlock> templ = new ArrayList<TransactionBlock>();
		for (int i = 0; i < lastBlocksList.length; i++) {
			if (lastBlocksList[i] != null)
				templ.add(lastBlocksList[i]);
		}
		TransactionBlock[] templl = new TransactionBlock[templ.size()];
		for (int i = 0; i < templl.length; i++)
			templl[i] = templ.remove(0);
		if (templl.length == 0)
			return null;
		//list of validblock ends and their sizes
		ArrayList<TransactionBlock> validBlocksList = new ArrayList<TransactionBlock>();
		ArrayList<Integer> validBlocksLengths = new ArrayList<Integer>();
		for (int i = 0; i < templl.length; i++) {
			TransactionBlock temp = templl[i];
			TransactionBlock temp2 = templl[i];
			int count = 0;
			while (true) {
				if (checkTransactionBlock(temp2))
					count++;
				else {
					count = 0;
					temp = temp2.previous;
				}
				temp2 = temp2.previous;
				if (temp2 == null) {
					validBlocksList.add(temp);
					validBlocksLengths.add(count);
					break;
				}
			}
		}
		//find longest valid blocks list
		int index = 0;
		for (int i = 0; i < validBlocksLengths.size(); i++) {
			if (validBlocksLengths.get(i) > validBlocksLengths.get(index))
				index = i;
		}
		return validBlocksList.get(index);
	}

	public void InsertBlock_Malicious(TransactionBlock newBlock) {
		TransactionBlock lastBlock = this.FindLongestValidChain();
		CRF myCRF = new CRF(64);
		if (lastBlock == null) {
			Long i = 1000000001l;
			for (; Long.compare(i, 9999999999l) <= 0; i++) {
				if (myCRF.Fn(start_string + "#" + newBlock.trsummary + "#" + Long.toString(i)).substring(0, 4)
						.equals("0000")) {
					newBlock.nonce = Long.toString(i);
					break;
				}
			}
			newBlock.dgst = myCRF.Fn(start_string + "#" + newBlock.trsummary + "#" + newBlock.nonce);
			newBlock.previous = null;
			//add to lastblocks list
			for (int j = 0; j < lastBlocksList.length && (lastBlocksList[j] != null); j++)
				if (lastBlocksList[j].equals(lastBlock)) {
					lastBlocksList[j] = newBlock;
					return;
				}
			for (int j = 0; j < lastBlocksList.length; j++)
				if (lastBlocksList[j] == null) {
					lastBlocksList[j] = newBlock;
					return;
				}
		} else {
			Long i = 1000000001l;
			for (; Long.compare(i, 9999999999l) <= 0; i++) {
				if (myCRF.Fn(lastBlock.dgst + "#" + newBlock.trsummary + "#" + Long.toString(i)).substring(0, 4)
						.equals("0000")) {
					newBlock.nonce = Long.toString(i);
					break;
				}
			}
			newBlock.dgst = myCRF.Fn(lastBlock.dgst + "#" + newBlock.trsummary + "#" + newBlock.nonce);
			newBlock.previous = lastBlock;
			//add to lastblocks list
			for (int j = 0; j < lastBlocksList.length && (lastBlocksList[j] != null); j++)
				if (lastBlocksList[j].equals(lastBlock)) {
					lastBlocksList[j] = newBlock;
					return;
				}
			for (int j = 0; j < lastBlocksList.length; j++)
				if (lastBlocksList[j] == null) {
					lastBlocksList[j] = newBlock;
					return;
				}
		}

	}

	public static boolean checkTransactions(Transaction t, TransactionBlock tBlock) {
		//function to check transaction's validity between coinsrc block and current block
		TransactionBlock temp = tBlock;
		if (t.coinsrc_block == null)
			return true;
		temp = temp.previous;
		if (temp == null)
			return true;
		while (temp != t.coinsrc_block) {
			for (int i = 0; i < temp.trarray.length; i++) {
				if (temp.trarray[i].coinID.equals(t.coinID)) {
					return false;
				}
			}
			temp = temp.previous;
		}
		if (temp == null)
			return true;
		else {
			for (int i = 0; i < temp.trarray.length; i++) {
				if ((temp.trarray[i].coinID.equals(t.coinID)) && (temp.trarray[i].Destination.UID.equals(t.Source.UID)))
					return true;
			}
			return false;
		}
	}
}
