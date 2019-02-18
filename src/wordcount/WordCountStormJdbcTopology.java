package wordcount;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;


public class WordCountStormJdbcTopology {

    public static void main(String[] args) {

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new WordCountSpout());
        builder.setBolt("bolt", new CountBolt()).shuffleGrouping("spout");

        Config config = new Config();
        config.setNumWorkers(3);

        //本地模式
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("stormJdbcTopology", config, builder.createTopology());



    }

}
