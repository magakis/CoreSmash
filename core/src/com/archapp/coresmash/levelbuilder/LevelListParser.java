package com.archapp.coresmash.levelbuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Comparator;

import static org.xmlpull.v1.XmlPullParser.NO_NAMESPACE;

public class LevelListParser {
    private static final String LEVEL_LIST = "levelList";
    private static final String LEVEL = "level";

    public static final Comparator<RegisteredLevel> compLevel;
    public static final Comparator<RegisteredLevel> compNames;

    private final Array<RegisteredLevel> helperArray;

    private static final FilenameFilter xmlFileFilter;

    static {
        compLevel = new Comparator<RegisteredLevel>() {
            @Override
            public int compare(RegisteredLevel level, RegisteredLevel t1) {
                if (level.num == 0 && t1.num == 0) return 0;
                else if (level.num == 0) return 1;
                else if (t1.num == 0) return -1;
                else return Integer.compare(level.num, t1.num);
            }
        };

        compNames = new Comparator<RegisteredLevel>() {
            @Override
            public int compare(RegisteredLevel level, RegisteredLevel t1) {
                return String.CASE_INSENSITIVE_ORDER.compare(level.name, t1.name);
            }
        };

        xmlFileFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                // TODO(25/5/2018): Enable this at some point? !s.startsWith("_editor_") &&
                return !s.startsWith("_editor_") && s.toLowerCase().endsWith(".xml");
            }
        };
    }

    public LevelListParser() {
        helperArray = new Array<>();
    }

    public void serializeLevelList(Array<RegisteredLevel> registeredLevels) {
        XmlSerializer serializer = XmlManager.getSerializer();
        FileHandle file = Gdx.files.external("/CoreSmash/levels/level_list");

        registeredLevels.sort(compLevel);

        try {

            serializer.setOutput(file.writer(false));
            serializer.startDocument("UTF-8", false);

            serializer.startTag(NO_NAMESPACE, LEVEL_LIST);

            for (RegisteredLevel level : registeredLevels) {
                if (level.num == 0) break;

                serializer.startTag(NO_NAMESPACE, LEVEL);
                serializer.attribute(NO_NAMESPACE, "num", String.valueOf(level.num));
                serializer.attribute(NO_NAMESPACE, "name", level.name);
                serializer.endTag(NO_NAMESPACE, LEVEL);
            }

            serializer.endTag(NO_NAMESPACE, LEVEL_LIST);
            serializer.endDocument();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Outputs ONLY the assigned levels
     */
    public void parseAssignedLevels(Array<RegisteredLevel> output, Source source) {
        if (source == null) throw new RuntimeException("Source type is required!");


        FileHandle file;
        if (source.equals(Source.EXTERNAL)) {
            file = Gdx.files.external("/CoreSmash/levels/level_list");
        } else {
            file = Gdx.files.internal("levels/level_list");
        }
        if (!file.exists()) return;

        output.clear();

        XmlPullParser parser = XmlManager.getParser();
        try {
            parser.setInput(file.reader());
            int type = parser.getEventType();
            do {
                switch (type) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(LEVEL)) {
                            String text = parser.getAttributeValue(NO_NAMESPACE, "num");
                            int num = text == null || text.isEmpty() ? 0 : Integer.parseInt(text);
                            text = parser.getAttributeValue(NO_NAMESPACE, "name");

                            output.add(new RegisteredLevel(num, text));
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                }
                type = parser.next();
            } while (type != XmlPullParser.END_DOCUMENT);

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Outputs a list with all the levels found in the levels folder
     */
    private void getFoundLevels(Array<RegisteredLevel> output) {
        String[] files = Gdx.files.external("/CoreSmash/levels/").file().list(xmlFileFilter);

        output.ensureCapacity(files.length);
        for (String filename : files) {
            output.add(new RegisteredLevel(0, filename.substring(0, filename.length() - 4)));
        }
    }

    public void getAllLevels(Array<RegisteredLevel> output) {
        helperArray.clear();

        getFoundLevels(output);
        parseAssignedLevels(helperArray, Source.EXTERNAL);
        for (RegisteredLevel assigned : helperArray) {
            int index = output.indexOf(assigned, false);
            if (index != -1) {
                output.get(index).num = assigned.num;
            }
        }
    }

    public enum Source {
        INTERNAL, EXTERNAL
    }

    public static class RegisteredLevel {
        static StringBuilder stringBuilder;

        public int num;
        public String name;

        static {
            stringBuilder = new StringBuilder();
        }

        public RegisteredLevel(int num, String fileName) {
            this.num = num;
            this.name = fileName;
        }

        @Override
        public String toString() {
            stringBuilder.setLength(0);
            return num == 0 ? name :
                    stringBuilder.append('(').append(num).append(')').append("  ").append(name).toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RegisteredLevel) {
                return name.equals(((RegisteredLevel) o).name);
            }
            return false;
        }
    }
}
