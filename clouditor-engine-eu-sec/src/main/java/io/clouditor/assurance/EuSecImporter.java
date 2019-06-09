/*
 * Copyright (c) 2016-2019, Fraunhofer AISEC. All rights reserved.
 *
 *
 *            $$\                           $$\ $$\   $$\
 *            $$ |                          $$ |\__|  $$ |
 *   $$$$$$$\ $$ | $$$$$$\  $$\   $$\  $$$$$$$ |$$\ $$$$$$\    $$$$$$\   $$$$$$\
 *  $$  _____|$$ |$$  __$$\ $$ |  $$ |$$  __$$ |$$ |\_$$  _|  $$  __$$\ $$  __$$\
 *  $$ /      $$ |$$ /  $$ |$$ |  $$ |$$ /  $$ |$$ |  $$ |    $$ /  $$ |$$ |  \__|
 *  $$ |      $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$\ $$ |  $$ |$$ |
 *  \$$$$$$\  $$ |\$$$$$   |\$$$$$   |\$$$$$$  |$$ |  \$$$   |\$$$$$   |$$ |
 *   \_______|\__| \______/  \______/  \_______|\__|   \____/  \______/ \__|
 *
 * This file is part of Clouditor Community Edition.
 *
 * Clouditor Community Edition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Clouditor Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * long with Clouditor Community Edition.  If not, see <https://www.gnu.org/licenses/>
 */

package io.clouditor.assurance;

import java.util.List;

public class EuSecImporter extends CertificationImporter {

  @Override
  public Certification load() {
    Certification certification = new Certification();
    certification.setId(this.getName());
    certification.setDescription(
        "The Cloud Security Alliance Cloud Controls Matrix (CCM) is specifically designed to provide fundamental security principles to guide cloud vendors and to assist prospective cloud customers in assessing the overall security risk of a cloud provider. The CSA CCM provides a controls framework that gives detailed understanding of security concepts and principles that are aligned to the Cloud Security Alliance guidance in 13 domains. The foundations of the Cloud Security Alliance Controls Matrix rest on its customized relationship to other industry-accepted security standards, regulations, and controls frameworks such as the ISO 27001/27002, ISACA COBIT, PCI, NIST, Jericho Forum and NERC CIP and will augment or provide internal control direction for service organization control reports attestations provided by cloud providers.");
    certification.setPublisher("Cloud Security Alliance");
    certification.setWebsite(
        "https://cloudsecurityalliance.org/working-groups/cloud-controls-matrix");

    Control grm02 = new Control();
    grm02.setControlId("GRM-02");
    grm02.setName("Data Focus Risk Assessments");
    grm02.setDescription(
        "Risk assessments associated with data governance requirements shall be conducted at planned intervals and shall consider the following: • Awareness of where sensitive data is stored and transmitted across applications, databases, servers, and network infrastructure • Compliance with defined retention periods and end-of-life disposal requirements • Data classification and protection from unauthorized use, access, loss, destruction, and falsification");
    grm02.setDomain(new Domain("Governance and Risk Management"));
    grm02.setAutomated(true);

    Control ekm04 = new Control();
    ekm04.setControlId("EKM-04");
    ekm04.setName("Storage and Access");
    ekm04.setDescription(
        "Platform and data-appropriate encryption (e.g., AES-256) in open/validated formats and standard algorithms shall be required. Keys shall not be stored in the cloud (i.e., at the cloud provider in question), but maintained by the cloud consumer or trusted key management provider. Key management and key usage shall be separated duties.");
    ekm04.setDomain(new Domain("Encryption & Key Management"));
    ekm04.setAutomated(true);

    Control iam12 = new Control();
    iam12.setControlId("IAM-12");
    iam12.setName("User ID Credentials");
    iam12.setDescription(
        "\"Internal corporate or customer (tenant) user account credentials shall be restricted as per the following, ensuring appropriate identity, entitlement, and access management and in accordance with established policies and procedures:\n"
            + "* Identity trust verification and service-to-service application (API) and information processing interoperability (e.g., SSO and Federation)\n"
            + "* Account credential lifecycle management from instantiation through revocation\n"
            + "* Account credential and/or identity store minimization or re-use when feasible\n"
            + "* Adherence to industry acceptable and/or regulatory compliant authentication, authorization, and accounting (AAA) rules (e.g., strong/multi-factor, expireable, non-shared authentication secrets)\"");
    iam12.setDomain(new Domain("Identity & Access Management"));
    iam12.setAutomated(true);

    certification.setControls(List.of(grm02, ekm04, iam12));

    return certification;
  }

  public String getName() {
    return "Cloud Control Matrix";
  }
}
