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
package org.apache.fineract.portfolio.savings.domain;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.request.FixedDepositPreClosureReq;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.Map;

public interface DepositAccountDomainService {

    SavingsAccountTransaction handleWithdrawal(SavingsAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, boolean applyWithdrawFee, boolean isRegularTransaction);

    SavingsAccountTransaction handleFDDeposit(FixedDepositAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail);

    SavingsAccountTransaction handleRDDeposit(RecurringDepositAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, boolean isRegularTransaction);
    
    SavingsAccountTransaction handleSavingDeposit(SavingsAccount account, DateTimeFormatter fmt, LocalDate transactionDate,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, boolean isRegularTransaction);

    Long handleFDAccountClosure(FixedDepositAccount account, PaymentDetail paymentDetail, AppUser user, JsonCommand command,
            LocalDate tenantsTodayDate, Map<String, Object> changes);

    Long handleRDAccountClosure(RecurringDepositAccount account, PaymentDetail paymentDetail, AppUser user, JsonCommand command,
            LocalDate tenantsTodayDate, Map<String, Object> changes);

    Long prematurelyCloseFDAccount(FixedDepositAccount account, PaymentDetail paymentDetail, FixedDepositPreClosureReq fixedDepositPreclosureReq, Map<String, Object> changes);

    Long handleRDAccountPreMatureClosure(RecurringDepositAccount account, PaymentDetail paymentDetail, AppUser user, JsonCommand command,
            LocalDate tenantsTodayDate, Map<String, Object> changes);
}