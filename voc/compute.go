// Copyright 2021 Fraunhofer AISEC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//           $$\                           $$\ $$\   $$\
//           $$ |                          $$ |\__|  $$ |
//  $$$$$$$\ $$ | $$$$$$\  $$\   $$\  $$$$$$$ |$$\ $$$$$$\    $$$$$$\   $$$$$$\
// $$  _____|$$ |$$  __$$\ $$ |  $$ |$$  __$$ |$$ |\_$$  _|  $$  __$$\ $$  __$$\
// $$ /      $$ |$$ /  $$ |$$ |  $$ |$$ /  $$ |$$ |  $$ |    $$ /  $$ |$$ | \__|
// $$ |      $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$\ $$ |  $$ |$$ |
// \$$$$$$\  $$ |\$$$$$   |\$$$$$   |\$$$$$$  |$$ |  \$$$   |\$$$$$   |$$ |
//  \_______|\__| \______/  \______/  \_______|\__|   \____/  \______/ \__|
//
// This file is part of Clouditor Community Edition.

package voc

// type HasLog interface {
// 	GetLog() *Log
// }

type HasAccessRestriction interface {
	GetAccessRestriction() *AccessRestriction
}

type IsCompute interface {
	IsResource
}

type ComputeResource struct {
	Resource
}

// Virtual Machine
type VirtualMachineResource struct {
	ComputeResource
	//NetworkInterfaceResource
	//BlockStorage

	Log *Log `json:"log"`
}

func (v *VirtualMachineResource) GetLog() *Log {
	return v.Log
}

type ContainerResource struct {
	ComputeResource
}

// Network Interface
type NetworkInterfaceResource struct {
	ComputeResource
	//NetworkService

	VmID              string             `json:"vmId"` // For debugging reasons
	AccessRestriction *AccessRestriction `json:"accessRestriction"`
}

func (n *NetworkInterfaceResource) GetAccessRestriction() *AccessRestriction {
	return n.AccessRestriction
}