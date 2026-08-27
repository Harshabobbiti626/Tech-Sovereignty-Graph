package com.wexa.sovereignty.seed;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Loads the demo scenario. Everything is MERGE-based, so re-running refreshes
 * properties in place instead of duplicating data. Constraints and indexes are
 * created before any data is written — on a 256 MB c0 instance lookups should
 * hit those, not scans.
 *
 * Kept free of Spring CLI concerns so the Testcontainers suite can call it directly.
 */
@Component
public class GraphSeeder {

    private static final Logger log = LoggerFactory.getLogger(GraphSeeder.class);

    private final Driver driver;

    public GraphSeeder(Driver driver) {
        this.driver = driver;
    }

    public void seed() {
        createSchema();
        load();
    }

    public List<Record> counts() {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run(COUNTS).list());
        }
    }

    // ---------------------------------------------------------------- schema

    private static final String[] SCHEMA_STATEMENTS = {
            "CREATE CONSTRAINT unique_identity_email IF NOT EXISTS FOR (i:Identity) REQUIRE i.email IS UNIQUE",
            "CREATE CONSTRAINT unique_group_name IF NOT EXISTS FOR (g:Group) REQUIRE g.name IS UNIQUE",
            "CREATE CONSTRAINT unique_resource_name IF NOT EXISTS FOR (r:Resource) REQUIRE r.name IS UNIQUE",
            "CREATE INDEX resource_sensitivity_idx IF NOT EXISTS FOR (r:Resource) ON (r.sensitivity)"
    };

    private static final String MERGE_IDENTITY =
            "MERGE (i:Identity {id: $id}) SET i.email = $email, i.role = $role, i.status = $status";
    private static final String MERGE_GROUP =
            "MERGE (g:Group {id: $id}) SET g.name = $name";
    private static final String MERGE_RESOURCE =
            "MERGE (r:Resource {id: $id}) SET r.name = $name, r.sensitivity = $sensitivity";

    // relationship types cannot be parameterized, hence one statement per type
    private static final String MERGE_MEMBER_OF =
            "MATCH (a {id: $from}), (b {id: $to}) MERGE (a)-[:MEMBER_OF]->(b)";
    private static final String MERGE_INHERITS =
            "MATCH (a {id: $from}), (b {id: $to}) MERGE (a)-[:INHERITS]->(b)";
    private static final String MERGE_DEPENDS_ON =
            "MATCH (a {id: $from}), (b {id: $to}) MERGE (a)-[:DEPENDS_ON]->(b)";
    private static final String MERGE_ACCESS =
            "MATCH (a {id: $from}), (b {id: $to}) MERGE (a)-[x:ACCESS]->(b) SET x.level = $level";

    private static final String COUNTS = "MATCH (n) RETURN labels(n)[0] AS label, count(*) AS count";

    // ---------------------------------------------------------------- dataset

    private record SeedIdentity(String id, String email, String role, String status) {
    }

    private record SeedGroup(String id, String name) {
    }

    private record SeedResource(String id, String name, String sensitivity) {
    }

    private record Link(String from, String to) {
    }

    private record Access(String from, String to, String level) {
    }

    private static final List<SeedIdentity> IDENTITIES = List.of(
            new SeedIdentity("id_cto", "sarah.kim@wexa.ai", "CTO", "Active"),
            new SeedIdentity("id_sec_eng", "arjun.mehta@wexa.ai", "Security Engineer", "Active"),
            new SeedIdentity("id_sec_auditor", "nadia.rahman@wexa.ai", "Compliance Auditor", "Active"),
            new SeedIdentity("id_backend", "dev.patel@wexa.ai", "Backend Engineer", "Active"),
            new SeedIdentity("id_frontend", "lisa.wong@wexa.ai", "Frontend Engineer", "Active"),
            new SeedIdentity("id_sre", "omar.farouk@wexa.ai", "Site Reliability Engineer", "Active"),
            new SeedIdentity("id_devops2", "kenji.tanaka@wexa.ai", "DevOps Engineer", "Active"),
            new SeedIdentity("id_intern", "intern_developer@wexa.ai", "Intern", "Active"),
            new SeedIdentity("id_qa", "ravi.kumar@wexa.ai", "QA Engineer", "Active"),
            new SeedIdentity("id_ml", "aisha.khan@wexa.ai", "ML Engineer", "Active"),
            new SeedIdentity("id_analyst", "maria.gomez@wexa.ai", "Data Analyst", "Active"),
            new SeedIdentity("id_hr", "hr.manager@wexa.ai", "HR Lead", "Active"),
            new SeedIdentity("id_finance", "finance.lead@wexa.ai", "Finance Controller", "Active"),
            new SeedIdentity("id_sales", "zoe.adams@wexa.ai", "Sales Lead", "Active"),
            new SeedIdentity("id_llm_engine", "llm_context_engine_v4", "AI Integration", "Active"),
            new SeedIdentity("id_cicd_bot", "production_ci_cd_bot", "CI/CD Automation", "Automated"),
            new SeedIdentity("id_support_bot", "zendesk_sync_bot", "Support Sync", "Automated"),
            new SeedIdentity("id_vendor_consultant", "former_vendor_consultant@external.com", "Vendor Consultant", "Suspended"),
            new SeedIdentity("id_vendor_support", "vendor_support@external.com", "Vendor Support", "Suspended")
    );

    private static final List<SeedGroup> GROUPS = List.of(
            new SeedGroup("gr_eng", "Engineering"),
            new SeedGroup("gr_devops", "DevOps_Prod"),
            new SeedGroup("gr_eng_ro", "Engineering_Read_Only"),
            new SeedGroup("gr_db_admin", "Global_Database_Admin"),
            new SeedGroup("gr_legacy_ext", "Legacy_External_Contractors"),
            new SeedGroup("gr_hr", "HR_Team"),
            new SeedGroup("gr_fin", "Finance_Team"),
            new SeedGroup("gr_ai", "AI_Research"),
            new SeedGroup("gr_qa", "QA_Team")
    );

    private static final List<SeedResource> RESOURCES = List.of(
            new SeedResource("res_pii", "Customer_PII_Database", "Critical"),
            new SeedResource("res_aws_keys", "AWS_Root_Keys", "Critical"),
            new SeedResource("res_s3_logs", "AWS_S3_Logs_Bucket", "High"),
            new SeedResource("res_github", "GitHub_Main_Repository", "High"),
            new SeedResource("res_api_gw", "Prod_API_Gateway", "High"),
            new SeedResource("res_hr_portal", "HR_Portal", "High"),
            new SeedResource("res_analytics", "Analytics_Warehouse", "Medium"),
            new SeedResource("res_billing", "Billing_Dashboard", "Medium"),
            new SeedResource("res_datadog", "Datadog_Monitoring", "Medium"),
            new SeedResource("res_slack", "Slack_Public_Channels", "Low"),
            new SeedResource("res_jira", "Jira_Board", "Low"),
            new SeedResource("res_statuspage", "StatusPage_Public", "Low")
    );

    private static final List<Link> MEMBER_OF = List.of(
            new Link("id_cto", "gr_eng"),
            new Link("id_cto", "gr_db_admin"),
            new Link("id_cto", "gr_ai"),
            new Link("id_sec_eng", "gr_devops"),
            new Link("id_sec_eng", "gr_eng"),
            new Link("id_sre", "gr_devops"),
            new Link("id_sre", "gr_eng"),
            new Link("id_devops2", "gr_devops"),
            new Link("id_devops2", "gr_eng"),
            new Link("id_backend", "gr_eng"),
            new Link("id_frontend", "gr_eng"),
            new Link("id_intern", "gr_eng"),
            new Link("id_intern", "gr_qa"),
            new Link("id_qa", "gr_qa"),
            new Link("id_ml", "gr_ai"),
            new Link("id_ml", "gr_eng"),
            new Link("id_analyst", "gr_ai"),
            new Link("id_llm_engine", "gr_ai"),
            new Link("id_cicd_bot", "gr_devops"),
            new Link("id_hr", "gr_hr"),
            new Link("id_finance", "gr_fin"),
            new Link("id_sales", "gr_fin"),
            new Link("id_sec_auditor", "gr_eng_ro"),
            // the toxic entry point: suspended externals are still members of a legacy group
            new Link("id_vendor_consultant", "gr_legacy_ext"),
            new Link("id_vendor_support", "gr_legacy_ext")
    );

    /**
     * Inheritance chain worth remembering:
     * Legacy_External_Contractors -> Engineering_Read_Only -> Global_Database_Admin.
     * That second hop was never supposed to exist — it hands every legacy
     * contractor database-admin power through two nested INHERITS edges.
     */
    private static final List<Link> INHERITS = List.of(
            new Link("gr_devops", "gr_eng"),
            new Link("gr_eng", "gr_eng_ro"),
            new Link("gr_qa", "gr_eng_ro"),
            new Link("gr_ai", "gr_eng_ro"),
            new Link("gr_legacy_ext", "gr_eng_ro"),
            new Link("gr_eng_ro", "gr_db_admin")
    );

    private static final List<Access> ACCESS = List.of(
            new Access("gr_db_admin", "res_pii", "ADMIN"),
            new Access("gr_db_admin", "res_aws_keys", "ADMIN"),
            new Access("gr_devops", "res_s3_logs", "ADMIN"),
            new Access("gr_devops", "res_api_gw", "WRITE"),
            new Access("gr_devops", "res_datadog", "WRITE"),
            new Access("gr_eng", "res_github", "WRITE"),
            new Access("gr_eng", "res_jira", "WRITE"),
            new Access("gr_eng_ro", "res_github", "READ"),
            new Access("gr_eng_ro", "res_slack", "READ"),
            new Access("gr_qa", "res_github", "READ"),
            new Access("gr_qa", "res_jira", "WRITE"),
            new Access("gr_ai", "res_analytics", "WRITE"),
            new Access("gr_ai", "res_s3_logs", "READ"),
            new Access("gr_hr", "res_hr_portal", "WRITE"),
            new Access("gr_hr", "res_slack", "WRITE"),
            new Access("gr_fin", "res_billing", "WRITE"),
            new Access("gr_fin", "res_analytics", "READ"),
            new Access("gr_legacy_ext", "res_jira", "READ"),
            new Access("gr_legacy_ext", "res_slack", "READ"),
            // direct grants on identities
            new Access("id_cto", "res_github", "ADMIN"),
            new Access("id_cicd_bot", "res_aws_keys", "ADMIN"),
            new Access("id_cicd_bot", "res_github", "WRITE"),
            new Access("id_llm_engine", "res_pii", "READ"),
            new Access("id_llm_engine", "res_analytics", "WRITE"),
            new Access("id_support_bot", "res_slack", "WRITE"),
            new Access("id_intern", "res_slack", "WRITE"),
            new Access("id_sec_eng", "res_datadog", "ADMIN"),
            new Access("id_sre", "res_statuspage", "ADMIN"),
            new Access("id_devops2", "res_datadog", "READ"),
            new Access("id_backend", "res_api_gw", "WRITE"),
            new Access("id_sec_auditor", "res_s3_logs", "READ"),
            new Access("id_sec_auditor", "res_datadog", "READ"),
            new Access("id_sales", "res_billing", "READ")
    );

    private static final List<Link> DEPENDS_ON = List.of(
            new Link("res_api_gw", "res_pii"),
            new Link("res_api_gw", "res_github"),
            new Link("res_analytics", "res_s3_logs"),
            new Link("res_analytics", "res_pii"),
            new Link("res_billing", "res_analytics"),
            new Link("res_datadog", "res_api_gw"),
            new Link("res_statuspage", "res_api_gw")
    );

    // ---------------------------------------------------------------- loading

    private void createSchema() {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                for (String statement : SCHEMA_STATEMENTS) {
                    tx.run(statement).consume();
                }
                return null;
            });
            log.info("Schema ready (3 uniqueness constraints, 1 sensitivity index)");
        }
    }

    private void load() {
        try (Session session = driver.session()) {
            long started = System.currentTimeMillis();
            session.executeWrite(tx -> {
                for (SeedIdentity identity : IDENTITIES) {
                    tx.run(MERGE_IDENTITY, Map.of(
                            "id", identity.id(), "email", identity.email(),
                            "role", identity.role(), "status", identity.status())).consume();
                }
                for (SeedGroup group : GROUPS) {
                    tx.run(MERGE_GROUP, Map.of("id", group.id(), "name", group.name())).consume();
                }
                for (SeedResource resource : RESOURCES) {
                    tx.run(MERGE_RESOURCE, Map.of(
                            "id", resource.id(), "name", resource.name(),
                            "sensitivity", resource.sensitivity())).consume();
                }
                for (Link link : MEMBER_OF) {
                    tx.run(MERGE_MEMBER_OF, Map.of("from", link.from(), "to", link.to())).consume();
                }
                for (Link link : INHERITS) {
                    tx.run(MERGE_INHERITS, Map.of("from", link.from(), "to", link.to())).consume();
                }
                for (Link link : DEPENDS_ON) {
                    tx.run(MERGE_DEPENDS_ON, Map.of("from", link.from(), "to", link.to())).consume();
                }
                for (Access access : ACCESS) {
                    tx.run(MERGE_ACCESS, Map.of(
                            "from", access.from(), "to", access.to(), "level", access.level())).consume();
                }
                return null;
            });
            log.info("Seeded {} nodes and {} relationships in {} ms",
                    IDENTITIES.size() + GROUPS.size() + RESOURCES.size(),
                    MEMBER_OF.size() + INHERITS.size() + DEPENDS_ON.size() + ACCESS.size(),
                    System.currentTimeMillis() - started);
        }
    }
}
