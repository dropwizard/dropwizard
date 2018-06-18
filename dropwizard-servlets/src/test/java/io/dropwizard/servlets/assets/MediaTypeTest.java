/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dropwizard.servlets.assets;

import io.dropwizard.util.Maps;
import io.dropwizard.util.Sets;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.dropwizard.servlets.assets.MediaType.ANY_APPLICATION_TYPE;
import static io.dropwizard.servlets.assets.MediaType.ANY_AUDIO_TYPE;
import static io.dropwizard.servlets.assets.MediaType.ANY_IMAGE_TYPE;
import static io.dropwizard.servlets.assets.MediaType.ANY_TEXT_TYPE;
import static io.dropwizard.servlets.assets.MediaType.ANY_TYPE;
import static io.dropwizard.servlets.assets.MediaType.ANY_VIDEO_TYPE;
import static io.dropwizard.servlets.assets.MediaType.HTML_UTF_8;
import static io.dropwizard.servlets.assets.MediaType.JPEG;
import static io.dropwizard.servlets.assets.MediaType.PLAIN_TEXT_UTF_8;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MediaType}.
 *
 * @author Gregory Kick
 */
public class MediaTypeTest {
    @Test
    public void testParse_useConstants() throws Exception {
        for (MediaType constant : getConstants()) {
            assertSame(constant, MediaType.parse(constant.toString()));
        }
    }

    @Test
    public void testCreate_useConstants() throws Exception {
        for (MediaType constant : getConstants()) {
            assertSame(
                    constant,
                    MediaType.create(constant.type(), constant.subtype())
                            .withParameters(constant.parameters()));
        }
    }

    @Test
    public void testConstants_charset() throws Exception {
        for (Field field : getConstantFields()) {
            Optional<Charset> charset = ((MediaType) field.get(null)).charset();
            if (field.getName().endsWith("_UTF_8")) {
                assertThat(charset).hasValue(UTF_8);
            } else {
                assertThat(charset).isEmpty();
            }
        }
    }

    @Test
    public void testConstants_areUnique() {
        assertThat(getConstants()).doesNotHaveDuplicates();
    }

    private static Set<Field> getConstantFields() {
        return Arrays.stream(MediaType.class.getDeclaredFields())
                .filter(
                        input -> {
                            int modifiers = input.getModifiers();
                            return isPublic(modifiers)
                                    && isStatic(modifiers)
                                    && isFinal(modifiers)
                                    && MediaType.class.equals(input.getType());
                        })
                .collect(Collectors.toSet());
    }

