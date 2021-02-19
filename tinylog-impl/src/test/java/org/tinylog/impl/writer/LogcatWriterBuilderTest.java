package org.tinylog.impl.writer;

import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.MockedStatic;
import org.tinylog.core.Framework;
import org.tinylog.core.Level;
import org.tinylog.core.test.log.CaptureLogEntries;
import org.tinylog.impl.LogEntry;
import org.tinylog.impl.test.LogEntryBuilder;
import org.tinylog.impl.test.Logcat;

import com.google.common.collect.ImmutableMap;

import android.util.Log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@CaptureLogEntries
class LogcatWriterBuilderTest {

	@Inject
	private Framework framework;

	/**
	 * Verifies that the builder is registered as service.
	 */
	@Test
	void service() {
		assertThat(ServiceLoader.load(WriterBuilder.class)).anySatisfy(builder -> {
			assertThat(builder).isInstanceOf(LogcatWriterBuilder.class);
			assertThat(builder.getName()).isEqualTo("logcat");
		});
	}

	/**
	 * Tests for {@link LogcatWriter} creation with mocked {@link Log} on non-Android platforms.
	 */
	@Nested
	@DisabledIfSystemProperty(named = "java.runtime.name", matches = "Android Runtime")
	class MockedLogging {

		private MockedStatic<Log> logMock;

		/**
		 * Mocks the static {@link Log} class.
		 */
		@BeforeEach
		void init() {
			logMock = mockStatic(Log.class);
		}

		/**
		 * Restores the statically mocked {@link Log} class.
		 */
		@AfterEach
		void dispose() {
			logMock.close();
		}

		/**
		 * Verifies that a {@link LogcatWriter} without tag pattern can be created.
		 */
		@Test
		void noTag() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.message("Hello World!")
				.create();

			Writer writer = new LogcatWriterBuilder().create(framework, Collections.emptyMap());
			try {
				writer.log(logEntry);
				logMock.verify(() -> Log.println(Log.INFO, null, "Hello World!"));
			} finally {
				writer.close();
			}
		}

		/**
		 * Verifies that a {@link LogcatWriter} with a custom tag pattern can be created.
		 */
		@Test
		void customTag() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.tag("foo")
				.message("Hello World!")
				.create();

			Map<String, String> configuration = ImmutableMap.of("tag-pattern", "{tag}");
			Writer writer = new LogcatWriterBuilder().create(framework, configuration);
			try {
				writer.log(logEntry);
				logMock.verify(() -> Log.println(Log.INFO, "foo", "Hello World!"));
			} finally {
				writer.close();
			}
		}

		/**
		 * Verifies that the generated tag placeholder for {@link LogcatWriter} has a maximum length of 23 characters.
		 */
		@Test
		void tooLongTag() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.tag("123456789012345678901234")
				.message("Hello World!")
				.create();

			Map<String, String> configuration = ImmutableMap.of("tag-pattern", "{tag}");
			Writer writer = new LogcatWriterBuilder().create(framework, configuration);
			try {
				writer.log(logEntry);
				logMock.verify(() -> Log.println(Log.INFO, "12345678901234567890...", "Hello World!"));
			} finally {
				writer.close();
			}
		}

		/**
		 * Verifies that a {@link LogcatWriter} with a custom message pattern can be created.
		 */
		@Test
		void customMessage() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.className("org.foo.MyClass")
				.message("Hello World!")
				.create();

			Map<String, String> configuration = ImmutableMap.of("message-pattern", "{class-name}: {message}");
			Writer writer = new LogcatWriterBuilder().create(framework, configuration);
			try {
				writer.log(logEntry);
				logMock.verify(() -> Log.println(Log.INFO, null, "MyClass: Hello World!"));
			} finally {
				writer.close();
			}
		}

	}

	/**
	 * Tests for {@link LogcatWriter} creation with real {@link Log} on Android.
	 */
	@Nested
	@EnabledIfSystemProperty(named = "java.runtime.name", matches = "Android Runtime")
	class RealLogging {

		private Logcat logcat;

		/**
		 * Creates an instance of {@link Logcat}.
		 */
		@BeforeEach
		void init() {
			logcat = new Logcat();
		}

		/**
		 * Verifies that a {@link LogcatWriter} without tag pattern can be created.
		 */
		@Test
		void noTag() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.message("Hello World!")
				.create();

			Writer writer = new LogcatWriterBuilder().create(framework, Collections.emptyMap());
			try {
				writer.log(logEntry);
			} finally {
				writer.close();
			}

			Pattern pattern = Pattern.compile("\\W+I\\W+Hello World!$");
			assertThat(logcat.fetchOutput()).anySatisfy(line -> assertThat(line).containsPattern(pattern));
		}

		/**
		 * Verifies that a {@link LogcatWriter} with a custom tag pattern can be created.
		 */
		@Test
		void customTag() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.tag("foo")
				.message("Hello World!")
				.create();

			Map<String, String> configuration = ImmutableMap.of("tag-pattern", "{tag}");
			Writer writer = new LogcatWriterBuilder().create(framework, configuration);
			try {
				writer.log(logEntry);
			} finally {
				writer.close();
			}
			Pattern pattern = Pattern.compile("\\W+I\\W+foo\\W+Hello World!$");
			assertThat(logcat.fetchOutput()).anySatisfy(line -> assertThat(line).containsPattern(pattern));
		}

		/**
		 * Verifies that the generated tag placeholder for {@link LogcatWriter} has a maximum length of 23 characters.
		 */
		@Test
		void tooLongTag() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.tag("123456789012345678901234")
				.message("Hello World!")
				.create();

			Map<String, String> configuration = ImmutableMap.of("tag-pattern", "{tag}");
			Writer writer = new LogcatWriterBuilder().create(framework, configuration);
			try {
				writer.log(logEntry);
			} finally {
				writer.close();
			}

			Pattern pattern = Pattern.compile("\\W+I\\W+12345678901234567890\\.\\.\\.\\W+Hello World!$");
			assertThat(logcat.fetchOutput()).anySatisfy(line -> assertThat(line).containsPattern(pattern));
		}

		/**
		 * Verifies that a {@link LogcatWriter} with a custom message pattern can be created.
		 */
		@Test
		void customMessage() throws Exception {
			LogEntry logEntry = new LogEntryBuilder()
				.severityLevel(Level.INFO)
				.className("org.foo.MyClass")
				.message("Hello World!")
				.create();

			Map<String, String> configuration = ImmutableMap.of("message-pattern", "{class-name}: {message}");
			Writer writer = new LogcatWriterBuilder().create(framework, configuration);
			try {
				writer.log(logEntry);
			} finally {
				writer.close();
			}

			Pattern pattern = Pattern.compile("\\W+I\\W+MyClass: Hello World!$");
			assertThat(logcat.fetchOutput()).anySatisfy(line -> assertThat(line).containsPattern(pattern));
		}

	}

}
