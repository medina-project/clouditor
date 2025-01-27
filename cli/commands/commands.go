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

package commands

import (
	"clouditor.io/clouditor/cli"
	"clouditor.io/clouditor/cli/commands/assessmentresult"
	"clouditor.io/clouditor/cli/commands/cloud"
	"clouditor.io/clouditor/cli/commands/completion"
	"clouditor.io/clouditor/cli/commands/evidence"
	"clouditor.io/clouditor/cli/commands/login"
	"clouditor.io/clouditor/cli/commands/metric"
	"clouditor.io/clouditor/cli/commands/requirement"
	"clouditor.io/clouditor/cli/commands/resource"
	"clouditor.io/clouditor/cli/commands/service"
	"clouditor.io/clouditor/cli/commands/tool"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

// AddCommands adds all subcommands
func AddCommands(cmd *cobra.Command) {
	cmd.AddCommand(
		// commands for resources
		login.NewLoginCommand(),
		metric.NewMetricCommand(),
		requirement.NewRequirementCommand(),
		tool.NewToolCommand(),
		resource.NewResourceCommand(),
		evidence.NewEvidenceCommand(),
		assessmentresult.NewAssessmentResultCommand(),
		completion.NewCompletionCommand(),
		cloud.NewCloudCommand(),
		// command consisting of service commands
		service.NewServiceCommand(),
	)

	cmd.PersistentFlags().StringP(cli.SessionFolderFlag, "s", cli.DefaultSessionFolder, "the directory where the session will be saved and loaded from")
	_ = viper.BindPFlag(cli.SessionFolderFlag, cmd.PersistentFlags().Lookup(cli.SessionFolderFlag))
}
