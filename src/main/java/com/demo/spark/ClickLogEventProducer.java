package main.java.com.demo.spark;

import java.time.Instant;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class ClickLogEventProducer {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.0.50:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		props.put("schema.registry.url", "http://192.168.0.50:8081");

		String schemaString = "{\"namespace\": \"clicklog.avro\", \"type\": \"record\", " + "\"name\": \"click_log\","
				+ "\"fields\": [" + "{\"name\": \"user_id\", \"type\": \"int\"},"
				+ "{\"name\": \"time\", \"type\": \"string\"}," + "{\"name\": \"action\", \"type\": \"string\"},"
				+ "{\"name\": \"destination\", \"type\": \"string\"}," + "{\"name\": \"hotel\", \"type\": \"string\"}"
				+ "]}";
		
		Producer<String, GenericRecord> producer = new KafkaProducer<String, GenericRecord>(props);

		Schema.Parser parser = new Schema.Parser();
		Schema schema = parser.parse(schemaString);

		for (int nEvents = 11; nEvents <= 15; nEvents++) {

			int user_id = nEvents;
			String time = String.valueOf(Instant.now().getEpochSecond());
			String action = "clicked";
			String destination = "India";
			String hotel = "TajMahal";

			GenericRecord click_logs = new GenericData.Record(schema);
			click_logs.put("user_id", user_id);
			click_logs.put("time", time);
			click_logs.put("action", action);
			click_logs.put("destination", destination);
			click_logs.put("hotel", hotel);

			ProducerRecord<String, GenericRecord> data = new ProducerRecord<String, GenericRecord>("clicklog",click_logs);
			producer.send(data);
		}
		System.out.println("Message sent");
		producer.close();
	}
}