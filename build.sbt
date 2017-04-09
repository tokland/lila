import NativePackagerHelper._

javaOptions in Universal ++= Seq(
  "-Dhttp.port=9663",
  "-Dpidfile.path=/dev/null",
  "-J-Xmx4000m",
  "-J-Xms512m"
)

mappings in Universal += file("data/GeoLite2-City.mmdb") -> "data/GeoLite2-City.mmdb"

mappings in Universal ++= directory("public")