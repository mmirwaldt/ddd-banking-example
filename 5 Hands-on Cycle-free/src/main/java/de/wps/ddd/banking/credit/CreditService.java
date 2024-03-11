package de.wps.ddd.banking.credit;

import static de.wps.common.contracts.BaseContracts.require;
import static de.wps.common.contracts.BaseContracts.requireNotNull;

import de.wps.ddd.banking.sharedKernel.AccountNumberFactory;
import de.wps.ddd.banking.sharedKernel.CreditNumberFactory;
import de.wps.ddd.banking.sharedKernel.CustomerNumberFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.wps.ddd.banking.sharedKernel.AccountNumber;
import de.wps.ddd.banking.sharedKernel.Amount;
import de.wps.ddd.banking.sharedKernel.CreditNumber;
import de.wps.ddd.banking.sharedKernel.CustomerNumber;

public class CreditService {
	private Map<CustomerNumber, CreditCustomer> customerList = new HashMap<>();
	private Map<AccountNumber, CreditAccount> accountList = new HashMap<>();
	private Map<CreditNumber, Credit> creditList = new HashMap<>();
	private final CreditNumberFactory creditNumberFactory;
	private final AccountNumberFactory accountNumberFactory;
	private final CustomerNumberFactory customerNumberFactory;

	public CreditService() {
		this(new CreditNumberFactory(), new AccountNumberFactory(), new CustomerNumberFactory());
	}

	CreditService(CreditNumberFactory creditNumberFactory, AccountNumberFactory accountNumberFactory, CustomerNumberFactory customerNumberFactory) {
		this.creditNumberFactory = creditNumberFactory;
		this.accountNumberFactory = accountNumberFactory;
		this.customerNumberFactory = customerNumberFactory;
	}

	// should only be called by AccountManagementService
	public void newCustomer(String firstName, String familyName, LocalDate dateOfBirth, CustomerNumber customerNumber) {
		customerList.put(customerNumber, new CreditCustomer(customerNumber, firstName, familyName, dateOfBirth));
	}

	public CreditAccount newCreditAccount(CreditCustomer customer, Amount balance, Credit credit) {
		CreditAccount account = new CreditAccount(accountNumberFactory.newAccountNumber(), credit.getAmountOfCredit());
		account.deposit(balance);
		accountList.put(account.getAccountNumber(), account);
		customer.addAccount(account);
		return account;
	}

	public CreditNumber applyForCredit(Amount amount, CreditCustomer customer) {

		Credit credit = new Credit(creditNumberFactory.newCreditNumber(), amount);
		customer.addCredit(credit);
		CreditNumber creditNumber = credit.getCreditNumber();
		creditList.put(creditNumber, credit);

		return creditNumber;
	}

	public CreditAccount grantCredit(CreditNumber creditNumber) {
		require(getCredit(creditNumber).canBeGranted(), "getCredit(creditNumber).canBeGranted()");

		Credit credit = getCredit(creditNumber);
		CreditAccount newCreditAccount = newCreditAccount(credit);
		credit.grant(newCreditAccount);
		return newCreditAccount;
	}

	public Credit getCredit(CreditNumber creditNumber) {
		return creditList.get(creditNumber);
	}

	public Credit getCreditFromAccountNumber(AccountNumber accountNumber) {
		requireNotNull(accountNumber, "accountNumber");

		Credit credit = null;
		for (Map.Entry<CreditNumber, Credit> entry : creditList.entrySet()) {
			Optional<CreditAccount> account = entry.getValue().getAccount();
			if (account.isPresent() && account.get().getAccountNumber().equals(accountNumber)) {
				credit = entry.getValue();
			}
		}
		return credit;
	}

	public void makePaymentForCredit(CreditNumber creditNumber, Amount amount) {
		Credit credit = creditList.get(creditNumber);
		CreditAccount creditAccount = credit.getAccount().orElseThrow();
		creditAccount.deposit(amount);
	}

	public CreditCustomer getCustomerForCredit(Credit credit) {
		CreditCustomer customer = null;
		for (Map.Entry<CustomerNumber, CreditCustomer> entry : customerList.entrySet()) {
			if (entry.getValue().getCreditList().contains(credit)) {
				customer = entry.getValue();
			}
		}
		return customer;
	}

	public CreditAccount newCreditAccount(Credit credit) {
		CreditAccount account = new CreditAccount(accountNumberFactory.newAccountNumber(), credit.getAmountOfCredit());
		accountList.put(account.getAccountNumber(), account);
		CreditCustomer customer = this.getCustomerForCredit(credit);
		customer.addAccount(account);
		return account;
	}

	public List<CreditAccount> getCreditAccountList() {
		return List.copyOf(accountList.values());
	}

	public List<CreditCustomer> getCreditCustomerList() {
		return List.copyOf(customerList.values());
	}

	public CreditAccount getCreditAccount(AccountNumber accountNumber) {
		return accountList.get(accountNumber);
	}

	public Set<AccountNumber> getAccountNumberList() {
		return Set.copyOf(accountList.keySet());
	}
}
