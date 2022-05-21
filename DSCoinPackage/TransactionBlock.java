package DSCoinPackage;

import HelperClasses.MerkleTree;
import HelperClasses.CRF;

public class TransactionBlock {

	public Transaction[] trarray;
	public TransactionBlock previous;
	public MerkleTree Tree;
	public String trsummary;
	public String nonce;
	public String dgst;

	TransactionBlock(Transaction[] t) {
		Transaction[] temp = new Transaction[t.length];
		for (int i = 0; i < t.length; i++) {
			temp[i] = t[i];
		}
		trarray = temp;
		Tree = new MerkleTree();
		Tree.Build(trarray);
		previous = null;
		dgst = null;
		trsummary = Tree.rootnode.val;
	}

	public boolean checkTransaction(Transaction t) {
		//function to check transaction validity from current block to block before coinsrc block
		TransactionBlock temp = this;
		if (t.coinsrc_block == null)
			return true;

		while (temp != t.coinsrc_block) {
			for (int i = 0; i < temp.trarray.length; i++) {
				if (temp.trarray[i].coinID.equals(t.coinID))
					return false;
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
