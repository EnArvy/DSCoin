package DSCoinPackage;

public class TransactionQueue {

	public Transaction firstTransaction;
	public Transaction lastTransaction;
	public int numTransactions;

	public void AddTransactions(Transaction transaction) {
		//if queue is empty or not
		if (firstTransaction == null) {
			firstTransaction = transaction;
			lastTransaction = transaction;
			numTransactions = 1;
			transaction.next = null;
			transaction.previous = null;
		} else {
			lastTransaction.next = transaction;
			transaction.previous = lastTransaction;
			lastTransaction = transaction;
			numTransactions = numTransactions + 1;
		}
	}

	public Transaction RemoveTransaction() throws EmptyQueueException {
		//if queue is empty or has 1 element or more than 1 elements
		if (firstTransaction == null) {
			throw new EmptyQueueException();
		} else if (firstTransaction == lastTransaction) {
			firstTransaction = null;
			Transaction temp = lastTransaction;
			lastTransaction = null;
			numTransactions = 0;
			return temp;
		} else {
			Transaction temp = firstTransaction;
			firstTransaction = firstTransaction.next;
			firstTransaction.previous = null;
			numTransactions = numTransactions - 1;
			return temp;
		}
	}

	public int size() {
		int sizes=0;
		Transaction temp = firstTransaction;
		while(temp!=null){
			sizes++;
			temp=temp.next;
		}
		return sizes;
	}
}
