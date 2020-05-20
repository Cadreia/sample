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

package org.apache.fineract.portfolio.savings.request;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.toSavingsAccountIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.transferDescriptionParamName;

public class FixedDepositPreClosureReq {

    Locale locale;
    String dateFormat;
    LocalDate closedDate;
    DepositAccountOnClosureType closureType;
    DateTimeFormatter formatter;
    Long toSavingsId;
    String transferDescription;

    public static FixedDepositPreClosureReq instance(JsonCommand command) {
        FixedDepositPreClosureReq instance = new FixedDepositPreClosureReq();
        Integer onAccountClosureId = command.integerValueOfParameterNamed(DepositsApiConstants.onAccountClosureIdParamName);
        instance.toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
        instance.transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
        instance.locale = command.extractLocale();
        instance.dateFormat = command.dateFormat();
        instance.formatter = DateTimeFormat.forPattern(instance.dateFormat).withLocale(instance.locale);
        instance.closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);
        instance.closureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        return instance;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public DepositAccountOnClosureType getClosureType() {
        return closureType;
    }

    public void setClosureType(DepositAccountOnClosureType closureType) {
        this.closureType = closureType;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    public Long getToSavingsId() {
        return toSavingsId;
    }

    public void setToSavingsId(Long toSavingsId) {
        this.toSavingsId = toSavingsId;
    }

    public String getTransferDescription() {
        return transferDescription;
    }

    public void setTransferDescription(String transferDescription) {
        this.transferDescription = transferDescription;
    }
}
