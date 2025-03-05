package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @MockBean
  private AccountsRepository accountRepository;

  @MockBean
  private NotificationService notificationService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  void testSuccessfulTransfer() {
    // Arrange
    BigDecimal amountToTransfer = new BigDecimal("200.00");

    // Set up mock repository behavior
    Account accountFrom = new Account("1");
    accountFrom.setBalance(new BigDecimal("1000.00"));
    Account accountTo = new Account("2");
    accountTo.setBalance(new BigDecimal("500.00"));

    when(accountRepository.getAccount("1")).thenReturn(accountFrom);
    when(accountRepository.getAccount("2")).thenReturn(accountTo);

    // Act
    accountsService.transfer("1", "2", amountToTransfer);

    // Assert
    assertThat(accountFrom.getBalance()).isEqualTo(new BigDecimal("800.00"));
    assertThat(accountTo.getBalance()).isEqualTo(new BigDecimal("700.00"));

    // Verify that the notification service was called
    verify(notificationService, times(1)).notifyAboutTransfer(accountFrom, "Transferred 200.00 to account 2");
    verify(notificationService, times(1)).notifyAboutTransfer(accountTo, "Received 200.00 from account 1");
  }
}
