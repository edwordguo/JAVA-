package wordcount;

import com.google.common.collect.Maps;
import org.apache.storm.jdbc.common.Column;
import org.apache.storm.jdbc.common.ConnectionProvider;
import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.common.JdbcClient;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountBolt extends BaseRichBolt {

    private OutputCollector collector;
    private JdbcClient jdbcClient;
    private ConnectionProvider connectionProvider;

    Map<String,Integer> map = new HashMap<String,Integer>();

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        Map hikariConfigMap = Maps.newHashMap();
        hikariConfigMap.put("dataSourceClassName","com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikariConfigMap.put("dataSource.url", "jdbc:mysql://localhost/mc_config");
        hikariConfigMap.put("dataSource.user","root");
        hikariConfigMap.put("dataSource.password","123456");
        connectionProvider = new HikariCPConnectionProvider(hikariConfigMap);
        //对数据库连接进行初始化
        connectionProvider.prepare();
        jdbcClient = new JdbcClient(connectionProvider,30);
    }

    /**
     * 方法名：
     * 功能：业务逻辑
     *     1.获取每个单词
     *     2.对所有单词进行汇总
     *     3.输出
     */
    public void execute(Tuple tuple) {
        String word = tuple.getStringByField("word");
        Integer count = map.get(word);
        if(count == null){
            count = 0;
        }
        count++;
        map.put(word,count);
        //查询该word是否存在
        List<Column> list = new ArrayList<Column>();
        //创建一列将值传入   列名  值  值的类型
        list.add(new Column("word",word, Types.VARCHAR));
        List<List<Column>> select = jdbcClient.select("select word from wc where word = ?",list);
        //计算出查询的条数
        Long n = select.stream().count();
        if(n>=1){
            //update
            jdbcClient.executeSql("update wc set word_count = "+map.get(word)+" where word = '"+word+"'");
        }else{
            //insert
            jdbcClient.executeSql("insert into wc values( '"+word+"',"+map.get(word)+")");
        }

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //后面jdbc insert bolt直接把这里的输出写Mysql里去了，所以这里的fileds的名字要跟mysql表的字段名字对应
        declarer.declare(new Fields("word","word_count"));
    }
}
