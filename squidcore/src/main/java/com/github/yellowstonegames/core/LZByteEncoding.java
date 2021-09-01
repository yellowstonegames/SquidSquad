package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.ByteList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Compresses Strings to byte arrays (and back again) using a type of LZ-compression. This is very similar to
 * {@link LZSEncoding}, but instead of compressing Strings to Strings, this produces binary compressed data. This is
 * compatible with GWT, unlike many forms of compression, but where {@link LZSEncoding} can use a special alternate
 * version on GWT that is faster, this cannot. It makes up for it by producing less garbage on all platforms and
 * compressing with very little waste in terms of storage.
 */
public class LZByteEncoding {
    /**
     * Compresses a String using a type of LZ-compression and returns it as a byte array. If you are transmitting data
     * over the network or writing it directly to a binary file, this wastes fewer bits than using
     * {@link LZSEncoding#compressToUTF16(String)}. You can read the byte array this produces with
     * {@link #decompressFromBytes(byte[])}, which will produce the original String. This does very well if
     * {@code uncompressedStr} contains highly repetitive data, and fairly well in some cases where it doesn't.
     * @param uncompressedStr a String that you want to compress
     * @return a byte array containing the compressed data for {@code uncompressedStr}
     */
    public static byte[] compressToBytes(String uncompressedStr) {
        if (uncompressedStr == null) return null;
        if (uncompressedStr.isEmpty()) return new byte[0];
        final int bitsPerChar = 8;
        int i, value;
        HashMap<String, Integer> context_dictionary = new HashMap<>(256, 0.5f);
        HashSet<String> context_dictionaryToCreate = new HashSet<>(256, 0.5f);
        String context_c;
        String context_wc;
        String context_w = "";
        int context_enlargeIn = 2; // Compensate for the first entry which should not count
        int context_dictSize = 3;
        int context_numBits = 2;
        ByteList context_data = new ByteList(uncompressedStr.length() >>> 1);
        byte context_data_val = 0;
        int context_data_position = 0;
        int ii;

        char[] uncompressed = uncompressedStr.toCharArray();
        for (ii = 0; ii < uncompressed.length; ii++) {
            context_c = String.valueOf(uncompressed[ii]);
            if (!context_dictionary.containsKey(context_c)) {
                context_dictionary.put(context_c, context_dictSize++);
                context_dictionaryToCreate.add(context_c);
            }

            context_wc = context_w + context_c;
            if (context_dictionary.containsKey(context_wc)) {
                context_w = context_wc;
            } else {
                if (context_dictionaryToCreate.contains(context_w)) {
                    if ((value = context_w.charAt(0)) < 256) {
                        for (i = 0; i < context_numBits; i++) {
                            context_data_val = (byte)(context_data_val << 1);
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.add(context_data_val);
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                        }
                        for (i = 0; i < 8; i++) {
                            context_data_val = (byte)(context_data_val << 1 | (value & 1));
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.add(context_data_val);
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                            value >>>= 1;
                        }
                    } else {
                        value = 1;
                        for (i = 0; i < context_numBits; i++) {
                            context_data_val = (byte)((context_data_val << 1) | value);
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.add(context_data_val);
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                            value = 0;
                        }
                        value = context_w.charAt(0);
                        for (i = 0; i < 16; i++) {
                            context_data_val = (byte)((context_data_val << 1) | (value & 1));
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.add(context_data_val);
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                            value >>>= 1;
                        }
                    }
                    context_enlargeIn--;
                    if (context_enlargeIn == 0) {
                        context_enlargeIn = 1 << context_numBits++;
                    }
                    context_dictionaryToCreate.remove(context_w);
                } else {
                    value = context_dictionary.get(context_w);
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (byte)((context_data_val << 1) | (value & 1));
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.add(context_data_val);
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>>= 1;
                    }

                }
                context_enlargeIn--;
                if (context_enlargeIn == 0) {
                    context_enlargeIn = 1 << context_numBits++;
                }
                // Add wc to the dictionary.
                context_dictionary.put(context_wc, context_dictSize++);
                context_w = context_c;
            }
        }

