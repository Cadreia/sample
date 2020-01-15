/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.service;

import org.apache.fineract.accounting.journalentry.exception.JournalEntryInvalidException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavingsSchedulerServiceImpl implements SavingsSchedulerService {

	private final SavingsAccountAssembler savingAccountAssembler;
	private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
	private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
	private final SavingsAccountReadPlatformService savingAccountReadPlatformService;

	@Autowired
	public SavingsSchedulerServiceImpl(final SavingsAccountAssembler savingAccountAssembler,
									   final SavingsAccountWritePlatformService savingsAccountWritePlatformService,
									   final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper,
									   final SavingsAccountReadPlatformService savingAccountReadPlatformService) {
		this.savingAccountAssembler = savingAccountAssembler;
		this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
		this.savingAccountRepositoryWrapper = savingAccountRepositoryWrapper;
		this.savingAccountReadPlatformService = savingAccountReadPlatformService;
	}

	@CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
	@Override
	public void postInterestForAccounts() throws JobExecutionException {
		final List<SavingsAccount> savingsAccounts = this.savingAccountRepositoryWrapper.findSavingAccountByStatus(SavingsAccountStatusType.ACTIVE
				.getValue());
		StringBuffer sb = new StringBuffer();
		for (SavingsAccount savingsAccount : savingsAccounts) {
			try {
				if (savingsAccount instanceof FixedDepositAccount) {
					continue;
				}
				this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
				boolean postInterestAsOn = false;
				LocalDate transactionDate = null;
				this.savingsAccountWritePlatformService.postInterest(savingsAccount, postInterestAsOn, transactionDate);
			} catch (Exception e) {
				Throwable realCause = e;
				if (e.getCause() != null) {
					realCause = e.getCause();
				}
				String message = realCause.getMessage();
				if (message == null && realCause instanceof JournalEntryInvalidException) {
					message = ((JournalEntryInvalidException) realCause).getDefaultUserMessage();
				}
				sb.append(" Failed to post interest for Savings with id " + savingsAccount.getAccountNumber() + " with message "
						+ message);
			}
		}

		if (sb.length() > 0) {
			throw new JobExecutionException(sb.toString());
		}
	}

	@CronTarget(jobName = JobName.UPDATE_SAVINGS_DORMANT_ACCOUNTS)
	@Override
	public void updateSavingsDormancyStatus() throws JobExecutionException {
		final LocalDate tenantLocalDate = DateUtils.getLocalDateOfTenant();

		final List<Long> savingsPendingInactive = this.savingAccountReadPlatformService
				.retrieveSavingsIdsPendingInactive(tenantLocalDate);
		if (null != savingsPendingInactive && savingsPendingInactive.size() > 0) {
			for (Long savingsId : savingsPendingInactive) {
				this.savingsAccountWritePlatformService.setSubStatusInactive(savingsId);
			}
		}

		final List<Long> savingsPendingDormant = this.savingAccountReadPlatformService
				.retrieveSavingsIdsPendingDormant(tenantLocalDate);
		if (null != savingsPendingDormant && savingsPendingDormant.size() > 0) {
			for (Long savingsId : savingsPendingDormant) {
				this.savingsAccountWritePlatformService.setSubStatusDormant(savingsId);
			}
		}

		final List<Long> savingsPendingEscheat = this.savingAccountReadPlatformService
				.retrieveSavingsIdsPendingEscheat(tenantLocalDate);
		if (null != savingsPendingEscheat && savingsPendingEscheat.size() > 0) {
			for (Long savingsId : savingsPendingEscheat) {
				this.savingsAccountWritePlatformService.escheat(savingsId);
			}
		}
	}

	@CronTarget(jobName = JobName.CHECK_VALIDITY_OF_OVERDRAFT)
	@Override
	public void checkValidityForOverdraft() throws JobExecutionException {
		LocalDate today = DateUtils.getLocalDateOfTenant();
		final List<SavingsAccount> savingsAccountDueForOverdraftStart = this.savingAccountRepositoryWrapper
				.findByAccountsDueForOverdraftStart(today.toDate());
		if (savingsAccountDueForOverdraftStart != null || !savingsAccountDueForOverdraftStart.isEmpty()) {
			this.savingsAccountWritePlatformService.startOrCloseSavingsAccountOverdraft(savingsAccountDueForOverdraftStart, true);
		}

		final List<SavingsAccount> savingsAccountDueForOverdraftClose = this.savingAccountRepositoryWrapper
				.findByAccountsDueForOverdraftClose(today.toDate());
		if (savingsAccountDueForOverdraftClose != null || !savingsAccountDueForOverdraftClose.isEmpty()) {
			this.savingsAccountWritePlatformService.startOrCloseSavingsAccountOverdraft(savingsAccountDueForOverdraftClose, false);
		}
	}
}
