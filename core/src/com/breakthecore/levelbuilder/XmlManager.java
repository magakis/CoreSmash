package com.breakthecore.levelbuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntMap;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static org.xmlpull.v1.XmlPullParser.NO_NAMESPACE;


/* Maybe this should not be a static class in the future for multithreading? */
public final class XmlManager {
    private static XmlSerializer serializer = new KXmlSerializer();
    private static XmlPullParser parser = new KXmlParser();

    private XmlManager() {
    }

    static <P> void createElement(String name, P value) throws IOException {
        serializer.startTag(NO_NAMESPACE, name).text(String.valueOf(value)).endTag(NO_NAMESPACE, name);
    }

    public static XmlSerializer getSerializer() {
        return serializer;
    }

    public static XmlPullParser getParser() {
        return parser;
    }

    public static boolean fileExists(String name) {
        FileHandle file = Gdx.files.external("/CoreSmash/levels/" + name + ".xml");
        return file.exists();
    }

    static IntMap<String> loadLevelNames() {
        FileHandle file = Gdx.files.external("/CoreSmash/level_list.xml");
        try (Reader reader = file.reader()) {
            parser.setInput(reader);
            int type = parser.getEventType();
            do {
                switch (type) {
                    case XmlPullParser.START_TAG:
                        switch (parser.getName()) {
                            case "list":
                                break;
                            case "entry":
                                //parse entry
                        }
                        break;
                }
                type = parser.next();
            } while (type != XmlPullParser.END_DOCUMENT);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void saveLevelNames(IntMap<String> nameList) {
        FileHandle file = Gdx.files.external("/CoreSmash/level_list.xml");

        try (Writer writer = file.writer(false)) {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", false);
            serializer.startTag(NO_NAMESPACE, "list");
            for (IntMap.Entry<String> entry : nameList.entries()) {
                serializer.startTag(NO_NAMESPACE, "entry");
                serializer.attribute(NO_NAMESPACE, "lvl", String.valueOf(entry.key));
                serializer.attribute(NO_NAMESPACE, "file", entry.value);
                serializer.endTag(NO_NAMESPACE, "list");
            }
            serializer.endTag(NO_NAMESPACE, "levels");
            serializer.endDocument();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
