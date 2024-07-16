pluginManagement {
 repositories {
  gradlePluginPortal()
  mavenCentral()
 }
}

rootProject.name = "cf-lob-platform"

include (
 ":support",
 ":accounting_reporting_core",
 ":blockchain_publisher",
 ":netsuite_altavia_erp_adapter",
 ":notification_gateway",
 ":organisation",
)
