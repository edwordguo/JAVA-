package wordcount;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import java.util.Map;
import java.util.Random;


public class WordCountSpout extends BaseRichSpout {

    private SpoutOutputCollector collector;

    private static final String[] words = new String[]{"aaa","bbb","cad","adv","ed","eaf"};

    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.collector = spoutOutputCollector;
    }

    public void nextTuple() {
        Random random = new Random();
        String word = words[random.nextInt(words.length)];
        //发射出去
        this.collector.emit(new Values(word));
        System.out.println("emit==="+word);
//        Utils.sleep(1000);
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("word"));
    }
}