        // Output the code for w.
        if (!context_w.isEmpty()) {
            if (context_dictionaryToCreate.contains(context_w)) {
                if (context_w.charAt(0) < 256) {
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (byte)(context_data_val << 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.add(context_data_val);
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                    }
                    value = context_w.charAt(0);
                    for (i = 0; i < 8; i++) {
                        context_data_val = (byte)((context_data_val << 1) | (value & 1));
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.add(context_data_val);
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>>= 1;
                    }
                } else {
                    value = 1;
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (byte)((context_data_val << 1) | value);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.add(context_data_val);
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value = 0;
                    }
                    value = context_w.charAt(0);
                    for (i = 0; i < 16; i++) {
                        context_data_val = (byte)((context_data_val << 1) | (value & 1));
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.add(context_data_val);
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>>= 1;
                    }
                }

                context_dictionaryToCreate.remove(context_w);
            } else {
                value = context_dictionary.get(context_w);
                for (i = 0; i < context_numBits; i++) {
                    context_data_val = (byte)((context_data_val << 1) | (value & 1));
                    if (context_data_position == bitsPerChar - 1) {
                        context_data_position = 0;
                        context_data.add(context_data_val);
                        context_data_val = 0;
                    } else {
                        context_data_position++;
                    }
                    value >>>= 1;
                }

            }
        }

        // Mark the end of the stream
        value = 2;
        for (i = 0; i < context_numBits; i++) {
            context_data_val = (byte)((context_data_val << 1) | (value & 1));
            if (context_data_position == bitsPerChar - 1) {
                context_data_position = 0;
                context_data.add(context_data_val);
                context_data_val = 0;
            } else {
                context_data_position++;
            }
            value >>>= 1;
        }

        // Flush the last char
        while (true) {
            context_data_val = (byte)(context_data_val << 1);
            if (context_data_position == bitsPerChar - 1) {
                context_data.add(context_data_val);
                break;
            } else
                context_data_position++;
        }
        return context_data.shrink();
    }

    /**
     * Decompresses a byte array produced (at some point) by {@link #compressToBytes(String)}, getting the original
     * String back that was given to compressToBytes().
     * @param compressedBytes a byte array produced by {@link #compressToBytes(String)}
     * @return the String that was originally passed to {@link #compressToBytes(String)}
     */
    public static String decompressFromBytes(byte[] compressedBytes) {
        if(compressedBytes == null)
            return null;
        final int length = compressedBytes.length;
        if(length == 0)
            return "";
        final int resetValue = 128;
        ArrayList<String> dictionary = new ArrayList<>(256);
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = resetValue, index = 1, resb, maxpower, power;
        String entry, w, c;
        StringBuilder res = new StringBuilder(length);
        char bits;
        int val = compressedBytes[0];

        for (char i = 0; i < 3; i++) {
            dictionary.add(String.valueOf(i));
        }

        bits = 0;
        maxpower = 2;
        power = 0;
        while (power != maxpower) {
            resb = val & position;
            position >>>= 1;
            if (position == 0) {
                position = resetValue;
                val = compressedBytes[index++];
            }
            bits |= (resb != 0 ? 1 : 0) << power++;
        }

        switch (bits) {
            case 0:
                bits = 0;
                maxpower = 8;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = compressedBytes[index++];
                    }
                    bits |= (resb != 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            case 1:
                bits = 0;
                maxpower = 16;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = compressedBytes[index++];
                    }
                    bits |= (resb != 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            default:
                return "";
        }
        dictionary.add(c);
        w = c;
        res.append(w);
        while (true) {
            if (index > length) {
                return "";
            }
            int cc = 0;
            maxpower = numBits;
            power = 0;
            while (power != maxpower) {
                resb = val & position;
                position >>>= 1;
                if (position == 0) {
                    position = resetValue;
                    val = compressedBytes[index++];
                }
                cc |= (resb != 0 ? 1 : 0) << power++;
            }
            switch (cc) {
                case 0:
                    bits = 0;
                    maxpower = 8;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = compressedBytes[index++];
                        }
                        bits |= (resb != 0 ? 1 : 0) << power++;
                    }

                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 1:
                    bits = 0;
                    maxpower = 16;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = compressedBytes[index++];
                        }
                        bits |= (resb != 0 ? 1 : 0) << power++;
                    }
                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 2:
                    return res.toString();
            }

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

            if (cc < dictionary.size() && dictionary.get(cc) != null) {
                entry = dictionary.get(cc);
            } else {
                if (cc == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    return "";
                }
            }
            res.append(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.add(w + entry.charAt(0));
            dictSize++;
            enlargeIn--;

            w = entry;

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

        }
    }

    /**
     * A convenience method to get a printable version of one of the byte arrays this can produce.
     * You can use {@link #readJoined(String)} to get the original byte array back from a String this returned.
     * @param elements an array or varargs of bytes, typically produced by {@link #compressToBytes(String)}
     * @return a readable String containing a verbose representation of the given bytes
     */
    public static String join(byte... elements) {
        return StringTools.join(",", elements);
    }

    /**
     * Given a String produced by {@link #join(byte...)}, this gets the original byte array back and returns it.
     * @param source a String produced by {@link #join(byte...)}
     * @return the byte array used by join originally to produce {@code source}
     */
    public static byte[] readJoined(String source) {
        if(source == null) return null;
        final int length = source.length();
        if(length == 0) return new byte[0];
        int amount = 1, idx = -1;
        while ((idx = source.indexOf(',', idx+1)) >= 0)
            ++amount;
        byte[] res = new byte[amount];
        idx = 0;
        for (int i = 0; i < amount; i++) {
            res[i] = Base.BASE10.readByte(source, idx, idx = source.indexOf(',', idx+1));
        }
        return res;
    }

}
