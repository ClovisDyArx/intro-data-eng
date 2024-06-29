import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import java.nio.file.Paths
import scala.io.Source
import java.net.{InetSocketAddress, ServerSocket, Socket}
import java.io.{BufferedWriter, OutputStreamWriter}

case class DroneInfo(
  id: String,
  created: String,
  latitude: Float,
  longitude: Float,
  event_type: String,
  danger_level: Int,
  survivors: Int
)

object DroneDataAnalyticsApp {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setAppName("DroneDataAnalyticsApp")
      .setIfMissing("spark.master", "local[*]")

    val spark = SparkSession.builder.config(sparkConf).getOrCreate()

    val dataPath = Paths.get("../store_data/data/output").toAbsolutePath.toString()
    val droneDF = spark.read.parquet(dataPath)

    // Converts data to JSON for the UI
    val summaryStats = droneDF.describe("danger_level", "survivors").toJSON.collect().mkString("[", ",", "]")
    val eventTypeCounts = droneDF.groupBy("event_type").count().toJSON.collect().mkString("[", ",", "]")
    val avgDangerLevel = droneDF.groupBy("event_type").agg(org.apache.spark.sql.functions.avg("danger_level")).toJSON.collect().mkString("[", ",", "]")
    //val totalSurvivors = droneDF.agg(org.apache.spark.sql.functions.sum("survivors")).toJSON.collect().mkString("[", ",", "]")
    
    // HTML UI
    val htmlContent = s"""
    |<!DOCTYPE html>
    |<html lang="en">
    |<head>
    |    <meta charset="UTF-8">
    |    <title>Drone Data Analytics</title>
    |    <style>
    |        body { font-family: Arial, sans-serif; }
    |        table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }
    |        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    |        th { background-color: #f2f2f2; }
    |    </style>
    |    <script>
    |        function loadData() {
    |            const summaryStats = $summaryStats;
    |            const eventTypeCounts = $eventTypeCounts;
    |            const avgDangerLevel = $avgDangerLevel;
    |
    |            document.getElementById('summaryStatistics').innerHTML = createTable(summaryStats, 'Summary Statistics');
    |            document.getElementById('eventTypeCounts').innerHTML = createTable(eventTypeCounts, 'Event Type Counts');
    |            document.getElementById('averageDangerLevel').innerHTML = createTable(avgDangerLevel, 'Average Danger Level');
    |        }
    |
    |        function createTable(data, title) {
    |            let html = '<h2>' + title + '</h2><table>';
    |            html += '<tr>';
    |            for (const key in data[0]) {
    |                html += '<th>' + key + '</th>';
    |            }
    |            html += '</tr>';
    |            data.forEach(row => {
    |                html += '<tr>';
    |                for (const key in row) {
    |                    html += '<td>' + row[key] + '</td>';
    |                }
    |                html += '</tr>';
    |            });
    |            html += '</table>';
    |            return html;
    |        }
    |    </script>
    |</head>
    |<body onload="loadData()">
    |    <h1>Drone Data Analytics</h1>
    |    <div id="summaryStatistics"></div>
    |    <div id="eventTypeCounts"></div>
    |    <div id="averageDangerLevel"></div>
    |</body>
    |</html>
    |""".stripMargin

    // HTTP server
    val server = new ServerSocket(8080)
    println("Server is running on http://localhost:8080")
    while (true) {
      val socket = server.accept()
      new Thread(new Runnable {
        def run(): Unit = {
          handleRequest(socket, htmlContent)
        }
      }).start()
    }

    spark.stop()
  }

  def handleRequest(socket: Socket, content: String): Unit = {
    val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
    try {
      val request = Source.fromInputStream(socket.getInputStream).getLines().take(1).toList.headOption.getOrElse("")
      if (request.startsWith("GET / ")) {
        out.write("HTTP/1.1 200 OK\r\n")
        out.write("Content-Type: text/html\r\n")
        out.write("Content-Length: " + content.length + "\r\n")
        out.write("\r\n")
        out.write(content)
      } else {
        out.write("HTTP/1.1 404 Not Found\r\n")
        out.write("\r\n")
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      out.close()
      socket.close()
    }
  }
}
