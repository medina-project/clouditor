# Install Monitoring Agent Extension on VMs

Monitoring agent extensions should be installed on Azure VMs.

```ccl
VirtualMachine has properties.type == "MicrosoftMonitoringAgent" in any extensions
# TODO: support AND expression, since the provisioning state also must be "succeeded"
```

## Controls

* CIS Microsoft Azure Foundations Benchmark/Azure 7.1
