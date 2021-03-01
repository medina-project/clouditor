package io.clouditor.credentials;

import java.io.IOException;
import java.util.Set;

public class MultipleAccountsService {
  AccountService accountService;

  public MultipleAccountsService(AccountService accountService) {
    this.accountService = accountService;
  }

  // Currently AWS only
  // ToDo: Maybe with returning boolean (if adding was successful or not)
  public void addAccounts(CloudAccount auditorAccount, Set<String> arnsOfAssumedAccounts)
      throws IOException {
    // Add auditorAccount to DB
    accountService.addAccount(auditorAccount.provider, auditorAccount);

    // Add assumedAccounts to DB
    // Get credentials

  }

  // ToDo: Enable/Disable accounts, list enabled accounts, remove account

}
