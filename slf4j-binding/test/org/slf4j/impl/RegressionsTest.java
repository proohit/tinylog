package org.slf4j.impl;
/*
 * Copyright 2012 Martin Winandy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import org.junit.Test;
import org.pmw.tinylog.AbstractTinylogTest;
import org.slf4j.MDC;

/**
 * Tests old fixed bugs to prevent regressions.
 */
public class RegressionsTest extends AbstractTinylogTest {

	/**
	 * Bug: SLF4Js MDC implementation failed due to incorrect return type of
	 * {@link org.slf4j.impl.StaticMDCBinder#getMDCA()}.
	 */
	@Test
	public final void testReturnTypeOfStaticMDCBinder() {
		try {
			MDC.put("pi", "3.14");
		} finally {
			MDC.clear();
		}
	}

}