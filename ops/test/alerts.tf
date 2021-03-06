module "metric_alerts" {
  source              = "../services/alerts/app_service_metrics"
  env                 = local.env
  app_service_plan_id = module.simple_report_api.app_service_plan_id
  app_service_id      = module.simple_report_api.app_service_id
  action_group_id     = data.terraform_remote_state.global.outputs.slack_alert_action_id
  rg_name             = data.azurerm_resource_group.rg.name
  tags                = local.management_tags
  disabled_alerts     = ["cpu_util", "mem_util"]

  mem_threshold = 80
}
