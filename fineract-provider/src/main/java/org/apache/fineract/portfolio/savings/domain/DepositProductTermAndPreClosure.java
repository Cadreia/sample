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
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.charge.domain.Charge;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosureChargeIdParamName;

@Entity
@Table(name = "m_deposit_product_term_and_preclosure")
public class DepositProductTermAndPreClosure extends AbstractPersistableCustom<Long> {

    @Embedded
    private DepositPreClosureDetail preClosureDetail;

    @Embedded
    protected DepositTermDetail depositTermDetail;

    @OneToOne
    @JoinColumn(name = "savings_product_id", nullable = false)
    private FixedDepositProduct product;

    @OneToOne
    @JoinColumn(name = "pre_closure_charge_id")
    private Charge preClosureCharge;

    @Embedded
    private DepositProductAmountDetails depositProductAmountDetails;

    protected DepositProductTermAndPreClosure() {
        super();
    }

    public static DepositProductTermAndPreClosure createNew(DepositPreClosureDetail preClosureDetail, DepositTermDetail depositTermDetail,
            DepositProductAmountDetails depositProductMinMaxAmountDetails, SavingsProduct product) {

        return new DepositProductTermAndPreClosure(preClosureDetail, depositTermDetail, depositProductMinMaxAmountDetails, product);
    }

    private DepositProductTermAndPreClosure(DepositPreClosureDetail preClosureDetail, DepositTermDetail depositTermDetail,
            DepositProductAmountDetails depositProductMinMaxAmountDetails, SavingsProduct product) {
        this.preClosureDetail = preClosureDetail;
        this.depositTermDetail = depositTermDetail;
        this.depositProductAmountDetails = depositProductMinMaxAmountDetails;
        this.product = (FixedDepositProduct) product;
    }

    public Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);
        if (this.preClosureDetail != null) {
            actualChanges.putAll(this.preClosureDetail.update(command, baseDataValidator));
            this.appendPreClosureChargeChanges(command, actualChanges);
        }
        if (this.depositTermDetail != null) {
            actualChanges.putAll(this.depositTermDetail.update(command, baseDataValidator));
        }
        if (this.depositProductAmountDetails != null) {
            actualChanges.putAll(this.depositProductAmountDetails.update(command));
        }
        return actualChanges;
    }

    private void appendPreClosureChargeChanges(JsonCommand command, Map<String, Object> actualChanges) {
        if (preClosureDetail.isPreClosureChargeApplicable()) {
            Long chargeId = this.preClosureCharge != null ? this.preClosureCharge.getId() : null;
            if (command.isChangeInLongParameterNamed(preClosureChargeIdParamName, chargeId)) {
                Long newValue = command.longValueOfParameterNamed(preClosureChargeIdParamName);
                actualChanges.put(preClosureChargeIdParamName, newValue);
            }
        } else {
            this.preClosureCharge = null;
        }
    }

    public DepositPreClosureDetail depositPreClosureDetail() {
        return this.preClosureDetail;
    }

    public DepositTermDetail depositTermDetail() {
        return this.depositTermDetail;
    }

    public void updateProductReference(final SavingsProduct product) {
        this.product = (FixedDepositProduct) product;
    }

    public Charge getPreClosureCharge() {
        return preClosureCharge;
    }

    public void setPreClosureCharge(Charge preClosureCharge) {
        this.preClosureCharge = preClosureCharge;
    }
}