package org.apache.s4.example.twitter;

import java.net.ServerSocket;

import org.apache.s4.base.Event;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

public class TopicExtractorPE extends ProcessingElement {

    static private ServerSocket serverSocket;
    Streamable<Event> downStream;
    static Logger logger = LoggerFactory.getLogger(TopicExtractorPE.class);

    public TopicExtractorPE(App app) {
        super(app);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate() {

    }

    public void setDownStream(Streamable<Event> stream) {
        this.downStream = stream;
    }

    public void onEvent(Event event) {
        String text = event.get("statusText", String.class);
        logger.trace("event text [{}]", text);
        if (text.contains("#")) {
            Iterable<String> split = Splitter.on("#").omitEmptyStrings().trimResults()
                    .split(text.substring(text.indexOf("#") + 1, text.length()));
            for (String topic : split) {
                String topicOnly = topic.split(" ")[0];
                Event event2 = new Event();
                event2.put("topic", String.class, topicOnly);
                event2.put("count", Integer.class, 1);
                downStream.put(event2);
            }
        }
    }

    @Override
    protected void onRemove() {
        // TODO Auto-generated method stub

    }

}
