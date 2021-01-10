package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import credit.Credit;
import credit.CreditCustomer;
import valueObjects.Amount;

class CreditTest {

	@Test
	void testCreditConstruction() {
		
		Credit credit = new Credit(new Amount(1000));
		assertEquals(new Amount(1000), credit.getAmountOfCredit());
		assertNotNull(credit.getCreditNumber());

		CreditCustomer customer = new CreditCustomer("Carola", "Lilienthal", LocalDate.of(1967,9,11));
		customer.getCreditList().add(credit);
		assertTrue(customer.getCreditList().contains(credit));
	}
}
