/*
 * Copyright 2020 Martin Winandy
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

package org.tinylog.runtime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModernRuntimeTest {

	/**
	 * Verifies that a timestamp with the current date and time is created.
	 */
	@Test
	void timestamp() {
		Instant before = Instant.now();
		ModernTimestamp timestamp = new ModernJavaRuntime().createTimestamp();
		Instant after = Instant.now();

		assertThat(timestamp.resole()).isBetween(before, after);
	}

	/**
	 * Verifies that the provided timestamp formatter can format a {@link ModernTimestamp ModernTimestamp}.
	 */
	@Test
	void timestampFormatter() {
		ZonedDateTime zonedDateTime = LocalDateTime.parse("2020-12-31T11:55").atZone(ZoneId.systemDefault());
		ModernTimestamp timestamp = new ModernTimestamp(zonedDateTime.toInstant());
		ModernJavaRuntime runtime = new ModernJavaRuntime();
		ModernTimestampFormatter formatter = runtime.createTimestampFormatter("dd.MM.yyyy, HH:mm", Locale.GERMANY);

		assertThat(formatter.format(timestamp)).isEqualTo("31.12.2020, 11:55");
	}

}