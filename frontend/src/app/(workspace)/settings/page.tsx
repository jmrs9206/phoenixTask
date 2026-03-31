import PageHeader from "../../../components/ui/PageHeader";
import KeyValueList from "../../../components/ui/KeyValueList";
import { getCompany } from "../../../lib/api/workspace.server";

export default async function SettingsPage() {
  const company = await getCompany();

  return (
    <div className="page">
      <PageHeader title="Settings" subtitle="Workspace configuration and defaults." />

      <section className="section">
        <div className="section-title">Company</div>
        <KeyValueList
          items={[
            { label: "Name", value: company.name },
            { label: "Code", value: company.code },
            { label: "Status", value: company.status }
          ]}
        />
      </section>

      <section className="section">
        <div className="section-title">Company Settings</div>
        <KeyValueList
          items={[
            { label: "Timezone", value: company.settings.timezone },
            { label: "Locale", value: company.settings.locale },
            { label: "Week start", value: company.settings.weekStart }
          ]}
        />
      </section>
    </div>
  );
}
