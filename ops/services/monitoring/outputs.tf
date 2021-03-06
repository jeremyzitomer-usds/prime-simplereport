output "app_insights_id" {
  value = azurerm_application_insights.app_insights.id
}

output "app_insights_app_id" {
  value = azurerm_application_insights.app_insights.app_id
}

output "app_insights_instrumentation_key" {
  value = azurerm_application_insights.app_insights.instrumentation_key
}

output "log_analytics_workspace_id" {
  value = data.azurerm_log_analytics_workspace.law.id
}
