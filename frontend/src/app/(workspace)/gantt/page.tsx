import Link from "next/link";
import PageHeader from "../../../components/ui/PageHeader";
import SimpleTable from "../../../components/ui/SimpleTable";
import { getGanttProjects } from "../../../lib/api/workspace.server";
import type { GanttProject } from "../../../types/domain/workspace";

export default async function GanttPage() {
  const projects = await getGanttProjects();

  const columns = [
    {
      key: "project",
      header: "Project",
      render: (row: GanttProject) => (
        <Link href={`/gantt/projects/${row.projectId}`}>{row.projectKey} · {row.projectName}</Link>
      )
    },
    {
      key: "start",
      header: "Planned start",
      render: (row: GanttProject) => row.plannedStartDate ?? "—"
    },
    {
      key: "end",
      header: "Planned end",
      render: (row: GanttProject) => row.plannedEndDate ?? "—"
    }
  ];

  return (
    <div className="page">
      <PageHeader title="Gantt" subtitle="Timeline derived from project and issue dates." />
      <section className="section">
        <h3 className="section-title">Projects Timeline</h3>
        <SimpleTable
          columns={columns}
          rows={projects}
          emptyTitle="No gantt data"
          emptyMessage="No accessible projects or timelines available yet."
        />
      </section>
    </div>
  );
}
