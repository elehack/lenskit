package org.lenskit.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Utf8;
import org.joda.convert.FromString;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Arrays;

/**
 * UTF-8 encoded text string.
 */
public final class Text implements CharSequence, Serializable {
    private static final long serialVersionUID = 1L;
    private static final byte[] UTF8_START_MASKS = {
            0, 0,
            (byte) 0x1F,
            (byte) 0x0F,
            (byte) 0x07
    };
    private static final byte UTF8_CONTINUATION_MASK = 0x3F;

    private final byte[] data;
    private transient int length;

    /**
     * Create a new Text instance from bytes.
     * @param bytes The bytes (must be valid UTF-8).  The array of bytes is copied.
     */
    public Text(byte[] bytes) {
        Preconditions.checkArgument(Utf8.isWellFormed(bytes), "input bytes must be UTF-8");
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Create a new Text instance from a string.
     * @param string The string.
     */
    @FromString
    public static Text fromString(String string) {
        return new Text(Normalizer.normalize(string, Normalizer.Form.NFC)
                                  .getBytes(Charsets.UTF_8));
    }

    public int size() {
        return data.length;
    }

    @Override
    public int length() {
        if (data.length == 0) return 0;

        if (length == 0) {
            length = utf8StringLength(data);
        }
        return length;
    }

    @Override
    public char charAt(int index) {
        int i = findCharStart(index);
        if (i >= 0 && isAscii(data[i])) {
            return (char) data[i];
        } else {
            boolean second = false;
            if (i < 0) {
                second = true;
                i = -i - 1;
            }
            int cp = codePointAt(i);
            if (second) {
                assert Character.isSupplementaryCodePoint(cp);
                return Character.lowSurrogate(cp);
            } else if (Character.isSupplementaryCodePoint(cp)) {
                return Character.highSurrogate(cp);
            } else {
                return (char) cp;
            }
        }
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }

    /**
     * Find the start of a character by index.
     * @param index The character index.
     * @return The index into the data array where this character starts, sort-of. If positive, will be the index of
     * character.  If a negative value `i`, that means that the requested character is the second (low-surrogate) of a surrogate
     * pair, the UTF-8 encoding of which starts at `-i - 1`
     */
    int findCharStart(int index) {
        Preconditions.checkElementIndex(index, length());

        int n = 0;
        int i = 0;
        while (n < index) {
            int len = utf8ByteCount(data[i]);
            if (len <= 3) {
                // BMP
                n += 1;
                i += len;
            } else if (n == index - 1) {
                // surrogate pair, want 2nd half
                return -i - 1;
            } else {
                // surrogate pair but skip it
                n += 2;
                i += len;
            }
        }
        return i;
    }

    /**
     * Get the code point at a given position in the byte stream.
     * @param i The byte stream.
     * @return The code point.
     */
    int codePointAt(int i) {
        byte b = data[i];
        assert (b & 0xC0) != 0x80; // we aren't pointing in the middle
        int l = utf8ByteCount(b);
        if (l == 1) {
            // ASCII character, yippee
            return b;
        }

        int cp = b & UTF8_START_MASKS[l];
        // accumulate the code point
        for (int j = 1; j < l; j++) {
            cp <<= 6;
            cp |= data[i+j] & UTF8_CONTINUATION_MASK;
        }
        return cp;
    }

    @Override
    public String toString() {
        return new String(data, Charsets.UTF_8);
    }

    static boolean isAscii(byte b) {
        return (b & 0x80) == 0;
    }

    static int utf8ByteCount(byte b) {
        if (isAscii(b)) return 1;

        int n = 1;
        byte cb = (byte)(b << 1);
        while ((cb & 0x80) != 0) {
            n += 1;
            cb <<= 1;
        }
        assert n > 1;
        return n;
    }

    static int utf8StringLength(byte[] bytes) {
        int n = 0;
        int i = 0;
        while (i < bytes.length) {
            int cpl = utf8ByteCount(bytes[i]);
            if (cpl <= 3) {
                // char <= U+FFFF, guaranteed BMP if valid
                i += cpl;
                n += 1;
            } else {
                // char >= U+FFFF, will need to be encoded as a surrogate
                i += cpl;
                n += 2;
            }
        }
        return n;
    }
}
