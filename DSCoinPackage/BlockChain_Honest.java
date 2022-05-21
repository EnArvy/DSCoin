package DSCoinPackage;

import HelperClasses.CRF;

public class BlockChain_Honest {

	public int tr_count;
	public static final String start_string = "DSCoin";
	public TransactionBlock lastBlock;

	public void InsertBlock_Honest(TransactionBlock newBlock) {
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
			lastBlock = newBlock;
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
			lastBlock = newBlock;
		}
	}
}
