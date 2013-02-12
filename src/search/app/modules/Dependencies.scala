package modules

import com.tzavellas.sse.guice.ScalaModule
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.{Node, NodeBuilder}
import play.api.Play
import play.api.Play.current

class Dependencies extends ScalaModule {
  def configure() {
    bind[Node].toInstance({
      val path = Play.configuration.getString("elasticsearch.path").getOrElse(throw new Exception("ElasticSearch not set.  Check you have environment variable $DFID_ELASTICSEARCH_PATH configured"))

      val settings = ImmutableSettings.settingsBuilder()
        .put("node.name", "dfidsearch-node")
        .put("path.data", path)
        .put("http.enabled", false)
        .put("index.blocks.read_only", true)

      val node = NodeBuilder.nodeBuilder()
        .settings(settings)
        .clusterName("dfidsearch-cluster")
        .data(true).local(true).node()

      node
    })
  }
}
