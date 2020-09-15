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
package org.apache.fineract.infrastructure.classdupes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@link ClasspathHellDuplicatesCheckRule} test.
 *
 * @author Michael Vorburger
 */
@ExtendWith(ClasspathHellDuplicatesCheckExtension.class)
public class ClasspathHellDuplicatesCheckRuleTest {

    // @ClassRule public static ClasspathHellDuplicatesCheckRule jHades = new
    // ClasspathHellDuplicatesCheckRule();

    @Test // we just need this because JUnit doesn't like a *Test class with
          // only a Rule
    public void testIfThereAreAnyDuplicatesOnTheClasspath() throws Exception {}
}