    private static Set<MediaType> getConstants() {
        return getConstantFields().stream()
                .map(input -> {
                    try {
                        return (MediaType) input.get(null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    @Test
    public void testCreate_invalidType() {
        try {
            MediaType.create("te><t", "plaintext");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCreate_invalidSubtype() {
        try {
            MediaType.create("text", "pl@intext");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCreate_wildcardTypeDeclaredSubtype() {
        try {
            MediaType.create("*", "text");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCreateApplicationType() {
        MediaType newType = MediaType.createApplicationType("yams");
        assertEquals("application", newType.type());
        assertEquals("yams", newType.subtype());
    }

    @Test
    public void testCreateAudioType() {
        MediaType newType = MediaType.createAudioType("yams");
        assertEquals("audio", newType.type());
        assertEquals("yams", newType.subtype());
    }

    @Test
    public void testCreateImageType() {
        MediaType newType = MediaType.createImageType("yams");
        assertEquals("image", newType.type());
        assertEquals("yams", newType.subtype());
    }

    @Test
    public void testCreateTextType() {
        MediaType newType = MediaType.createTextType("yams");
        assertEquals("text", newType.type());
        assertEquals("yams", newType.subtype());
    }

    @Test
    public void testCreateVideoType() {
        MediaType newType = MediaType.createVideoType("yams");
        assertEquals("video", newType.type());
        assertEquals("yams", newType.subtype());
    }

    @Test
    public void testGetType() {
        assertEquals("text", MediaType.parse("text/plain").type());
        assertEquals("application", MediaType.parse("application/atom+xml; charset=utf-8").type());
    }

    @Test
    public void testGetSubtype() {
        assertEquals("plain", MediaType.parse("text/plain").subtype());
        assertEquals("atom+xml", MediaType.parse("application/atom+xml; charset=utf-8").subtype());
    }

    private static final Map<String, List<String>> PARAMETERS = Maps.of(
            "a", Arrays.asList("1", "2"),
            "b", Collections.singletonList("3"));

    @Test
    public void testGetParameters() {
        assertEquals(Collections.emptyMap(), MediaType.parse("text/plain").parameters());
        assertEquals(
                Collections.singletonMap("charset", Collections.singletonList("utf-8")),
                MediaType.parse("application/atom+xml; charset=utf-8").parameters());
        assertEquals(PARAMETERS, MediaType.parse("application/atom+xml; a=1; a=2; b=3").parameters());
    }

    @Test
    public void testWithoutParameters() {
        assertSame(MediaType.parse("image/gif"), MediaType.parse("image/gif").withoutParameters());
        assertEquals(
                MediaType.parse("image/gif"), MediaType.parse("image/gif; foo=bar").withoutParameters());
    }

    @Test
    public void testWithParameters() {
        assertEquals(
                MediaType.parse("text/plain; a=1; a=2; b=3"),
                MediaType.parse("text/plain").withParameters(PARAMETERS));
        assertEquals(
                MediaType.parse("text/plain; a=1; a=2; b=3"),
                MediaType.parse("text/plain; a=1; a=2; b=3").withParameters(PARAMETERS));
    }

    @Test
    public void testWithParameters_invalidAttribute() {
        MediaType mediaType = MediaType.parse("text/plain");
        Map<String, List<String>> parameters = Maps.of(
                "a", Collections.singletonList("1"),
                "@", Collections.singletonList("2"),
                "b", Collections.singletonList("3"));
        try {
            mediaType.withParameters(parameters);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWithParameter() {
        assertEquals(
                MediaType.parse("text/plain; a=1"), MediaType.parse("text/plain").withParameter("a", "1"));
        assertEquals(
                MediaType.parse("text/plain; a=1"),
                MediaType.parse("text/plain; a=1; a=2").withParameter("a", "1"));
        assertEquals(
                MediaType.parse("text/plain; a=3"),
                MediaType.parse("text/plain; a=1; a=2").withParameter("a", "3"));
        assertEquals(
                MediaType.parse("text/plain; a=1; a=2; b=3"),
                MediaType.parse("text/plain; a=1; a=2").withParameter("b", "3"));
    }

    @Test
    public void testWithParameter_invalidAttribute() {
        MediaType mediaType = MediaType.parse("text/plain");
        try {
            mediaType.withParameter("@", "2");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWithParametersIterable() {
        assertEquals(
                MediaType.parse("text/plain"),
                MediaType.parse("text/plain; a=1; a=2").withParameters("a", Collections.emptySet()));
        assertEquals(
                MediaType.parse("text/plain; a=1"),
                MediaType.parse("text/plain").withParameters("a", Collections.singleton("1")));
        assertEquals(
                MediaType.parse("text/plain; a=1"),
                MediaType.parse("text/plain; a=1; a=2").withParameters("a", Collections.singleton("1")));
        assertEquals(
                MediaType.parse("text/plain; a=1; a=3"),
                MediaType.parse("text/plain; a=1; a=2").withParameters("a", Sets.of("1", "3")));
        assertEquals(
                MediaType.parse("text/plain; a=1; a=2; b=3; b=4"),
                MediaType.parse("text/plain; a=1; a=2").withParameters("b", Sets.of("3", "4")));
    }

    @Test
    public void testWithParametersIterable_invalidAttribute() {
        MediaType mediaType = MediaType.parse("text/plain");
        try {
            mediaType.withParameters("@", Collections.singleton("2"));
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWithParametersIterable_nullValue() {
        MediaType mediaType = MediaType.parse("text/plain");
        try {
            mediaType.withParameters("a", Collections.singletonList(null));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void testWithCharset() {
        assertEquals(
                MediaType.parse("text/plain; charset=utf-8"),
                MediaType.parse("text/plain").withCharset(UTF_8));
        assertEquals(
                MediaType.parse("text/plain; charset=utf-8"),
                MediaType.parse("text/plain; charset=utf-16").withCharset(UTF_8));
    }

    @Test
    public void testHasWildcard() {
        assertFalse(PLAIN_TEXT_UTF_8.hasWildcard());
        assertFalse(JPEG.hasWildcard());
        assertTrue(ANY_TYPE.hasWildcard());
        assertTrue(ANY_APPLICATION_TYPE.hasWildcard());
        assertTrue(ANY_AUDIO_TYPE.hasWildcard());
        assertTrue(ANY_IMAGE_TYPE.hasWildcard());
        assertTrue(ANY_TEXT_TYPE.hasWildcard());
        assertTrue(ANY_VIDEO_TYPE.hasWildcard());
    }

    @Test
    public void testIs() {
        assertTrue(PLAIN_TEXT_UTF_8.is(ANY_TYPE));
        assertTrue(JPEG.is(ANY_TYPE));
        assertTrue(ANY_TEXT_TYPE.is(ANY_TYPE));
        assertTrue(PLAIN_TEXT_UTF_8.is(ANY_TEXT_TYPE));
        assertTrue(PLAIN_TEXT_UTF_8.withoutParameters().is(ANY_TEXT_TYPE));
        assertFalse(JPEG.is(ANY_TEXT_TYPE));
        assertTrue(PLAIN_TEXT_UTF_8.is(PLAIN_TEXT_UTF_8));
        assertTrue(PLAIN_TEXT_UTF_8.is(PLAIN_TEXT_UTF_8.withoutParameters()));
        assertFalse(PLAIN_TEXT_UTF_8.withoutParameters().is(PLAIN_TEXT_UTF_8));
        assertFalse(PLAIN_TEXT_UTF_8.is(HTML_UTF_8));
        assertFalse(PLAIN_TEXT_UTF_8.withParameter("charset", "UTF-16").is(PLAIN_TEXT_UTF_8));
        assertFalse(PLAIN_TEXT_UTF_8.is(PLAIN_TEXT_UTF_8.withParameter("charset", "UTF-16")));
    }

    @Test
    public void testParse_empty() {
        try {
            MediaType.parse("");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testParse_badInput() {
        try {
            MediaType.parse("/");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("te<t/plain");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/pl@in");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain;");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; ");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=@");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=\"@");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=1;");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=1; ");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=1; b");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=1; b=");
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            MediaType.parse("text/plain; a=\u2025");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetCharset() {
        assertThat(MediaType.parse("text/plain").charset()).isEmpty();
        assertThat(MediaType.parse("text/plain; charset=utf-8").charset()).hasValue(UTF_8);
    }

    @Test
    public void testGetCharset_utf16() {
        assertThat(MediaType.parse("text/plain; charset=utf-16").charset()).hasValue(UTF_16);
    }

    @Test
    public void testGetCharset_tooMany() {
        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8; charset=utf-16");
        try {
            mediaType.charset();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testGetCharset_illegalCharset() {
        MediaType mediaType = MediaType.parse("text/plain; charset=\"!@#$%^&*()\"");
        try {
            mediaType.charset();
            fail();
        } catch (IllegalCharsetNameException expected) {
        }
    }

    @Test
    public void testGetCharset_unsupportedCharset() {
        MediaType mediaType = MediaType.parse("text/plain; charset=utf-wtf");
        try {
            mediaType.charset();
            fail();
        } catch (UnsupportedCharsetException expected) {
        }
    }

    @Test
    public void testToString() {
        assertEquals("text/plain", MediaType.create("text", "plain").toString());
        assertEquals(
                "text/plain; something-else=\"crazy with spaces\"; something=\"cr@zy\"",
                MediaType.create("text", "plain")
                        .withParameter("something", "cr@zy")
                        .withParameter("something-else", "crazy with spaces")
                        .toString());
    }
}