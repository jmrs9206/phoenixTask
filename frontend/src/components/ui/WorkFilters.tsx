type ProjectOption = {
  id: number;
  projectKey: string;
  projectName: string;
};

type UserOption = {
  id: number;
  name: string;
};

type FilterValues = {
  projectId?: string;
  assigneeId?: string;
  category?: string;
  priority?: string;
};

type WorkFiltersProps = {
  values: FilterValues;
  projects: ProjectOption[];
  users: UserOption[];
  categories: { value: string; label: string }[];
  priorities: { value: string; label: string }[];
};

export default function WorkFilters({ values, projects, users, categories, priorities }: WorkFiltersProps) {
  return (
    <form className="list-filters" method="get">
      <div className="filter-field">
        <label htmlFor="work-filter-project">Project</label>
        <select id="work-filter-project" name="projectId" defaultValue={values.projectId ?? ""}>
          <option value="">All projects</option>
          {projects.map((project) => (
            <option key={project.id} value={project.id}>
              {project.projectKey} · {project.projectName}
            </option>
          ))}
        </select>
      </div>
      <div className="filter-field">
        <label htmlFor="work-filter-assignee">Assignee</label>
        <select id="work-filter-assignee" name="assigneeId" defaultValue={values.assigneeId ?? ""}>
          <option value="">All assignees</option>
          {users.map((user) => (
            <option key={user.id} value={user.id}>
              {user.name}
            </option>
          ))}
        </select>
      </div>
      <div className="filter-field">
        <label htmlFor="work-filter-category">Category</label>
        <select id="work-filter-category" name="category" defaultValue={values.category ?? ""}>
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category.value} value={category.value}>
              {category.label}
            </option>
          ))}
        </select>
      </div>
      <div className="filter-field">
        <label htmlFor="work-filter-priority">Priority</label>
        <select id="work-filter-priority" name="priority" defaultValue={values.priority ?? ""}>
          <option value="">All priorities</option>
          {priorities.map((priority) => (
            <option key={priority.value} value={priority.value}>
              {priority.label}
            </option>
          ))}
        </select>
      </div>
      <div className="filter-actions">
        <button className="button-secondary" type="submit">
          Apply filters
        </button>
      </div>
    </form>
  );
}
