/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;

import java.nio.ByteBuffer;

public class CaseCodeGen extends ApplicationAdapter {

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Case Change Code Generator");
        config.setWindowedMode(320, 120);
        config.setIdleFPS(1);
        config.setResizable(false);
        new Lwjgl3Application(new CaseCodeGen(), config);
    }

    public void create() {
        generateToUpperCode("CaseChange.txt");
        generateToLowerCode("CaseChange.txt");
        Gdx.app.exit();
    }
    /**
     * This appends to a file called {@code filename} containing a code snippet that can be pasted
     * into Java code as a char array.
     * @param filename the name of the text file to append to
     */
    public static void generateToUpperCode(String filename){
        StringBuilder sb = new StringBuilder(65536 + 400);
        sb.append('"');
        for (int i = 0; i < 65536; i++) {
            char b = Character.toUpperCase((char)i);
            switch (b) {
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    if (Character.isISOControl(b))
                        sb.append(String.format("\\%03o", (int)b));
                    else
                        sb.append(b);
                    break;
            }
        }

        sb.append("\".toCharArray();\n");
        Gdx.files.local(filename).writeString(sb.toString(), true, "UTF-8");
        System.out.println("Wrote toUpper snippet to " + filename);
    }
    /**
     * This appends to a file called {@code filename} containing a code snippet that can be pasted
     * into Java code as a char array.
     * @param filename the name of the text file to append to
     */
    public static void generateToLowerCode(String filename){
        StringBuilder sb = new StringBuilder(65536 + 400);
        sb.append('"');
        for (int i = 0; i < 65536; i++) {
            char b = Character.toLowerCase((char)i);
            switch (b) {
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    if (Character.isISOControl(b))
                        sb.append(String.format("\\%03o", (int)b));
                    else
                        sb.append(b);
                    break;
            }
        }

        sb.append("\".toCharArray();\n");
        Gdx.files.local(filename).writeString(sb.toString(), true, "UTF-8");
        System.out.println("Wrote tUoLower snippet to " + filename);
    }

}
