package utils

import com.github.tminglei.slickpg._
import play.api.libs.json.{JsValue, Json}
import slick.basic.Capability
import slick.driver.JdbcProfile
import slick.jdbc.PostgresProfile

trait TransportPostgresProfile extends PostgresProfile
    with PgArraySupport
    with PgJsonSupport
    with PgNetSupport
    with PgLTreeSupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgPostGISSupport {
    def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

    // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
    override protected def computeCapabilities: Set[Capability] =
        super.computeCapabilities + JdbcProfile.capabilities.insertOrUpdate

    override val api = MyAPI

    object MyAPI extends API with ArrayImplicits
        with DateTimeImplicits
        with JsonImplicits
        with NetImplicits
        with LTreeImplicits
        with RangeImplicits
        with HStoreImplicits
        with SearchImplicits
        with PostGISImplicits
        with SearchAssistants {
        implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
        implicit val playJsonArrayTypeMapper =
            new AdvancedArrayJdbcType[JsValue](pgjson,
                (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
                (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
            ).to(_.toList)
    }

}

object TransportPostgresProfile extends TransportPostgresProfile
