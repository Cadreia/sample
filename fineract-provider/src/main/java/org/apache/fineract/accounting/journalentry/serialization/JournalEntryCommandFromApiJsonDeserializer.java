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
package org.apache.fineract.accounting.journalentry.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.fineract.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_SAVINGS;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelper;
import org.apache.fineract.accounting.journalentry.api.JournalEntryJsonInputParams;
import org.apache.fineract.accounting.journalentry.command.JournalEntryCommand;
import org.apache.fineract.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link JournalEntryCommand}'s.
 */
@Component
public final class JournalEntryCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<JournalEntryCommand> {

    private final FromJsonHelper fromApiJsonHelper;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final AccountingProcessorHelper accountingProcessorHelper;

    @Autowired
    public JournalEntryCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonfromApiJsonHelper, 
    final SavingsAccountReadPlatformService savingsAccountReadPlatformService, final AccountingProcessorHelper accountingProcessorHelper) {
        this.fromApiJsonHelper = fromApiJsonfromApiJsonHelper;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.accountingProcessorHelper = accountingProcessorHelper;
    }

    // public JournalEntryCommand createJournalEntryCommand(final JsonCommand command) {
    //     checkForMultiWithdrawal(command);
    //     checkForMultiDeposit(command);
    //     return commandFromApiJson(command.json());
    // }

    @Override
    public JournalEntryCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Set<String> supportedParameters = JournalEntryJsonInputParams.getAllValues();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue(), element);
        final String currencyCode = this.fromApiJsonHelper
                .extractStringNamed(JournalEntryJsonInputParams.CURRENCY_CODE.getValue(), element);
        final String comments = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.COMMENTS.getValue(), element);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(
                JournalEntryJsonInputParams.TRANSACTION_DATE.getValue(), element);
        final String referenceNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.REFERENCE_NUMBER.getValue(),
                element);
        final Long accountingRuleId = this.fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.ACCOUNTING_RULE.getValue(),
                element);
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(JournalEntryJsonInputParams.AMOUNT.getValue(), element,
                locale);
        final Long paymentTypeId = this.fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.PAYMENT_TYPE_ID.getValue(), element);
        final String accountNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.ACCOUNT_NUMBER.getValue(),
                element);
        final String checkNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.CHECK_NUMBER.getValue(), element);
        final String receiptNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.RECEIPT_NUMBER.getValue(),
                element);
        final String bankNumber = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.BANK_NUMBER.getValue(), element);
        final String routingCode = this.fromApiJsonHelper.extractStringNamed(JournalEntryJsonInputParams.ROUTING_CODE.getValue(), element);

        SingleDebitOrCreditEntryCommand[] credits = null;
        SingleDebitOrCreditEntryCommand[] debits = null;
        SingleDebitOrCreditEntryCommand[] savingsCredits = null;
        SingleDebitOrCreditEntryCommand[] savingsDebits = null;

        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(JournalEntryJsonInputParams.CREDITS.getValue())
                    && topLevelJsonElement.get(JournalEntryJsonInputParams.CREDITS.getValue()).isJsonArray()) {
                credits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, credits, JournalEntryJsonInputParams.CREDITS.getValue());           
            } 

            if (topLevelJsonElement.has(JournalEntryJsonInputParams.DEBITS.getValue())
                    && topLevelJsonElement.get(JournalEntryJsonInputParams.DEBITS.getValue()).isJsonArray()) {
                debits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, debits, JournalEntryJsonInputParams.DEBITS.getValue());
            }

            System.out.println("credits.length");
            System.out.println(credits.length);
            System.out.println(debits.length);
            System.out.println("debits.length");

            if (credits.length == 0) {
                if (topLevelJsonElement.has(JournalEntryJsonInputParams.SAVINGSCREDITS.getValue())
                        && topLevelJsonElement.get(JournalEntryJsonInputParams.SAVINGSCREDITS.getValue()).isJsonArray()) {
                    // multideposit(topLevelJsonElement);
                    credits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, credits, JournalEntryJsonInputParams.SAVINGSCREDITS.getValue());
                }           
            } else {
                if (topLevelJsonElement.has(JournalEntryJsonInputParams.SAVINGSCREDITS.getValue())
                        && topLevelJsonElement.get(JournalEntryJsonInputParams.SAVINGSCREDITS.getValue()).isJsonArray()) {
                    // multideposit(topLevelJsonElement);
                    savingsCredits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, credits, JournalEntryJsonInputParams.SAVINGSCREDITS.getValue());
                    credits = appendToArray(credits, savingsCredits);
                }
            }

            System.out.println("after updating with savings credits.length");
            System.out.println(credits.length);
            System.out.println(debits.length);
            System.out.println("after updating with savings credits.length");

            if (debits.length == 0) {
                if (topLevelJsonElement.has(JournalEntryJsonInputParams.SAVINGSDEBITS.getValue())
                        && topLevelJsonElement.get(JournalEntryJsonInputParams.SAVINGSDEBITS.getValue()).isJsonArray()) {
                    // multideposit(topLevelJsonElement);
                    debits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, debits, JournalEntryJsonInputParams.SAVINGSDEBITS.getValue());
                }            
            } else {
                if (topLevelJsonElement.has(JournalEntryJsonInputParams.SAVINGSDEBITS.getValue())
                        && topLevelJsonElement.get(JournalEntryJsonInputParams.SAVINGSDEBITS.getValue()).isJsonArray()) {
                    // multideposit(topLevelJsonElement);
                    savingsDebits = populateCreditsOrDebitsArray(topLevelJsonElement, locale, debits, JournalEntryJsonInputParams.SAVINGSDEBITS.getValue());
                    debits = appendToArray(debits, savingsDebits);
                }
            }
            System.out.println("after updating with savings debits.length");
            System.out.println(credits.length);
            System.out.println(debits.length);
            System.out.println("after updating with savings debits.length");
        }
        return new JournalEntryCommand(officeId, currencyCode, transactionDate, comments, credits, debits, referenceNumber,
                accountingRuleId, amount, paymentTypeId, accountNumber, checkNumber, receiptNumber, bankNumber, routingCode);
    }

    /**
     * @param comments
     * @param topLevelJsonElement
     * @param locale
     */
    private SingleDebitOrCreditEntryCommand[] populateCreditsOrDebitsArray(final JsonObject topLevelJsonElement, final Locale locale,
            SingleDebitOrCreditEntryCommand[] debitOrCredits, final String paramName) {
        final JsonArray array = topLevelJsonElement.get(paramName).getAsJsonArray();
        debitOrCredits = new SingleDebitOrCreditEntryCommand[array.size()];
        for (int i = 0; i < array.size(); i++) {

            final JsonObject creditElement = array.get(i).getAsJsonObject();
            final Set<String> parametersPassedInForCreditsCommand = new HashSet<>();

            Long glAccountId = null;
            final String comments = this.fromApiJsonHelper.extractStringNamed("comments", creditElement);
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed("amount", creditElement, locale);

            if (creditElement.has("savingsAccountId")) {
                final Long savingsAccountId = this.fromApiJsonHelper.extractLongNamed("savingsAccountId", creditElement);
                final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed("savingsProductId", creditElement);

                // get glAccountId to be credited for savingsAccountId
                JsonElement paymentTypeId = topLevelJsonElement.get("paymentTypeId");
                GLAccount linkedGLAccount = null;
                if (paymentTypeId != null) {
                    linkedGLAccount = this.accountingProcessorHelper.getLinkedGLAccountForSavingsProduct(savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), paymentTypeId.getAsLong());
                } else {
                    linkedGLAccount = this.accountingProcessorHelper.getLinkedGLAccountForSavingsProduct(savingsProductId, CASH_ACCOUNTS_FOR_SAVINGS.SAVINGS_CONTROL.getValue(), null);
                }
                glAccountId = linkedGLAccount.getId();
            } else {
                glAccountId = this.fromApiJsonHelper.extractLongNamed("glAccountId", creditElement);
            }

            debitOrCredits[i] = new SingleDebitOrCreditEntryCommand(parametersPassedInForCreditsCommand, glAccountId, amount, comments);
        }
        return debitOrCredits;
    }

    private SingleDebitOrCreditEntryCommand[] appendToArray (SingleDebitOrCreditEntryCommand[] initialArray, final SingleDebitOrCreditEntryCommand[] arrayToAdd) {
        // initialArray = Arrays.copyOf(arr, arr.length + 1);
        for (int i = 0; i < arrayToAdd.length; i++) {
            // initialArray[initialArray.length - 1] = arrayToAdd[i]
            initialArray = ArrayUtils.add(initialArray, arrayToAdd[i]);
        }
        return initialArray;
    }

    // private void multideposit(final JsonObject topLevelJsonElement, final JsonCommand command) {
    //     /********* deposit *****************/
    //     final JsonArray savingsCredits = topLevelJsonElement.get("savingsCredits").getAsJsonArray();
    //     if (savingsCredits.size() > 0) {
    //         for (int i = 0; i < savingsCredits.size(); i++) {
    //             // make deposit for each
    //             final JsonObject jsonObject = savingsCredits.get(i).getAsJsonObject();
    //             this.savingsAccountWritePlatformService.deposit(jsonObject.get("savingsAccountId").getAsLong(), command);
    //         }
    //     }
    // }

    // private void multiwithdrawal(final JsonObject topLevelJsonElement, final JsonCommand command) {
    //     /********* withdraw *****************/
    //     final JsonArray savingsDebits = topLevelJsonElement.get("savingsDebits").getAsJsonArray();
    //     if (savingsDebits.size() > 0) {
    //         for (int i = 0; i < savingsDebits.size(); i++) {
    //             // make withdrawal for each
    //             final JsonObject jsonObject = savingsDebits.get(i).getAsJsonObject();
    //             savingsAccountId = this.fromApiJsonHelper.extractLongNamed("savingsAccountId", jsonObject);
    //             this.savingsAccountWritePlatformService.withdrawal(savingsAccountId, command);                }
    //     }
    // }
}
