package org.lenskit.util;

import org.joda.convert.StringConvert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by michaelekstrand on 4/13/2017.
 */
public class TextTest {
    @Test
    public void testUTF8ByteCount() {
        assertThat(Text.utf8ByteCount((byte) 0), equalTo(1));
        // assertThat(Text.utf8ByteCount((byte) 0x80), equalTo(2));
        assertThat(Text.utf8ByteCount((byte) 0xC0), equalTo(2));
        assertThat(Text.utf8ByteCount((byte) 0xE0), equalTo(3));
        assertThat(Text.utf8ByteCount((byte) 0xF0), equalTo(4));
    }

    @Test
    public void testEmptyString() {
        Text text = Text.fromString("");
        assertThat(text.toString(), equalTo(""));
        assertThat(text.length(), equalTo(0));
        assertThat(text.size(), equalTo(0));
    }

    @Test
    public void testBasicString() {
        Text text = Text.fromString("wumpus");
        assertThat(text.toString(), equalTo("wumpus"));
        assertThat(text.size(), equalTo(6));
        assertThat(text.length(), equalTo(6));
    }

    @Test
    public void testStringWithLongerChar() {
        Text text = Text.fromString("wümpus");
        assertThat(text.toString(), equalTo("wümpus"));
        assertThat(text.size(), equalTo(7));
        assertThat(text.length(), equalTo(6));
    }

    @Test
    public void testStringWithNonBMPChar() {
        Text text = Text.fromString("wu\uD835\uDCC2pus");
        assertThat(text.toString(), equalTo("wu\uD835\uDCC2pus"));
        assertThat(text.size(), equalTo(9));
        assertThat(text.length(), equalTo(7));
    }

    @Test
    public void testBasicStringCharAt() {
        Text text = Text.fromString("wumpus");
        assertThat(text.charAt(0), equalTo('w'));
        assertThat(text.charAt(1), equalTo('u'));
        assertThat(text.charAt(2), equalTo('m'));
        assertThat(text.charAt(3), equalTo('p'));
        assertThat(text.charAt(4), equalTo('u'));
        assertThat(text.charAt(5), equalTo('s'));
    }

    @Test
    public void testAccentCharAt() {
        Text text = Text.fromString("wümpus");
        assertThat(text.charAt(0), equalTo('w'));
        assertThat(text.charAt(1), equalTo('ü'));
        assertThat(text.charAt(2), equalTo('m'));
        assertThat(text.charAt(3), equalTo('p'));
        assertThat(text.charAt(4), equalTo('u'));
        assertThat(text.charAt(5), equalTo('s'));
    }

    @Test
    public void testNonBMPCharAt() {
        Text text = Text.fromString("wu\uD835\uDCC2pus");
        assertThat(text.charAt(0), equalTo('w'));
        assertThat(text.charAt(1), equalTo('u'));
        assertThat(text.charAt(2), equalTo('\uD835'));
        assertThat(text.charAt(3), equalTo('\uDCC2'));
        assertThat(text.charAt(4), equalTo('p'));
        assertThat(text.charAt(5), equalTo('u'));
        assertThat(text.charAt(6), equalTo('s'));
    }

    @Test
    public void testJodaConvertFromString() {
        Text fb = StringConvert.INSTANCE.convertFromString(Text.class, "foobar");
        assertThat(fb.toString(), equalTo("foobar"));
    }

    @Test
    public void testJodaConvertToString() {
        Text fb = Text.fromString("foobar");
        assertThat(StringConvert.INSTANCE.convertToString(fb),
                   equalTo("foobar"));
    }
}